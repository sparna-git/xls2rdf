package fr.sparna.rdf.xls2rdf.postprocess;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import fr.sparna.rdf.xls2rdf.MappingRule;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;

/**
 * Applies a ModelPostProcessorIfc on each named graph of a Repository
 */
public class ModelDelegationPostProcessor implements Xls2RdfPostProcessorIfc {

    private ModelPostProcessorIfc delegate;

    public ModelDelegationPostProcessor(ModelPostProcessorIfc delegate) {
        this.delegate = delegate;
    }

    public void afterSheet(Repository repository, Resource mainResource, List<Resource> rowResources, Map<String, MappingRule> columnMapping) {
		// for each named graphs in the repository...
		try(RepositoryConnection c = repository.getConnection()) {
			for(Resource context : c.getContextIDs()) {
				// gather the content of the named graph in a model
				Model model = new LinkedHashModel();
				c.getStatements(null, null, null, context).forEach(s -> model.add(s));
				// apply post-processing on the model
				delegate.afterSheet(model, mainResource, rowResources, columnMapping);

                // remove all statements from the named graph and re-add the modified statements
                c.clear(context);
                c.add(model, context);
			}
		}
		catch(RepositoryException e) {
			throw new RuntimeException(e);
		}
	}
}
