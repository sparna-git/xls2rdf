package fr.sparna.rdf.xls2rdf.servlet;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import fr.sparna.rdf.xls2rdf.Xls2rdfServiceApplication;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Xls2rdfServiceApplication.class);
	}

}
