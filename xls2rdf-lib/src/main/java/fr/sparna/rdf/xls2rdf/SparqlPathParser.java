package fr.sparna.rdf.xls2rdf;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.parser.sparql.ast.ParseException;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlPathParser implements ValueProcessorIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * 
	 */
	protected ColumnHeader header;
	protected PrefixManager prefixManager;
	protected Xls2RdfMessageListenerIfc messageListener;
	protected ValueProcessorIfc delegateProcessor;
	
	public SparqlPathParser(ValueProcessorIfc delegate, ColumnHeader header, PrefixManager prefixManager, Xls2RdfMessageListenerIfc messageListener) {
		super();
		this.delegateProcessor = delegate;
		this.header = header;
		this.prefixManager = prefixManager;
		this.messageListener = messageListener;
	}

	@Override
	public List<Statement> processValue(Model model, Resource subject, String value, Cell cell, String language) {
		if (StringUtils.isBlank(ValueProcessorFactory.normalizeSpace(value))) {
			return null;
		}
		
		log.info("Parsing a SPARQL property path : '"+value+"'");

		try {
			SparqlPropertyPathToShaclPropertyPathConverter converter = new SparqlPropertyPathToShaclPropertyPathConverter(this.prefixManager);
			String shaclPath = converter.convertToShaclPropertyPath(ValueProcessorFactory.normalizeSpace(value));
			return this.delegateProcessor.processValue(model, subject, shaclPath, cell, language);
		} catch (TokenMgrError | ParseException e) {
			// will default to a normal parsing
			this.delegateProcessor.processValue(model, subject, value, cell, language);
			return null;
		}
	};		
	

}