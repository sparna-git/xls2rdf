package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitValueProcessor extends BaseRepositoryValueProcessor {

    private final RepositoryValueProcessorIfc delegate;
    private final String separator;

    public SplitValueProcessor(RepositoryValueProcessorIfc delegate, String separator) {
        this.delegate = delegate;
        this.separator = separator;
    }

    @Override
    public void init(RepositoryConnection connection, Resource workingGraph) {
        super.init(connection, workingGraph);
        delegate.init(connection, workingGraph);
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        Pair<List<Statement>, List<Statement>> result = toPair(new ArrayList<>(), new ArrayList<>());
        Arrays.stream(StringUtils.split(value, separator)).forEach(aValue -> {
            Pair<List<Statement>, List<Statement>> statements = delegate.processValue(subject, normalizeSpace(aValue), cell);
            if (statements != null) {
                if (statements.getLeft() != null) result.getLeft().addAll(statements.getLeft());
                if (statements.getRight() != null) result.getRight().addAll(statements.getRight());
            }
        });
        return result;
    }

    private static String normalizeSpace(String s) {
        return s.replaceAll("\\h+", " ").trim();
    }
}
