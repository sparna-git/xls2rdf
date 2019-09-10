package fr.sparna.rdf.xls2rdf;

import static fr.sparna.rdf.xls2rdf.ExcelHelper.getCellValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import fr.sparna.rdf.xls2rdf.reconcile.DynamicReconciliableValueSet;
import fr.sparna.rdf.xls2rdf.reconcile.PreloadedReconciliableValueSet;
import fr.sparna.rdf.xls2rdf.reconcile.ReconciliableValueSetIfc;
import fr.sparna.rdf.xls2rdf.reconcile.SparqlReconcileService;



public class Xls2RdfConverter {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * Language used to generate the literals
	 */
	protected String lang;
	
	/**
	 * Object capable of serializing the resulting models
	 */
	protected ModelWriterIfc modelWriter;

	/**
	 * List of identifiers of all the graphs / concept schemes converted
	 */
	private final List<String> convertedVocabularyIdentifiers = new ArrayList<String>();
	
	/**
	 * Internal list of value generators
	 */
	protected final Map<String, ValueGeneratorIfc> valueGenerators = new HashMap<>();
	
	/**
	 * The prefixes declared in the file along with utility classes and default prefixes
	 */
	protected PrefixManager prefixManager = new PrefixManager();
	
	/**
	 * The workbook currently being processed, to get references to fonts
	 */
	private transient Workbook workbook;
	
	/**
	 * Global Repository containing all the converted data from all sheets, useful for reconciling values
	 */
	private transient Repository globalRepository = new SailRepository(new MemoryStore());
	
	/**
	 * Supporting Repository containing external data on which to reconcile values
	 */
	private transient Repository supportRepository = null;
	
	/**
	 * List of post-processors to be applied to generated RDF data. If null or empty, no post-processing will happen
	 */
	private List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
	
	/**
	 * Result of reconciliation
	 */
	private transient Map<Short, ReconciliableValueSetIfc> reconcileColumnsValues = new HashMap<Short, ReconciliableValueSetIfc>();
	
	
	
	public Xls2RdfConverter(ModelWriterIfc modelWriter, String lang) {
		
		this.globalRepository.init();
		this.modelWriter = modelWriter;
		this.lang = lang;

	}

	/**
	 * Parses a File into a Workbook, and defer processing to processWorkbook(Workbook workbook)
	 * @param input
	 * @return
	 */
	public List<Model> processFile(File input) {
		try {
			log.info("Converting file "+input.getAbsolutePath()+"...");
			Workbook workbook = WorkbookFactory.create(input);
			return processWorkbook(workbook);
		} catch (Exception e) {
			throw Xls2RdfException.rethrow(e);
		}			
	}
	
	/**
	 * Parses an InputStream into a Workbook, and defer processing to processWorkbook(Workbook workbook)
	 * @param input
	 * @return
	 */
	public List<Model> processInputStream(InputStream input) {
		try {
			Workbook workbook = WorkbookFactory.create(input);
			return processWorkbook(workbook);
		} catch (Exception e) {
			throw Xls2RdfException.rethrow(e);
		}			
	}
	
	/**
	 * Process an Excel sheet.
	 * 
	 * @param workbook
	 * @return
	 */
	public List<Model> processWorkbook(Workbook workbook) {

		List<Model> models = new ArrayList<>();

		try {
			
			// store the workbook reference
			this.workbook = workbook;
			
			// notify begin
			modelWriter.beginWorkbook();
			
			// for every sheet...
			for (Sheet sheet : workbook) {

				// process the sheet, possibly returning null
				// if load(sheet) returns null, the sheet was ignored
				Model model = processSheet(sheet);
				models.add(model);
				try(RepositoryConnection connection = this.globalRepository.getConnection()) {
					connection.add(model);
				}
			}
			
			// notify end
			modelWriter.endWorkbook();
			
		} catch (Exception e) {
			throw Xls2RdfException.rethrow(e);
		}
		
		return models;
	}

	/**
	 * Process a single sheet and returns corresponding Model
	 * 
	 * @param sheet
	 * @return
	 */
	private Model processSheet(Sheet sheet) {
		
		// initialize target Model
		Model model = new LinkedHashModelFactory().createEmptyModel();
		SimpleValueFactory svf = SimpleValueFactory.getInstance();
		
		RdfizableSheet rdfizableSheet = new RdfizableSheet(sheet, this);
		
		if(!rdfizableSheet.canRDFize()) {
			log.debug(sheet.getSheetName()+" : Ignoring sheet.");
			return model;
		} else {
			log.debug("Processing sheet: " + sheet.getSheetName());
		}
		
		// read the prefixes
		this.prefixManager.register(rdfizableSheet.readPrefixes());

		// read the concept scheme or graph URI
		String csUri = prefixManager.uri(rdfizableSheet.getSchemeOrGraph(), true);
		
		// if the URI was already processed, output a warning (this is a possible case)
		if(this.convertedVocabularyIdentifiers.contains(csUri)) {
			log.debug("Duplicate graph declaration found: " + csUri + " (declared in more than one sheet)");
		}
		
		Resource csResource = svf.createIRI(csUri);	
		
		// read the title row index
		int headerRowIndex = rdfizableSheet.getTitleRowIndex();
		log.debug("Found title row at index "+headerRowIndex);
		// si la ligne d'entete n'a pas été trouvée, on ne génère que le ConceptScheme
		if(headerRowIndex == 1) {
			log.info("Could not find header row index in sheet "+sheet.getSheetName()+", will parse header object until end of sheet (last rowNum = "+ sheet.getLastRowNum() +")");
			headerRowIndex = sheet.getLastRowNum();
		}
		
		// read the properties on the header by reading the top rows
		ColumnHeaderParser headerParser = new ColumnHeaderParser(prefixManager);
		for (int rowIndex = 1; rowIndex <= headerRowIndex; rowIndex++) {
			if(sheet.getRow(rowIndex) != null) {
				String key = getCellValue(sheet.getRow(rowIndex).getCell(0));
				String value = getCellValue(sheet.getRow(rowIndex).getCell(1));
				
				ColumnHeader header = headerParser.parse(key, (short)-1);
				if(
						header != null
						&&
						StringUtils.isNotBlank(value)
				) {
					ValueGeneratorIfc valueGenerator = null;
					if(valueGenerators.containsKey(header.getDeclaredProperty())) {
						valueGenerator = valueGenerators.get(header.getDeclaredProperty());
					} else if(header.getProperty() != null) {
						valueGenerator = ValueGeneratorFactory.resourceOrLiteral(
								header,
								prefixManager
						);
					}
					
					if(valueGenerator != null) {
						log.debug("Adding value on header object \""+value+"\"@"+header.getLanguage().orElse(this.lang));
						valueGenerator.addValue(
								model,
								csResource,
								value,
								header.getLanguage().orElse(this.lang)
						);
					}
				}
			}
		}		

		if(rdfizableSheet.hasDataSection()) {
			// read the column names from the header row
			List<ColumnHeader> columnNames = rdfizableSheet.getColumnHeaders(headerRowIndex);
			
			log.debug("Converting data with these column headers: ");
			for (ColumnHeader columnHeader : columnNames) {
				log.debug(columnHeader.toString());
			}
			
			// reconcile columns that need to be reconciled, and store result
			for (ColumnHeader columnHeader : columnNames) {
				if(columnHeader.isReconcileExternal()) {
					SparqlReconcileService reconcileService = new SparqlReconcileService(this.supportRepository);
					
					PreloadedReconciliableValueSet reconciliableValueSet = new PreloadedReconciliableValueSet(
							reconcileService,
							true
					);
					reconciliableValueSet.initReconciledValues(
							PreloadedReconciliableValueSet.extractDistinctValues(sheet, columnHeader.getColumnIndex(), headerRowIndex),
							columnHeader.getReconcileOn()
					);
					
					this.reconcileColumnsValues.put(columnHeader.getColumnIndex(), reconciliableValueSet);
					
				} else if (columnHeader.isReconcileLocal()) {
					SparqlReconcileService reconcileService = new SparqlReconcileService(this.globalRepository);
					
					DynamicReconciliableValueSet reconciliableValueSet = new DynamicReconciliableValueSet(
							reconcileService,
							columnHeader.getReconcileOn(),
							true
					);

					this.reconcileColumnsValues.put(columnHeader.getColumnIndex(), reconciliableValueSet);
				} 
			}
			
			// read the rows after the header and process each row
			for (int rowIndex = (headerRowIndex + 1); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row r = sheet.getRow(rowIndex);
				if(r != null) {
					handleRow(model, columnNames, prefixManager, r);
				}
			}
			
		} else {
			log.info("Sheet has no title row, skipping data processing.");
		}
		
		
		if(this.postProcessors != null && this.postProcessors.size() > 0) {
			log.info("Applying SKOS post-processings on the result");
			for(Xls2RdfPostProcessorIfc aProcessor : this.postProcessors) {
				aProcessor.afterSheet(model, csResource);
			}
		} else {
			log.info("No post-processings to apply");
		}
		
		// writes the resulting Model
		log.debug("Saving graph of "+model.size()+" statements generated from Sheet "+sheet.getSheetName());
		modelWriter.saveGraphModel(csUri, model, prefixManager.getPrefixes());
		
		// stores the idenifier of generated vocabulary
		convertedVocabularyIdentifiers.add(csUri);
		return model;
	}
	

	private Resource handleRow(Model model, List<ColumnHeader> columnHeaders, PrefixManager prefixManager, Row row) {
		RowBuilder rowBuilder = null;
		for (int colIndex = 0; colIndex < columnHeaders.size(); colIndex++) {
			ColumnHeader header = columnHeaders.get(colIndex);
			
			Cell c = row.getCell(colIndex);			
			String value = getCellValue(c);
			// if it is the first column...
			if (null == rowBuilder) {
				// if the value of the first column is empty, or is striked through, skip the whole row
				if (StringUtils.isBlank(value) || this.workbook.getFontAt(c.getCellStyle().getFontIndex()).getStrikeout()) {
					return null;
				}
				// create the RowBuilder with the URI in the first column
				// the URI could be null
				rowBuilder = new RowBuilder(model, prefixManager.uri(value, false));
				continue;
			}
			
			// process the cell for each subsequent columns after the first one
			if (StringUtils.isNotBlank(value)) {
				if(this.workbook.getFontAt(c.getCellStyle().getFontIndex()).getStrikeout()) {
					// skip the cell if it is striked out
					continue;
				}
				
				ValueGeneratorIfc valueGenerator = valueGenerators.get(header.getDeclaredProperty());
				
				if(header.getParameters().get(ColumnHeader.PARAMETER_LOOKUP_COLUMN) != null) {
					// finds the index of the column corresponding to lookupColumn reference
					String lookupColumnRef = header.getParameters().get(ColumnHeader.PARAMETER_LOOKUP_COLUMN);
					short lookupColumnIndex = ColumnHeader.idRefOrPropertyRefToColumnIndex(columnHeaders, lookupColumnRef);
					if(lookupColumnIndex == -1) {
						throw new Xls2RdfException("Unable to find lookupColumn reference '"+lookupColumnRef+"' (full header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
					}
					
					// now find the subject at which the lookupColumn property is attached
					ColumnHeader lookupColumnHeader = ColumnHeader.findByColumnIndex(columnHeaders, lookupColumnIndex);
					short lookupSubjectColumn = 0;
					if(header.getParameters().get(ColumnHeader.PARAMETER_SUBJECT_COLUMN) != null) {
						String subjectColumnRef = lookupColumnHeader.getParameters().get(ColumnHeader.PARAMETER_SUBJECT_COLUMN);
						lookupSubjectColumn = ColumnHeader.idRefToColumnIndex(columnHeaders, subjectColumnRef);
						if(lookupSubjectColumn == -1) {
							throw new Xls2RdfException("Unable to find subjectColumn reference '"+subjectColumnRef+"' (full header "+lookupColumnHeader.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+", while processing lookupColumn in header "+header.getOriginalValue());
						}
					}
					
					valueGenerator = ValueGeneratorFactory.lookup(
							header,
							row.getSheet(),
							lookupColumnIndex,
							lookupSubjectColumn,
							prefixManager
					);
				}
				
				else if(header.getParameters().get(ColumnHeader.PARAMETER_RECONCILE) != null) {
					String reconcileParameterValue = header.getParameters().get(ColumnHeader.PARAMETER_RECONCILE);					
					
					if(reconcileParameterValue.equals("local")) {						
						valueGenerator = ValueGeneratorFactory.reconcile(
								header,
								prefixManager,
								this.reconcileColumnsValues.get(header.getColumnIndex())
						);
					} else if(reconcileParameterValue.equals("external") && this.supportRepository != null) {						
						valueGenerator = ValueGeneratorFactory.reconcile(
								header,
								prefixManager,
								this.reconcileColumnsValues.get(header.getColumnIndex())
						);
					}
					
				}
				
				// if this is not one of the known processor, but the property is known, then defaults to a generic processor
				// also defaults to a generic processor if a custom datatype is declared on the property
				else if(
						(
								valueGenerator == null
								||
								header.getDatatype().isPresent()
								||
								(header.getParameters() != null && !header.getParameters().isEmpty())
						)
						&&
						header.getProperty() != null
				) {
					valueGenerator = ValueGeneratorFactory.resourceOrLiteral(
							header,
							prefixManager
					);
				}
				
				if(header.getParameters().get(ColumnHeader.PARAMETER_SEPARATOR) != null) {
					valueGenerator = ValueGeneratorFactory.split(valueGenerator, header.getParameters().get(ColumnHeader.PARAMETER_SEPARATOR));
					// use a default comma separator for cells that contain URI references
				} else if(
					// if it is a true column wih a declared property...
					header.getProperty() != null
					&&
					!header.getDatatype().isPresent()
					&&
					!header.getLanguage().isPresent()
					&&
					(value.startsWith("http") || prefixManager.usesKnownPrefix(value.trim()))
				) {
					valueGenerator = ValueGeneratorFactory.split(valueGenerator, ",");
				}
				
				// determine the subject of the triple, be default it is the value of the first column but can be overidden
				if(header.getParameters().get(ColumnHeader.PARAMETER_SUBJECT_COLUMN) != null) {
					String subjectColumnRef = header.getParameters().get(ColumnHeader.PARAMETER_SUBJECT_COLUMN);
					int subjectColumnIndex = ColumnHeader.idRefOrPropertyRefToColumnIndex(columnHeaders, subjectColumnRef);
					if(subjectColumnIndex == -1) {
						throw new Xls2RdfException("Unable to find subjectColumn reference '"+subjectColumnRef+"' (full header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
					}
					
					String currentSubject = getCellValue(row.getCell(subjectColumnIndex));
					
					if(currentSubject != null) {
						try {
							rowBuilder.setCurrentSubject(SimpleValueFactory.getInstance().createIRI(currentSubject));
						} catch (Exception e) {
							e.printStackTrace();
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							e.printStackTrace(new PrintStream(baos));
							String stacktraceString = new String(baos.toByteArray());
							String stacktraceStringBegin = (stacktraceString.length() > 256)?stacktraceString.substring(0, 256):stacktraceString;
							throw new Xls2RdfException(e, "Cannot set subject URI in cell "+subjectColumnRef+(row.getRowNum()+1)+", value is '"+ currentSubject +"' (header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".\n Message is : "+e.getMessage()+"\n Beginning of stacktrace is "+stacktraceStringBegin);
						}
					} else {
						log.warn("Unable to set a new current subject from cell '"+CellReference.convertNumToColString(colIndex)+(row.getRowNum()+1)+"' (header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
					}
				}
				
				// if a value generator was successfully generated, then process the value
				if(valueGenerator != null) {
					try {
						rowBuilder.processCell(
								valueGenerator,
								value,
								header.getLanguage().orElse(this.lang)
						);
					} catch (Exception e) {
						e.printStackTrace();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						String stacktraceString = new String(baos.toByteArray());
						String stacktraceStringBegin = (stacktraceString.length() > 256)?stacktraceString.substring(0, 256):stacktraceString;
						throw new Xls2RdfException(e, "Convert exception while processing value '"+value+"', cell "+CellReference.convertNumToColString(colIndex)+(row.getRowNum()+1)+" (header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".\n Message is : "+e.getMessage()+"\n Beginning of stacktrace is "+stacktraceStringBegin);
					}
				}
				
				// reset the current subject after that
				rowBuilder.resetCurrentSubject();
			}
		}
		
		// if, after row processing, no rdf:type was generated, then we consider the row to be a skos:Concept
		// this allows to generate something else that skos:Concept
		if(rowBuilder != null && rowBuilder.rowMainResource != null) {
			if(this.postProcessors != null && this.postProcessors.size() > 0) {
				for(Xls2RdfPostProcessorIfc aProcessor : this.postProcessors) {
					aProcessor.afterRow(model, rowBuilder.rowMainResource);
				}
			}
		}
		
		return null == rowBuilder ? null : rowBuilder.rowMainResource;
	}

	private class RowBuilder {
		private final Model model;
		private Resource rowMainResource;
		private Resource currentSubject;

		public RowBuilder(Model model) {
			this(model, null);
		}
		
		public RowBuilder(Model model, String uri) {
			this.model = model;
			if(uri != null) {
				rowMainResource = SimpleValueFactory.getInstance().createIRI(uri);
				// set the current subject to the main resource by default
				currentSubject = rowMainResource;
			}
		}

		public void processCell(ValueGeneratorIfc valueGenerator, String value, String language) {
			// if the column is unknown, ignore it
			// if no current subject was found, cannot add any value
			if(valueGenerator != null && this.currentSubject != null) {				
				valueGenerator.addValue(model, currentSubject, value, language);
			}
		}

		public void setCurrentSubject(Resource currentSubject) {
			this.currentSubject = currentSubject;
		}
		
		public void resetCurrentSubject() {
			this.currentSubject = this.rowMainResource;
		}

	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public List<String> getConvertedVocabularyIdentifiers() {
		return convertedVocabularyIdentifiers;
	}
	
	public void setSupportRepository(Repository supportRepository) {
		this.supportRepository = supportRepository;
	}

	public List<Xls2RdfPostProcessorIfc> getPostProcessors() {
		return postProcessors;
	}

	public void setPostProcessors(List<Xls2RdfPostProcessorIfc> postProcessors) {
		this.postProcessors = postProcessors;
	}

	public static void main(String[] args) throws Exception {
		
		// quick and dirty Log4J config
		BasicConfigurator bc = new BasicConfigurator();
		bc.configure((LoggerContext) LoggerFactory.getILoggerFactory());
		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.eclipse.rdf4j")).setLevel(ch.qos.logback.classic.Level.INFO);
		
		
		// Method 1 : save each scheme to a separate directory
//		DirectoryModelWriter writer = new DirectoryModelWriter(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/controlled-vocabularies-xls2skos/cv-from-xls2skos"));
//		writer.setSaveGraphFile(true);
//		writer.setGraphSuffix("/graph");
		
		// Method 2 : save everything to a single SKOS file
		// OutputStreamModelWriter ms = new OutputStreamModelWriter(new File("/home/thomas/controlled-vocabularies.ttl"));
		
		// Method 3 : save each scheme to a separate entry in a ZIP file.
		ZipOutputStreamModelWriter writer = new ZipOutputStreamModelWriter(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/controlled-vocabularies-xls2skos/cv-from-xls2skos.zip"));
		writer.setSaveGraphFile(true);
		writer.setGraphSuffix("/graph");
		
		Xls2RdfConverter me = new Xls2RdfConverter(writer, "fr");
		me.setPostProcessors(Collections.singletonList(new SkosPostProcessor()));
		
		// me.loadAllToFile(new File("/home/thomas/sparna/00-Clients/Sparna/20-Repositories/sparna/fr.sparna/rdf/skos/xls2skos/src/test/resources/test-excel-saved-from-libreoffice.xlsx"));
		// me.loadAllToFile(new File("/home/thomas/sparna/00-Clients/Sparna/20-Repositories/sparna/fr.sparna/rdf/skos/xls2skos/src/test/resources/test-libreoffice.ods"));
		me.processFile(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/controlled-vocabularies-xls2skos/jolux-controlled-voc-travail-20161026-recup.xlsx"));
		// me.processFile(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/jolux-controlled-voc-travail-20161012.xlsx"));
	}
	
	
	public static void runLikeInSkosPlay(
			InputStream input,
			OutputStream output,
			String lang
	) throws Exception {
		OutputStreamModelWriter modelWriter = new OutputStreamModelWriter(output);
		Xls2RdfConverter converter = new Xls2RdfConverter(modelWriter, lang);
		converter.setPostProcessors(Collections.singletonList(new SkosPostProcessor()));
		converter.processInputStream(input);
	}
	
	

}
