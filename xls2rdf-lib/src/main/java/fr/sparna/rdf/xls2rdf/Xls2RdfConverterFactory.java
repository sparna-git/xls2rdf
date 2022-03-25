package fr.sparna.rdf.xls2rdf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.reconcile.SparqlReconcileService;

public class Xls2RdfConverterFactory {
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());

	private boolean applyPostProcessings = true;
	private boolean generateXl = false;
	private boolean generateXlDefinitions = false;
	private boolean failIfNoReconcile = true;
	private final boolean generateBroaderTransitive;
	private Repository supportRepository = null;
	
	public Xls2RdfConverterFactory(
			boolean applyPostProcessings,
			boolean generateXl,
			boolean generateXlDefinitions,
			boolean generateBroaderTransitive,
			boolean failIfNoReconcile
	) {
		super();
		this.applyPostProcessings = applyPostProcessings;
		this.generateXl = generateXl;
		this.generateXlDefinitions = generateXlDefinitions;
		this.generateBroaderTransitive = generateBroaderTransitive;
		this.failIfNoReconcile = failIfNoReconcile;
	}
	
	public Xls2RdfConverter newConverter(Repository outputRepository, String lang) {
		return this.newConverter(new RepositoryModelWriter(outputRepository), lang);
	}
	
	/**
	 * @param modelWriter
	 * @param lang can be null
	 * @return
	 */
	public Xls2RdfConverter newConverter(ModelWriterIfc modelWriter, String lang) {
		Xls2RdfConverter converter = new Xls2RdfConverter(modelWriter, lang);
		if(this.applyPostProcessings) {
			List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
			// add QB post processor
			postProcessors.add(new QBPostProcessor());
			// add SKOS post processor
			postProcessors.add(new SkosPostProcessor(this.generateBroaderTransitive));
			// if needed, add SKOS-XL post-processor
			if(this.generateXl || this.generateXlDefinitions) {
				postProcessors.add(new SkosXlPostProcessor(generateXl, generateXlDefinitions));
			}
			
			converter.setPostProcessors(postProcessors);
		}
		
		if(this.supportRepository != null) {
			log.info("Setting a support repository that contains "+this.supportRepository.getConnection().size()+" triples");
			converter.setReconcileService(new SparqlReconcileService(this.supportRepository));
		}
		
		converter.setFailIfNoReconcile(this.failIfNoReconcile);
		
		return converter;
	}

	public boolean isApplyPostProcessings() {
		return applyPostProcessings;
	}

	public void setApplyPostProcessings(boolean applyPostProcessings) {
		this.applyPostProcessings = applyPostProcessings;
	}

	public boolean isGenerateXl() {
		return generateXl;
	}

	public void setGenerateXl(boolean generateXl) {
		this.generateXl = generateXl;
	}

	public boolean isGenerateXlDefinitions() {
		return generateXlDefinitions;
	}

	public void setGenerateXlDefinitions(boolean generateXlDefinitions) {
		this.generateXlDefinitions = generateXlDefinitions;
	}

	public Repository getSupportRepository() {
		return supportRepository;
	}

	public void setSupportRepository(Repository supportRepository) {
		this.supportRepository = supportRepository;
	}

	public boolean isFailIfNoReconcile() {
		return failIfNoReconcile;
	}

	public void setFailIfNoReconcile(boolean failIfNoReconcile) {
		this.failIfNoReconcile = failIfNoReconcile;
	}
	
}
