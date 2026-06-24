package fr.sparna.rdf.xls2rdf.postprocess;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.MappingRule;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;

public class AsListPostProcessor implements Xls2RdfPostProcessorIfc {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());

	
	public AsListPostProcessor() {
		super();
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources, Map<String, MappingRule> columnMapping) {
		log.debug("Postprocessing : "+this.getClass().getSimpleName());
		
		for (MappingRule aMappingRule : columnMapping.values()) {
			if(aMappingRule.isAsList()) {
				Model toRemove = new LinkedHashModel();
				Model toAdd = new LinkedHashModel();
				// fetch all subject that have this predicate
				Set<Resource> subjects = model.filter(null, aMappingRule.getProperty(), null).subjects();
				for (Resource aSubject : subjects) {
					// gather all the values
					Set<Value> values = model.filter(aSubject, aMappingRule.getProperty(), null).objects();
					// aggregate in list
					Resource listHead = Values.bnode();
					RDFCollections.asRDF(values,listHead,toAdd);
					// remove all original triples
					toRemove.addAll(model.filter(aSubject, aMappingRule.getProperty(), null));
					// add instead triple to the list
					toAdd.add(aSubject, aMappingRule.getProperty(), listHead);
				}
				// remove everything that needs to be removed
				model.removeAll(toRemove);
				model.addAll(toAdd);
			}
		}	
	}
	
}
