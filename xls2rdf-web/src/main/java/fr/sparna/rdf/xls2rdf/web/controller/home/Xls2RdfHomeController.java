package fr.sparna.rdf.xls2rdf.web.controller.home;


import fr.sparna.rdf.xls2rdf.web.utils.ConvertFormModelKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>Controller for the home page of the application and the documentation page.</p>
 * @author Arthur Leroux
 */
@Controller
public class Xls2RdfHomeController {

    final static Logger log = LoggerFactory.getLogger(Xls2RdfHomeController.class);
    
    @GetMapping(value = "/")
    public String home(Model model){
        model.addAttribute(ConvertFormModelKey.VIEW.getKey(), ConvertFormModelKey.API.getKey());
        return "index";
    }

}
