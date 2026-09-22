package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation for RepositoryValueProcessorIfc that provides common methods
 * for adding and removing statements from a RepositoryConnection in a working graph.
 */
public abstract class BaseRepositoryValueProcessor implements RepositoryValueProcessorIfc {

    protected RepositoryConnection connection;
    protected Resource workingGraph;

    @Override
    public void init(RepositoryConnection connection, Resource workingGraph) {
        this.connection = connection;
        this.workingGraph = workingGraph;
    }

    /**
     * Adds statements to the working graph
     * @param statements the statements to add
     */
    protected void addStatements(List<Statement> statements) {
        if (statements != null && !statements.isEmpty()) {
            connection.add(statements, workingGraph);
        }
    }

    /**
     * Removes statements from the working graph
     * @param statements the statements to remove
     */
    protected void removeStatements(List<Statement> statements) {
        if (statements != null && !statements.isEmpty()) {
            connection.remove(statements, workingGraph);
        }
    }

    /**
     * Creates a pair from a list of statements (left) and an empty list (right)
     */
    protected Pair<List<Statement>, List<Statement>> toPair(List<Statement> left) {
        return toPair(left, new ArrayList<>());
    }

    /**
     * Creates a pair from two lists of statements
     */
    protected Pair<List<Statement>, List<Statement>> toPair(List<Statement> left, List<Statement> right) {
        return new ImmutablePair<>(left, right);
    }
}
