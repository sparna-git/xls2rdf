package fr.sparna.rdf.xls2rdf;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import ch.qos.logback.core.recovery.ResilientSyslogOutputStream;

public class MyOWLEntityChecker implements OWLEntityChecker {

	private static final String DATATYPE_NAME_MARKER = "literal_";
	protected OWLDataFactory df;
	protected PrefixManager prefixManager;
	
	
	public MyOWLEntityChecker(PrefixManager prefixManager, OWLDataFactory df) {
		super();
		this.prefixManager = prefixManager;
		this.df = df;
	}

	@Override
	public OWLClass getOWLClass(String token) {
		if(!isName(token)) {
			return null;
		}
		
		if(token.startsWith("xsd")) {
			return null;
		}
		
		if(token.contains(DATATYPE_NAME_MARKER)) {
			return null;
		}
		
		return df.getOWLClass(this.prefixManager.expand(token));
	}

	@Override
	public OWLObjectProperty getOWLObjectProperty(String token) {
		if(!isName(token)) {
			return null;
		}
		
		if(token.startsWith("xsd")) {
			return null;
		}
		
		if(token.contains(DATATYPE_NAME_MARKER)) {
			return null;
		}
		
		return df.getOWLObjectProperty(this.prefixManager.expand(token));
	}

	@Override
	public OWLDataProperty getOWLDataProperty(String token) {
		if(!isName(token)) {
			return null;
		}
		
		return df.getOWLDataProperty(this.prefixManager.expand(token));
	}

	@Override
	public OWLNamedIndividual getOWLIndividual(String token) {
		if(!isName(token)) {
			return null;
		}
		
		return df.getOWLNamedIndividual(this.prefixManager.expand(token));
	}

	@Override
	public OWLDatatype getOWLDatatype(String token) {
		if(!isName(token)) {
			return null;
		}
		
		return df.getOWLDatatype(this.prefixManager.expand(token));
	}

	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String token) {
		if(!isName(token)) {
			return null;
		}
		
		return df.getOWLAnnotationProperty(this.prefixManager.expand(token));
	}
	
	private boolean isName(String token) {
		return !(token.equals("(") || token.equals(")"));
	}

}
