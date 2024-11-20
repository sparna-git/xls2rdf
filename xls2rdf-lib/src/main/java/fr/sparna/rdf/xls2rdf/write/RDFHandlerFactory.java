package fr.sparna.rdf.xls2rdf.write;

import java.io.OutputStream;
import java.net.URISyntaxException;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.helpers.BufferedGroupingRDFHandler;

public class RDFHandlerFactory {
    
	public static RDFHandler buildHandler(boolean grouping, String baseIri, RDFFormat format, OutputStream out) 
	throws URISyntaxException {
		RDFHandler handler;
		if(baseIri != null) {
			if(grouping) {
				handler = new BufferedGroupingRDFHandler(100000, RDFWriterRegistry.getInstance().get(format).get().getWriter(out, baseIri));
			} else {
				handler = RDFWriterRegistry.getInstance().get(format).get().getWriter(out, baseIri);
			}
		} else {
			if(grouping) {
				handler = new BufferedGroupingRDFHandler(100000, RDFWriterRegistry.getInstance().get(format).get().getWriter(out));
			} else {
				handler = RDFWriterRegistry.getInstance().get(format).get().getWriter(out);
			}
		}
		return handler;
	}

}
