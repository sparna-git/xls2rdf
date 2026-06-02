package fr.sparna.rdf.xls2rdf.web.rest;

import fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfRestControllerException;
import fr.sparna.rdf.xls2rdf.web.service.Xls2RdfRestService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


@OpenAPIDefinition(
		info = @Info(title = "xls2rdf REST API", version = "4", description = """
				This is the REST API of xls2rdf. You may choose to convert from an URL or from a local file.""")
)
@Tag(name = "Convert")
@RestController
@RequestMapping("/api")
public class Xls2RdfRestController {

	final static Logger log = LoggerFactory.getLogger(Xls2RdfRestController.class);

	private final Xls2RdfRestService restService;

	@Autowired
	public Xls2RdfRestController(Xls2RdfRestService restService){
		this.restService = restService;
	}

	@SwaggerRestInfo
	@Operation(summary = "Convert excel file from an URL to a specific RDF media type.")
	@GetMapping(
			value = "/convert",
			produces = {"text/turtle", "application/rdf+xml", "application/n-triples", "application/n-quads", "text/n3", "application/trig"})
	public ResponseEntity<ByteArrayResource> convertRDFFromGet(
			@RequestParam(value="lang", required=false) String language,
			@Parameter(
					name = "url",
					required = true,
					description = "The URL of the Excel file to convert. Note that Google spreadsheets can be converted if they are publicly accessible (\"Everyone with the link\"), and by adding `/export?format=xlsx` at the end of their URL.",
					in = ParameterIn.QUERY,
					examples = {
							@ExampleObject(
									name = "Exemple 1",
									value = "https://xls2rdf.sparna.fr/web/excel_test/excel2skos-exemple-1.xlsx"),
							@ExampleObject(
									name = "Exemple 2",
									value = "https://xls2rdf.sparna.fr/web/excel_test/excel2skos-exemple-2.xlsx")
					}
					)
			@RequestParam(value="url", required=true) String url,
			@RequestParam(value="format", required=false) String format,
			@RequestParam(value="skosxl", required=false, defaultValue = "false") boolean useSkosXl,
			@RequestParam(value="skipHidden", required=false, defaultValue = "false") boolean skipHidden,
			@RequestParam(value="broaderTransitive", required=false, defaultValue = "false") boolean broaderTransitive,
			@RequestParam(value="noPostProcessing", required=false, defaultValue = "false") boolean ignorePostProc){
			URL clientURL = null;
			try{
				if(url.isEmpty()) ExceptionManager.throwException(Xls2RdfRestControllerException.class, ExceptionManager.URL_MISSING.getMessage());
				else clientURL = URI.create(url).toURL();
			}catch(MalformedURLException ex){
				ExceptionManager.throwException(Xls2RdfRestControllerException.class, ex.getMessage());
			}
			return this.restService.runRestConversion(language, clientURL, format, useSkosXl, skipHidden, broaderTransitive, ignorePostProc);
    }

	@SwaggerRestInfo
	@Operation(summary = "Convert excel file from a local file to a specific RDF media type.")
	@PostMapping(
			value = "/convert",
			consumes = {"multipart/form-data"},
			produces = {"text/turtle", "application/rdf+xml", "application/n-triples", "application/n-quads", "text/n3", "application/trig"})
	public ResponseEntity<ByteArrayResource> convertRDFFromPost(
			@RequestParam(value="lang", required=false) String language,
			@Parameter(
					name = "file",
					required = true,
					description = "The file of the Excel file to convert.")
			@RequestParam(value="file", required=true) MultipartFile clientFile,
			@RequestParam(value="format", required=false) String format,
			@RequestParam(value="skosxl", required=false, defaultValue = "false") boolean useSkosXl,
			@RequestParam(value="skipHidden", required=false, defaultValue = "false") boolean skipHidden,
			@RequestParam(value="broaderTransitive", required=false, defaultValue = "false") boolean broaderTransitive,
			@RequestParam(value="noPostProcessing", required=false, defaultValue = "false") boolean ignorePostProc){
			try{
				if(clientFile.isEmpty()) ExceptionManager.throwException(Xls2RdfRestControllerException.class, ExceptionManager.FILE_MISSING.getMessage());
			}catch(Exception ex){
				ExceptionManager.throwException(Xls2RdfRestControllerException.class, ex.getMessage());
			}
			return this.restService.runRestConversion(language, clientFile, format, useSkosXl, skipHidden, broaderTransitive, ignorePostProc);
	}

}
