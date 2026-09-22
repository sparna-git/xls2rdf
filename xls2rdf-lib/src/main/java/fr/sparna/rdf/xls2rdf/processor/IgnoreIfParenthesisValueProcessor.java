package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.List;

public class IgnoreIfParenthesisValueProcessor extends BaseRepositoryValueProcessor {

    private final RepositoryValueProcessorIfc delegate;

    public IgnoreIfParenthesisValueProcessor(RepositoryValueProcessorIfc delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(RepositoryConnection connection, Resource workingGraph) {
        super.init(connection, workingGraph);
        delegate.init(connection, workingGraph);
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        String theValue = normalizeSpace(value);
        if (theValue.startsWith("(") && theValue.endsWith(")")) {
            return null;
        } else {
            return delegate.processValue(subject, theValue, cell);
        }
    }

    private static String normalizeSpace(String s) {
        return s.replaceAll("\\h+", " ").trim();
    }
}
