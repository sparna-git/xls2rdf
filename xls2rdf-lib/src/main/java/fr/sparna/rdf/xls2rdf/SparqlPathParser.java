package fr.sparna.rdf.xls2rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.parser.sparql.ast.ParseException;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.QNameShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc.MessageCode;

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
	public Value processValue(Model model, Resource subject, String value, Cell cell, String language) {
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