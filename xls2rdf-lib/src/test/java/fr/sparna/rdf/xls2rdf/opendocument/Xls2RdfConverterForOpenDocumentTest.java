package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.SimpleInvalidPropertyListValidator;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;
import fr.sparna.rdf.xls2rdf.postprocess.QBPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.SkosPostProcessor;
import fr.sparna.rdf.xls2rdf.reconcile.SparqlReconcileService;
import fr.sparna.rdf.xls2rdf.write.RepositoryModelWriter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.trig.TriGWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Xls2RdfConverterForOpenDocumentTest {


     final static Logger LOGGER = LoggerFactory.getLogger(Xls2RdfConverterForOpenDocumentTest.class);

     List<Model> models;
     Xls2RdfConverter converter;
     DirectoryStream<Path> testFolder;
     final static URL PATH_TO_FOLDER_FOR_TESTING;
     static{
         PATH_TO_FOLDER_FOR_TESTING = Xls2RdfConverterForOpenDocumentTest.class.getResource("/opendocument/suite");
         Assert.assertNotNull("PATH_TO_FOLDER_TO_TESTING is null.", PATH_TO_FOLDER_FOR_TESTING);
     }
     Repository outputRepository;


     @Before
     public void init() throws URISyntaxException, IOException {
         //Init d'un directoryStream pour iterer sur tous les éléments de testFolder
         this.testFolder = Files.newDirectoryStream(Path.of(PATH_TO_FOLDER_FOR_TESTING.toURI()));
         Assert.assertNotNull("directoryStream is null.", this.testFolder);

         for(Path p: testFolder){
             if(Files.isRegularFile(p)) continue;
             Assert.assertNotNull("AbstractPath p is null.", p);
         }

         this.outputRepository = new SailRepository(new MemoryStore());
         this.outputRepository.init();

         this.converter = new Xls2RdfConverter(new RepositoryModelWriter(outputRepository), "fr");
         this.converter.setSkipHidden(true);

         // init post processors
         List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
         postProcessors.add(new QBPostProcessor());
         postProcessors.add(new SkosPostProcessor(false));
         this.converter.setPostProcessors(postProcessors);
         this.converter.setFailIfNoReconcile(false);

         // to test for invalid properties
         this.converter.setPropertyValidator(new SimpleInvalidPropertyListValidator(Collections.singletonList(
                 SimpleValueFactory.getInstance().createIRI("http://labs.sparna.fr/skos-play/convert/invalidProperty")
         )));


     }


     @Test
     public void run() throws URISyntaxException {

         // On itere sur le dossier de test pour trouver chaque sous-dossier
         try(DirectoryStream<Path> testFolder = Files.newDirectoryStream(Path.of(PATH_TO_FOLDER_FOR_TESTING.toURI()));){
             for(Path path: testFolder){
                 //Si des fichiers sont présents on les ignores: ex: gitignore ou autre pour rechercher uniquement les sous-dossiers
                 if(path.toFile().isFile()) continue;
                 //On itere sur chaque sous dossier un par un.
                 try(DirectoryStream<Path> currentTestFolder = Files.newDirectoryStream(path)){
                     //On itere sur chaque fichier du sous-dossier
                     for(Path file: currentTestFolder){
                         // set external data for reconcile if present
                         File external = new File(path.toFile().getAbsoluteFile(), "/external.ttl");
                         System.out.println("exnternal.ttl file : " + external);
                         if(external.exists()){
                             Repository externalRepository = new SailRepository(new MemoryStore());
                             externalRepository.init();
                             try(RepositoryConnection c = externalRepository.getConnection()) {
                                 c.add(Rio.parse(new FileInputStream(external), external.toURI().toURL().toString(), RDFFormat.TURTLE));
                             }catch (Exception e){
                                 throw new IllegalArgumentException("Problem with external.ttl in unit test "+this.testFolder, e);
                             }
                             this.converter.setReconcileService(new SparqlReconcileService(externalRepository));
                         }

                         //On cherche le fichier .ods à essayer
                         if(file.toFile().getName().endsWith(".ods")){
                             System.out.println("Testing converter for " + file.toAbsolutePath() + ".");
                             this.models = this.converter.processFile(file.toFile());

                             // reput everything in flat repositories for proper comparisons without the graphs
                             Repository outputRepositoryToCompare = new SailRepository(new MemoryStore());
                             outputRepositoryToCompare.init();

                             Model outputModel = new LinkedHashModelFactory().createEmptyModel();
                             try(RepositoryConnection connection = outputRepository.getConnection()) {
                                 // print result in ttl (notes: prints all graphs)
                                 connection.export(new TriGWriter(System.out));
                                 connection.export(new StatementCollector(outputModel));

                                 try {
                                     File output = new File(path.toFile().getAbsoluteFile(), "/output.trig");
                                     System.out.println("output.trig file : " + output);
                                     if(!output.exists()) {
                                         output.createNewFile();
                                     }
                                     FileOutputStream out = new FileOutputStream(output);
                                     connection.export(new TriGWriter(out));
                                 } catch (Exception e) {
                                     throw new IllegalArgumentException("Problem with output.trig in unit test "+this.testFolder, e);
                                 }

                                 try(RepositoryConnection connectionToCompare = outputRepositoryToCompare.getConnection()) {
                                     connectionToCompare.add(outputModel, (Resource)null);
                                     try {
                                         File output = new File(path.toFile().getAbsoluteFile(), "/output.ttl");
                                         System.out.println("output.ttl file : " + output);
                                         if(!output.exists()) {
                                             output.createNewFile();
                                         }
                                         FileOutputStream out = new FileOutputStream(output);
                                         connection.export(new TurtleWriter(out));
                                     } catch (Exception e) {
                                         throw new IllegalArgumentException("Problem with output.ttl in unit test "+this.testFolder, e);
                                     }
                                 }
                             }
                             //Look for expected to compare
                             File expected = new File(path.toFile().getAbsolutePath(), "/expected.ttl");
                             System.out.println("Expected file : " + expected);
                             if(expected.exists()) {
                                 Repository expectedRepository = new SailRepository(new MemoryStore());
                                 expectedRepository.init();
                                 try(RepositoryConnection expectedConnection = expectedRepository.getConnection()) {
                                     expectedConnection.add(Rio.parse(new FileInputStream(expected), expected.toURI().toURL().toString(), RDFFormat.TURTLE));
                                 } catch (Exception e) {
                                     throw new IllegalArgumentException("Problem with expected.ttl in unit test "+this.testFolder, e);
                                 }
                             }
                         }

                     }
                 }
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }

     }
}
