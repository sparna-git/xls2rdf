package fr.sparna.rdf.xls2rdf;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;

import fr.sparna.rdf.xls2rdf.mapping.MappingRule;

public interface Xls2RdfPostProcessorIfc {
	
	/**
	 * Post-processes the model converted from a Sheet
	 * 
	 * @param repository full repository containing all converted statements from one sheet
	 * @param mainResource resource declared in the sheet header
	 * @param rowResources resources generated from each row (URI in the first column)
	 * @param columnMapping column mapping
	 */
	public void afterSheet(Repository repository, Resource mainResource, List<Resource> rowResources, Map<String, MappingRule> columnMapping);
	
}
