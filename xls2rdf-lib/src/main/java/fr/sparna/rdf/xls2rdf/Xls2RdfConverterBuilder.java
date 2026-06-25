package fr.sparna.rdf.xls2rdf;

import fr.sparna.rdf.xls2rdf.postprocess.OWLPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.QBPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.SkosPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.SkosXlPostProcessor;
import fr.sparna.rdf.xls2rdf.reconcile.SparqlReconcileService;
import fr.sparna.rdf.xls2rdf.write.ModelWriterFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Xls2RdfConverterBuilder {

    private static final Logger log = LoggerFactory.getLogger(Xls2RdfConverterBuilder.class);

    /*
    PROPERTIES
     */
    private Repository supportRepository;

    private ModelWriterFactory modelWriterFactory;

    private ModelWriterIfc modelWriter;

    private RDFFormat format;

    private String language;

    private WorkbookMapping workbookMapping;

    private boolean applyPostProcessing;

    private boolean generateXl;

    private boolean generateXlDefinitions;

    private boolean failOnReconcile;

    private boolean generateBroaderTransitive;

    private boolean skipHidden;

    private Xls2RdfConverterBuilder(){}

    public static Xls2RdfConverterBuilder getInstance(){
         return new Xls2RdfConverterBuilder();
    }

    /*
    BUILD OPTIONS
     */
    public Xls2RdfConverterBuilder withFormat(String format){
        RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
        if(format != null && writerRegistry.getFileFormatForMIMEType(format).isPresent()) this.format = writerRegistry.getFileFormatForMIMEType(format).get();
        else this.format = RDFFormat.TURTLE;
        return this;
    }

    public Xls2RdfConverterBuilder withFormat(Supplier<RDFFormat> applyFormatConversion){
        this.format = applyFormatConversion.get();
        return this;
    }

    public Xls2RdfConverterBuilder withModelWriterFactory(boolean useZip, boolean useGraph, boolean isPretty){
        this.modelWriterFactory = new ModelWriterFactory(useZip, this.format, useGraph);
        this.modelWriterFactory.setGrouping(isPretty);
        return this;
    }

    public Xls2RdfConverterBuilder withWorkbookMapping(WorkbookMapping workbookMapping){
        this.workbookMapping = workbookMapping;
        return this;
    }

    public Xls2RdfConverterBuilder withOutputDirectory(File output) {
        this.modelWriter = this.modelWriterFactory.buildNewModelWriter(output);
        return this;
    }

    public Xls2RdfConverterBuilder withOutputStream(OutputStream out)  {
        this.modelWriter = this.getModelWriterFactory().buildNewModelWriter(out);
        return this;
    }

    public Xls2RdfConverterBuilder withSupportRepository(Consumer<Repository> initRDFRepository)  {
        this.supportRepository = new SailRepository(new MemoryStore());
        this.supportRepository.init();
        initRDFRepository.accept(this.supportRepository);
        return this;
    }

    public Xls2RdfConverterBuilder withSupportRepository(File externalArg) throws IOException {
         this.supportRepository = new SailRepository(new MemoryStore());
         this.supportRepository.init();
         try (RepositoryConnection connection = this.supportRepository.getConnection()) {
            if (externalArg != null) {
                if (externalArg.isFile()) {
                    connection.add(externalArg, externalArg.toURI().toString(), this.getFormat());
                } else {
                    for (File anExternalFile : externalArg.listFiles()) {
                        connection.add(anExternalFile, anExternalFile.toURI().toString(), this.format);
                    }
                }
            }
            return this;
        }
    }

    public Xls2RdfConverterBuilder withApplyPostProcessing(boolean applyPostProcessing) {
        this.applyPostProcessing = applyPostProcessing;
        return this;
    }

    public Xls2RdfConverterBuilder withGenerateXl(boolean generateXl) {
        this.generateXl = generateXl;
        return this;
    }

    public Xls2RdfConverterBuilder withGenerateXlDefinitions(boolean generateXlDefinitions) {
        this.generateXlDefinitions = generateXlDefinitions;
        return this;
    }

    public Xls2RdfConverterBuilder withFailOnReconcile(boolean failOnReconcile) {
        this.failOnReconcile = failOnReconcile;
        return this;
    }

    public Xls2RdfConverterBuilder withGenerateBroaderTransitive(boolean generateBroaderTransitive) {
        this.generateBroaderTransitive = generateBroaderTransitive;
        return this;
    }

    public Xls2RdfConverterBuilder withSkipHidden(boolean skipHidden) {
        this.skipHidden = skipHidden;
        return this;
    }

    public Xls2RdfConverterBuilder withLanguage(String language){
         this.language = language;
         return this;
    }

    /*
    BUILD METHOD
     */
    public Xls2RdfConverter buildConverter(){
        Xls2RdfConverter converter = new Xls2RdfConverter(this.modelWriter, this.language);
        if(this.isApplyPostProcessing()) {
            List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
            // add QB post processor
            postProcessors.add(new QBPostProcessor());
            // add OWL post process
            postProcessors.add(new OWLPostProcessor());
            // add SKOS post processor
            postProcessors.add(new SkosPostProcessor(this.generateBroaderTransitive));
            // if needed, add SKOS-XL post-processor
            if(this.generateXl || this.generateXlDefinitions) {
                postProcessors.add(new SkosXlPostProcessor(this.generateXl, this.generateXlDefinitions));
            }
            converter.setPostProcessors(postProcessors);
        }

        if(this.supportRepository != null) {
            log.info("Setting a support repository that contains " + this.supportRepository.getConnection().size() + " triples");
            converter.setReconcileService(new SparqlReconcileService(this.supportRepository));
        }

        converter.setFailIfNoReconcile(!this.failOnReconcile);
        converter.setSkipHidden(this.skipHidden);
        converter.setWorkbookMapping(this.workbookMapping);

        return converter;
    }

    /*
    GETTERS
     */
    public Repository getSupportRepository() {
        return this.supportRepository;
    }

    public ModelWriterFactory getModelWriterFactory() {
        return this.modelWriterFactory;
    }

    public ModelWriterIfc getModelWriter() {
        return this.modelWriter;
    }

    public RDFFormat getFormat() {
        return this.format;
    }

    public String getLanguage() {
        return this.language;
    }

    public boolean isApplyPostProcessing() {
        return this.applyPostProcessing;
    }

    public boolean isGenerateXl() {
        return this.generateXl;
    }

    public boolean isGenerateXlDefinitions() {
        return this.generateXlDefinitions;
    }

    public boolean isFailOnReconcile() {
        return this.failOnReconcile;
    }

    public boolean isGenerateBroaderTransitive() {
        return this.generateBroaderTransitive;
    }

    public boolean isSkipHidden() {
        return this.skipHidden;
    }

    public WorkbookMapping getWorkbookMapping() {return this.workbookMapping;}


}
