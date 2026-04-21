package fr.sparna.rdf.xls2rdf.web;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.web.form.convert.ConvertFormModelKey;
import fr.sparna.rdf.xls2rdf.web.form.convert.Xls2RdfConvertException;
import jakarta.servlet.http.HttpServletResponse;


/**
 * @author Arthur Leroux
 */
@ControllerAdvice
public class Xls2RdfControllerAdvice {

    private RequestData requestData;
	private final ApplicationData applicationData;
	private SessionData sessionData;


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
	
    //Préparation du modèle pour les end-points
    @ModelAttribute
	public void prepareConvertModel(Model model){
		model.addAllAttributes(convertFormData());
	}


	//Gestion des expcetions selon le type
	//Gérer les exceptions pour Xls2RdfConvertException
	@ExceptionHandler(exception={Xls2RdfConvertException.class})
	public String xls2RdfConvertExceptionHandler(Xls2RdfConvertException ex, Model model){
		this.requestData.setErrorMessage(ex.getMessage());
		model.addAllAttributes(convertFormData());
		return "convert";
	}

	//Gérer les exceptions pour convertForm
	//Gérer les exceptions pour Xls2RdfException
	@ExceptionHandler(exception={Xls2RdfException.class})
	public String xls2RdfConvertExceptionHandler(Xls2RdfException ex, RedirectAttributes redirect){
		redirect.addFlashAttribute(ConvertFormModelKey.ERROR.getKey(), ex.getMessage());
		return "redirect:/convert";
	}

	
    
}
