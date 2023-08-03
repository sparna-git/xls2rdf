package fr.sparna.rdf.xls2rdf.postprocess;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;

public class RowHeaderLinkPostProcessor implements Xls2RdfPostProcessorIfc {

	protected IRI property;
	protected boolean addOnlyIfNotPresent = true;
	
	public RowHeaderLinkPostProcessor(IRI property, boolean addOnlyIfNotPresent) {
		super();
		this.property = property;
		this.addOnlyIfNotPresent = addOnlyIfNotPresent;
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources) {
		rowResources.stream().forEach(rowResource -> {
			if(
					!this.addOnlyIfNotPresent
					||
					!model.contains(rowResource, this.property, null)
			) {
				model.add(rowResource, this.property, mainResource);
			}
		});
	}
	
}
