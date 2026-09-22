package fr.sparna.rdf.xls2rdf.processor;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated
 */
@Deprecated
public class ReconcileLocalValueProcessor extends BaseRepositoryValueProcessor {

    private static Logger log = LoggerFactory.getLogger(ReconcileLocalValueProcessor.class.getName());

    private final ValueProcessorFactory valueProcessorFactory;
    private final MappingRule mappingRule;
    private final PrefixManager prefixManager;
    private final IRI reconcileOn;
    private final Repository supportRepository;
    private final Xls2RdfMessageListenerIfc messageListener;

    public ReconcileLocalValueProcessor(ValueProcessorFactory valueProcessorFactory, MappingRule mappingRule, PrefixManager prefixManager, IRI reconcileOn, Repository supportRepository, Xls2RdfMessageListenerIfc messageListener) {
        this.valueProcessorFactory = valueProcessorFactory;
        this.mappingRule = mappingRule;
        this.prefixManager = prefixManager;
        this.reconcileOn = reconcileOn;
        this.supportRepository = supportRepository;
        this.messageListener = messageListener;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        String lookupValue = ValueProcessorFactory.normalizeSpace(value);
        String language = mappingRule.getLanguage().orElse(null);

        if (lookupValue.equals("")) {
            return null;
        }

        try (RepositoryConnection c = supportRepository.getConnection()) {
            // look for every value in any predicate
            List<org.eclipse.rdf4j.model.Statement> statementsWithValue = Iterations.asList(c.getStatements(null, null, SimpleValueFactory.getInstance().createLiteral(lookupValue, language)));

            List<Statement> filteredStatements = new ArrayList<Statement>();
            // filter with the reconcileOn if present
            if (reconcileOn != null) {
                for (Statement s : connection.getStatements(null, null, null, workingGraph).asList()) {
                    filteredStatements.addAll(Iterations.asList(
                            c.getStatements(s.getSubject(), RDF.TYPE, reconcileOn)
                    ));
                    filteredStatements.addAll(Iterations.asList(
                            c.getStatements(s.getSubject(), SKOS.IN_SCHEME, reconcileOn)
                    ));
                }
            } else {
                filteredStatements = statementsWithValue;
            }

            if (filteredStatements.size() == 1) {
                ResourceOrLiteralValueProcessor g = new ResourceOrLiteralValueProcessor(valueProcessorFactory, mappingRule, prefixManager, messageListener);
                g.init(connection, workingGraph);
                return g.processValue(subject, filteredStatements.get(0).getSubject().toString(), cell);
            } else if (filteredStatements.size() > 1) {
                log.error("Found multiple values for '" + lookupValue + "' in type/scheme '" + reconcileOn + "' : " + filteredStatements.stream().map(s -> s.getSubject().toString()).reduce((a, b) -> a + ", " + b).orElse(""));
            } else {
                log.error("Unable to find value '" + lookupValue + "'@" + language + " in a type/scheme '" + reconcileOn + "' in the model");
            }
        }

        return null;
    }
}
