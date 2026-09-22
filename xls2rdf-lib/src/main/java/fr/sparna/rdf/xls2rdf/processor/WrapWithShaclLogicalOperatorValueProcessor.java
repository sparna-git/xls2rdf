package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.RepositoryValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WrapWithShaclLogicalOperatorValueProcessor extends BaseRepositoryValueProcessor {

    private final MappingRule mappingRule;
    private final IRI logicalOperator;
    private final RepositoryValueProcessorIfc delegate;

    public WrapWithShaclLogicalOperatorValueProcessor(MappingRule mappingRule, IRI logicalOperator, RepositoryValueProcessorIfc delegate) {
        this.mappingRule = mappingRule;
        this.logicalOperator = logicalOperator;
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

        Model toRemove = new LinkedHashModel();
        Model toAdd = new LinkedHashModel();

        // get all values
        Set<Value> values = originalStatements.getLeft().stream().map(Statement::getObject).collect(Collectors.toSet());

        // join with the boolean operator only if there is more than 1 value
        if (values.size() > 1) {
            // for each values...
            List<BNode> items = new ArrayList<>();
            for (Value v : values) {
                BNode bnode = Values.bnode();
                items.add(bnode);
                toAdd.add(
                    SimpleValueFactory.getInstance().createStatement(
                        bnode,
                        mappingRule.getProperty(),
                        v
                    )
                );
            }

            // aggregate in list
            Resource listHead = Values.bnode();
            // 3rd parameter is a sink
            RDFCollections.asRDF(items, listHead, toAdd);

            toAdd.add(subject, logicalOperator, listHead);

            // remove all original triples
            toRemove.addAll(originalStatements.getLeft());

            // Apply changes to repository
            removeStatements(toRemove.stream().collect(Collectors.toList()));
            addStatements(toAdd.stream().collect(Collectors.toList()));

            return toPair(toAdd.stream().collect(Collectors.toList()), toRemove.stream().collect(Collectors.toList()));
        } else {
            return originalStatements;
        }
    }
}
