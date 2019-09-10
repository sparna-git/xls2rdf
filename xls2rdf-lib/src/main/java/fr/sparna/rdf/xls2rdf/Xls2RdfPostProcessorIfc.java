package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface Xls2RdfPostProcessorIfc {

	/**
	 * Post-processes the model after a row conversion
	 * 
	 * @param model
	 * @param mainResource
	 */
	public void afterRow(Model model, Resource rowResource);

	
	/**
	 * Post-processes the model converted from a Sheet
	 * 
	 * @param model
	 * @param mainResource
	 */
	public void afterSheet(Model model, Resource mainResource);
	
}
