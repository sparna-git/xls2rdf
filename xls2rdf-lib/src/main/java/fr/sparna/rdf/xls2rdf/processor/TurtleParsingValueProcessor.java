package fr.sparna.rdf.xls2rdf.processor;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.sheet.Cell;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TurtleParsingValueProcessor extends BaseRepositoryValueProcessor {

    private static Logger log = LoggerFactory.getLogger(TurtleParsingValueProcessor.class.getName());

    private final ValueProcessorFactory valueProcessorFactory;
    private final MappingRule mappingRule;
    private final IRI property;
    private final PrefixManager prefixManager;
    private final Xls2RdfMessageListenerIfc messageListener;
    private final String BLANK_NODE_TEMP_IRI = "http://blanknode.com";

    public TurtleParsingValueProcessor(ValueProcessorFactory valueProcessorFactory, MappingRule mappingRule, IRI property, PrefixManager prefixManager, Xls2RdfMessageListenerIfc messageListener) {
        this.valueProcessorFactory = valueProcessorFactory;
        this.mappingRule = mappingRule;
        this.property = property;
        this.prefixManager = prefixManager;
        this.messageListener = messageListener;
    }

    @Override
    public Pair<List<Statement>, List<Statement>> processValue(Resource subject, String value, Cell cell) {
        String language = mappingRule.getLanguage().orElse(null);
        // create a small piece of Turtle by concatenating...
        StringBuffer turtle = new StringBuffer();
        // ... the prefixes
        turtle.append(prefixManager.getPrefixesTurtleHeader());
        // ... the subject and the predicate
        if (subject.isBNode()) {
            // BNode
            turtle.append("<" + BLANK_NODE_TEMP_IRI + ">" + " " + "<" + property.stringValue() + "> ");
        } else {
            // normal IRI
            turtle.append("<" + subject.stringValue() + ">" + " " + "<" + property.stringValue() + "> ");
        }
        // ... the value (blank node or list or value with datatype or language)
        turtle.append(value);
        // ... and a final dot if there is not one already at the end
        if (!ValueProcessorFactory.normalizeSpace(value).endsWith(".")) {
            turtle.append(".");
        }

        // now parse the Turtle String and collect the statements in a StatementCollector
        StatementCollector collector = new StatementCollector();
        RDFParser parser = RDFParserRegistry.getInstance().get(RDFFormat.TURTLE).get().getParser();
        parser.setRDFHandler(collector);
        try {
            parser.parse(new StringReader(turtle.toString()), RDF.NS.toString());

            if (subject.isBNode()) {
                // process the content of the collector to replace the BLANK_NODE_TEMP_IRI with the
                // actual blank node resource
                BNode actualBlankNode = (BNode) subject;
                IRI tempIri = SimpleValueFactory.getInstance().createIRI(BLANK_NODE_TEMP_IRI);
                List<Statement> processedStatements = new ArrayList<>();

                for (Statement stmt : collector.getStatements()) {
                    if (stmt.getSubject().equals(tempIri)) {
                        // Replace the temp IRI subject with the actual blank node
                        Statement newStmt = SimpleValueFactory.getInstance().createStatement(
                            actualBlankNode,
                            stmt.getPredicate(),
                            stmt.getObject(),
                            stmt.getContext()
                        );
                        processedStatements.add(newStmt);
                    } else {
                        processedStatements.add(stmt);
                    }
                }

                // then add all the resulting statements to the repository
                addStatements(processedStatements);
                return toPair(processedStatements);
            } else {
                List<Statement> statements = new ArrayList<>(collector.getStatements());
                addStatements(statements);
                return toPair(statements);
            }

        } catch (Exception e) {
            // if anything goes wrong, default to creating a literal
            log.error("Error in parsing Turtle :\n" + turtle);
            e.printStackTrace();
            // Create a LangOrPlainLiteral processor and process the value
            LangOrPlainLiteralValueProcessor fallbackProcessor = new LangOrPlainLiteralValueProcessor(property, language);
            fallbackProcessor.init(connection, workingGraph);
            return fallbackProcessor.processValue(subject, value, cell);
        }
    }
}
