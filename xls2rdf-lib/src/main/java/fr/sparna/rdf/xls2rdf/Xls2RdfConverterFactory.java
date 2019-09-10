package fr.sparna.rdf.xls2rdf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.repository.Repository;

public class Xls2RdfConverterFactory {

	private boolean applyPostProcessings = true;
	private boolean generateXl = false;
	private boolean generateXlDefinitions = false;
	
	public Xls2RdfConverterFactory(boolean applyPostProcessings, boolean generateXl, boolean generateXlDefinitions) {
		super();
		this.applyPostProcessings = applyPostProcessings;
		this.generateXl = generateXl;
		this.generateXlDefinitions = generateXlDefinitions;
	}
	
	public Xls2RdfConverter newConverter(Repository outputRepository, String lang) {
		return this.newConverter(new RepositoryModelWriter(outputRepository), lang);
	}
	
	public Xls2RdfConverter newConverter(ModelWriterIfc modelWriter, String lang) {
		Xls2RdfConverter converter = new Xls2RdfConverter(modelWriter, lang);
		if(this.applyPostProcessings) {
			List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
			postProcessors.add(new SkosPostProcessor());
			if(this.generateXl || this.generateXlDefinitions) {
				postProcessors.add(new SkosXlPostProcessor(generateXl, generateXlDefinitions));
			}
			converter.setPostProcessors(postProcessors);
		}
		
		return converter;
	}
	
}
