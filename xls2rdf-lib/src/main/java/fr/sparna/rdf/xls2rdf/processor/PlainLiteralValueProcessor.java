package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.Collections;
import java.util.List;

public class PlainLiteralValueProcessor extends BaseRepositoryValueProcessor {

    private final IRI property;

    public PlainLiteralValueProcessor(IRI property) {
        this.property = property;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        Literal literal = SimpleValueFactory.getInstance().createLiteral(value);
        Statement s = SimpleValueFactory.getInstance().createStatement(subject, property, literal);
        addStatements(Collections.singletonList(s));
        return toPair(Collections.singletonList(s));
    }
}
