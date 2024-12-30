package fr.sparna.rdf.xls2rdf.postprocess;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.ColumnHeader;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;

public class OWLPostProcessor implements Xls2RdfPostProcessorIfc {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	public static class XLS2RDF {
		public static final String NAMESPACE = "https://xls2rdf.sparna.fr/vocabulary#";
		
		public static final IRI IS_COVERED_BY = SimpleValueFactory.getInstance().createIRI(NAMESPACE+"isCoveredBy");

	}
	
	public OWLPostProcessor() {
		super();
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources, List<ColumnHeader> columnHeaders) {
		log.debug("Postprocessing : "+this.getClass().getSimpleName());
		
		// if it is said in the graph that the main resource is an owl:Ontology...
		if(
				model.contains(mainResource, RDF.TYPE, OWL.ONTOLOGY)					
		) {
			log.debug("Detected a sheet with an owl:Ontology in the header");
			
			
			rowResources.stream().filter(r -> !model.filter(r, XLS2RDF.IS_COVERED_BY, null).isEmpty()).forEach(r -> {
				// gather all the is_covered_by values
				List<IRI> isCoveredByValues = model.filter(r, XLS2RDF.IS_COVERED_BY, null).objects().stream()
				.filter(v -> (v instanceof IRI))
				.map(v -> (IRI)v).collect(Collectors.toList());
				
				// make an RDF list of this
				Resource listHead = Values.bnode();
				RDFCollections.asRDF(isCoveredByValues,listHead,model);
				
				// create union class and add covering axiom
				Resource unionClass = Values.bnode();
				model.add(unionClass, RDF.TYPE, OWL.CLASS);
				model.add(unionClass, OWL.UNIONOF, listHead);
				model.add(r, RDFS.SUBCLASSOF, unionClass);
				
			});
			
		}		
	}
	
}
