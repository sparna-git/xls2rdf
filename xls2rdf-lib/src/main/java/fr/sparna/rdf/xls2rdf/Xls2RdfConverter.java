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
import fr.sparna.rdf.xls2rdf.processor.ModelDelegationRepositoryValueProcessor;
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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
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
     * Language used to generate the literals
     */
    protected String lang;

    /**
     * Object capable of serializing the resulting models
     */
    protected RepositoryWriterIfc modelWriter;

    /**
     * List of identifiers of all the graphs / concept schemes converted
     */
    private final List<String> convertedVocabularyIdentifiers = new ArrayList<String>();

	public Xls2RdfConverter(RepositoryWriterIfc modelWriter) {		
		this.globalRepository.init();
		this.modelWriter = modelWriter;
	}

    // No direct dependency on Apache POI workbook; use abstraction only.

    /**
     * Global Repository containing all the converted data from all sheets, useful for reconciling values
     */
    private transient Repository globalRepository = new SailRepository(new MemoryStore());

	/*
	 *****************************
	 * PROCESS FROM FILE         *
	 *****************************
	 */
	public Repository processFile(File input) {
		try {
			log.info("Converting file " + input.getAbsolutePath() + "...");
			Workbook workbook = WorkbookFactory.createWorkbook(input);
			return this.processWorkbook(workbook);
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
		Workbook workbook;
		//On garde le contenu de l'input stream car sinon le stream une fois fermé ne peut pas être réutilisé.
		byte[] buffer = null;

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

    private WorkbookMapping workbookMapping;

    public Xls2RdfConverter(RepositoryWriterIfc modelWriter) {
        this(modelWriter, null);
    }

    public Xls2RdfConverter(RepositoryWriterIfc modelWriter, String lang) {
        this.globalRepository.init();
        this.modelWriter = modelWriter;
        this.lang = lang;
    }


    /*
     *****************************
     * PROCESS PART START        *
     * ***************************
     */

    /*
     *****************************
     * PROCESS FROM FILE         *
     *****************************
     */
    public Repository processFile(File input) {
        try {
            log.info("Converting file " + input.getAbsolutePath() + "...");
            Workbook workbook;
            String extension = "";
            //Le pattern récupére l'extension du fichier soit le dernier .xxx
            Pattern p = Pattern.compile("\\.[^.]+$");
            //On applique le pattern sur le nom du fichier
            Matcher m = p.matcher(input.getName().trim());
            //Si c'est bien un fichier on trouve une extension qu'on récupére
            if (m.find()) extension = m.group();
            workbook = switch (extension) {
                //Voir https://support.microsoft.com/fr-fr/office/formats-de-fichier-pris-en-charge-dans-excel-0943ff2c-6014-4e8d-aaea-b83d51d46247
                case ".xls", ".xlsx", ".xlsm" -> ExcelWorkbookFactory.open(input);
                case ".ods" -> OpenDocumentWorkbookFactory.open(input);
                case ".csv" ->
                        CSVWorkbookFactory.open(CSVFormat.DEFAULT, new InputStreamReader(new FileInputStream(input)));
                default -> null;
            };
            return processWorkbook(workbook);
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
        return processWorkbook(workbook);
    }

    /*
     *****************************
     * PROCESS FROM WORKBOOK     *
     * ***************************
     */
    public Repository processWorkbook(Workbook workbook) {

		try {
			// notify begin
			modelWriter.beginWorkbook();
			
			// read all prefixes in all sheets, so that prefixes are shared across all sheets
			initPrefixManager(workbook);
			
			// for every sheet...
			for (Sheet sheet : workbook) {
				// process the sheet, possibly returning an empty result
				Repository r = processSheet(sheet, workbook);
				// we need both ! mrging into the global one is necessary for local reconciliation
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

            // read all prefixes in all sheets, so that prefixes are shared across all sheets
            this.initPrefixManager(workbook);

            // for every sheet...
            for (Sheet sheet : workbook) {
                // process the sheet, possibly returning an empty result
                Repository r = processSheet(sheet);
                // we need both ! mrging into the global one is necessary for local reconciliation
                RepositoryUtil.mergeRepositories(r, outputRepository);
                RepositoryUtil.mergeRepositories(r, this.globalRepository);
            }


            // notify end
            modelWriter.endWorkbook();

		// register prefixes from the workbookMapping if any, and set the baseIRI if any
		if(this.workbookMapping != null){
			baseIRI = this.workbookMapping.getBaseIRI();//<------------- Try get the baseIRI if not null
			if(baseIRI != null) this.prefixManager.setBaseUri(baseIRI);
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
    private void initPrefixManager(Workbook workbook) {
        String baseIRI = null;

        // register prefixes from the workbookMapping if any, and set the baseIRI if any
        if (this.workbookMapping != null) {
            this.workbookMapping.setPrefixManager(this.prefixManager);//<-------- Add the common prefixManager of the current converter to register prefix FROM yaml file
            this.workbookMapping.registerPrefixes();//<----------------- Register prefixes within the prefixManager
            baseIRI = this.workbookMapping.getBaseIRI();//<------------- Try get the baseIRI if not null
            if (baseIRI != null) this.prefixManager.setBaseUri(baseIRI);
        }

	/*
	 *****************************
	 * PROCESS A SINGLE SHEET    *
	 ****************************
	 */
	private Repository processSheet(Sheet sheet, Workbook workbook) {

		Repository outputRepository = new SailRepository(new MemoryStore());
		SimpleValueFactory svf = SimpleValueFactory.getInstance();
		RdfizableSheet rdfizableSheet;
		SheetMapping sheetMapping = null;

        PREFIXES_SHEETS.forEach(sheetName -> {
            Sheet sheet = workbook.getSheet(sheetName);
            if (
                    sheet == null
                    ||
                    sheet.getRow(0) == null
                    ||
                    sheet.getRow(0).getColumnValue(2) != null) {
                return;
            }
            // look if there is a prefix column
            Row firstRow = sheet.getRow(0);

		// We check if a workbookMapping has been sent to treat it
		if(this.workbookMapping != null){
			sheetMapping = workbookMapping.getSheetMappingFor(sheet.getSheetName());
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

                if (prefixColumnIndex != -1) {

                    for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                        if (sheet.getRow(rowIndex) != null) {
                            Row row = sheet.getRow(rowIndex);
                            String prefix = row.getColumnValue(prefixColumnIndex);
                            String namespace = (prefixColumnIndex == 0) ? row.getColumnValue(1) : row.getColumnValue(0);
                            if (StringUtils.isNotBlank(prefix) && StringUtils.isNotBlank(namespace)) {
                                log.debug("Found prefix : " + prefix + " : <" + namespace + ">");
                                this.prefixManager.register(prefix, namespace);
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
        if (baseIRI != null) this.prefixManager.setBaseUri(baseIRI);
    }

    /*
     *****************************
     * PROCESS A SINGLE SHEET    *
     ****************************
     */
    private Repository processSheet(Sheet sheet) {

        Repository outputRepository = new SailRepository(new MemoryStore());
        // initialize target Model
        Model model = new LinkedHashModelFactory().createEmptyModel();
        SimpleValueFactory svf = SimpleValueFactory.getInstance();
        RdfizableSheet rdfizableSheet;
        SheetMapping sheetMapping = null;

        Supplier<RdfizableSheet> getRdfziableSheet = () -> new RdfizableSheet(sheet, this.prefixManager, RdfizableSheet.autoDetectMappingRules(sheet, prefixManager));

        //We check if a workbookMapping has been sent to treat it
        if (this.workbookMapping != null) {
            sheetMapping = workbookMapping.doSheetMappingFromYaml(sheet.getSheetName()); //<------------ init the mapping between the yaml file and RuleMapping class and return the sheetMapping
            if (sheetMapping == null) rdfizableSheet = getRdfziableSheet.get();
            else rdfizableSheet = new RdfizableSheet(sheet, this.prefixManager, sheetMapping);
        } else rdfizableSheet = getRdfziableSheet.get();

					ValueProcessorFactory processorFactory = new ValueProcessorFactory(messageListener);
					
					// always use a default processor
					ValueProcessorIfc cellProcessor = processorFactory.resourceOrLiteral(
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
					ModelDelegationRepositoryValueProcessor repoValueProcessor = new ModelDelegationRepositoryValueProcessor(cellProcessor);
					repoValueProcessor.processValue(outputRepository, graphResource, graphResource, value, cell);
				}
			}
		}

        // read the concept scheme or graph URI
        String graphUri = rdfizableSheet.b1ContainsUri() ? prefixManager.isValidURI(rdfizableSheet.getSchemeOrGraph(), true) : null;
        Resource graphResource = null;
        if (graphUri != null) {
            graphResource = svf.createIRI(graphUri);
        }

        // if the URI was already processed, output a warning (this is a possible case)
        if (graphUri != null && this.convertedVocabularyIdentifiers.contains(graphUri)) {
            log.debug("Duplicate graph declaration found: " + graphUri + " (declared in more than one sheet)");
        }

        // find the title row index
        int headerRowIndex;

					this.reconcileColumnsValues.put(mappingRule, reconciliableValueSet);
				} 
			}
			// read the rows after the header line and process each row
			log.info("Converting rows...");
			int processedRows = 0;
			for (int rowIndex = (headerRowIndex + 1); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row r = sheet.getRow(rowIndex);
				if(r != null) {
					if(rowIndex % 1000 == 0) {
						log.info("Row "+rowIndex+"...");
					}

					Resource rowResource;
					try {
						rowResource = handleRow(r, outputRepository, graphResource, rdfizableSheet, prefixManager);
						processedRows++;
					} catch (Exception e) {
						throw new Xls2RdfException(e, "Exception when processing row "+r.getRowNum()+" in sheet "+r.getSheet().getSheetName()+" : "+e.getMessage(), (Object[])null);
					}
					if(rowResource != null) {
						rowResources.add(rowResource);
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

                // parse the property
                MappingRule mappingRule = headerParser.parse(key);

                if (
                        mappingRule != null
                                &&
                                mappingRule.getProperty() != null
                                &&
                                StringUtils.isNotBlank(value)
                ) {

	private Resource handleRow(
		Row row,
		Repository repository,
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
			rowBuilder = new RowBuilder(repository, headerResource, mainSubject);
		}

                    // support separator option in the header
                    if (mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR) != null) {
                        cellProcessor = processorFactory.split(
                                cellProcessor,
                                mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR)
                        );
                    }

                    log.debug("Adding value on header object \"" + value + "\" with lang " + mappingRule.getLanguage().orElse(this.lang));
                    cellProcessor.processValue(
                            model,
                            graphResource,
                            value,
                            cell,
                            mappingRule.getLanguage().orElse(this.lang)
                    );
                }
            }
        }

        List<Resource> rowResources = new ArrayList<>();
        Map<String, MappingRule> mappingRules = new HashMap<>();

        if (rdfizableSheet.hasDataSection()) {
            // read the column names from the header row
            mappingRules = rdfizableSheet.getSheetMapping().getMappingRule();

            log.debug("Converting data with these columns : ");
            for (String oneHeader : rdfizableSheet.headerLine.getHeaders()) {
                log.debug(oneHeader + " --> " + ((rdfizableSheet.findMappingRuleByHeader(oneHeader) != null) ? rdfizableSheet.findMappingRuleByHeader(oneHeader).getOriginalValue() : " _no mapping_ "));
            }

            // reconcile columns that need to be reconciled, and store result
            for (int i = 0; i < rdfizableSheet.getHeaderLine().getHeaders().size(); i++) {
                String oneHeader = rdfizableSheet.getHeaderLine().getHeaders().get(i);

                // find corresponding mapping rule
                int columnIndex = i;
                MappingRule mappingRule = rdfizableSheet.findMappingRuleByHeader(oneHeader);
                if (mappingRule == null) {
                    continue;
                }
                if (mappingRule.isReconcileExternal() && this.reconcileService != null) {
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

                    this.reconcileColumnsValues.put(mappingRule, reconciliableValueSet);
                }
            }
            // read the rows after the header line and process each row
            log.info("Converting rows...");
            for (int rowIndex = (headerRowIndex + 1); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row r = sheet.getRow(rowIndex);
                if (r != null) {
                    if (rowIndex % 1000 == 0) {
                        log.info("Row " + rowIndex + "...");
                    }

                    Resource rowResource;
                    try {
                        rowResource = handleRow(r, model, graphResource, rdfizableSheet, prefixManager);
                    } catch (Exception e) {
                        throw new Xls2RdfException(e, "Exception when processing row " + r.getRowNum() + " in sheet " + r.getSheet().getSheetName() + " : " + e.getMessage(), (Object[]) null);
                    }
                    if (rowResource != null) {
                        rowResources.add(rowResource);
                    }
                }
            }
        } else {
            log.info("Sheet has no title row, skipping data processing.");
        }


        // writes the resulting Model
        log.debug("Saving graph of " + model.size() + " statements generated from Sheet " + sheet.getSheetName());

        // wraps the Model into a Repository
        try (RepositoryConnection target = outputRepository.getConnection()) {
            prefixManager.getOutputPrefixes().entrySet().forEach(p -> target.setNamespace(p.getKey(), p.getValue()));
            if (graphUri == null) {
                target.add(model);
            } else {
                target.add(model, target.getValueFactory().createIRI(graphUri));
            }
        }

        // always post-process with asList
        ModelDelegationPostProcessor alpp = new ModelDelegationPostProcessor(new AsListPostProcessor());
        alpp.afterSheet(outputRepository, graphResource, rowResources, mappingRules);

        if (this.postProcessors != null && this.postProcessors.size() > 0) {
            log.info("Applying SKOS post-processings on the result");
            for (Xls2RdfPostProcessorIfc aProcessor : this.postProcessors) {
                aProcessor.afterSheet(outputRepository, graphResource, rowResources, mappingRules);
            }
        } else {
            log.info("No post-processings to apply");
        }

        modelWriter.saveRepository(outputRepository, prefixManager.getBaseUri());

        // stores the identifier of generated vocabulary
        convertedVocabularyIdentifiers.add(graphUri);
        return outputRepository;
    }

    private Resource handleRow(Row row, Model model, Resource headerResource, RdfizableSheet rdfizableSheet, PrefixManager prefixManager) {
        RowBuilder rowBuilder = null;

        // first determine the main subject resource
        Resource mainSubject = this.findMainSubject(row, rdfizableSheet, prefixManager);
        if (mainSubject == null) {
            return null;
        } else {
            rowBuilder = new RowBuilder(model, mainSubject);
        }

	private class RowBuilder {
		private final Repository repository;
		private Resource targetGraph;
		private Resource rowMainResource;
		private Resource currentSubject;
		
		public RowBuilder(Repository repository, Resource targetGraph, Resource rowMainResource) {
			this.repository = repository;
			this.targetGraph = targetGraph;
			if(rowMainResource != null) {
				this.rowMainResource = rowMainResource;
				// set the current subject to the main resource by default
				currentSubject = rowMainResource;
			}
		}

		public void processCell(ValueProcessorIfc valueGenerator, String value, Cell cell) {
			// if the column is unknown, ignore it
			// if no current subject was found, cannot add any value
			if(valueGenerator != null && this.currentSubject != null) {     
				ModelDelegationRepositoryValueProcessor repoValueProcessor = new ModelDelegationRepositoryValueProcessor(valueGenerator);
				repoValueProcessor.processValue(this.repository, this.targetGraph, currentSubject, value, cell);           
			}
		}

            Cell cell = row.getCell(colIndex);
            String value = (cell != null) ? cell.getCellValue() : null;

	}

	public List<String> getConvertedVocabularyIdentifiers() {
		return convertedVocabularyIdentifiers;
	}

            ValueProcessorFactory processorFactory = new ValueProcessorFactory(this.messageListener);
            ValueProcessorIfc cellProcessor = null;

            if (mappingRule.getParameters().get(MappingRule.PARAMETER_LOOKUP_COLUMN) != null) {
                // finds the index of the column corresponding to lookupColumn reference
                String lookupColumnRef = mappingRule.getParameters().get(MappingRule.PARAMETER_LOOKUP_COLUMN);
                int lookupColumnIndex = RdfizableSheet.idRefOrPropertyRefToColumnIndex(rdfizableSheet, lookupColumnRef);
                if (lookupColumnIndex == -1) {
                    throw new Xls2RdfException("Unable to find lookupColumn reference '" + lookupColumnRef + "' (full header " + mappingRule.getOriginalValue() + ") in sheet " + row.getSheet().getSheetName() + ".");
                }

                // now find the subject at which the lookupColumn property is attached
                int lookupSubjectColumn = 0;
                if (mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN) != null) {
                    String subjectColumnRef = mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN);
                    lookupSubjectColumn = RdfizableSheet.idRefToColumnIndex(rdfizableSheet, subjectColumnRef);
                    if (lookupSubjectColumn == -1) {
                        throw new Xls2RdfException("Unable to find subjectColumn reference '" + subjectColumnRef + "' (full header " + mappingRule.getOriginalValue() + ") in sheet " + row.getSheet().getSheetName() + ", while processing lookupColumn in header " + mappingRule.getOriginalValue());
                    }
                }

                cellProcessor = processorFactory.lookup(
                        mappingRule,
                        row.getSheet(),
                        lookupColumnIndex,
                        lookupSubjectColumn,
                        prefixManager
                );
            } else if (mappingRule.getParameters().get(MappingRule.PARAMETER_RECONCILE) != null) {
                String reconcileParameterValue = mappingRule.getParameters().get(MappingRule.PARAMETER_RECONCILE);

                if (reconcileParameterValue.equals("local")) {
                    cellProcessor = processorFactory.reconcile(
                            mappingRule,
                            prefixManager,
                            this.reconcileColumnsValues.get(mappingRule)
                    );
                } else if (reconcileParameterValue.equals("external") && this.reconcileService != null) {
                    cellProcessor = processorFactory.reconcile(
                            mappingRule,
                            prefixManager,
                            this.reconcileColumnsValues.get(mappingRule)
                    );
                }

            } else if (mappingRule.getParameters().get(MappingRule.PARAMETER_MANCHESTER) != null) {
                cellProcessor = processorFactory.manchesterClassExpressionParser(mappingRule, prefixManager);
            } else if (mappingRule.getProperty() != null && mappingRule.getProperty().toString().equals("http://www.w3.org/ns/shacl#path")) {
                cellProcessor = new SparqlPathParserProcessor(
                        processorFactory.resourceOrLiteral(mappingRule, prefixManager),
                        mappingRule,
                        prefixManager,
                        messageListener
                );
            }

            // if this is not one of the known processor, but the property is known, then defaults to a generic processor
            // also defaults to a generic processor if a custom datatype is declared on the property
            else if (
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
            if (mappingRule.isIgnoreIfParenthesis()) {
                cellProcessor = processorFactory.ignoreIfParenthesis(cellProcessor);
            }

            // copy to another predicate if needed
            if (mappingRule.getCopyTo() != null) {
                cellProcessor = processorFactory.copyTo(mappingRule.getCopyTo(), cellProcessor);
            }

            if (mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR) != null) {
                cellProcessor = processorFactory.split(cellProcessor, mappingRule.getParameters().get(MappingRule.PARAMETER_SEPARATOR));
                // use a default comma separator for cells that contain URI references
            } else if (
                // if it is a true column with a declared property...
                    mappingRule.getProperty() != null
                            &&
                            !mappingRule.getDatatype().isPresent()
                            &&
                            !mappingRule.getLanguage().isPresent()
                            &&
                            !mappingRule.isManchester()
                            &&
                            (value.startsWith("http") || value.startsWith("mailto") || prefixManager.usesKnownPrefix(value.trim()))
            ) {
                cellProcessor = processorFactory.split(cellProcessor, ",");
            }

            // determine the subject of the triple, be default it is the value of the first column but can be overidden
            if (mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN) != null) {
                String subjectColumnRef = mappingRule.getParameters().get(MappingRule.PARAMETER_SUBJECT_COLUMN);
                int subjectColumnIndex = RdfizableSheet.idRefOrPropertyRefToColumnIndex(rdfizableSheet, subjectColumnRef);
                if (subjectColumnIndex == -1) {
                    throw new Xls2RdfException("Unable to find subjectColumn reference '" + subjectColumnRef + "' (full header " + mappingRule.getOriginalValue() + ") in sheet " + row.getSheet().getSheetName() + ".");
                }

                String currentSubject = (row.getCell(subjectColumnIndex) != null) ? row.getCell(subjectColumnIndex).getCellValue() : null;

                if (currentSubject != null) {
                    try {
                        Resource currentSubjectResource;
                        if (currentSubject.startsWith("_:")) {
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
                        String stacktraceStringBegin = (stacktraceString.length() > 256) ? stacktraceString.substring(0, 256) : stacktraceString;
                        throw new Xls2RdfException(e, "Cannot set subject URI in cell " + subjectColumnRef + (row.getRowNum() + 1) + ", value is '" + currentSubject + "' (header " + mappingRule.getOriginalValue() + ") in sheet " + row.getSheet().getSheetName() + ".\n Message is : " + e.getMessage() + "\n Beginning of stacktrace is " + stacktraceStringBegin);
                    }
                } else {
                    log.warn("Unable to set a new current subject from cell '" + ExcelRefs.cellRef(row.getRowNum(), colIndex) + "' (header " + mappingRule.getOriginalValue() + ") in sheet " + row.getSheet().getSheetName() + ".");
                }
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
        converter.setPostProcessors(Collections.singletonList(new ModelDelegationPostProcessor(new SkosPostProcessor(false))));
        converter.processInputStream(input);
    }

}
