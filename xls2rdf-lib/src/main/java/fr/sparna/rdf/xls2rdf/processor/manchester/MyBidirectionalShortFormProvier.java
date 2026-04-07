package fr.sparna.rdf.xls2rdf;

import java.util.Collections;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class MyBidirectionalShortFormProvier implements BidirectionalShortFormProvider {

	protected String defaultNamespace;
	protected OWLDataFactory df;
	
	
	public MyBidirectionalShortFormProvier() {
		super();
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		return new SimpleShortFormProvider().getShortForm(entity);
	}

	@Override
	public Stream<OWLEntity> entities(String shortForm) {
		return Collections.singletonList(this.getEntity(shortForm)).stream();
	}

	@Override
	public OWLEntity getEntity(String shortForm) {
		// if short form starts with capital letter
		if(Character.isUpperCase(shortForm.charAt(0))) {
			// then it is a class
			return df.getOWLClass(this.defaultNamespace+shortForm);
		} else {
			// otherwise consider it an ObjectProperty
			return df.getOWLObjectProperty(this.defaultNamespace+shortForm);
		}
	}

	@Override
	public Stream<String> shortForms() {
		return null;
	}

}
