package fr.sparna.rdf.xls2rdf.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Xls2rdfRestServiceApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(Xls2rdfRestServiceApplication.class, args);
	}

}
