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
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
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
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

public class ManchesterClassExpressionParser implements ValueProcessorIfc {

	/**
	 * 
	 */
	protected ColumnHeader header;
	protected PrefixManager prefixManager;
	protected Xls2RdfMessageListenerIfc messageListener;
	
	public ManchesterClassExpressionParser(ColumnHeader header, PrefixManager prefixManager, Xls2RdfMessageListenerIfc messageListener) {
		super();
		this.header = header;
		this.prefixManager = prefixManager;
		this.messageListener = messageListener;
	}

	@Override
	public Value processValue(Model model, Resource subject, String value, Cell cell, String language) {
		if (StringUtils.isBlank(ValueProcessorFactory.normalizeSpace(value))) {
			return null;
		}
		
		
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory df = manager.getOWLDataFactory();
			ValueFactory vf = SimpleValueFactory.getInstance();
			
			// create dummy ontology
			OWLOntology ontology = manager.createOntology();
			ontology.add(df.getOWLEquivalentClassesAxiom(df.getOWLClass("http://example.com/Pizza"), df.getOWLClass("http://example.com/Dummy")));
			
			// init manchester parser
			ManchesterOWLSyntaxParser manchesterParser = OWLManager.createManchesterParser();
			// ShortFormEntityChecker checker = new ShortFormEntityChecker(getShortFormProvider(ontology, this.prefixManager.getOutputPrefixes()));			
			OWLEntityChecker checker = new MyOWLEntityChecker(this.prefixManager, df);
			manchesterParser.setOWLEntityChecker(checker);
			
			// parse the class expression
			OWLClassExpression expr = manchesterParser.parseClassExpression(value);
			
			// stores the class expression in our ontology, in a dummy axiom
			ontology.add(df.getOWLEquivalentClassesAxiom(df.getOWLClass("http://x"), expr));
			

			
			/*
			RioStorer storer = new RioStorer(new TurtleDocumentFormatFactory(), collector);
			storer.setRioHandler(collector);
			
			
			TurtleDocumentFormat turtle = new TurtleDocumentFormat();
			storer.storeOntology(ontology, vf.createIRI("http://dummy.com"), turtle);
			*/
			
			// save ontology to turtle
			TurtleDocumentFormat turtle = new TurtleDocumentFormat();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ontology.saveOntology(turtle, baos);
	        
			// init the collector that will receive our triples
			StatementCollector collector = new StatementCollector();
			
	        // parse it back to a Model
	        RDFParser parser = RDFParserRegistry.getInstance().get(RDFFormat.TURTLE).get().getParser();
	        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	        parser.setRDFHandler(collector);
	        parser.parse(bais, "UTF-8");
	        
	        // populate a Model with triples
	        Model ontologyModel = new LinkedHashModel();
	        ontologyModel.addAll(collector.getStatements());
	        
	        // find our class expression on the dummy class
	        ValueFactory factory = SimpleValueFactory.getInstance();
	        Value equivalentClassEntity = ontologyModel.filter(
	        		factory.createIRI("http://x"),
	        		factory.createIRI("http://www.w3.org/2002/07/owl#equivalentClass"),
	        		null
	        )
	        .objects().iterator().next();
	        
	        // read triple from the class expression
	        Model theInterestingTriples = retrieveStatementsTreeRec(ontologyModel, (Resource)equivalentClassEntity);
	        model.addAll(theInterestingTriples);
	        
	        // add link from subject to class expression
	        model.add(subject, header.getProperty(), (Resource)equivalentClassEntity);
		
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			throw new Xls2RdfException("Cannot create new ontology to init manchester parsing",e);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			throw new Xls2RdfException("Exception while serializing class expression to turtle syntax",e);
		} catch (RDFParseException e) {
			e.printStackTrace();
			throw new Xls2RdfException("Exception while re-parsing class expression from Turtle",e);
		} catch (RDFHandlerException e) {
			e.printStackTrace();
			throw new Xls2RdfException("Exception while re-parsing class expression from Turtle",e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Xls2RdfException("Exception while re-parsing class expression from Turtle",e);
		}
		
		
		return null;
	};		
	
	private BidirectionalShortFormProvider getShortFormProvider(OWLOntology ont, Map<String, String> prefixes) {
        Set<OWLOntology> ontologies = ont.getOWLOntologyManager().getOntologies(); // my OWLOntologyManager        
        
        // ShortFormProvider sfp = new SimpleShortFormProvider(); 
        System.out.println(prefixes);
        QNameShortFormProvider sfp = new QNameShortFormProvider(prefixes);
        
        BidirectionalShortFormProvider shortFormProvider = new BidirectionalShortFormProviderAdapter(
                ontologies,
                sfp
        );
        return shortFormProvider;
    }
	
    private Model retrieveStatementsTreeRec(final Model m, Resource r) {
    	final Model output = new LinkedHashModel();
    	Model statementsWhereRIsSubject = m.filter(r, null, null);
    	output.addAll(statementsWhereRIsSubject);
    	statementsWhereRIsSubject.forEach(s -> {
    		if(s.getObject() instanceof BNode) {
    			output.addAll(retrieveStatementsTreeRec(m,(Resource)s.getObject()));
    		}	
    	});
    	return output;
    }
}