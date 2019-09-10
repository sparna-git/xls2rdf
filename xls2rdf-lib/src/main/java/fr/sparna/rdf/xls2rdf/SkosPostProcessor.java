package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

public class SkosPostProcessor implements Xls2RdfPostProcessorIfc {

	
	
	@Override
	public void afterRow(Model model, Resource rowResource) {
		// if, after row processing, no rdf:type was generated, then we consider the row to be a skos:Concept
		// this allows to generate something else that skos:Concept
		if(!model.contains(rowResource, RDF.TYPE, null)) {
			model.add(rowResource, RDF.TYPE, SKOS.CONCEPT);
		}
	}

	@Override
	public void afterSheet(Model model, Resource mainResource) {
		// add the inverse broaders and narrowers
		model.filter(null, SKOS.BROADER, null).forEach(
				s -> {
					model.add(((Resource)s.getObject()), SKOS.NARROWER, s.getSubject());
				}
		);
		model.filter(null, SKOS.NARROWER, null).forEach(
				s -> {
					model.add(((Resource)s.getObject()), SKOS.BROADER, s.getSubject());
				}
		);
		
		if(!model.filter(mainResource, RDF.TYPE, SKOS.COLLECTION).isEmpty()) {
			// if the header object was explicitely typed as skos:Collection, then add skos:members to every included skos:Concept
			model.filter(null, RDF.TYPE, SKOS.CONCEPT).forEach(
					s -> { model.add(mainResource, SKOS.MEMBER, ((Resource)s.getSubject())); }
			);
		} else if(
				!model.filter(mainResource, RDF.TYPE, OWL.CLASS).isEmpty()
				||
				!model.filter(mainResource, RDF.TYPE, RDFS.CLASS).isEmpty()
		) {
			// for each resource without an explicit rdf:type, declare it of the type specified in the header
			model.subjects().stream().filter(s -> model.filter(s, RDF.TYPE, null).isEmpty()).forEach(s -> {
				model.add(s, RDF.TYPE, mainResource);
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
	
}
