package fr.sparna.rdf.xls2rdf.web.controller.doc;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Xls2RdfDocController {


    private static final String VIEW_NAME = "doc";

    @GetMapping(value = "/doc")
    public String doc(Model model){
        model.addAttribute("view", VIEW_NAME);
        return "doc";
    }
    
}
