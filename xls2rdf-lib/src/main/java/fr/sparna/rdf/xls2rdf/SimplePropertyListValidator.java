package fr.sparna.rdf.xls2rdf;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;

public class SimplePropertyListValidator implements Xls2RdfPropertyValidatorIfc {

	protected List<IRI> allowedProperties;
	
	public SimplePropertyListValidator(List<IRI> allowedProperties) {
		super();
		this.allowedProperties = allowedProperties;
	}

	@Override
	public boolean isValid(IRI propertyIRI) {
		return this.allowedProperties.contains(propertyIRI);
	}

}
