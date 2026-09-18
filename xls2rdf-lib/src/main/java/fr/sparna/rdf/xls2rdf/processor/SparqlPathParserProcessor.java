package fr.sparna.rdf.xls2rdf.processor;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.ValueProcessorIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.parser.sparql.ast.ParseException;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SparqlPathParserProcessor implements ValueProcessorIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * 
	 */
	protected MappingRule mappingRule;
	protected PrefixManager prefixManager;
	protected Xls2RdfMessageListenerIfc messageListener;
	protected ValueProcessorIfc delegateProcessor;
	
	public SparqlPathParserProcessor(ValueProcessorIfc delegate, MappingRule mappingRule, PrefixManager prefixManager, Xls2RdfMessageListenerIfc messageListener) {
		super();
		this.delegateProcessor = delegate;
		this.mappingRule = mappingRule;
		this.prefixManager = prefixManager;
		this.messageListener = messageListener;
	}

	@Override
	public Pair<List<Statement>, List<Statement>> processValue(Model model, Resource subject, String value, Cell cell) {
		if (StringUtils.isBlank(ValueProcessorFactory.normalizeSpace(value))) {
			return null;
		}

		log.debug("Parsing a SPARQL property path : '"+value+"'");

		try {
			SparqlPropertyPathToShaclPropertyPathConverter converter = new SparqlPropertyPathToShaclPropertyPathConverter(this.prefixManager);
			String shaclPath = converter.convertToShaclPropertyPath(ValueProcessorFactory.normalizeSpace(value));
			return this.delegateProcessor.processValue(model, subject, shaclPath, cell);
		} catch (TokenMgrError | ParseException e) {
			// will default to a normal parsing
			return this.delegateProcessor.processValue(model, subject, value, cell);
		}
	}

}
