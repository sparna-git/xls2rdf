package fr.sparna.rdf.xls2rdf.write;

import fr.sparna.rdf.xls2rdf.RepositoryWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author thomas
 *
 */
public class ZipOutputStreamModelWriter implements RepositoryWriterIfc {
	
	private OutputStream underlyingStream;
	private ZipOutputStream out;
	private RDFFormat format = RDFFormat.TURTLE;
	private boolean saveGraphFile = false;
	
	private boolean grouping = true;
	
	public ZipOutputStreamModelWriter(ZipOutputStream out) {
		super();
		this.out = out;
	}
	
	public ZipOutputStreamModelWriter(File f) {
		super();
		try {
			if(!f.exists()) {
				f.createNewFile();
			}
			this.underlyingStream = new FileOutputStream(f);
			this.out = new ZipOutputStream(this.underlyingStream, Charset.forName("UTF-8"));
			this.out.setLevel(9);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public ZipOutputStreamModelWriter(OutputStream underlyingStream) {
		super();
		try {
			this.underlyingStream = underlyingStream;
			this.out = new ZipOutputStream(this.underlyingStream, Charset.forName("UTF-8"));
			this.out.setLevel(9);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void saveRepository(Repository repository, String baseIri) {

		try {

			try(RepositoryConnection source = repository.getConnection()) {
				for(Resource context : source.getContextIDs()) {
					String entryName = URLEncoder.encode(context.stringValue(), "UTF-8") + "." + format.getDefaultFileExtension();
					out.putNextEntry(new ZipEntry(entryName));
					
					// writes in the entry
					RDFHandler handler = RDFHandlerFactory.buildHandler(grouping, baseIri, format, out);
					source.export(handler, context);
					
					// close the entry
					out.closeEntry();
					
					if(saveGraphFile) {
						String graphFileName = entryName + ".graph";
						out.putNextEntry(new ZipEntry(graphFileName));
						out.write(context.stringValue().getBytes());
					}
				}
			}
			
		} catch(Exception e) {
			throw Xls2RdfException.rethrow(e);
		}
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
	
	public boolean isSaveGraphFile() {
		return saveGraphFile;
	}

	public void setSaveGraphFile(boolean saveGraphFile) {
		this.saveGraphFile = saveGraphFile;
	}

	public boolean isGrouping() {
		return grouping;
	}

	public void setGrouping(boolean grouping) {
		this.grouping = grouping;
	}

	@Override
	public void beginWorkbook() {

	}

	@Override
	public void endWorkbook() {
		try {
			out.flush();
			out.close();
			this.underlyingStream.close();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RDFFormat getFormat() {
		return format;
	}

	public void setFormat(RDFFormat format) {
		this.format = format;
	}

}
