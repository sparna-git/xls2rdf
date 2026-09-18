package fr.sparna.rdf.xls2rdf.processor;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.ValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

/**
 * Applies a ModelPostProcessorIfc on each named graph of a Repository
 */
public class ModelDelegationRepositoryValueProcessor implements RepositoryValueProcessorIfc {

    private ValueProcessorIfc delegate;

    public ModelDelegationRepositoryValueProcessor(ValueProcessorIfc delegate) {
        this.delegate = delegate;
    }

	@Override
	public Pair<List<Statement>, List<Statement>> processValue(Repository repository, Resource workingGraph, Resource subject, String value, Cell cell) {
		// gather the content of the named graph in a model
		try(RepositoryConnection c = repository.getConnection()) {
			Model model = new LinkedHashModel();
			c.getStatements(null, null, null, workingGraph).forEach(s -> model.add(s));
			// call delegate on the model
			Pair<List<Statement>, List<Statement>> result = delegate.processValue(model, subject, value, cell);

			if(result != null) {
				// apply modification to the target graph
				c.remove(result.getRight(), workingGraph);
				c.add(result.getLeft(), workingGraph);
			}

			return result;
		}
	}

    
}
