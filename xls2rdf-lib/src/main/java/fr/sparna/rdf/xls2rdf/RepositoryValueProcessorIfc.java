package fr.sparna.rdf.xls2rdf;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.sheet.Cell;

public interface RepositoryValueProcessorIfc {
	
	public void init(RepositoryConnection connection, Resource workingGraph);

	public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell);
	
}