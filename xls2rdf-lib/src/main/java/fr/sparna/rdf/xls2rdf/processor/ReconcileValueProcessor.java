package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.reconcile.ReconciliableValueSetIfc;
import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.util.List;

public class ReconcileValueProcessor extends BaseRepositoryValueProcessor {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReconcileValueProcessor.class.getName());

    private final ValueProcessorFactory valueProcessorFactory;
    private final MappingRule mappingRule;
    private final PrefixManager prefixManager;
    private final ReconciliableValueSetIfc reconciledValues;
    private final Xls2RdfMessageListenerIfc messageListener;

    public ReconcileValueProcessor(ValueProcessorFactory valueProcessorFactory, MappingRule mappingRule, PrefixManager prefixManager, ReconciliableValueSetIfc reconciledValues, Xls2RdfMessageListenerIfc messageListener) {
        this.valueProcessorFactory = valueProcessorFactory;
        this.mappingRule = mappingRule;
        this.prefixManager = prefixManager;
        this.reconciledValues = reconciledValues;
        this.messageListener = messageListener;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        String lookupValue = ValueProcessorFactory.normalizeSpace(value);

        if (lookupValue.equals("")) {
            return null;
        }

        IRI result = reconciledValues.getReconciledValue(value);
        if (result != null) {
            // Create a ResourceOrLiteral processor and process the value
            ResourceOrLiteralValueProcessor g = new ResourceOrLiteralValueProcessor(valueProcessorFactory, mappingRule, prefixManager, messageListener);
            g.init(connection, workingGraph);
            return g.processValue(subject, result.toString(), cell);
        } else {
            log.error("Unable to find value '" + lookupValue + " in reconciled values");
        }

        return null;
    }
}
