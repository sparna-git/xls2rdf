package fr.sparna.rdf.xls2rdf.web.controller.doc;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.sparna.rdf.xls2rdf.web.form.convert.ConvertFormModelKey;

@Controller
public class Xls2RdfDocController {


    @GetMapping(value = "/doc")
    public String doc(Model model){
        model.addAttribute(ConvertFormModelKey.VIEW_NAME.getKey(), ConvertFormModelKey.VIEW_NAME.getKey());
        return "doc";
    }
    
}
