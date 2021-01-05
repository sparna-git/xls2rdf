package fr.sparna.rdf.xls2rdf;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;

public class SkosPostProcessor implements Xls2RdfPostProcessorIfc {
  private static final String CALCULATE_BROADER_TRANSITIVE_SPARQL = "postprocessing/broaderTransitive.ru";

  private Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private final boolean runBroaderTransitive;

  @Deprecated
  public SkosPostProcessor() {
    this(false);
  }

  public SkosPostProcessor(boolean runBroaderTransitive) {
    this.runBroaderTransitive = runBroaderTransitive;
  }

  @Override
  public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources) {
    log.debug("Postprocessing : " + this.getClass().getSimpleName());

    boolean isMainResourceConceptScheme = true;
    if (
            new HasRdfTypeTest(model).test(mainResource)
            &&
            model.filter(mainResource, RDF.TYPE, SKOS.CONCEPT_SCHEME).isEmpty()
    ) {
      isMainResourceConceptScheme = false;
    }

    if (isMainResourceConceptScheme) {
      log.debug("Considering main resource like a ConceptScheme.");
    }


    // if the main resource is a ConceptScheme, add skos:Concept to each entry
    if (isMainResourceConceptScheme) {
      rowResources.stream().forEach(rowResource -> {
        // if, after row processing, no rdf:type was generated, then we consider the row to be a skos:Concept
        // this allows to generate something else that skos:Concept
        if (model.filter(rowResource, RDF.TYPE, null).isEmpty()) {
          model.add(rowResource, RDF.TYPE, SKOS.CONCEPT);
        }
      });
    }

    // add the inverse broaders and narrowers
    log.debug("Adding inverse skos:broader and skos:narrower");
    model.filter(null, SKOS.BROADER, null).forEach(
            s -> {
              if (s.getObject() instanceof Resource) {
                model.add(((Resource) s.getObject()), SKOS.NARROWER, s.getSubject());
              }
              else {
                log.warn("Found a skos:broader with Literal value : '" + s.getObject().stringValue() + "'");
              }
            }
    );
    model.filter(null, SKOS.NARROWER, null).forEach(
            s -> {
              if (s.getObject() instanceof Resource) {
                model.add(((Resource) s.getObject()), SKOS.BROADER, s.getSubject());
              }
              else {
                log.warn("Found a skos:narrower with Literal value : '" + s.getObject().stringValue() + "'");
              }
            }
    );

    if (!model.filter(mainResource, RDF.TYPE, SKOS.COLLECTION).isEmpty()) {
      log.debug("Main resource is a skos:Collection, adding skos:member to every Concept");
      // if the header object was explicitely typed as skos:Collection, then add skos:members to every included skos:Concept
      model.filter(null, RDF.TYPE, SKOS.CONCEPT).forEach(
              s -> {
                model.add(mainResource, SKOS.MEMBER, ((Resource) s.getSubject()));
              }
      );
    }
    else if (new IsClassTest(model).test(mainResource)) {
      log.debug("Main resource is a rdfs: or owl:Class, adding rdf:type to every Concept");
      // for each resource without an explicit rdf:type, declare it of the type specified in the header
      rowResources.stream().filter(r -> model.filter(r, RDF.TYPE, null).isEmpty()).forEach(r -> {
        model.add(r, RDF.TYPE, mainResource);
      });
    }
    else if (isMainResourceConceptScheme) {
      // no explicit type given in header : we suppose this is a ConceptScheme and apply SKOS post processings

      // add a skos:inScheme to every skos:Concept or skos:Collection or skos:OrderedCollection that was created
      log.debug("Adding skos:inScheme");
      model.filter(null, RDF.TYPE, SKOS.CONCEPT).forEach(
              s -> {
                model.add(((Resource) s.getSubject()), SKOS.IN_SCHEME, mainResource);
              }
      );
      model.filter(null, RDF.TYPE, SKOS.COLLECTION).forEach(
              s -> {
                model.add(((Resource) s.getSubject()), SKOS.IN_SCHEME, mainResource);
              }
      );
      model.filter(null, RDF.TYPE, SKOS.ORDERED_COLLECTION).forEach(
              s -> {
                model.add(((Resource) s.getSubject()), SKOS.IN_SCHEME, mainResource);
              }
      );

      // if at least one skos:Concept was generated,
      // or if no entry was generated at all, declare the URI in B1 as a ConceptScheme
      log.debug("Setting rdf:type skos:ConceptScheme to main resource");
      if (
              !model.filter(null, RDF.TYPE, SKOS.CONCEPT).isEmpty()
              ||
              model.filter(null, RDF.TYPE, null).isEmpty()
      ) {
        model.add(mainResource, RDF.TYPE, SKOS.CONCEPT_SCHEME);
      }

      // add skos:topConceptOf and skos:hasTopConcept for each skos:Concept without broader/narrower
      log.debug("Adding skos:hasTopConcept / skos:topConceptOf");
      model.filter(null, RDF.TYPE, SKOS.CONCEPT).subjects().forEach(
              concept -> {
                if (
                        model.filter(concept, SKOS.BROADER, null).isEmpty()
                        &&
                        model.filter(null, SKOS.NARROWER, concept).isEmpty()
                ) {
                  model.add(mainResource, SKOS.HAS_TOP_CONCEPT, concept);
                  model.add(concept, SKOS.TOP_CONCEPT_OF, mainResource);
                }
              }
      );

      if(runBroaderTransitive) {
        addBroaderTransitive(model);
      }
    }
  }

  private void addBroaderTransitive(Model model) {
    log.info("Running addBroaderTransitive");
    //TODO: following code is reused in multiple places, would be good to Util-ize it
    Repository r = new SailRepository(new MemoryStore());
    r.init();

    try (RepositoryConnection c = r.getConnection()) {
      c.add(model);

      // Load SPARQL query definition
      InputStream src = this.getClass().getResourceAsStream(CALCULATE_BROADER_TRANSITIVE_SPARQL);
      String sparql = IOUtils.toString(src, StandardCharsets.UTF_8);
      Update u = c.prepareUpdate(sparql);
      u.execute();

      // re-export to a new Model
      model.clear();
      c.export(new AbstractRDFHandler() {
        public void handleStatement(Statement st) throws RDFHandlerException {
          model.add(st);
        }
      });

    }
    catch (IOException e) {
      throw Xls2RdfException.rethrow(e);
    }
  }

  private class IsClassTest implements Predicate<Resource> {

    protected Model model;

    public IsClassTest(Model model) {
      this.model = model;
    }

    @Override
    public boolean test(Resource resource) {
      return
              model.contains(resource, RDF.TYPE, OWL.CLASS)
              ||
              model.contains(resource, RDF.TYPE, RDFS.CLASS)
              ;
    }
  }

  private class HasRdfTypeTest implements Predicate<Resource> {

    protected Model model;

    public HasRdfTypeTest(Model model) {
      this.model = model;
    }

    @Override
    public boolean test(Resource resource) {
      return
              model.contains(resource, RDF.TYPE, null)
              ||
              model.contains(resource, RDF.TYPE, null)
              ;
    }
  }

}
