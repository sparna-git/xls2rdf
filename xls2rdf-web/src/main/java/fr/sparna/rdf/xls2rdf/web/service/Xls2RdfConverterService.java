package fr.sparna.rdf.xls2rdf.web.service;

import fr.sparna.rdf.xls2rdf.ModelWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverterFactory;
import fr.sparna.rdf.xls2rdf.write.ModelWriterFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class Xls2RdfConverterService {

	final static Logger log = LoggerFactory.getLogger(Xls2RdfConverterService.class);

	public List<String> convert(InputStream input,
		OutputStream output, 
		String lang, 
		RDFFormat format, 
		boolean skosxl, 
		boolean broaderTransitive, 
		boolean ignorePostProc,
		boolean failIfNoReconcile,
		boolean skipHidden,
		boolean zip) {

		ModelWriterFactory factory = new ModelWriterFactory(zip, format, false);

		ModelWriterIfc modelWriter = factory.buildNewModelWriter(output);
		
		Xls2RdfConverterFactory converterFactory = new Xls2RdfConverterFactory(!ignorePostProc, skosxl, skosxl, broaderTransitive, failIfNoReconcile, skipHidden);
		
		Xls2RdfConverter converter = converterFactory.newConverter(modelWriter, lang);
		
		converter.processInputStream(input);
		return converter.getConvertedVocabularyIdentifiers();
	}

	public ResponseEntity<ByteArrayResource> transformConversionToResponseEntity(String filename, String contentType, byte[] file, ContentDisposition.Builder builder) {
		HttpHeaders header = new HttpHeaders();
		header.setContentDisposition(builder.filename(filename, StandardCharsets.UTF_8).build());
		header.setContentType(MediaType.parseMediaType(contentType));
		return new ResponseEntity<>(new ByteArrayResource(file), header, HttpStatus.CREATED);
	}
}
