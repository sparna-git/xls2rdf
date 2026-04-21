package fr.sparna.rdf.xls2rdf.web.controller.doc;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Xls2RdfDocController {


    @GetMapping(value = "/doc")
    public String doc(){
        return "doc";
    }
    
}
