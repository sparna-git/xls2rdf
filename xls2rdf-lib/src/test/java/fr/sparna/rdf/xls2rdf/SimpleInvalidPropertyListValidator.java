package fr.sparna.rdf.xls2rdf;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.rdf4j.model.IRI;

public class SimpleInvalidPropertyListValidator implements Predicate<IRI> {

	protected List<IRI> invalidProperties;
	
	public SimpleInvalidPropertyListValidator(List<IRI> invalidProperties) {
		super();
		this.invalidProperties = invalidProperties;
	}

	@Override
	public boolean test(IRI propertyIRI) {
		return !this.invalidProperties.contains(propertyIRI);
	}

}
