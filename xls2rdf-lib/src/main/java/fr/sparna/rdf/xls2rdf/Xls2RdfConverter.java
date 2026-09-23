package fr.sparna.rdf.xls2rdf;

import fr.sparna.rdf.RepositoryUtil;
import fr.sparna.rdf.xls2rdf.listen.LogXls2RdfMessageListener;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.mapping.MappingRuleParser;
import fr.sparna.rdf.xls2rdf.mapping.SheetMapping;
import fr.sparna.rdf.xls2rdf.mapping.WorkbookMapping;
import fr.sparna.rdf.xls2rdf.postprocess.AsListPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.ModelDelegationPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.SkosPostProcessor;
import fr.sparna.rdf.xls2rdf.processor.SparqlPathParserProcessor;
import fr.sparna.rdf.xls2rdf.processor.ValueProcessorFactory;
import fr.sparna.rdf.xls2rdf.reconcile.*;
import fr.sparna.rdf.xls2rdf.sheet.*;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import fr.sparna.rdf.xls2rdf.sheet.excel.ExcelWorkbookFactory;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentWorkbookFactory;
import fr.sparna.rdf.xls2rdf.write.OutputStreamModelWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;


public class Xls2RdfConverter {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * Object capable of serializing the resulting models
	 */
	protected RepositoryWriterIfc modelWriter;

	/**
	 * List of identifiers of all the graphs / concept schemes converted
	 */
	private final List<String> convertedVocabularyIdentifiers = new ArrayList<String>();
	
	/**
	 * The prefixes declared in the file along with utility classes and default prefixes
	 */
	protected PrefixManager prefixManager = new PrefixManager();
	
	// No direct dependency on Apache POI workbook; use abstraction only.
	
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
	 * Result of reconciliation per header
	 */
	private transient Map<MappingRule, ReconciliableValueSetIfc> reconcileColumnsValues = new HashMap<MappingRule, ReconciliableValueSetIfc>();
	
	/**
	 * Validator capable of telling if a property is valid or not
	 */
	private Predicate<IRI> propertyValidator;
	
	/**
	 * Triggers an Exception if a reconcile fails
	 */
	private boolean failIfNoReconcile = false;

	/**
	 * Whether to skip hidden columns and rows
	 */
	private boolean skipHidden = false;

	public Xls2RdfConverter(RepositoryWriterIfc modelWriter) {		
		this.globalRepository.init();
		this.modelWriter = modelWriter;
	}

	/*
	 *****************************
	 * PROCESS FROM FILE         *
	 *****************************
	 */
    public Repository processFile(File input) {
        return this.processFile(input, null);
    }

	public Repository processFile(File input, WorkbookMapping workbookMapping) {
		try {
			log.info("Converting file " + input.getAbsolutePath() + "...");
			Workbook workbook = WorkbookFactory.createWorkbook(input);
			return this.processWorkbook(workbook, workbookMapping);
		} catch (Exception e) {
			throw Xls2RdfException.rethrow(e);
		}			
	}
	
	/*
	 *****************************
	 * PROCESS FROM INPUTSTREAM  *
	 * ***************************
	 */
    public Repository processInputStream(InputStream input) {
        return this.processInputStream(input, null);
    }

	public Repository processInputStream(InputStream input, WorkbookMapping workbookMapping) {
		Workbook workbook;
		//On garde le contenu de l'input stream car sinon le stream une fois fermé ne peut pas être réutilisé.
		byte[] buffer = null;

		//On essaie de charger le buffer dans chaque implémentations des workbook
		//si une erreur on passe au prochain, jusqu'à ce qu'un workbook soit chargé
		try {
			buffer = input.readAllBytes();
			workbook = ExcelWorkbookFactory.open(new ByteArrayInputStream(buffer));
		} catch (Exception e) {
            try {
				workbook = OpenDocumentWorkbookFactory.open(new ByteArrayInputStream(buffer));
            } catch (Exception ex) {
                try {
					workbook = CSVWorkbookFactory.open(CSVFormat.DEFAULT, new ByteArrayInputStream(buffer), "csv");
                } catch (Exception exc) {
					//Si aucun workbook ne fonctionne on attrape la dernière exception et on lève une erreur
                    throw Xls2RdfException.rethrow(exc);
                }
            }
        }
		return processWorkbook(workbook, workbookMapping);
	}
	
	/*
	 *****************************
	 * PROCESS FROM WORKBOOK     *
	 * ***************************
	 */
    public Repository processWorkbook(Workbook workbook) {
        return this.processWorkbook(workbook, null);
    }

	public Repository processWorkbook(Workbook workbook, WorkbookMapping workbookMapping) {

		Repository outputRepository = new SailRepository(new MemoryStore());		

		try {
			// notify begin
			modelWriter.beginWorkbook();
			
			// read all prefixes in all sheets, so that prefixes are shared across all sheets
			initPrefixManager(workbook, workbookMapping);

			// for every sheet...
			for (Sheet sheet : workbook) {
				// process the sheet, possibly returning an empty result
				Repository r = processSheet(sheet, workbook, workbookMapping);
				// we need both ! merging into the global one is necessary for local reconciliation
				RepositoryUtil.mergeRepositories(r, outputRepository);
				RepositoryUtil.mergeRepositories(r, this.globalRepository);
			}			
			
			// notify end
			modelWriter.endWorkbook();
			
		} catch (Exception e) {
			throw Xls2RdfException.rethrow(e);
		}
		return outputRepository;
	}

	/*
	 *****************************
	 * PROCESS PART END          *
	 * ***************************
	 */



	/*
	 *****************************
	 * INIT PREFIX REGISTRATION  *
	 * ***************************
	 */
	private void initPrefixManager(Workbook workbook, WorkbookMapping workbookMapping) {
		String baseIRI = null;

		// register prefixes from the workbookMapping if any, and set the baseIRI if any
		if(workbookMapping != null){
			baseIRI = workbookMapping.getBaseIRI();//<------------- Try get the baseIRI if not null
			if(baseIRI != null) this.prefixManager.setBaseUri(baseIRI);
		}

		// auto-detect prefixes with PREFIX keyword in the first column
		for (Sheet sheet : workbook) {
			this.autoDetectPrefixes(sheet);
		}

		// also look for a sheet named "prefixes" containing a "prefix" column (all case-insensitive)
		List<String> PREFIXES_SHEETS = Arrays.asList("prefixes", "PREFIXES", "Prefixes");
		List<String> PREFIX_HEADERS = Arrays.asList("prefix", "PREFIX", "Prefix", "@prefix", "@PREFIX", "@Prefix");
		
		PREFIXES_SHEETS.forEach(sheetName -> {
			Sheet sheet = workbook.getSheet(sheetName);
			if(sheet != null) {
				// look if there is a prefix column
				Row firstRow = sheet.getRow(0);
				if(firstRow != null) {
					int prefixColumnIndex = -1;
					if(firstRow.getColumnValue(0) != null && PREFIX_HEADERS.contains(firstRow.getColumnValue(0))) {
						prefixColumnIndex = 0;
					} else if(firstRow.getColumnValue(1) != null && PREFIX_HEADERS.contains(firstRow.getColumnValue(1))) {
						prefixColumnIndex = 1;
					}

					if(prefixColumnIndex != -1) {

						for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
							if(sheet.getRow(rowIndex) != null) {
								Row row = sheet.getRow(rowIndex);
								String prefix = row.getColumnValue(prefixColumnIndex);
								String namespace = (prefixColumnIndex == 0) ? row.getColumnValue(1) : row.getColumnValue(0);
								if(StringUtils.isNotBlank(prefix) && StringUtils.isNotBlank(namespace)) {
									log.debug("Found prefix : "+prefix+" : <"+namespace+">");
									this.prefixManager.register(prefix, namespace);
								}
							}
						}
					}
				}
			}
		});
	}	

	private void autoDetectPrefixes(Sheet sheet) {
		this.prefixManager.register(PrefixManager.readPrefixes(sheet));
		String baseIRI = PrefixManager.readBaseIri(sheet);
		// sets the value only if not null, so that sheets not containing a base will not overwrite previous base declaration
		if(baseIRI != null) this.prefixManager.setBaseUri(baseIRI);
	}

	/*
	 *****************************
	 * PROCESS A SINGLE SHEET    *
	 ****************************
	 */
	private Repository processSheet(Sheet sheet, Workbook workbook, WorkbookMapping workbookMapping) {

		Repository outputRepository = new SailRepository(new MemoryStore());
		
        RdfizableSheet rdfizableSheet;
		// We check if a workbookMapping has been sent to treat it
		if(workbookMapping != null){
			SheetMapping sheetMapping = workbookMapping.getSheetMappingFor(sheet.getSheetName());
			if(sheetMapping == null) {
				// no mapping found by name, try with a unique sheet mapping if we have only one sheet
				if(workbook.size() == 1) {
					sheetMapping = workbookMapping.getUniqueSheetMapping();
				}
			}
			rdfizableSheet = new RdfizableSheet(sheet, this.prefixManager, sheetMapping);
		}
		else rdfizableSheet = new RdfizableSheet(sheet, this.prefixManager, RdfizableSheet.autoDetectMappingRules(sheet, prefixManager));
		
		if(!rdfizableSheet.canRDFize()) {
			log.debug(sheet.getSheetName()+" : Ignoring sheet.");
			return outputRepository;
		}

		// read the concept scheme or graph URI
		String graphUri = rdfizableSheet.b1ContainsUri() ? prefixManager.isValidURI(rdfizableSheet.getSchemeOrGraph(), true) : null;
		Resource graphResource = null;
		if(graphUri != null) {
			graphResource = SimpleValueFactory.getInstance().createIRI(graphUri);
		}

		// if the URI was already processed, output a warning (this is a possible case)
		if(graphUri != null && this.convertedVocabularyIdentifiers.contains(graphUri)) {
			log.debug("Duplicate graph declaration found: " + graphUri + " (declared in more than one sheet)");
		}

		// find the title row index
		int headerRowIndex;

		HeaderLine headerLine = rdfizableSheet.getHeaderLine();		

		// si la ligne d'entete n'a pas été trouvée, on ne génère que la ressource d'entête
		if(headerLine == null) {
			log.info("Could not find header row index in sheet "+sheet.getSheetName()+", will parse header object until end of sheet (last rowNum = "+ sheet.getLastRowNum() +")");
			headerRowIndex = sheet.getLastRowNum()+1;
		} else {
			headerRowIndex = headerLine.getRowIndex();
		}
		
		// validate the sheet
		if(this.propertyValidator != null) {
			log.info("Will validate sheet "+sheet.getSheetName());
			boolean valid = rdfizableSheet.validateHeaders(this.propertyValidator, messageListener);
			if(!valid) {
				log.error("Sheet "+sheet.getSheetName()+" is invalid, skipping sheet processing");
				return outputRepository;
			}
		}
		
		// read the properties on the header by reading the top rows
        if(graphResource != null) {
            try(RepositoryConnection conn = outputRepository.getConnection()) {
                MappingRuleParser headerParser = new MappingRuleParser(prefixManager);
                for (int rowIndex = 1; rowIndex < headerRowIndex; rowIndex++) {
                    if(sheet.getRow(rowIndex) != null) {
                        Row row = sheet.getRow(rowIndex);
                        Cell cellKey = row.getCell(0);
                        String key = (cellKey != null) ? cellKey.getCellValue() : null;
                        Cell cell = row.getCell(1);
                        String value = (cell != null) ? cell.getCellValue() : null;
                            
                        // parse the property	
                        MappingRule mappingRule = headerParser.parse(key);

                        if(
                                mappingRule != null
                                &&
                                mappingRule.getProperty() != null
                                &&
                                StringUtils.isNotBlank(value)
                        ) {				

                            ValueProcessorFactory processorFactory = new ValueProcessorFactory(messageListener);
                            
                            // always use a default processor
                            RepositoryValueProcessorIfc cellProcessor = processorFactory.resourceOrLiteral(
                                mappingRule,
                                prefixManager
                            );
                            
                            // support separator option in the header
                            if(mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR) != null) {
                                cellProcessor = processorFactory.split(
                                        cellProcessor,
                                        mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR)
                                );
                            } 

                            log.debug("Adding value on header object \""+value+"\"");
                            cellProcessor.init(conn, graphResource);
                            cellProcessor.processValue(graphResource, value, cell);
                        }
                    }
                }
            }
        }

		List<Resource> rowResources = new ArrayList<>();
		Map<String, MappingRule> mappingRules = new HashMap<>();

		if(rdfizableSheet.hasDataSection()) {
			// read the column names from the header row
			mappingRules = rdfizableSheet.getSheetMapping().getMappingRule();
			
			log.debug("Converting data with these columns : ");
			for(String oneHeader : rdfizableSheet.headerLine.getHeaders()) {
				log.debug(oneHeader + " --> "+((rdfizableSheet.findMappingRuleByHeader(oneHeader) != null)?rdfizableSheet.findMappingRuleByHeader(oneHeader).getOriginalValue():" _no mapping_ "));
			}
			
			// reconcile columns that need to be reconciled, and store result
			for(int i = 0; i < rdfizableSheet.getHeaderLine().getHeaders().size(); i++) {
				String oneHeader = rdfizableSheet.getHeaderLine().getHeaders().get(i);

				// find corresponding mapping rule
				int columnIndex = i;
				MappingRule mappingRule = rdfizableSheet.findMappingRuleByHeader(oneHeader);
				if(mappingRule == null){
					continue;
				}
				if(mappingRule.isReconcileExternal() && this.reconcileService != null) {
					    PreloadedReconciliableValueSet reconciliableValueSet = new PreloadedReconciliableValueSet(
							reconcileService,
							this.failIfNoReconcile
					);
					    reconciliableValueSet.initReconciledValues(
						    PreloadedReconciliableValueSet.extractDistinctValues(sheet, columnIndex, headerRowIndex),
							mappingRule.getReconcileOn(),
							this.messageListener
					);
					
					this.reconcileColumnsValues.put(mappingRule, reconciliableValueSet);
					
				} else if (mappingRule.isReconcileLocal()) {
					SparqlReconcileService reconcileService = new SparqlReconcileService(this.globalRepository);
					
					DynamicReconciliableValueSet reconciliableValueSet = new DynamicReconciliableValueSet(
							reconcileService,
							mappingRule.getReconcileOn(),
							this.failIfNoReconcile,
							this.messageListener
					);

					this.reconcileColumnsValues.put(mappingRule, reconciliableValueSet);
				} 
			}
			// read the rows after the header line and process each row
			log.info("Converting rows...");
			int processedRows = 0;
            try(RepositoryConnection conn = outputRepository.getConnection()) {
                for (int rowIndex = (headerRowIndex + 1); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row r = sheet.getRow(rowIndex);
                    if(r != null) {
                        if(rowIndex % 1000 == 0) {
                            log.info("Row "+rowIndex+"...");
                        }

                        Resource rowResource;
                        try {
                            rowResource = handleRow(r, conn, graphResource, rdfizableSheet, prefixManager);
                            processedRows++;
                        } catch (Exception e) {
                            throw new Xls2RdfException(e, "Exception when processing row "+r.getRowNum()+" in sheet "+r.getSheet().getSheetName()+" : "+e.getMessage(), (Object[])null);
                        }
                        if(rowResource != null) {
                            rowResources.add(rowResource);
                        }
                    }
                }
            }
            log.info("Converted "+processedRows+" rows");
		} else {
			log.info("Sheet has no title row, skipping data processing.");
		}
		
		// writes the resulting Model
		log.debug("Saving graph of "+outputRepository.getConnection().size()+" statements generated from Sheet "+sheet.getSheetName());

		// always post-process with asList
		ModelDelegationPostProcessor alpp = new ModelDelegationPostProcessor(new AsListPostProcessor());
		alpp.afterSheet(outputRepository, graphResource, rowResources, mappingRules);

		if(this.postProcessors != null && this.postProcessors.size() > 0) {
			log.info("Applying SKOS post-processings on the result");
			for(Xls2RdfPostProcessorIfc aProcessor : this.postProcessors) {
				aProcessor.afterSheet(outputRepository, graphResource, rowResources, mappingRules);
			}
		} else {
			log.info("No post-processings to apply");
		}

        // write prefixes
        try(RepositoryConnection conn = outputRepository.getConnection()) {
            this.prefixManager.getOutputPrefixes().entrySet().forEach(p -> conn.setNamespace(p.getKey(), p.getValue()));
        }

		modelWriter.saveRepository(outputRepository, prefixManager.getBaseUri());
		
		// stores the identifier of generated vocabulary
		convertedVocabularyIdentifiers.add(graphUri);
		return outputRepository;
	}

	private Resource handleRow(
		Row row,
		RepositoryConnection connection,
		Resource headerResource,
		RdfizableSheet rdfizableSheet,
		PrefixManager prefixManager
	) {
		RowBuilder rowBuilder = null;

		// first determine the main subject resource
		Resource mainSubject = this.findMainSubject(row, rdfizableSheet, prefixManager);
		if(mainSubject == null) {
			log.debug("Cannot find main subject at row "+row.getRowNum()+", skip processing");
			return null;
		} else {
			rowBuilder = new RowBuilder(connection, headerResource, mainSubject);
		}

		for (int colIndex = 0; colIndex < rdfizableSheet.getHeaderLine().getHeaders().size(); colIndex++) {
			// skip hidden columns
			if(skipHidden && row.getSheet().isColumnHidden(colIndex)) {
				continue;
			}

			// get corresponding ColumnHeader + MappingRule
			String header = rdfizableSheet.getHeaderLine().getHeaders().get(colIndex);
			MappingRule mappingRule = rdfizableSheet.findMappingRuleByHeader(header);
			// if the column is not mapped, skip - don't even bother reading cell content
			if(mappingRule == null) continue;

			Cell cell = row.getCell(colIndex);            
			String value = (cell != null)?cell.getCellValue():null;

			// if nothing, skip
			if (value == null || StringUtils.isBlank(value)) {
				continue;
			}
			
			// skip the cell if it is striked out
			if(cell != null && cell.isStruckThrough()) {				
				continue;
			}

			// test if cell should be ignored
			if(mappingRule.getParameters().get(MappingRule.PARAMETER_IGNORE_IF) != null) {
				if(value.equals(mappingRule.getParameters().get(MappingRule.PARAMETER_IGNORE_IF))) {
					// skip cell
					continue;
				}
			}
			
			ValueProcessorFactory processorFactory = new ValueProcessorFactory(this.messageListener);
			RepositoryValueProcessorIfc cellProcessor = null;			
			
			if(mappingRule.getParameters().get(MappingRule.PARAMETER_LOOKUP_COLUMN) != null) {
				// finds the index of the column corresponding to lookupColumn reference
				String lookupColumnRef = mappingRule.getParameters().get(MappingRule.PARAMETER_LOOKUP_COLUMN);
				int lookupColumnIndex = RdfizableSheet.idRefOrPropertyRefToColumnIndex(rdfizableSheet, lookupColumnRef);
				if(lookupColumnIndex == -1) {
					throw new Xls2RdfException("Unable to find lookupColumn reference '"+lookupColumnRef+"' (full header "+mappingRule.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
				}
				
				// now find the subject at which the lookupColumn property is attached
				int lookupSubjectColumn = 0;
				if(mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN) != null) {
					String subjectColumnRef = mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN);
					lookupSubjectColumn = RdfizableSheet.idRefToColumnIndex(rdfizableSheet, subjectColumnRef);
					if(lookupSubjectColumn == -1) {
						throw new Xls2RdfException("Unable to find subjectColumn reference '"+subjectColumnRef+"' (full header "+mappingRule.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+", while processing lookupColumn in header "+mappingRule.getOriginalValue());
					}
				}
				
				cellProcessor = processorFactory.lookup(
						mappingRule,
						row.getSheet(),
						lookupColumnIndex,
						lookupSubjectColumn,
						prefixManager
				);
			}
			
			else if(mappingRule.getParameters().get(MappingRule.PARAMETER_RECONCILE) != null) {
				String reconcileParameterValue = mappingRule.getParameters().get(MappingRule.PARAMETER_RECONCILE);					
				
				if(reconcileParameterValue.equals("local")) {						
					cellProcessor = processorFactory.reconcile(
							mappingRule,
							prefixManager,
							this.reconcileColumnsValues.get(mappingRule)
					);
				} else if(reconcileParameterValue.equals("external") && this.reconcileService != null) {						
					cellProcessor = processorFactory.reconcile(
							mappingRule,
							prefixManager,
							this.reconcileColumnsValues.get(mappingRule)
					);
				}
				
			}
			
			else if(mappingRule.getParameters().get(MappingRule.PARAMETER_MANCHESTER) != null) {
				cellProcessor = processorFactory.manchesterClassExpressionParser(mappingRule, prefixManager);
			}

			else if(mappingRule.getProperty() != null && mappingRule.getProperty().toString().equals("http://www.w3.org/ns/shacl#path")) {
				cellProcessor = new SparqlPathParserProcessor(
					processorFactory.resourceOrLiteral(mappingRule, prefixManager),
					mappingRule,
					prefixManager,
					messageListener
				);
			}
			
			// if this is not one of the known processor, but the property is known, then defaults to a generic processor
			// also defaults to a generic processor if a custom datatype is declared on the property
			else if(
					(
							cellProcessor == null
							||
							mappingRule.getDatatype().isPresent()
							||
							(mappingRule.getParameters() != null && !mappingRule.getParameters().isEmpty())
					)
					&&
					mappingRule.getProperty() != null
			) {
				cellProcessor = processorFactory.resourceOrLiteral(
						mappingRule,
						prefixManager
				);
			}
			
			// if we requested to ignore values in parenthesis, wrap the processor into adequate processor
			if(mappingRule.isIgnoreIfParenthesis()) {
				cellProcessor = processorFactory.ignoreIfParenthesis(cellProcessor);
			}

			// copy to another predicate if needed
			if(mappingRule.getCopyTo() != null) {
				cellProcessor = processorFactory.copyTo(mappingRule.getCopyTo(), cellProcessor);
			}
			
			if(mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR) != null) {
				cellProcessor = processorFactory.split(cellProcessor, mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR));
				// use a default comma separator for cells that contain URI references
			} else if(
				// if it is a true column with a declared property...
				mappingRule.getProperty() != null
				&&
				!mappingRule.getDatatype().isPresent()
				&&
				!mappingRule.getLanguage().isPresent()
				&&
				!mappingRule.isManchester()
				&&
				(value.startsWith("http")  || value.startsWith("mailto") || prefixManager.usesKnownPrefix(value.trim()))
			) {
				cellProcessor = processorFactory.split(cellProcessor, ",");
			}
			
			// determine the subject of the triple, be default it is the value of the first column but can be overidden
			if(mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN) != null) {
				String subjectColumnRef = mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN);
				int subjectColumnIndex = RdfizableSheet.idRefOrPropertyRefToColumnIndex(rdfizableSheet, subjectColumnRef);
				if(subjectColumnIndex == -1) {
					throw new Xls2RdfException("Unable to find subjectColumn reference '"+subjectColumnRef+"' (full header "+mappingRule.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
				}
				
				String currentSubject = (row.getCell(subjectColumnIndex) != null)?row.getCell(subjectColumnIndex).getCellValue():null;

				if(currentSubject != null) {
					try {
						Resource currentSubjectResource;
						if(currentSubject.startsWith("_:")) {
							currentSubjectResource = SimpleValueFactory.getInstance().createBNode(currentSubject.substring(2));
						} else {
							currentSubjectResource = SimpleValueFactory.getInstance().createIRI(prefixManager.isValidURI(currentSubject, false));
						}
						
						rowBuilder.setCurrentSubject(currentSubjectResource);
					} catch (Exception e) {
						e.printStackTrace();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(baos));
						String stacktraceString = new String(baos.toByteArray());
						String stacktraceStringBegin = (stacktraceString.length() > 256)?stacktraceString.substring(0, 256):stacktraceString;
						throw new Xls2RdfException(e, "Cannot set subject URI in cell "+subjectColumnRef+(row.getRowNum()+1)+", value is '"+ currentSubject +"' (header "+mappingRule.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".\n Message is : "+e.getMessage()+"\n Beginning of stacktrace is "+stacktraceStringBegin);
					}
				} else {
					log.warn("Unable to set a new current subject from cell '"+ExcelRefs.cellRef(row.getRowNum(), colIndex)+"' (header "+mappingRule.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".");
				}
			}

			// TODO : this can enable to generate RDF list from a comma-separated cell values
			// if(mappingRule.isAsList()) {
			// 	cellProcessor = processorFactory.asList(mappingRule, cellProcessor);
			// }

			if(mappingRule.getWrapper() != null) {
				if(mappingRule.getWrapper().toString().equals(SHACL.OR.toString())) {
					cellProcessor = processorFactory.wrapWithShaclLogicalOperator(mappingRule, SHACL.OR, cellProcessor);
				} else if(mappingRule.getWrapper().toString().equals(SHACL.AND.toString())) {
					cellProcessor = processorFactory.wrapWithShaclLogicalOperator(mappingRule, SHACL.AND, cellProcessor);
				} else if(mappingRule.getWrapper().toString().equals(SHACL.XONE.toString())) {
					cellProcessor = processorFactory.wrapWithShaclLogicalOperator(mappingRule, SHACL.XONE, cellProcessor);
				}
			} 
			
			// if a value generator was successfully generated, then process the value
			if(cellProcessor != null) {
				try {
                        cellProcessor.init(connection, headerResource);
					    rowBuilder.processCell(
							cellProcessor,
							value,
						    cell
					);
				} catch (Exception e) {
					e.printStackTrace();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					e.printStackTrace(new PrintStream(baos));
					String stacktraceString = new String(baos.toByteArray());
					String stacktraceStringBegin = (stacktraceString.length() > 256)?stacktraceString.substring(0, 256):stacktraceString;
					throw new Xls2RdfException(e, "Convert exception while processing value '"+value+"', cell "+ExcelRefs.cellRef(row.getRowNum(), colIndex)+" (header "+mappingRule.getOriginalValue()+") in sheet "+row.getSheet().getSheetName()+".\n Message is : "+e.getMessage()+"\n Beginning of stacktrace is "+stacktraceStringBegin);
				}
			}
			
			// reset the current subject after that
			rowBuilder.resetCurrentSubject();
		}
		
		return null == rowBuilder ? null : rowBuilder.rowMainResource;
	}

	private Resource findMainSubject(Row row, RdfizableSheet rdfizableSheet, PrefixManager prefixManager) {
		int subjectColumnIndex = findSubjectColumnIndex(row, rdfizableSheet, prefixManager);
		Resource subjectResource = null;
		if(subjectColumnIndex >= 0) {
			Cell cell = row.getCell(subjectColumnIndex);            
			String value = (cell != null)?cell.getCellValue():null;

			if(value != null) {
				if(value.startsWith("_:")) {
					subjectResource = SimpleValueFactory.getInstance().createBNode(value.substring(2));
				} else {
					String iriPossiblyNull = prefixManager.isValidURI(value, false);
					// this can be null in the case column A does not contain a valid full or prefixed IRI
					if(iriPossiblyNull != null) {
						subjectResource = SimpleValueFactory.getInstance().createIRI(iriPossiblyNull);
					}
				}
			}
		}

		return subjectResource;
	}

	private int findSubjectColumnIndex(Row row, RdfizableSheet rdfizableSheet, PrefixManager prefixManager) {
		int subjectColumnIndex = -1;

		// first look for a column named "URI"
		for (int colIndex = 0; colIndex < rdfizableSheet.getHeaderLine().getHeaders().size(); colIndex++) {
			String header = rdfizableSheet.getHeaderLine().getHeaders().get(colIndex);
			if(header.equals("URI") || header.equals("IRI")) {
				Cell cell = row.getCell(colIndex);            
				String value = (cell != null)?cell.getCellValue():null;

				// if the value is empty, or is struck through, or if it is hidden, don't use it
				if (
					!(
						StringUtils.isBlank(value)
						||
						(cell != null && cell.isStruckThrough())
						||
						(skipHidden && row.isHidden())	
					)						
				) {
					subjectColumnIndex = colIndex;
					break;
				}
			}
		}

		// not found, find the first (left-most) non-empty column
		if(subjectColumnIndex == -1) {
			for (int colIndex = 0; colIndex < rdfizableSheet.getHeaderLine().getHeaders().size(); colIndex++) {
				Cell cell = row.getCell(colIndex);            
				String value = (cell != null)?cell.getCellValue():null;

				String oneHeader = rdfizableSheet.getHeaderLine().getHeaders().get(colIndex);

				// find corresponding mapping rule
				MappingRule mappingRule = rdfizableSheet.findMappingRuleByHeader(oneHeader);
				// everything mapped cannot be the subject URI, skip it
				if(mappingRule != null && mappingRule.getProperty() != null) {
					continue;
				}

				// use the first non-empty cell
				if (
					!(
						StringUtils.isBlank(value)
						||
						(cell != null && cell.isStruckThrough())
						||
						(skipHidden && row.isHidden())		
					)				
				) {
					subjectColumnIndex = colIndex;
					break;
				}
			}
		}

		return subjectColumnIndex;
	}

	private class RowBuilder {
		private final RepositoryConnection connection;
		private Resource targetGraph;
		private Resource rowMainResource;
		private Resource currentSubject;
		
		public RowBuilder(RepositoryConnection connection, Resource targetGraph, Resource rowMainResource) {
			this.connection = connection;
			this.targetGraph = targetGraph;
			if(rowMainResource != null) {
				this.rowMainResource = rowMainResource;
				// set the current subject to the main resource by default
				currentSubject = rowMainResource;
			}
		}

		public void processCell(RepositoryValueProcessorIfc valueGenerator, String value, Cell cell) {
			// if the column is unknown, ignore it
			// if no current subject was found, cannot add any value
			if(valueGenerator != null && this.currentSubject != null) {     
                valueGenerator.init(connection, targetGraph);
				valueGenerator.processValue(currentSubject, value, cell);           
			}
		}

		public void setCurrentSubject(Resource currentSubject) {
			this.currentSubject = currentSubject;
		}
		
		public void resetCurrentSubject() {
			this.currentSubject = this.rowMainResource;
		}

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

	public boolean isSkipHidden() {
		return skipHidden;
	}

	public void setSkipHidden(boolean skipHidden) {
		this.skipHidden = skipHidden;
	}


	public static void main(String[] args) throws Exception {
		
		// quick and dirty Log4J config
		// BasicConfigurator bc = new BasicConfigurator();
		// bc.configure((LoggerContext) LoggerFactory.getILoggerFactory());
		// ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.eclipse.rdf4j")).setLevel(Level.INFO);
		
		runLikeInSkosPlay(new FileInputStream(args[0]), System.out);
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
			OutputStream output
	) throws Exception {
		OutputStreamModelWriter modelWriter = new OutputStreamModelWriter(output);
		Xls2RdfConverter converter = new Xls2RdfConverter(modelWriter);
		converter.setPostProcessors(Collections.singletonList(new ModelDelegationPostProcessor(new SkosPostProcessor(false))));
		converter.processInputStream(input);
	}

}
