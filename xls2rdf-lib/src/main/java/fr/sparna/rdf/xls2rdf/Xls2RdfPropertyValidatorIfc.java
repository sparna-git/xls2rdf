package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.model.IRI;

public interface Xls2RdfPropertyValidatorIfc {

	public boolean isValid(IRI propertyIRI);
	
}
