package fr.sparna.rdf.xls2rdf.web.service;

import fr.sparna.rdf.xls2rdf.WorkbookMapping;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverterBuilder;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
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

	public List<String> convert(InputStream input, //<-------- Peut etre null
	                            OutputStream output,
	                            String lang,
	                            RDFFormat format,
	                            boolean skosxl,
	                            boolean broaderTransitive,
	                            boolean ignorePostProc,
	                            boolean failIfNoReconcile,
	                            boolean skipHidden,
	                            boolean zip,
								Workbook workbook, //<-------- Peut etre null
								WorkbookMapping mapping ) {


		/*
		 ****************************
		 * CONVERTER BUILDER        *
		 * **************************
		 */
		Xls2RdfConverterBuilder builder = Xls2RdfConverterBuilder.getInstance()
				.withLanguage(lang)
				.withGenerateXl(skosxl)
				.withGenerateXlDefinitions(skosxl)
				.withApplyPostProcessing(!ignorePostProc)
				.withFailOnReconcile(failIfNoReconcile)
				.withGenerateBroaderTransitive(broaderTransitive)
				.withSkipHidden(skipHidden)
				.withFormat(format.getDefaultMIMEType())
				.withModelWriterFactory(zip, false, false)
				.withOutputStream(output)
				.withWorkbookMapping(mapping);

		//Generate builder instance
		Xls2RdfConverter converter = builder.buildConverter();

		/*
		 ***********************************************
		 * VERIFY WORKBOOK OR INPUT AND RUN CONVERSION *
		 * *********************************************
		 */
		if(workbook != null){
			converter.processWorkbook(workbook);
		}
		else if(input != null){
			converter.processInputStream(input);
		}
		return converter.getConvertedVocabularyIdentifiers();
	}

	public ResponseEntity<ByteArrayResource> transformConversionToResponseEntity(String filename, String contentType, byte[] file, ContentDisposition.Builder builder) {
		HttpHeaders header = new HttpHeaders();
		//On ajoute le ContentDisposition soit à inline soit attachment, actuellement le code prend attachment ce qui provoque le download du fichier par le client
		header.setContentDisposition(builder.filename(filename, StandardCharsets.UTF_8).build());
		//on indique bien le type mime dans la réponse
		header.setContentType(MediaType.parseMediaType(contentType));
		return new ResponseEntity<>(new ByteArrayResource(file), header, HttpStatus.CREATED); //<----- la réponse et faite ici, on encapsule le tableau de byes dans un ResponseEntity<ByteArrayResource>
	}
}
