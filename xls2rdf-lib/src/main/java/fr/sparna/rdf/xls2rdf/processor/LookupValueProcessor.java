package fr.sparna.rdf.xls2rdf.processor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import fr.sparna.rdf.xls2rdf.ExcelHelper;
import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.ExcelRefs;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;

import java.util.List;

public class LookupValueProcessor extends BaseRepositoryValueProcessor {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LookupValueProcessor.class.getName());

    private final ValueProcessorFactory valueProcessorFactory;
    private final MappingRule mappingRule;
    private final Sheet sheet;
    private final int lookupColumn;
    private final int uriColumn;
    private final PrefixManager prefixManager;
    private final Xls2RdfMessageListenerIfc messageListener;

    public LookupValueProcessor(ValueProcessorFactory valueProcessorFactory, MappingRule mappingRule, Sheet sheet, int lookupColumn, int uriColumn, PrefixManager prefixManager, Xls2RdfMessageListenerIfc messageListener) {
        this.valueProcessorFactory = valueProcessorFactory;
        this.mappingRule = mappingRule;
        this.sheet = sheet;
        this.lookupColumn = lookupColumn;
        this.uriColumn = uriColumn;
        this.prefixManager = prefixManager;
        this.messageListener = messageListener;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        String lookupValue = value;

        if (lookupValue.equals("")) {
            return null;
        }

        Row foundRow = ExcelHelper.columnLookup(lookupValue, sheet, lookupColumn, true);

        if (foundRow != null) {
            // Create a ResourceOrLiteral processor and process the value
            ResourceOrLiteralValueProcessor g = new ResourceOrLiteralValueProcessor(valueProcessorFactory, mappingRule, prefixManager, messageListener);
            // Initialize it with the current connection and working graph
            g.init(connection, workingGraph);
            return g.processValue(subject, foundRow.getColumnValue(uriColumn), cell);
        } else {
            // throw Exception if a reference was not found
            log.error((cell != null ? cell.getCellExcelReference() : "?") + " Unable to find value '" + lookupValue + "' in column " + ExcelRefs.colIndexToLetters(lookupColumn) + ", while trying to generate property " + mappingRule.getProperty());
            return null;
        }
    }
}
