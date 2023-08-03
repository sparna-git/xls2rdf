package fr.sparna.rdf.xls2rdf.postprocess;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;

public class SkosXlPostProcessor implements Xls2RdfPostProcessorIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * Whether to automatically generates SKOS-XL labels
	 */
	protected boolean generateXl = true;
	
	/**
	 * Whether to automatically reify definitions (for Vocbench)
	 */
	private boolean generateXlDefinitions = false;

	public SkosXlPostProcessor(boolean generateXl, boolean generateXlDefinitions) {
		super();
		this.generateXl = generateXl;
		this.generateXlDefinitions = generateXlDefinitions;
	}

	@Override
	public void afterSheet(Model model, Resource mainResource, List<Resource> rowResources) {
		log.debug("Postprocessing : "+this.getClass().getSimpleName());
		
		Repository r = new SailRepository(new MemoryStore());
		r.init();
		
		try(RepositoryConnection c = r.getConnection()) {
			c.add(model);

			if(this.generateXl) {
				final List<String> SKOS2SKOSXL_URI_RULESET = Arrays.asList(new String[] { 
						"skos2skosxl/S55-S56-S57-URIs.ru"
				});			
				
				for (String aString : SKOS2SKOSXL_URI_RULESET) {
					// Load SPARQL query definition
			        InputStream src = this.getClass().getResourceAsStream(aString);		        
			        String sparql =  IOUtils.toString(src);					
					Update u = c.prepareUpdate(sparql);
					u.execute();
				}
			}
			
			if(this.generateXlDefinitions) {
				final List<String> SKOS2SKOSXL_NOTES_URI_RULESET = Arrays.asList(new String[] { 
						"skos2skosxl/S16-URIs.ru"
				});
				
				for (String aString : SKOS2SKOSXL_NOTES_URI_RULESET) {
					// Load SPARQL query definition
			        InputStream src = this.getClass().getResourceAsStream(aString);	
			        String sparql =  IOUtils.toString(src);
					Update u = c.prepareUpdate(sparql);
					u.execute();
				}
			}
			
			// re-export to a new Model
			model.clear();
			c.export(new AbstractRDFHandler() {
				public void handleStatement(Statement st) throws RDFHandlerException {
					model.add(st);
				}			
			});
		} catch (Exception e) {
			throw Xls2RdfException.rethrow(e);
		}
		
	}
	
}
