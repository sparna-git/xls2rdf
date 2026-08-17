package fr.sparna.rdf.xls2rdf.postprocess;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import fr.sparna.rdf.xls2rdf.MappingRule;

public interface ModelPostProcessorIfc {
	
	/**
	 * Post-processes the model converted from a Sheet
	 * 
	 * @param model full model containing all converted statements
	 * @param mainResource resource declared in the sheet header
	 * @param rowResources resources generated from each row (URI in the first column)
	 * @param columnMapping column mapping
	 */
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources, Map<String, MappingRule> columnMapping);
	
}
