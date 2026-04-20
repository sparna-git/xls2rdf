package fr.sparna.rdf.xls2rdf;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@PropertySources({
	@PropertySource("classpath:version.properties"),
})
@SpringBootApplication
public class Xls2rdfServiceApplication {
	
	// Pour lancer l'application en dehors d'un conteneur à servlet
	public static void main(String[] args) {
		SpringApplication.run(Xls2rdfServiceApplication.class, args);
	}
}
