package fr.sparna.rdf.xls2rdf;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;

public class SimpleInvalidPropertyListValidator implements Xls2RdfPropertyValidatorIfc {

	protected List<IRI> invalidProperties;
	
	public SimpleInvalidPropertyListValidator(List<IRI> invalidProperties) {
		super();
		this.invalidProperties = invalidProperties;
	}

	@Override
	public boolean isValid(IRI propertyIRI) {
		return !this.invalidProperties.contains(propertyIRI);
	}

}
