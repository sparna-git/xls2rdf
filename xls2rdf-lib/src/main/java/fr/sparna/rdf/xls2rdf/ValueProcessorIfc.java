package fr.sparna.rdf.xls2rdf;

import org.apache.poi.ss.usermodel.Cell;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

public interface ValueProcessorIfc {
	
	/**
	 * Generates one (or more) properties on the given subject from the given value, and insert them in the input model.
	 * The language is passed as a parameter to be able to overwrite a global language parameter with column-specific language declaration.
	 * 
	 * @param model
	 * @param subject
	 * @param value
	 * @param language
	 * @return
	 */
	public Value processValue(Model model, Resource subject, String value, Cell cell, String language);
	
}