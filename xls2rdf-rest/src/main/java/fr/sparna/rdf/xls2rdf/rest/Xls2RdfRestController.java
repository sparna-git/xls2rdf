package fr.sparna.rdf.xls2rdf.rest;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class Xls2RdfRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private Xls2RdfService xls2RdfService;	

	@RequestMapping(value = "/convert", method = RequestMethod.GET, params = {"url"})
	public ResponseEntity<ByteArrayResource> convertRDF(
			@RequestParam(value="lang", required=false) String language,
			@RequestParam(value="url", required=true) String url,
			@RequestParam(value="format", required=false) String format,
			@RequestParam(value="skosxl", required=false, defaultValue = "false") boolean useSkosXl,
			@RequestParam(value="broaderTransitive", required=false, defaultValue = "false") boolean broaderTransitive,
			@RequestParam(value="noPostProcessings", required=false, defaultValue = "false") boolean ignorePostProc)
					throws Exception {
		
		RDFFormat theFormat = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(format).orElse(RDFFormat.TURTLE);

		InputStream in;
		String resultFileName = "xls2rdf";

		try {
			URL urls = new URL(url);
			InputStream urlInputStream = urls.openStream(); // throws an IOException
			in = new DataInputStream(new BufferedInputStream(urlInputStream));

			// set the output file name to the final part of the URL
			resultFileName = (!urls.getPath().equals("")) ? urls.getPath() : resultFileName;
			// keep only latest file, after final /
			resultFileName =
					(resultFileName.contains("/"))
					? resultFileName.substring(0, resultFileName.lastIndexOf("/"))
							: resultFileName;
		} catch (IOException e) {
			log.error("error", e);
			throw new RuntimeException(e);
		}

		try {
			resultFileName =
					(resultFileName.contains("."))
					? resultFileName.substring(0, resultFileName.lastIndexOf('.'))
							: resultFileName;

					// add the date in the filename
					String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

					ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
					try (var openedIn = in) {
						List<String> cvIds = this.xls2RdfService.convert(
								openedIn,
								responseOutputStream,
								language,
								theFormat,
								useSkosXl,
								broaderTransitive,
								ignorePostProc,
								//fail if no reconcile
								false
						);

						Collections.sort(cvIds);
						cvIds.stream().map(cv -> "Converted Graph: " + cv).forEach(log::info);

						String filename = String.format("%s-%s.%s", resultFileName, dateString, theFormat.getDefaultFileExtension());
						return transformToByteArrayResource(filename, theFormat.getDefaultMIMEType(), responseOutputStream.toByteArray());
					}
		} catch (Exception e) {
			log.error("error", e);
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value = "/convert", method = RequestMethod.GET)
	public ModelAndView home() {
		return new ModelAndView("forward:/index.html");
	}



	static ResponseEntity<ByteArrayResource> transformToByteArrayResource(
			String filename,
			String contentType,
			byte[] file
    ) {
		return Optional.ofNullable(file)
				.map(
						u ->
						ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(contentType))
						.header(
								HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + filename + "\"")
						.body(new ByteArrayResource(file)))
				.orElse(ResponseEntity.badRequest().body(null));
	}

}
