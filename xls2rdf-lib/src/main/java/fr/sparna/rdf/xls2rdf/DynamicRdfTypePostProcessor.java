package fr.sparna.rdf.xls2rdf;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicRdfTypePostProcessor implements Xls2RdfPostProcessorIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	protected Predicate<Resource> classTest;
	
	/**
	 * @param classTest returns true if the provided Resource is a class (e.g. instance of owl:Class or rdfs:Class)
	 */
	public DynamicRdfTypePostProcessor(Predicate<Resource> classTest) {
		super();
		this.classTest = classTest;
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources) {
		log.debug("Postprocessing : "+this.getClass().getSimpleName());
		if(this.classTest.test(mainResource)) {
			rowResources.stream().forEach(rowResource -> {
				if(
						model.filter(rowResource, RDF.TYPE, null).isEmpty()					
				) {
					model.add(rowResource, RDF.TYPE, mainResource);
				}
			});			
		}
	}
	
}
