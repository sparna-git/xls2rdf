package fr.sparna.rdf.xls2rdf.postprocess;

import fr.sparna.rdf.xls2rdf.ColumnHeader;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import java.util.List;

public class RowHeaderLinkPostProcessor implements Xls2RdfPostProcessorIfc {

	protected IRI property;
	protected boolean addOnlyIfNotPresent = true;
	
	public RowHeaderLinkPostProcessor(IRI property, boolean addOnlyIfNotPresent) {
		super();
		this.property = property;
		this.addOnlyIfNotPresent = addOnlyIfNotPresent;
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources, List<ColumnHeader> columnHeaders) {
		if(mainResource != null){
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
	
}
