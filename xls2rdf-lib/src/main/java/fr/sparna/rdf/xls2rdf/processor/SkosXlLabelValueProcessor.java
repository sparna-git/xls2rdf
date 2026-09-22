package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.ArrayList;
import java.util.List;

public class SkosXlLabelValueProcessor extends BaseRepositoryValueProcessor {

    private final IRI xlLabelProperty;
    private final PrefixManager prefixManager;

    public SkosXlLabelValueProcessor(IRI xlLabelProperty, PrefixManager prefixManager) {
        this.xlLabelProperty = xlLabelProperty;
        this.prefixManager = prefixManager;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        String labelUri = prefixManager.isValidURI(value, true);
        IRI labelResource = SimpleValueFactory.getInstance().createIRI(labelUri);
        List<Statement> statements = new ArrayList<>();

        statements.add(SimpleValueFactory.getInstance().createStatement(labelResource, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, org.eclipse.rdf4j.model.vocabulary.SKOSXL.LABEL));
        statements.add(SimpleValueFactory.getInstance().createStatement(subject, xlLabelProperty, labelResource));

        addStatements(statements);
        return toPair(statements);
    }
}
