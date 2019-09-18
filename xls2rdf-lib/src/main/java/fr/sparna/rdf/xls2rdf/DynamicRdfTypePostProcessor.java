package fr.sparna.rdf.xls2rdf;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class DynamicRdfTypePostProcessor implements Xls2RdfPostProcessorIfc {

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
		if(this.classTest.test(mainResource)) {
			rowResources.stream().forEach(rowResource -> {
				if(
						!model.contains(rowResource, RDF.TYPE, null)					
				) {
					model.add(rowResource, RDF.TYPE, mainResource);
				}
			});			
		}
	}
	
}
