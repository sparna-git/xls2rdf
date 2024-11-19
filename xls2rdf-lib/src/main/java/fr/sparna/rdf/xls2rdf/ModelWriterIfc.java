package fr.sparna.rdf.xls2rdf;

import java.util.Map;

import org.eclipse.rdf4j.model.Model;

public interface ModelWriterIfc {

	public void beginWorkbook();
	
	public void saveGraphModel(String graph, Model model, Map<String, String> prefixes, String baseIri);
	
	public void endWorkbook();	
	

}