package fr.sparna.rdf.xls2rdf;


import fr.sparna.rdf.xls2rdf.web.ApplicationData;
import fr.sparna.rdf.xls2rdf.web.SwaggerUICustom;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@PropertySources({
	@PropertySource("classpath:version.properties"),
})
@SpringBootApplication
@OpenAPIDefinition(servers = {@io.swagger.v3.oas.annotations.servers.Server(url = "/")})
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

	@Bean
	public SwaggerIndexPageTransformer swaggerIndexPageTransformer(
			SwaggerUiConfigProperties a,
			SwaggerUiOAuthProperties b,
			SwaggerWelcomeCommon c,
			ObjectMapperProvider d
	){
		return new SwaggerUICustom(a,b,c,d);
	}

	/*
    Permet de customiser le rendu de la page swagger UNE FOIS la spécification faite par springdoc-open api
     */
	@Bean
	public OpenApiCustomizer customizer(ApplicationData data){
		return openApi -> {
			openApi.getInfo().setVersion(data.getBuildVersion());
		};
	}
}
