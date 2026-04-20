package fr.sparna.rdf.xls2rdf.web.controller.convert;

/** SUPPRESION A VERIFIER 
import fr.sparna.rdf.skosplay.log.LogEntry;
*/
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import fr.sparna.rdf.xls2rdf.ModelWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.Xls2RdfPostProcessorIfc;
import fr.sparna.rdf.xls2rdf.postprocess.SkosPostProcessor;
import fr.sparna.rdf.xls2rdf.postprocess.SkosXlPostProcessor;
import fr.sparna.rdf.xls2rdf.web.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfConvertException;
import fr.sparna.rdf.xls2rdf.write.ModelWriterFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Thomas Francart
 *
 */
@Controller
public class Xls2RdfConvertController {

	//Logger
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());

	//Enumération des types de sources possibles pour la conversion
	private enum SOURCE_TYPE {
		FILE,
		URL,
		EXAMPLE
	}
	
	@GetMapping(value = "/convert", produces = "text/html")
	public String convertRemade(
			ModelMap model,
			HttpSession session
			) throws IOException  {	
		return "convertRemade";
	}

	//Traitement du formulaire de conversion en POST
	@PostMapping(value = "/convert")
	public ModelAndView convertRDF(
			// type of source ("file", "url", "example", "google")
			@RequestParam(value="source", required=true) String sourceString,
			// uploaded file if source=file
			@RequestParam(value="file", required=false) MultipartFile file,
			// language of the labels to generate
			@RequestParam(value="language", required=false) String language,
			// URL of the file if source=url
			@RequestParam(value="url", required=false) String url,
			// output format of the generated files
			@RequestParam(value="output", required=false) String format,
			// reference of the example if source=example
			@RequestParam(value="example", required=false) String example,
			// flag to generate SKOS-XL or not
			@RequestParam(value="useskosxl", required=false) boolean useskosxl,
			// flag to generate broaderTransitive or not
			@RequestParam(value="broaderTransitive", required=false) boolean broaderTransitive,
			// flag to output result in a ZIP file or not
			@RequestParam(value="usezip", required=false) boolean useZip,
			// flag to indicate if graph files should be generated or not
			@RequestParam(value="ignorePostProc", required=false) boolean ignorePostProc,
			// the request
			HttpServletRequest request,
			// the response
			HttpServletResponse response
	){

		log.debug("convert(source="+sourceString+",file="+file+"format="+format+",usexl="+useskosxl+",broaderTransitive="+broaderTransitive+",useZip="+useZip+",language="+language+",url="+url+",ex="+example+")");
		//source, it can be: file, example, url or google
		SOURCE_TYPE source = SOURCE_TYPE.valueOf(sourceString.toUpperCase());
		// format
		RDFFormat theFormat = RDFWriterRegistry.getInstance().getFileFormatForMIMEType(format).orElse(RDFFormat.RDFXML);

		URL exampleUrl = null;
		URL urls = null;
		String resultFileName = null;

		try(InputStream in = switch(source){
			case EXAMPLE -> {
				log.debug("*Conversion à partir d'un fichier d'exemple : " + example);
				exampleUrl = new URL(example);
				InputStream urlInputStream = exampleUrl.openStream(); // throws an IOException
				// set the output file name to the name of the example
				resultFileName = (!exampleUrl.getPath().equals(""))?exampleUrl.getPath():resultFileName;
				// keep only latest file, after final /
				resultFileName = (resultFileName.contains("/"))?resultFileName.substring(resultFileName.lastIndexOf("/")+1):resultFileName;
				yield new DataInputStream(new BufferedInputStream(urlInputStream));
			}
			case FILE -> {
				log.debug("*Conversion à partir d'un fichier uploadé : " + file.getOriginalFilename());
				if(file.isEmpty()) ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.FILE_MISSING);
				// set the output file name to the name of the input file
				resultFileName = (file.getOriginalFilename().contains("."))?file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')):file.getOriginalFilename();	
				yield file.getInputStream();
			}
			case URL -> {
				log.debug("*Conversion à partir d'une URL : " + url);
				if(url.isEmpty()) ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.URL_MISSING);
				urls = new URL(url);
				InputStream urlInputStream = urls.openStream(); // throws an IOException
				// set the output file name to the final part of the URL
				resultFileName = (!urls.getPath().equals(""))?urls.getPath():resultFileName;
				// keep only latest file, after final /
				resultFileName = (resultFileName.contains("/"))?resultFileName.substring(0, resultFileName.lastIndexOf("/")):resultFileName;
				yield new DataInputStream(new BufferedInputStream(urlInputStream));
			}
		}){

			System.out.println("in2 = " + in);
					URL baseURL = new URL("http://"+request.getServerName()+((request.getServerPort() != 80)?":"+request.getServerPort():"")+request.getContextPath());
					log.debug("Base URL is "+ baseURL.toString());
					/**************************CONVERSION RDF**************************/
							System.setProperty("org.eclipse.rdf4j.rio.turtle.abbreviate_numbers", "false");			
							log.debug("*Lancement de la conversion avec lang="+language+" et usexl="+useskosxl);
							// le content type est toujours positionné à "application/zip" si on nous a demandé un zip, sinon il dépend du format de retour demandé
							response.setContentType((useZip)?"application/zip":theFormat.getDefaultMIMEType());
							// set response charset corresponding to the format, if applicable
							if(theFormat.hasCharset()) {
								response.setCharacterEncoding(theFormat.getCharset().name());
							}
							// le nom du fichier de retour
							// strip extension, if any
							resultFileName = (resultFileName.contains("."))?resultFileName.substring(0, resultFileName.lastIndexOf('.')):resultFileName;
							String extension = (useZip)?"zip":theFormat.getDefaultFileExtension();
						
							// add the date in the filename
							String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
						
							response.setHeader("Content-Disposition", "inline; filename=\""+resultFileName+"-"+dateString+"."+extension+"\"");
						
							List<String> identifiant = runConversion(
									new ModelWriterFactory(useZip, theFormat).buildNewModelWriter(response.getOutputStream()),
									in,
									language.equals("")?null:language,
									useskosxl,
									broaderTransitive,
									ignorePostProc
							);
						
							// sort to garantee order
							List<String> uri=new ArrayList<>(identifiant);
							Collections.sort(uri);
		}catch(IOException io){
			ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.IO_EXCEPTION);
		}
		return null;
	}


	
	private List<String> runConversion(ModelWriterIfc writer, InputStream filefrom, String lang, boolean generatexl, boolean broaderTransitive, boolean ignorePostProc) {
		Xls2RdfConverter converter;
		if(lang == null || lang.trim().equals("")) {
			converter = new Xls2RdfConverter(writer);
		} else {
			converter = new Xls2RdfConverter(writer, lang);
		}
		converter.setFailIfNoReconcile(true);
		
		List<Xls2RdfPostProcessorIfc> postProcessors = new ArrayList<>();
	
		if(!ignorePostProc) {
			postProcessors.add(new SkosPostProcessor(broaderTransitive));
		
			if (generatexl) {
				postProcessors.add(new SkosXlPostProcessor(generatexl, generatexl));
			}
		}
		converter.setPostProcessors(postProcessors);
	
		converter.processInputStream(filefrom);
		return converter.getConvertedVocabularyIdentifiers();
	}


	

}
