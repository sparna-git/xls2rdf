package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.Collections;
import java.util.List;

public class ResourceValueProcessor extends BaseRepositoryValueProcessor {

    private final IRI property;
    private final PrefixManager prefixManager;

    public ResourceValueProcessor(IRI property, PrefixManager prefixManager) {
        this.property = property;
        this.prefixManager = prefixManager;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        IRI iri = SimpleValueFactory.getInstance().createIRI(prefixManager.isValidURI(normalizeSpace(value), true));

        // can be null if we expected an IRI but we had a literal
        if (iri == null) {
            throw new Xls2RdfException("Expected a URI but got '" + normalizeSpace(value) + "'");
        }

        Statement s = SimpleValueFactory.getInstance().createStatement(subject, property, iri);
        addStatements(Collections.singletonList(s));
        return toPair(Collections.singletonList(s));
    }

    private static String normalizeSpace(String s) {
        return s.replaceAll("\\h+", " ").trim();
    }
}
