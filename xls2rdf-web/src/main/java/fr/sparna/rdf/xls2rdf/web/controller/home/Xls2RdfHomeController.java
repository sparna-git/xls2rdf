package fr.sparna.rdf.xls2rdf.web.controller.home;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>Controller for the home page of the application and the documentation page.</p>
 * @author Arthur Leroux
 */
@Controller
public class Xls2RdfHomeController {
    
    @GetMapping(value = "/")
    public String home(){
        return "index";
    }
}
