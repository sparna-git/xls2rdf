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
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.ColumnHeader;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;

public class QBPostProcessor implements Xls2RdfPostProcessorIfc {

	public static class QB {
		public static final String QB = "http://purl.org/linked-data/cube#";
		
		public static final IRI DATASET_CLASS = SimpleValueFactory.getInstance().createIRI(QB+"DataSet");
		
		public static final IRI OBSERVATION = SimpleValueFactory.getInstance().createIRI(QB+"Observation");
		
		public static final IRI COMPONENT_SPECIFICATION = SimpleValueFactory.getInstance().createIRI(QB+"ComponentSpecification");
		
		public static final IRI DATA_STRUCTURE_DEFINITION = SimpleValueFactory.getInstance().createIRI(QB+"DataStructureDefinition");
		
		public static final IRI DATASET_PROPERTY = SimpleValueFactory.getInstance().createIRI(QB+"dataSet");
		
		public static final IRI STRUCTURE = SimpleValueFactory.getInstance().createIRI(QB+"structure");	
		
		public static final IRI DIMENSION = SimpleValueFactory.getInstance().createIRI(QB+"dimension");
		
		public static final IRI MEASURE = SimpleValueFactory.getInstance().createIRI(QB+"measure");
		
		public static final IRI COMPONENT = SimpleValueFactory.getInstance().createIRI(QB+"component");
	}

	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	public QBPostProcessor() {
		super();
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources, List<ColumnHeader> columnHeaders) {
		log.debug("Postprocessing : "+this.getClass().getSimpleName());
		
		// if it is said in the graph that the main resource is a qb:DataSet...
		if(
				model.contains(mainResource, RDF.TYPE, QB.DATASET_CLASS)					
		) {
			log.debug("Detected a sheet with a qb:DataSet in the header");
			
			// if the dataset has a qb:structure
			// then 1/ everything with either a qb:dimension or qb:measure is considered a qb:ComponentSpecification and
			// and  2/ they are linked with the structure
			Set<Value> structures = model.filter(mainResource, QB.STRUCTURE, null).objects();
			
			if(structures.size() > 0) {
				if(structures.size() > 1) {
					log.error("Found multiple structure on Dataset "+mainResource+" : "+structures+", can't attach components");
				} else {
					Value structureValue = structures.iterator().next();
					
					if(structureValue instanceof Resource) {
						Resource structure = (Resource)structureValue;
						
						Stream.concat(
								model.filter(null, QB.DIMENSION, null).subjects().stream(),
								model.filter(null, QB.MEASURE, null).subjects().stream()
						).forEach(c -> {
							model.add(c, RDF.TYPE, QB.COMPONENT_SPECIFICATION);
							model.add(structure, QB.COMPONENT, c);
						});
						
						// also type the structure
						model.add(structure, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION);
					} else {
						log.error("Found structure not a Resource :"+structureValue+", can't attach components");
					}
				}
			}
			
			// then every resource in the sheet without a type will be considered an Observation, linked to this Dataset
			rowResources.stream().filter(r -> model.filter(r, RDF.TYPE, null).isEmpty()).forEach(r -> {
				model.add(r, RDF.TYPE, QB.OBSERVATION);
				model.add(r, QB.DATASET_PROPERTY, mainResource);
			});
			
		}
		
		
	}
	
}
