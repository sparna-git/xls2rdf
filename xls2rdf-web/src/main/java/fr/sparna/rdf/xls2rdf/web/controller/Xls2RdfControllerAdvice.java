package fr.sparna.rdf.xls2rdf.web.controller;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.web.ApplicationData;
import fr.sparna.rdf.xls2rdf.web.RequestData;
import fr.sparna.rdf.xls2rdf.web.SessionData;
import fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfConvertException;
import fr.sparna.rdf.xls2rdf.web.utils.ConvertFormModelKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Arthur Leroux
 */
@ControllerAdvice(basePackages = {"fr.sparna.rdf.xls2rdf.web.controller"})
public class Xls2RdfControllerAdvice {

	final static Logger log = LoggerFactory.getLogger(Xls2RdfControllerAdvice.class);

    private final RequestData requestData;
	private final ApplicationData applicationData;
	private final SessionData sessionData;


	//Injection des dépendances par Spring via le constructeur
	@Autowired
 	public Xls2RdfControllerAdvice(ApplicationData applicationData, RequestData requestData, SessionData sessionData) {
 	    this.applicationData = applicationData;
 	    this.requestData = requestData;
 	    this.sessionData = sessionData;
 	}

	private Map<String, Object> convertFormData(){
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put(ConvertFormModelKey.FORM_DATA.getKey(), this.requestData);
		modelMap.put(ConvertFormModelKey.APPLICATION_DATA.getKey(), this.applicationData);
		modelMap.put(ConvertFormModelKey.SESSION_DATA.getKey(), this.sessionData);
		this.requestData.setDefaultLanguage(this.sessionData.getUserLocale().getLanguage());	
		return modelMap;
	}

    @ModelAttribute
	public void prepareConvertModel(Model model){
		model.addAllAttributes(convertFormData());
	}


	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = {Xls2RdfConvertException.class}, produces = {"text/html"})
	public String xls2RdfConvertExceptionHandler(Xls2RdfConvertException ex, Model model){
		this.requestData.setErrorMessage(ex.getMessage());
		model.addAllAttributes(convertFormData());
		return "convert";
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(exception = {Xls2RdfException.class}, produces = {"text/html"})
	public String xls2RdfExceptionHandler(Xls2RdfException ex, Model model){
		this.requestData.setErrorMessage(ex.getMessage());
		model.addAllAttributes(convertFormData());
		return "convert";
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(exception = {Exception.class}, produces = {"text/html"})
	public String exceptionHandler(Exception ex, Model model){
		this.requestData.setErrorMessage(ExceptionManager.getStackTrace(ex));
		ex.printStackTrace();
		model.addAllAttributes(convertFormData());
		return "convert";
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(exception = {Error.class}, produces = {"text/html"})
	public String exceptionHandler(Error error, Model model){
		this.requestData.setErrorMessage(ExceptionManager.getStackTrace(error));
		error.printStackTrace();
		model.addAllAttributes(convertFormData());
		return "convert";
	}



}
