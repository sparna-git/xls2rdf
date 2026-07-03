package fr.sparna.rdf.xls2rdf.web.controller.form;

/** SUPPRESION A VERIFIER 
import fr.sparna.rdf.skosplay.log.LogEntry;
*/

import fr.sparna.rdf.xls2rdf.WorkbookMapping;
import fr.sparna.rdf.xls2rdf.YamlParser;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfConvertException;
import fr.sparna.rdf.xls2rdf.web.service.Xls2RdfConverterService;
import fr.sparna.rdf.xls2rdf.web.utils.ConvertFormModelKey;
import fr.sparna.rdf.xls2rdf.web.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager.GRIST_NULL;

/**
 *
 * @author Thomas Francart
 *
 */
@Controller
public class Xls2RdfConvertController {


	final static Logger log = LoggerFactory.getLogger(Xls2RdfConvertController.class);

	//Enumération des types de sources possibles pour la conversion
	public enum SOURCE_TYPE {
		FILE,
		URL,
		EXAMPLE,
		GRIST
	}

	private Xls2RdfConverterService converterService;

	@Autowired
	public Xls2RdfConvertController(Xls2RdfConverterService converterService){
		this.converterService = converterService;
	}

	@GetMapping(value = "/convert")
	public String convert(Model model) {	
		model.addAttribute(ConvertFormModelKey.VIEW.getKey(), ConvertFormModelKey.CONVERT.getKey());
		return "convert";
	}

	@ResponseBody
	@PostMapping(
			value = "/convert",
			produces = {"text/turtle", "application/rdf+xml", "application/n-triples", "application/n-quads", "text/n3", "application/trig"})
		public ResponseEntity<ByteArrayResource> convertRDF(
			@RequestParam(value="source", required=true) String sourceString,
			@RequestParam(value="file", required=false) MultipartFile file,
			@RequestParam(value="language", required=false) String language,
			@RequestParam(value="url", required=false) String url,
			@RequestParam(value="output", required=false) String format,
			@RequestParam(value="example", required=false) String example,
			@RequestParam(value="useskosxl", required=false) boolean useSkosXl,
			@RequestParam(value="broaderTransitive", required=false) boolean broaderTransitive,
			@RequestParam(value="usezip", required=false) boolean useZip,
			@RequestParam(value="ignorePostProc", required=false) boolean ignorePostProc,
			@RequestParam(value="gristDocumentId", required = false) String gristDocumentId,
			@RequestParam(value="gristTokenApi", required = false) String gristTokenApi,
			@RequestParam(value="gristMapping", required = false) MultipartFile gristMapping,
			// the request
			HttpServletRequest request
	) {

		/*
		 **************
		 * PROPERTIES *
		 * ************
		 */
		log.debug("convert(source="+sourceString+",file="+file+"format="+format+",usexl="+useSkosXl+",broaderTransitive="+broaderTransitive+",useZip="+useZip+",language="+language+",url="+url+",ex="+example+")");
		//source, it can be: file, example, url or google
		SOURCE_TYPE source = SOURCE_TYPE.valueOf(sourceString.toUpperCase());
		// format
		RDFFormat theFormat = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(format).orElse(RDFFormat.TURTLE);
		//Might be unused if client does not use grist api
		GristWorkbook gristWorkbook = null;
		WorkbookMapping mapping = null;
		//The spring's resource to handle the response through EntityResponse<ByteArrayOutputStream>
		ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
		// le content type est toujours positionné à "application/zip" si on nous a demandé un zip, sinon il dépend du format de retour demandé
		String extension = (useZip) ? "zip" : theFormat.getDefaultFileExtension();
		String fileName = StringUtils.DEFAULT_FILE_NAME.concat(".").concat(extension);


		/*
		 ****************************
		 * TRY URL PARSING          *
		 * **************************
		 */
		URL clientURL = null;

		try{
			switch(source){
            case URL 	 -> {
				if(url.isEmpty()) ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.URL_MISSING.getMessage());
				else clientURL = URI.create(url).toURL();
            }
            case EXAMPLE -> clientURL = URI.create(example).toURL();
        }
		} catch (MalformedURLException ex) {
			ExceptionManager.throwException(Xls2RdfConvertException.class, ex.getMessage());
        }


		/*
		 *******************************
		 * TRY INPUTSTREAM FROM SOURCE *
		 * *****************************
		 */
        try(InputStream in = switch(source){
			case EXAMPLE -> {
				log.debug("*Conversion à partir d'un fichier d'exemple : " + example);
				fileName = StringUtils.formatFileName(clientURL, extension);
				yield new DataInputStream(new BufferedInputStream(clientURL.openStream()));
			}
			case FILE 	 -> {
				log.debug("*Conversion à partir d'un fichier uploadé : " + file.getOriginalFilename());
				if(file.isEmpty()) ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.FILE_MISSING.getMessage());
				fileName = StringUtils.formatFileName(file, extension);
				yield file.getInputStream();
			}
			case URL 	 -> {
				log.debug("*Conversion à partir d'une URL : " + url);
				fileName = StringUtils.formatFileName(clientURL, extension);
				yield new DataInputStream(new BufferedInputStream(clientURL.openStream()));
			}
            case GRIST -> null;
        }){

			/*
			 ****************************
			 * TRY IF SOURCE IS GRIST   *
			 * **************************
			 */
			if(source == SOURCE_TYPE.GRIST){
				log.debug("*Conversion à partir d'une l'API Grist : " + "docID = " + gristDocumentId);
				//On vérifie si un des trois champs vaut null, si c'est le cas -> exception rendu à l'utilisateur pour indiquer qu'il y a un null
				if(gristMapping.isEmpty() || gristDocumentId.isBlank() || gristTokenApi.isBlank()) ExceptionManager.throwException(Xls2RdfConvertException.class, GRIST_NULL.getMessage());
				gristWorkbook = GristWorkbookFactory.open(gristDocumentId, gristTokenApi, true);
				mapping = new WorkbookMapping(YamlParser.getInstance(gristMapping.getInputStream()));//<--------- On récupère le fichier de Mapping et on on crée une instance de YamlParser à passer au WorkbookMapping
				//On crée le fileName
				fileName = StringUtils.formatGristName(gristDocumentId, extension);
			}

			URL baseURL = URI.create("http://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort(): "") + request.getContextPath()).toURL();
			log.debug("Base URL is "+ baseURL.toString());
			System.setProperty("org.eclipse.rdf4j.rio.turtle.abbreviate_numbers", "false");

			/** Si c'est un appel Grist on passe par une instance de Workbook -> {@link GristWorkbook}
			 * qui sera envoyé au convertisseur
			 * Pour les autres ressources c'est un InputStream, donc on passe un inputstream SOIT un workbook dans {@link Xls2RdfConverterService#convert(InputStream, OutputStream, String, RDFFormat, boolean, boolean, boolean, boolean, boolean, boolean, Workbook, WorkbookMapping)}
			 */
			/*
			 *****************************
			 * LAUNCH CONVERSION SERVICE *
			 * ***************************
			 *
			 */
			//La conversion se fait dans responseOutputStream qui va ensuite retourner un tableau de bytes pour this.converterService.transformConversionToResponseEntity
			List<String> cvIds = this.converterService.convert(
					in,//<-------------- Si non null Source=EXAMPLE ou FILE ou URL
					responseOutputStream, //<------ responseOutputStream qui encapsule la conversion
					language.isEmpty() ? null : language,
					theFormat,
					useSkosXl,
					broaderTransitive,
					ignorePostProc,
					//fail if no reconcile
					false,
					// skip hidden rows and columns
					false,
					useZip,
					gristWorkbook, //<--------- Si non null SOURCE=GRIST
					mapping //<----------- Peut être null
			);

			cvIds.stream().map(cv -> "Converted Graph: " + cv).forEach(log::info);

			/*
			 ****************************
			 * PREPARE CLIENT ANSWER    *
			 * **************************
			 */
			return this.converterService.transformConversionToResponseEntity(fileName, theFormat.getDefaultMIMEType(), responseOutputStream.toByteArray(), ContentDisposition.attachment());

		} catch (IOException ex) {
			ExceptionManager.throwException(Xls2RdfConvertException.class, ex.getMessage());
        }
        return ResponseEntity.badRequest().build();
	}

}
