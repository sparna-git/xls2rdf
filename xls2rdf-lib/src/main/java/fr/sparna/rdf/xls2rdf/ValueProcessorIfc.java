package fr.sparna.rdf.xls2rdf;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import fr.sparna.rdf.xls2rdf.sheet.Cell;

public interface ValueProcessorIfc {
	
	/**
	 * Generates one (or more) properties on the given subject from the given value, and insert them in the input model.
	 * 
	 * @param model
	 * @param subject
	 * @param value
	 * @param language
	 * @return
	 */
	public Pair<List<Statement>, List<Statement>> processValue(Model model, Resource subject, String value, Cell cell);
	
}