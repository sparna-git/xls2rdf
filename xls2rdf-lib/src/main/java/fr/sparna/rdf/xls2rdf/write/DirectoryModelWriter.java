package fr.sparna.rdf.xls2rdf.write;


import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.helpers.BufferedGroupingRDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import fr.sparna.rdf.xls2rdf.ModelWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;

/**
 * Saves each Model in a separate file in the given directory, and optionnaly generates a graph file for easy loading into Virtuoso.
 * 
 * @author thomas
 *
 */
public class DirectoryModelWriter implements ModelWriterIfc {
	
	private File outputFolder;
	private boolean saveGraphFile = true;
	private RDFFormat format = RDFFormat.RDFXML;
	private String graphSuffix = null;
	
	private Map<String, Model> modelsByGraph = new HashMap<>();
	private Map<String, Map<String, String>> prefixesByGraph = new HashMap<>();

	private Map<String, String> baseIriByGraph = new HashMap<>();
	
	private boolean grouping = true;
	
	public DirectoryModelWriter(File outputFolder) {
		super();
		this.outputFolder = outputFolder;
	}

	/* (non-Javadoc)
	 * @see fr.sparna.rdf.skos.xls2skos.ModelSaverIfc#saveGraphModel(java.lang.String, org.eclipse.rdf4j.model.Model)
	 */
	@Override
	public void saveGraphModel(String graph, Model model, Map<String, String> prefixes, String baseIri) {
		graph = graph + ((this.graphSuffix != null)?graphSuffix:"");
		
		if(modelsByGraph.containsKey(graph)) {
			modelsByGraph.get(graph).addAll(model);
		} else {
			modelsByGraph.put(graph, model);
		}
		
		prefixesByGraph.put(graph, prefixes);
		baseIriByGraph.put(graph, baseIri);
	}
	
	public void exportModel(Model model, RDFHandler handler, Map<String, String> prefixes) {
		Repository r = new SailRepository(new MemoryStore());
		r.init();
		try(RepositoryConnection c = r.getConnection()) {
			// register the prefixes
			prefixes.entrySet().forEach(e -> c.setNamespace(e.getKey(), e.getValue()));
			c.add(model);
			c.export(handler);			
		}
	} 

	public String getGraphSuffix() {
		return graphSuffix;
	}

	public void setGraphSuffix(String graphSuffix) {
		this.graphSuffix = graphSuffix;
	}

	@Override
	public void beginWorkbook() {

	}

	@Override
	public void endWorkbook() {
		
		for (Map.Entry<String, Model> anEntry : this.modelsByGraph.entrySet()) {
			String graph = anEntry.getKey();
			Model model = anEntry.getValue();
			
			try {
				String filename = URLEncoder.encode(graph, "UTF-8");
				File file = new File(outputFolder, filename + "." + format.getDefaultFileExtension());
				try (FileOutputStream fos = new FileOutputStream(file)) {
					
					RDFHandler handler;
					if(grouping) {
						handler = new BufferedGroupingRDFHandler(20000, RDFWriterRegistry.getInstance().get(format).get().getWriter(fos));
					} else {
						// pass the baseIri to the getWriter call
						handler = RDFWriterRegistry.getInstance().get(format).get().getWriter(fos, baseIriByGraph.get(graph));
					}
					exportModel(model, handler, prefixesByGraph.get(graph));
					fos.flush();
				} catch (Exception e) {
					throw new RuntimeException("Failed to save model", e);
				}
				
				if(saveGraphFile) {
					File graphFile = new File(outputFolder, file.getName() + ".graph");
					IOUtils.write(graph, new FileOutputStream(graphFile));
				}
			} catch(Exception e) {
				throw Xls2RdfException.rethrow(e);
			}			
		}

		// reset accumulated data so that the same writer does not accumulate data between every files
		this.modelsByGraph = new HashMap<>();
		this.prefixesByGraph = new HashMap<>();
	}

	public boolean isSaveGraphFile() {
		return saveGraphFile;
	}

	public void setSaveGraphFile(boolean saveGraphFile) {
		this.saveGraphFile = saveGraphFile;
	}

	public RDFFormat getFormat() {
		return format;
	}

	public void setFormat(RDFFormat format) {
		this.format = format;
	}
	
	public boolean isGrouping() {
		return grouping;
	}

	public void setGrouping(boolean grouping) {
		this.grouping = grouping;
	}

}
