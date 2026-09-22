package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.ArrayList;
import java.util.List;

public class CopyToValueProcessor extends BaseRepositoryValueProcessor {

    private final IRI copyTo;
    private final RepositoryValueProcessorIfc delegate;

    public CopyToValueProcessor(IRI copyTo, RepositoryValueProcessorIfc delegate) {
        this.copyTo = copyTo;
        this.delegate = delegate;
    }

    @Override
    public void init(RepositoryConnection connection, Resource workingGraph) {
        super.init(connection, workingGraph);
        delegate.init(connection, workingGraph);
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        Pair<List<Statement>, List<Statement>> statements = delegate.processValue(subject, value, cell);
        List<Statement> newStatements = new ArrayList<>();
        if (statements != null && statements.getLeft() != null) {
            statements.getLeft().forEach(v -> {
                newStatements.add(SimpleValueFactory.getInstance().createStatement(subject, copyTo, v.getObject()));
            });
            addStatements(newStatements);
        }
        newStatements.addAll(statements.getLeft());
        return toPair(newStatements);
    }
}
