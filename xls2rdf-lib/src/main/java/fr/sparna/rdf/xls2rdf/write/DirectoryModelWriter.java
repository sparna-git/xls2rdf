package fr.sparna.rdf.xls2rdf.write;


import fr.sparna.rdf.xls2rdf.RepositoryWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;

/**
 * Saves each Model in a separate file in the given directory, and optionnaly generates a graph file for easy loading into Virtuoso.
 * 
 * @author thomas
 *
 */
public class DirectoryModelWriter implements RepositoryWriterIfc {
	
	private File outputFolder;
	private boolean saveGraphFile = true;
	private RDFFormat format = RDFFormat.RDFXML;
	
	private Repository outputRepository;

	private String baseIri = null;
	
	private boolean grouping = true;
	
	public DirectoryModelWriter(File outputFolder) {
		super();
		this.outputFolder = outputFolder;

		this.outputRepository = new SailRepository(new MemoryStore());
		this.outputRepository.init();
	}

	@Override
	public void saveRepository(Repository repository, String baseIri) {
		try {
			try(RepositoryConnection target = this.outputRepository.getConnection()) {
				try(RepositoryConnection source = repository.getConnection()) {
					// register the prefixes
					source.getNamespaces().forEach(rr -> target.setNamespace(rr.getPrefix(), rr.getName()));
					// dump triples
					RDFInserter inserter = new RDFInserter(target);
					target.begin();
    				source.export(inserter);   // exports ALL statements from ALL contexts
    				target.commit();
				}
			}
			// keep track of baseIri
			this.baseIri = baseIri;
		} catch(Exception e) {
			throw Xls2RdfException.rethrow(e);
		}
	}

	@Override
	public void beginWorkbook() {

	}

	@Override
	public void endWorkbook() {
		
		try(RepositoryConnection source = this.outputRepository.getConnection()) {
			for(Resource context : source.getContextIDs()) {
				try {
					String filename = URLEncoder.encode(context.stringValue(), "UTF-8");
					File file = new File(outputFolder, filename + "." + format.getDefaultFileExtension());
					try (FileOutputStream fos = new FileOutputStream(file)) {						
						RDFHandler handler = RDFHandlerFactory.buildHandler(grouping, this.baseIri, format, fos);
						source.export(handler, context);
						fos.flush();
					} catch (Exception e) {
						throw new RuntimeException("Failed to save model", e);
					}
					
					if(saveGraphFile) {
						File graphFile = new File(outputFolder, file.getName() + ".graph");
						IOUtils.write(context.stringValue(), new FileOutputStream(graphFile));
					}
				} catch(Exception e) {
					throw Xls2RdfException.rethrow(e);
				}	
			}
		}

		// reset accumulated data so that the same writer does not accumulate data between every files
		this.outputRepository = new SailRepository(new MemoryStore());
		this.outputRepository.init();
		this.baseIri = null;
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
