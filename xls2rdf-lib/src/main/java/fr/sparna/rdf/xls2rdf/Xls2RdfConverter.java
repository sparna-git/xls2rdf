package fr.sparna.rdf.xls2rdf;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import fr.sparna.rdf.xls2rdf.listen.LogXls2RdfMessageListener;
import fr.sparna.rdf.xls2rdf.postprocess.AsListPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.SkosPostProcessor;
import fr.sparna.rdf.xls2rdf.reconcile.DynamicReconciliableValueSet;
import fr.sparna.rdf.xls2rdf.reconcile.PreloadedReconciliableValueSet;
import fr.sparna.rdf.xls2rdf.reconcile.ReconcileServiceIfc;
import fr.sparna.rdf.xls2rdf.reconcile.ReconciliableValueSetIfc;
import fr.sparna.rdf.xls2rdf.reconcile.SparqlReconcileService;
import fr.sparna.rdf.xls2rdf.write.OutputStreamModelWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.rdf4j.model.IRI;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static fr.sparna.rdf.xls2rdf.ExcelHelper.getCellValue;



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
	 * Reconciliation service on which to reconcile external values
	 */
	private ReconcileServiceIfc reconcileService;
	
	/**
	 * List of post-processors to be applied to generated RDF data. If null or empty, no post-processing will happen
	 */
	private List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
	
	/**
	 * Whether to strictly check if format of cells are correct
	 */
	private boolean strictFormat = false;
	
	/**
	 * Message listener for messages that need to be send to the outside world
	 */
	private Xls2RdfMessageListenerIfc messageListener = new LogXls2RdfMessageListener();
	
	/**
	 * Result of reconciliation
	 */
	private transient Map<Integer, ReconciliableValueSetIfc> reconcileColumnsValues = new HashMap<Integer, ReconciliableValueSetIfc>();
	
	/**
	 * Validator capable of telling if a property is valid or not
	 */
	private Predicate<IRI> propertyValidator;
	
	/**
	 * Triggers an Exception if a reconcile fails
	 */
	private boolean failIfNoReconcile = false;
	
	public Xls2RdfConverter(ModelWriterIfc modelWriter) {		
		this(modelWriter, null);
	}
	
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
			
			// read all prefixes in all sheets, so that prefixes are shared across all sheets
			for (Sheet sheet : workbook) {
				initPrefixManager(sheet);
			}
			
			// for every sheet...
			for (Sheet sheet : workbook) {

				// process the sheet, possibly returning an empty Model
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
	 * Init the prefix manager with the prefixes declared in the Sheet
	 * @param sheet
	 */
	private void initPrefixManager(Sheet sheet) {
		RdfizableSheet rdfizableSheet = new RdfizableSheet(sheet, this);
		
		// read the prefixes
		this.prefixManager.register(rdfizableSheet.readPrefixes());
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
		
		// init the sheet (after prefixes are read)
		rdfizableSheet.init();
		
		// read the concept scheme or graph URI
		String csUri = prefixManager.uri(rdfizableSheet.getSchemeOrGraph(), true);
		
		// if the URI was already processed, output a warning (this is a possible case)
		if(this.convertedVocabularyIdentifiers.contains(csUri)) {
			log.debug("Duplicate graph declaration found: " + csUri + " (declared in more than one sheet)");
		}
		
		Resource csResource = svf.createIRI(csUri);	
		
		// find the title row index
		int headerRowIndex = rdfizableSheet.getTitleRowIndex();
		log.debug("Found title row at index "+headerRowIndex);
		// si la ligne d'entete n'a pas été trouvée, on ne génère que la ressource d'entête
		if(headerRowIndex == 1) {
			log.info("Could not find header row index in sheet "+sheet.getSheetName()+", will parse header object until end of sheet (last rowNum = "+ sheet.getLastRowNum() +")");
			headerRowIndex = sheet.getLastRowNum()+1;
		}
		
		// validate the sheet
		if(this.propertyValidator != null) {
			log.info("Will validate sheet "+sheet.getSheetName());
			boolean valid = rdfizableSheet.validateHeaders(this.propertyValidator, messageListener);
			if(!valid) {
				log.error("Sheet "+sheet.getSheetName()+" is invalid, skipping sheet processing");
				return model;
			}
		}
		
		// read the properties on the header by reading the top rows
		ColumnHeaderParser headerParser = new ColumnHeaderParser(prefixManager);
		for (int rowIndex = 1; rowIndex < headerRowIndex; rowIndex++) {
			if(sheet.getRow(rowIndex) != null) {
				String key = getCellValue(sheet.getRow(rowIndex).getCell(0));
				Cell cell = sheet.getRow(rowIndex).getCell(1);
				String value = getCellValue(cell);
				
				// parse the property
				ColumnHeader header = headerParser.parse(key, sheet.getRow(rowIndex).getCell(0));
				if(
						header != null
						&&
						header.getProperty() != null
						&&
						StringUtils.isNotBlank(value)
				) {				
					
					ValueProcessorFactory processorFactory = new ValueProcessorFactory(messageListener);
					
					// always use a default processor
					ValueProcessorIfc cellProcessor = processorFactory.resourceOrLiteral(
							header,
							prefixManager
					);
					
					// support separator option in the header
					if(header.getParameters().get(ColumnHeader.PARAMETER_SEPARATOR) != null) {
						cellProcessor = processorFactory.split(
								cellProcessor,
								header.getParameters().get(ColumnHeader.PARAMETER_SEPARATOR)
						);
					} 
					
					log.debug("Adding value on header object \""+value+"\" with lang "+header.getLanguage().orElse(this.lang));
					cellProcessor.processValue(
							model,
							csResource,
							value,
							cell,
							header.getLanguage().orElse(this.lang)
					);
				}
			}
		}		

		List<Resource> rowResources = new ArrayList<>();
		List<ColumnHeader> columnNames = new ArrayList<>();
		if(rdfizableSheet.hasDataSection()) {
			// read the column names from the header row
			columnNames = rdfizableSheet.getColumnHeaders();
			
			log.debug("Converting data with these column headers: ");
			for (ColumnHeader columnHeader : columnNames) {
				log.debug(columnHeader.toString());
			}
			
			// reconcile columns that need to be reconciled, and store result
			for (ColumnHeader columnHeader : columnNames) {
				if(columnHeader.isReconcileExternal() && this.reconcileService != null) {					
					PreloadedReconciliableValueSet reconciliableValueSet = new PreloadedReconciliableValueSet(
							reconcileService,
							this.failIfNoReconcile
					);
					reconciliableValueSet.initReconciledValues(
							PreloadedReconciliableValueSet.extractDistinctValues(sheet, columnHeader.getHeaderCell().getColumnIndex(), headerRowIndex),
							columnHeader.getReconcileOn(),
							this.messageListener
					);
					
					this.reconcileColumnsValues.put(columnHeader.getHeaderCell().getColumnIndex(), reconciliableValueSet);
					
				} else if (columnHeader.isReconcileLocal()) {
					SparqlReconcileService reconcileService = new SparqlReconcileService(this.globalRepository);
					
					DynamicReconciliableValueSet reconciliableValueSet = new DynamicReconciliableValueSet(
							reconcileService,
							columnHeader.getReconcileOn(),
							this.failIfNoReconcile,
							this.messageListener
					);

					this.reconcileColumnsValues.put(columnHeader.getHeaderCell().getColumnIndex(), reconciliableValueSet);
				} 
			}
			
			// read the rows after the header line and process each row
			log.info("Converting rows...");
			for (int rowIndex = (headerRowIndex + 1); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row r = sheet.getRow(rowIndex);
				if(r != null) {
					if(rowIndex % 1000 == 0) {
						log.info("Row "+rowIndex+"...");
					}
					Resource rowResource;
					try {
						rowResource = handleRow(model, csResource, columnNames, prefixManager, r);
					} catch (Exception e) {
						throw new Xls2RdfException(e, "Exception when processing row "+r.getRowNum()+" in sheet "+r.getSheet().getSheetName()+" : "+e.getMessage(), (Object[])null);
					}
					if(rowResource != null) {
						rowResources.add(rowResource);
					}
				}
			}
			
		} else {
			log.info("Sheet has no title row, skipping data processing.");
		}
		
		// always post-process with asList
		AsListPostProcessor alpp = new AsListPostProcessor();
		alpp.afterSheet(model, csResource, rowResources, columnNames);
		if(this.postProcessors != null && this.postProcessors.size() > 0) {
			log.info("Applying SKOS post-processings on the result");
			for(Xls2RdfPostProcessorIfc aProcessor : this.postProcessors) {
				aProcessor.afterSheet(model, csResource, rowResources, columnNames);
			}
		} else {
			log.info("No post-processings to apply");
		}
		
		// writes the resulting Model
		log.debug("Saving graph of "+model.size()+" statements generated from Sheet "+sheet.getSheetName());
		modelWriter.saveGraphModel(csUri, model, prefixManager.getOutputPrefixes());
		
		// stores the identifier of generated vocabulary
		convertedVocabularyIdentifiers.add(csUri);
		return model;
	}

	private Resource handleRow(Model model, Resource headerResource, List<ColumnHeader> columnHeaders, PrefixManager prefixManager, Row row) {
		RowBuilder rowBuilder = null;
		for (int colIndex = 0; colIndex < columnHeaders.size(); colIndex++) {
			ColumnHeader header = columnHeaders.get(colIndex);
			
			Cell cell = row.getCell(colIndex);			
			String value = getCellValue(cell);
			// if it is the first column...
			if (null == rowBuilder) {
				// if the value of the first column is empty, or is striked through, or if it is hidden, skip the whole row
				if (
						StringUtils.isBlank(value)
						||
						this.workbook.getFontAt(cell.getCellStyle().getFontIndex()).getStrikeout()
						||
						row.getZeroHeight()
						
				) {
					return null;
				}
				// create the RowBuilder with the URI in the first column
				// the URI could be null
				rowBuilder = new RowBuilder(model, prefixManager.uri(value, false));
				continue;
			}
			
			if (StringUtils.isBlank(value)) {
				continue;
			}
			
			// process the cell for each subsequent columns after the first one
			if(this.workbook.getFontAt(cell.getCellStyle().getFontIndex()).getStrikeout()) {
				// skip the cell if it is striked out
				continue;
			}
			// test if cell should be ignored
			if(header.getParameters().get(ColumnHeader.PARAMETER_IGNORE_IF) != null) {
				if(value.equals(header.getParameters().get(ColumnHeader.PARAMETER_IGNORE_IF))) {
					// skip cell
					continue;
				}
			}
			
			ValueProcessorFactory processorFactory = new ValueProcessorFactory(this.messageListener);
			ValueProcessorIfc cellProcessor = null;
			
			
			
			if(header.getParameters().get(ColumnHeader.PARAMETER_LOOKUP_COLUMN) != null) {
				// finds the index of the column corresponding to lookupColumn reference
				String lookupColumnRef = header.getParameters().get(ColumnHeader.PARAMETER_LOOKUP_COLUMN);
				int lookupColumnIndex = ColumnHeader.idRefOrPropertyRefToColumnIndex(columnHeaders, lookupColumnRef);
				if(lookupColumnIndex == -1) {
					throw new Xls2RdfException("Unable to find lookupColumn reference '"+lookupColumnRef+"' (full header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
				}
				
				// now find the subject at which the lookupColumn property is attached
				ColumnHeader lookupColumnHeader = ColumnHeader.findByColumnIndex(columnHeaders, lookupColumnIndex);
				int lookupSubjectColumn = 0;
				if(header.getParameters().get(ColumnHeader.PARAMETER_SUBJECT_COLUMN) != null) {
					String subjectColumnRef = lookupColumnHeader.getParameters().get(ColumnHeader.PARAMETER_SUBJECT_COLUMN);
					lookupSubjectColumn = ColumnHeader.idRefToColumnIndex(columnHeaders, subjectColumnRef);
					if(lookupSubjectColumn == -1) {
						throw new Xls2RdfException("Unable to find subjectColumn reference '"+subjectColumnRef+"' (full header "+lookupColumnHeader.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+", while processing lookupColumn in header "+header.getOriginalValue());
					}
				}
				
				cellProcessor = processorFactory.lookup(
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
					cellProcessor = processorFactory.reconcile(
							header,
							prefixManager,
							this.reconcileColumnsValues.get(header.getHeaderCell().getColumnIndex())
					);
				} else if(reconcileParameterValue.equals("external") && this.reconcileService != null) {						
					cellProcessor = processorFactory.reconcile(
							header,
							prefixManager,
							this.reconcileColumnsValues.get(header.getHeaderCell().getColumnIndex())
					);
				}
				
			}
			
			// if this is not one of the known processor, but the property is known, then defaults to a generic processor
			// also defaults to a generic processor if a custom datatype is declared on the property
			else if(
					(
							cellProcessor == null
							||
							header.getDatatype().isPresent()
							||
							(header.getParameters() != null && !header.getParameters().isEmpty())
					)
					&&
					header.getProperty() != null
			) {
				cellProcessor = processorFactory.resourceOrLiteral(
						header,
						prefixManager
				);
			}
			
			// if we requested to ignore values in parenthesis, wrap the processor into adequate processor
			if(header.isIgnoreIfParenthesis()) {
				cellProcessor = processorFactory.ignoreIfParenthesis(cellProcessor);
			}
			
			if(header.getParameters().get(ColumnHeader.PARAMETER_SEPARATOR) != null) {
				cellProcessor = processorFactory.split(cellProcessor, header.getParameters().get(ColumnHeader.PARAMETER_SEPARATOR));
				// use a default comma separator for cells that contain URI references
			} else if(
				// if it is a true column with a declared property...
				header.getProperty() != null
				&&
				!header.getDatatype().isPresent()
				&&
				!header.getLanguage().isPresent()
				&&
				(value.startsWith("http")  || value.startsWith("mailto") || prefixManager.usesKnownPrefix(value.trim()))
			) {
				cellProcessor = processorFactory.split(cellProcessor, ",");
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
						Resource currentSubjectResource;
						if(currentSubject.startsWith("_:")) {
							currentSubjectResource = SimpleValueFactory.getInstance().createBNode(currentSubject.substring(2));
						} else {
							currentSubjectResource = SimpleValueFactory.getInstance().createIRI(prefixManager.uri(currentSubject, false));
						}
						
						rowBuilder.setCurrentSubject(currentSubjectResource);
					} catch (Exception e) {
						e.printStackTrace();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						String stacktraceString = new String(baos.toByteArray());
						String stacktraceStringBegin = (stacktraceString.length() > 256)?stacktraceString.substring(0, 256):stacktraceString;
						throw new Xls2RdfException(e, "Cannot set subject URI in cell "+subjectColumnRef+(row.getRowNum()+1)+", value is '"+ currentSubject +"' (header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".\n Message is : "+e.getMessage()+"\n Beginning of stacktrace is "+stacktraceStringBegin);
					}
				} else {
					log.warn("Unable to set a new current subject from cell '"+new CellReference(row.getRowNum()+1, colIndex).formatAsString()+"' (header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
				}
			}
			
			// if a value generator was successfully generated, then process the value
			if(cellProcessor != null) {
				try {
					rowBuilder.processCell(
							cellProcessor,
							value,
							cell,
							header.getLanguage().orElse(this.lang)
					);
				} catch (Exception e) {
					e.printStackTrace();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					e.printStackTrace(new PrintStream(baos));
					String stacktraceString = new String(baos.toByteArray());
					String stacktraceStringBegin = (stacktraceString.length() > 256)?stacktraceString.substring(0, 256):stacktraceString;
					throw new Xls2RdfException(e, "Convert exception while processing value '"+value+"', cell "+new CellReference(row.getRowNum()+1, colIndex).formatAsString()+" (header "+header.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".\n Message is : "+e.getMessage()+"\n Beginning of stacktrace is "+stacktraceStringBegin);
				}
			}
			
			// reset the current subject after that
			rowBuilder.resetCurrentSubject();
		}
		
		return null == rowBuilder ? null : rowBuilder.rowMainResource;
	}

	private class RowBuilder {
		private final Model model;
		private Resource rowMainResource;
		private Resource currentSubject;
		
		public RowBuilder(Model model, String uri) {
			this.model = model;
			if(uri != null) {
				rowMainResource = SimpleValueFactory.getInstance().createIRI(uri);
				// set the current subject to the main resource by default
				currentSubject = rowMainResource;
			}
		}

		public void processCell(ValueProcessorIfc valueGenerator, String value, Cell cell, String language) {
			// if the column is unknown, ignore it
			// if no current subject was found, cannot add any value
			if(valueGenerator != null && this.currentSubject != null) {				
				valueGenerator.processValue(model, currentSubject, value, cell, language);
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

	public List<Xls2RdfPostProcessorIfc> getPostProcessors() {
		return postProcessors;
	}

	public void setPostProcessors(List<Xls2RdfPostProcessorIfc> postProcessors) {
		this.postProcessors = postProcessors;
	}
	
	public boolean isStrictFormat() {
		return strictFormat;
	}

	public void setStrictFormat(boolean strictFormat) {
		this.strictFormat = strictFormat;
	}

	public Xls2RdfMessageListenerIfc getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(Xls2RdfMessageListenerIfc messageListener) {
		this.messageListener = messageListener;
	}

	public Predicate<IRI> getPropertyValidator() {
		return propertyValidator;
	}

	public void setPropertyValidator(Predicate<IRI> propertyValidator) {
		this.propertyValidator = propertyValidator;
	}

	public void setReconcileService(ReconcileServiceIfc reconcileService) {
		this.reconcileService = reconcileService;
	}

	public boolean isFailIfNoReconcile() {
		return failIfNoReconcile;
	}

	public void setFailIfNoReconcile(boolean failIfNoReconcile) {
		this.failIfNoReconcile = failIfNoReconcile;
	}

	public static void main(String[] args) throws Exception {
		
		// quick and dirty Log4J config
		BasicConfigurator bc = new BasicConfigurator();
		bc.configure((LoggerContext) LoggerFactory.getILoggerFactory());
		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.eclipse.rdf4j")).setLevel(ch.qos.logback.classic.Level.INFO);
		
		runLikeInSkosPlay(new FileInputStream(args[0]), System.out, "en");
		// Method 1 : save each scheme to a separate directory
//		DirectoryModelWriter writer = new DirectoryModelWriter(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/controlled-vocabularies-xls2skos/cv-from-xls2skos"));
//		writer.setSaveGraphFile(true);
//		writer.setGraphSuffix("/graph");
		
		// Method 2 : save everything to a single SKOS file
		// OutputStreamModelWriter ms = new OutputStreamModelWriter(new File("/home/thomas/controlled-vocabularies.ttl"));
		
		// Method 3 : save each scheme to a separate entry in a ZIP file.
		// ZipOutputStreamModelWriter writer = new ZipOutputStreamModelWriter(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/controlled-vocabularies-xls2skos/cv-from-xls2skos.zip"));
		// writer.setSaveGraphFile(true);
		// writer.setGraphSuffix("/graph");
		
		// Xls2RdfConverter me = new Xls2RdfConverter(writer, "fr");
		// me.setPostProcessors(Collections.singletonList(new SkosPostProcessor(false)));
		
		// me.loadAllToFile(new File("/home/thomas/sparna/00-Clients/Sparna/20-Repositories/sparna/fr.sparna/rdf/skos/xls2skos/src/test/resources/test-excel-saved-from-libreoffice.xlsx"));
		// me.loadAllToFile(new File("/home/thomas/sparna/00-Clients/Sparna/20-Repositories/sparna/fr.sparna/rdf/skos/xls2skos/src/test/resources/test-libreoffice.ods"));
		// me.processFile(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/controlled-vocabularies-xls2skos/jolux-controlled-voc-travail-20161026-recup.xlsx"));
		// me.processFile(new File("/home/thomas/sparna/00-Clients/Luxembourg/02-Migration/jolux-controlled-voc-travail-20161012.xlsx"));
	}
	
	
	public static void runLikeInSkosPlay(
			InputStream input,
			OutputStream output,
			String lang
	) throws Exception {
		OutputStreamModelWriter modelWriter = new OutputStreamModelWriter(output);
		Xls2RdfConverter converter = new Xls2RdfConverter(modelWriter, lang);
		converter.setPostProcessors(Collections.singletonList(new SkosPostProcessor(false)));
		converter.processInputStream(input);
	}
	
	

}
