package fr.sparna.rdf.xls2rdf;

import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface Xls2RdfPostProcessorIfc {
	
	/**
	 * Post-processes the model converted from a Sheet
	 * 
	 * @param model full model containing all converted statements
	 * @param mainResource resource declared in the sheet header
	 * @param rowResources resources generated from each row (URI in the first column)
	 */
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources);
	
}
