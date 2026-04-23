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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import fr.sparna.rdf.xls2rdf.web.form.convert.Xls2RdfConvertException;
import fr.sparna.rdf.xls2rdf.write.ModelWriterFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Thomas Francart
 *
 */
@Controller
public class Xls2RdfConvertController {

	//Logger
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());


	private final static String DEFAULT_FILE_NAME = "xls-2-rdf-convert";
	private static final String VIEW_NAME = "convert";

	//Enumération des types de sources possibles pour la conversion
	private enum SOURCE_TYPE {
		FILE,
		URL,
		EXAMPLE
	}
	
	@GetMapping(value = "/convert")
	public String convertRemade(Model model) {	
		model.addAttribute("view", VIEW_NAME);
		return "convert";
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

		String resultFileName = null;

		try(InputStream in = switch(source){
			case EXAMPLE -> {
				log.debug("*Conversion à partir d'un fichier d'exemple : " + example);
				URL exampleUrl = new URL(example);
				InputStream urlInputStream = createInFromUrl(exampleUrl, source);
				resultFileName = createFileNameFromUrl(exampleUrl, source);
				yield new DataInputStream(new BufferedInputStream(urlInputStream));
			}
			case FILE -> {
				log.debug("*Conversion à partir d'un fichier uploadé : " + file.getOriginalFilename());
				if(file.isEmpty()) ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.FILE_MISSING.getMessage());
				resultFileName = createFileNameFromFile(file, source);
				yield createInFromFile(file, source);
			}
			case URL -> {
				log.debug("*Conversion à partir d'une URL : " + url);
				if(url.isEmpty()) ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.URL_MISSING.getMessage());
				URL urls = new URL(url);
				InputStream urlInputStream = createInFromUrl(urls, source);
				resultFileName = createFileNameFromUrl(urls, source);
				yield new DataInputStream(new BufferedInputStream(urlInputStream));
			}
		}){
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
		}catch(Xls2RdfConvertException appEx){
			ExceptionManager.throwException(Xls2RdfConvertException.class, ExceptionManager.IO_EXCEPTION.getMessage());
		}
		catch(IOException | NullPointerException io){
			Xls2RdfException.rethrow(io);
		}
		return null;
	}


	private InputStream createInFromUrl(URL url, SOURCE_TYPE src) throws IOException{
		InputStream in = switch(src){
			case EXAMPLE -> {yield url.openStream();}
			case URL     -> {yield url.openStream();}
			default      -> {throw new Xls2RdfConvertException(ExceptionManager.NULL_POINTER.getMessage());}
		};
		return in;
	}

		private InputStream createInFromFile(MultipartFile file, SOURCE_TYPE src) throws IOException{
		InputStream in = switch(src){
			case FILE    -> {yield file.getInputStream();}
			default      -> {throw new Xls2RdfConvertException(ExceptionManager.NULL_POINTER.getMessage());}
		};
		return in;
	}

	private String createFileNameFromUrl(URL url, SOURCE_TYPE src){
		String fileName = switch(src){
			case EXAMPLE -> {
				// set the output file name to the name of the example
				String buffer = (!url.getPath().equals(""))?url.getPath():DEFAULT_FILE_NAME;
				// keep only latest file, after final /
				yield (buffer.contains("/"))?buffer.substring(buffer.lastIndexOf("/")+1):buffer;
			}
			case URL     -> {
				// set the output file name to the final part of the URL
				String buffer = (!url.getPath().equals(""))?url.getPath():DEFAULT_FILE_NAME;
				// keep only latest file, after final /
				yield (buffer.contains("/"))?buffer.substring(0, buffer.lastIndexOf("/")):buffer;
			}
			default      -> {throw new Xls2RdfConvertException(ExceptionManager.NULL_POINTER.getMessage());}
		};
		return fileName;
	}

	private String createFileNameFromFile(MultipartFile file, SOURCE_TYPE src){
		String fileName = switch(src){
			case FILE    -> {
				// set the output file name to the name of the input file
				yield (file.getOriginalFilename().contains("."))?file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.')):file.getOriginalFilename();	
			}
			default      -> {throw new Xls2RdfConvertException(ExceptionManager.NULL_POINTER.getMessage());}
		};
		return fileName;
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
