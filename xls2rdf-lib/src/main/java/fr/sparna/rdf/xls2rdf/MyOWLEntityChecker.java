package fr.sparna.rdf.xls2rdf;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class MyOWLEntityChecker implements OWLEntityChecker {

	protected OWLDataFactory df;
	protected PrefixManager prefixManager;
	
	
	public MyOWLEntityChecker(PrefixManager prefixManager, OWLDataFactory df) {
		super();
		this.prefixManager = prefixManager;
		this.df = df;
	}

	@Override
	public OWLClass getOWLClass(String name) {
		return df.getOWLClass(this.prefixManager.expand(name));
	}

	@Override
	public OWLObjectProperty getOWLObjectProperty(String name) {
		return df.getOWLObjectProperty(this.prefixManager.expand(name));
	}

	@Override
	public OWLDataProperty getOWLDataProperty(String name) {
		return df.getOWLDataProperty(this.prefixManager.expand(name));
	}

	@Override
	public OWLNamedIndividual getOWLIndividual(String name) {
		return df.getOWLNamedIndividual(this.prefixManager.expand(name));
	}

	@Override
	public OWLDatatype getOWLDatatype(String name) {
		return df.getOWLDatatype(this.prefixManager.expand(name));
	}

	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
		return df.getOWLAnnotationProperty(this.prefixManager.expand(name));
	}

}
