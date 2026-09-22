package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.Collections;
import java.util.List;

public class LangOrPlainLiteralValueProcessor extends BaseRepositoryValueProcessor {

    private final IRI property;
    private final String language;

    public LangOrPlainLiteralValueProcessor(IRI property, String language) {
        this.property = property;
        this.language = language;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        Literal literal;
        if (language != null) {
            literal = SimpleValueFactory.getInstance().createLiteral(value, language);
        } else {
            literal = SimpleValueFactory.getInstance().createLiteral(value);
        }
        Statement s = SimpleValueFactory.getInstance().createStatement(subject, property, literal);
        addStatements(Collections.singletonList(s));
        return toPair(Collections.singletonList(s));
    }
}
