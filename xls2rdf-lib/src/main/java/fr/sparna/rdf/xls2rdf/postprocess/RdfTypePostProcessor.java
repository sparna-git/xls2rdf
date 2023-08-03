package fr.sparna.rdf.xls2rdf.postprocess;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class RdfTypePostProcessor extends RowHeaderLinkPostProcessor {

	public RdfTypePostProcessor(IRI property, boolean addOnlyIfNotPresent) {
		super(RDF.TYPE, true);
	}
	
}
