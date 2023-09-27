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
	public OWLClass getOWLClass(String name) {
		System.out.println("getOWLClass : "+name);
		
		if(name.startsWith("xsd")) {
			return null;
		}
		
		if(name.contains(DATATYPE_NAME_MARKER)) {
			return null;
		}
		
		return df.getOWLClass(this.prefixManager.expand(name));
	}

	@Override
	public OWLObjectProperty getOWLObjectProperty(String name) {
		System.out.println("getOWLObjectProperty : "+name);
		
		if(name.startsWith("xsd")) {
			return null;
		}
		
		if(name.contains(DATATYPE_NAME_MARKER)) {
			return null;
		}
		
		return df.getOWLObjectProperty(this.prefixManager.expand(name));
	}

	@Override
	public OWLDataProperty getOWLDataProperty(String name) {
		System.out.println("getOWLDataProperty : "+name);
		return df.getOWLDataProperty(this.prefixManager.expand(name));
	}

	@Override
	public OWLNamedIndividual getOWLIndividual(String name) {
		System.out.println("getOWLIndividual : "+name);
		return df.getOWLNamedIndividual(this.prefixManager.expand(name));
	}

	@Override
	public OWLDatatype getOWLDatatype(String name) {
		System.out.println("getOWLDatatype : "+name);
		return df.getOWLDatatype(this.prefixManager.expand(name));
	}

	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
		System.out.println("getOWLAnnotationProperty : "+name);
		return df.getOWLAnnotationProperty(this.prefixManager.expand(name));
	}

}
