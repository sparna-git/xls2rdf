package fr.sparna.rdf.xls2rdf;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkosPostProcessor implements Xls2RdfPostProcessorIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources) {
		log.debug("Postprocessing : "+this.getClass().getSimpleName());
		
		if(!new ClassTest(model).test(mainResource)) {
			rowResources.stream().forEach(rowResource -> {
				// if, after row processing, no rdf:type was generated, then we consider the row to be a skos:Concept
				// this allows to generate something else that skos:Concept
				if(model.filter(rowResource, RDF.TYPE, null).isEmpty()) {
					model.add(rowResource, RDF.TYPE, SKOS.CONCEPT);
				}
			});
		}
		
		// add the inverse broaders and narrowers
		model.filter(null, SKOS.BROADER, null).forEach(
				s -> {
					if(s.getObject() instanceof Resource) {
						model.add(((Resource)s.getObject()), SKOS.NARROWER, s.getSubject());
					} else {
						log.warn("Found a skos:broadeer with Literal value : "+s.getObject().stringValue());
					}
				}
		);
		model.filter(null, SKOS.NARROWER, null).forEach(
				s -> {
					if(s.getObject() instanceof Resource) {
						model.add(((Resource)s.getObject()), SKOS.BROADER, s.getSubject());
					} else {
						log.warn("Found a skos:narrower with Literal value : "+s.getObject().stringValue());
					}
				}
		);
		
		if(!model.filter(mainResource, RDF.TYPE, SKOS.COLLECTION).isEmpty()) {
			// if the header object was explicitely typed as skos:Collection, then add skos:members to every included skos:Concept
			model.filter(null, RDF.TYPE, SKOS.CONCEPT).forEach(
					s -> { model.add(mainResource, SKOS.MEMBER, ((Resource)s.getSubject())); }
			);
		} else if(new ClassTest(model).test(mainResource)) {
			// for each resource without an explicit rdf:type, declare it of the type specified in the header
			rowResources.stream().filter(r -> model.filter(r, RDF.TYPE, null).isEmpty()).forEach(r -> {
				model.add(r, RDF.TYPE, mainResource);
			});
		}
		else {
			// no explicit type given in header : we suppose this is a ConceptScheme and apply SKOS post processings
			
			// add a skos:inScheme to every skos:Concept or skos:Collection or skos:OrderedCollection that was created
			model.filter(null, RDF.TYPE, SKOS.CONCEPT).forEach(
					s -> { model.add(((Resource)s.getSubject()), SKOS.IN_SCHEME, mainResource); }
			);
			model.filter(null, RDF.TYPE, SKOS.COLLECTION).forEach(
					s -> { model.add(((Resource)s.getSubject()), SKOS.IN_SCHEME, mainResource); }
			);
			model.filter(null, RDF.TYPE, SKOS.ORDERED_COLLECTION).forEach(
					s -> { model.add(((Resource)s.getSubject()), SKOS.IN_SCHEME, mainResource); }
			);	
			
			// if at least one skos:Concept was generated, 
			// or if no entry was generated at all, declare the URI in B1 as a ConceptScheme
			if(
					!model.filter(null, RDF.TYPE, SKOS.CONCEPT).isEmpty()
					||
					model.filter(null, RDF.TYPE, null).isEmpty()
			) {
				model.add(mainResource, RDF.TYPE, SKOS.CONCEPT_SCHEME);
			}
			
			// add skos:topConceptOf and skos:hasTopConcept for each skos:Concept without broader/narrower
			model.filter(null, RDF.TYPE, SKOS.CONCEPT).subjects().forEach(
					concept -> {
						if(
								model.filter(concept, SKOS.BROADER, null).isEmpty()
								&&
								model.filter(null, SKOS.NARROWER, concept).isEmpty()
						) {
							model.add(mainResource, SKOS.HAS_TOP_CONCEPT, concept);
							model.add(concept, SKOS.TOP_CONCEPT_OF, mainResource);
						}
					}
			);
		}
	}
	
	private class ClassTest implements Predicate<Resource> {
		
		protected Model model;

		public ClassTest(Model model) {
			super();
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
	
}
