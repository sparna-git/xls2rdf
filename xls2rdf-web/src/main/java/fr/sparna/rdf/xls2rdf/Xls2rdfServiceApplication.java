package fr.sparna.rdf.xls2rdf;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@PropertySources({
	@PropertySource("classpath:version.properties"),
})
@SpringBootApplication
public class Xls2rdfServiceApplication extends SpringBootServletInitializer {
	
	//// Pour lancer l'application en dehors d'un conteneur à servlet
	public static void main(String[] args) {
		SpringApplication.run(Xls2rdfServiceApplication.class, args);
	}

	//// Pour lancer l'application dans un conteneur à serlvet
	@Override
	public SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Xls2rdfServiceApplication.class);
	}
}
