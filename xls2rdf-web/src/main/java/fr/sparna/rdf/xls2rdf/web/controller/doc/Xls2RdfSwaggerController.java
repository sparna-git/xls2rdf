package fr.sparna.rdf.xls2rdf.web.controller.doc;


import fr.sparna.rdf.xls2rdf.web.utils.ConvertFormModelKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Xls2RdfSwaggerController {

    final static Logger log = LoggerFactory.getLogger(Xls2RdfSwaggerController.class);

    @GetMapping("/api-doc")
    public String swagger(Model model){
        model.addAttribute(ConvertFormModelKey.VIEW.getKey(), ConvertFormModelKey.API_DOC.getKey());
        return "api-doc";
    }

}
