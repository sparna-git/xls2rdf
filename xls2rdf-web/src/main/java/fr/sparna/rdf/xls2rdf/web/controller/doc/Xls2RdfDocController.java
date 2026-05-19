package fr.sparna.rdf.xls2rdf.web.controller.doc;

import fr.sparna.rdf.xls2rdf.web.utils.ConvertFormModelKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Xls2RdfDocController {

    final static Logger log = LoggerFactory.getLogger(Xls2RdfDocController.class);

    @GetMapping(value = "/doc")
    public String doc(Model model){
        model.addAttribute(ConvertFormModelKey.VIEW.getKey(), ConvertFormModelKey.DOC.getKey());
        return "doc";
    }
    
}
