package fr.sparna.rdf.xls2rdf.web.controller.home;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.sparna.rdf.xls2rdf.web.form.convert.ConvertFormModelKey;

/**
 * <p>Controller for the home page of the application and the documentation page.</p>
 * @author Arthur Leroux
 */
@Controller
public class Xls2RdfHomeController {
    
    @GetMapping(value = "/")
    public String home(Model model){
        model.addAttribute(ConvertFormModelKey.VIEW_NAME.getKey(), ConvertFormModelKey.API.getKey());
        return "index";
    }
}
