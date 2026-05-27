package fr.sparna.rdf.xls2rdf.web.service;

import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverterBuilder;
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

		Xls2RdfConverterBuilder builder = Xls2RdfConverterBuilder.getInstance()
				.withLanguage(lang)
				.withGenerateXl(skosxl)
				.withGenerateXlDefinitions(skosxl)
				.withApplyPostProcessing(ignorePostProc)
				.withFailOnReconcile(failIfNoReconcile)
				.withGenerateBroaderTransitive(broaderTransitive)
				.withSkipHidden(skipHidden)
				.withFormat(format.getDefaultMIMEType())
				.withModelWriterFactory(zip, false, false)
				.withOutputStream(output);

		Xls2RdfConverter converter = builder.buildConverter();
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
