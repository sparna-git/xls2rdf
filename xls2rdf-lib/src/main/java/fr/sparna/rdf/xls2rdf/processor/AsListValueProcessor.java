package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AsListValueProcessor extends BaseRepositoryValueProcessor {

    private final MappingRule mappingRule;
    private final RepositoryValueProcessorIfc delegate;

    public AsListValueProcessor(MappingRule mappingRule, RepositoryValueProcessorIfc delegate) {
        this.mappingRule = mappingRule;
        this.delegate = delegate;
    }

    @Override
    public void init(RepositoryConnection connection, Resource workingGraph) {
        super.init(connection, workingGraph);
        delegate.init(connection, workingGraph);
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        // Get current state from repository
        Model model = new LinkedHashModel();
        connection.getStatements(null, null, null, workingGraph).forEach(s -> model.add(s));
        
        Pair<List<Statement>, List<Statement>> originalStatements = delegate.processValue(subject, value, cell);

        Model toAdd = new LinkedHashModel();

        // get all values
        Set<Value> values = originalStatements.getLeft().stream().map(Statement::getObject).collect(Collectors.toSet());

        // aggregate in list
        Resource listHead = Values.bnode();
        RDFCollections.asRDF(values, listHead, toAdd);
        // add instead triple to the list
        toAdd.add(subject, mappingRule.getProperty(), listHead);

        // Apply changes to repository
        addStatements(toAdd.stream().collect(Collectors.toList()));
        removeStatements(originalStatements.getLeft());

        return toPair(toAdd.stream().collect(Collectors.toList()), originalStatements.getLeft());
    }
}
