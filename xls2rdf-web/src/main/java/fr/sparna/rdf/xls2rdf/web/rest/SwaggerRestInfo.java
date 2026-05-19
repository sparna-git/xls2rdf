package fr.sparna.rdf.xls2rdf.web.rest;


import fr.sparna.rdf.xls2rdf.web.exception.RestExceptionRendererer;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "The file is properly converted.", content = {
				@Content(mediaType = "text/turtle", examples = {
						@ExampleObject(value = """
								@prefix schema: <http://schema.org/> .
								@prefix adms: <http://www.w3.org/ns/adms#> .
								@prefix owl: <http://www.w3.org/2002/07/owl#> .
								@prefix org: <http://www.w3.org/ns/org#> .
								@prefix xls2rdf: <https://xls2rdf.sparna.fr/vocabulary#> .
								@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
								@prefix skosthes: <http://purl.org/iso25964/skos-thes#> .
								@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
								@prefix qb: <http://purl.org/linked-data/cube#> .
								@prefix dct: <http://purl.org/dc/terms/> .
								@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
								@prefix doap: <http://usefulinc.com/ns/doap#> .
								@prefix sh: <http://www.w3.org/ns/shacl#> .
								@prefix dcat: <http://www.w3.org/ns/dcat#> .
								@prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
								@prefix prov: <http://www.w3.org/ns/prov#> .
								@prefix foaf: <http://xmlns.com/foaf/0.1/> .
								@prefix dc: <http://purl.org/dc/elements/1.1/> .
								@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
								@prefix skosxl: <http://www.w3.org/2008/05/skos-xl#> .
								
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> a skos:Concept;
								  skos:prefLabel "Test"@fr;
								  skos:altLabel "toto"@fr;
								  skos:inScheme <http://labs.sparna.fr/skos-play/convert/test>;
								  skos:topConceptOf <http://labs.sparna.fr/skos-play/convert/test> .
								
								<http://labs.sparna.fr/skos-play/convert/test> a skos:ConceptScheme;
								  skos:hasTopConcept <http://labs.sparna.fr/skos-play/convert/test/concept_1> .
								""")
				}),
				@Content(mediaType = "application/rdf+xml",examples = {
						@ExampleObject(value = """
								<?xml version="1.0" encoding="UTF-8"?>
								<rdf:RDF
									xmlns:schema="http://schema.org/"
									xmlns:adms="http://www.w3.org/ns/adms#"
									xmlns:owl="http://www.w3.org/2002/07/owl#"
									xmlns:org="http://www.w3.org/ns/org#"
									xmlns:xls2rdf="https://xls2rdf.sparna.fr/vocabulary#"
									xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
									xmlns:skosthes="http://purl.org/iso25964/skos-thes#"
									xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
									xmlns:qb="http://purl.org/linked-data/cube#"
									xmlns:dct="http://purl.org/dc/terms/"
									xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
									xmlns:doap="http://usefulinc.com/ns/doap#"
									xmlns:sh="http://www.w3.org/ns/shacl#"
									xmlns:dcat="http://www.w3.org/ns/dcat#"
									xmlns:euvoc="http://publications.europa.eu/ontology/euvoc#"
									xmlns:prov="http://www.w3.org/ns/prov#"
									xmlns:foaf="http://xmlns.com/foaf/0.1/"
									xmlns:dc="http://purl.org/dc/elements/1.1/"
									xmlns:skos="http://www.w3.org/2004/02/skos/core#"
									xmlns:skosxl="http://www.w3.org/2008/05/skos-xl#">
								
								<rdf:Description rdf:about="http://labs.sparna.fr/skos-play/convert/test/concept_1">
									<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
									<skos:prefLabel xml:lang="fr">Test</skos:prefLabel>
									<skos:altLabel xml:lang="fr">toto</skos:altLabel>
									<skos:inScheme rdf:resource="http://labs.sparna.fr/skos-play/convert/test"/>
									<skos:topConceptOf rdf:resource="http://labs.sparna.fr/skos-play/convert/test"/>
								</rdf:Description>
								
								<rdf:Description rdf:about="http://labs.sparna.fr/skos-play/convert/test">
									<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#ConceptScheme"/>
									<skos:hasTopConcept rdf:resource="http://labs.sparna.fr/skos-play/convert/test/concept_1"/>
								</rdf:Description>
								</rdf:RDF>
								""")
				}),
				@Content(mediaType = "application/n-triples", examples = {
						@ExampleObject(value = """
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#prefLabel> "Test"@fr .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#altLabel> "toto"@fr .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#inScheme> <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#topConceptOf> <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#ConceptScheme> .
								<http://labs.sparna.fr/skos-play/convert/test> <http://www.w3.org/2004/02/skos/core#hasTopConcept> <http://labs.sparna.fr/skos-play/convert/test/concept_1> .
								""")
				}),
				@Content(mediaType = "application/n-quads", examples = {
						@ExampleObject(value = """
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#prefLabel> "Test"@fr <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#altLabel> "toto"@fr <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#inScheme> <http://labs.sparna.fr/skos-play/convert/test> <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://www.w3.org/2004/02/skos/core#topConceptOf> <http://labs.sparna.fr/skos-play/convert/test> <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#ConceptScheme> <http://labs.sparna.fr/skos-play/convert/test> .
								<http://labs.sparna.fr/skos-play/convert/test> <http://www.w3.org/2004/02/skos/core#hasTopConcept> <http://labs.sparna.fr/skos-play/convert/test/concept_1> <http://labs.sparna.fr/skos-play/convert/test> .							
								""")
				}),
				@Content(mediaType = "text/n3", examples = {
						@ExampleObject(value = """
								@prefix schema: <http://schema.org/> .
								@prefix adms: <http://www.w3.org/ns/adms#> .
								@prefix owl: <http://www.w3.org/2002/07/owl#> .
								@prefix org: <http://www.w3.org/ns/org#> .
								@prefix xls2rdf: <https://xls2rdf.sparna.fr/vocabulary#> .
								@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
								@prefix skosthes: <http://purl.org/iso25964/skos-thes#> .
								@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
								@prefix qb: <http://purl.org/linked-data/cube#> .
								@prefix dct: <http://purl.org/dc/terms/> .
								@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
								@prefix doap: <http://usefulinc.com/ns/doap#> .
								@prefix sh: <http://www.w3.org/ns/shacl#> .
								@prefix dcat: <http://www.w3.org/ns/dcat#> .
								@prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
								@prefix prov: <http://www.w3.org/ns/prov#> .
								@prefix foaf: <http://xmlns.com/foaf/0.1/> .
								@prefix dc: <http://purl.org/dc/elements/1.1/> .
								@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
								@prefix skosxl: <http://www.w3.org/2008/05/skos-xl#> .
								
								<http://labs.sparna.fr/skos-play/convert/test/concept_1> a skos:Concept;
								  skos:prefLabel "Test"@fr;
								  skos:altLabel "toto"@fr;
								  skos:inScheme <http://labs.sparna.fr/skos-play/convert/test>;
								  skos:topConceptOf <http://labs.sparna.fr/skos-play/convert/test> .
								
								<http://labs.sparna.fr/skos-play/convert/test> a skos:ConceptScheme;
								  skos:hasTopConcept <http://labs.sparna.fr/skos-play/convert/test/concept_1> .
								""")
				}),
				@Content(mediaType = "application/trig", examples = {
						@ExampleObject(value = """
								@prefix schema: <http://schema.org/> .
								@prefix adms: <http://www.w3.org/ns/adms#> .
								@prefix owl: <http://www.w3.org/2002/07/owl#> .
								@prefix org: <http://www.w3.org/ns/org#> .
								@prefix xls2rdf: <https://xls2rdf.sparna.fr/vocabulary#> .
								@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
								@prefix skosthes: <http://purl.org/iso25964/skos-thes#> .
								@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
								@prefix qb: <http://purl.org/linked-data/cube#> .
								@prefix dct: <http://purl.org/dc/terms/> .
								@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
								@prefix doap: <http://usefulinc.com/ns/doap#> .
								@prefix sh: <http://www.w3.org/ns/shacl#> .
								@prefix dcat: <http://www.w3.org/ns/dcat#> .
								@prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
								@prefix prov: <http://www.w3.org/ns/prov#> .
								@prefix foaf: <http://xmlns.com/foaf/0.1/> .
								@prefix dc: <http://purl.org/dc/elements/1.1/> .
								@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
								@prefix skosxl: <http://www.w3.org/2008/05/skos-xl#> .
								
								<http://labs.sparna.fr/skos-play/convert/test> {
								  <http://labs.sparna.fr/skos-play/convert/test/concept_1> a skos:Concept;
								    skos:prefLabel "Test"@fr;
								    skos:altLabel "toto"@fr;
								    skos:inScheme <http://labs.sparna.fr/skos-play/convert/test>;
								    skos:topConceptOf <http://labs.sparna.fr/skos-play/convert/test> .
								
								  <http://labs.sparna.fr/skos-play/convert/test> a skos:ConceptScheme;
								    skos:hasTopConcept <http://labs.sparna.fr/skos-play/convert/test/concept_1> .
								}
								""")
				}),
		}),
        @ApiResponse(responseCode = "400", description = "The url isn't supported for conversion or is empty.", content = {@Content(mediaType = "application/json",
				schema = @Schema(implementation = RestExceptionRendererer.class),
				examples = {
				@ExampleObject(value = "{\"nameException\":\"fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfRestControllerException\",\"message\":\"Url is not valid for conversion!\",\"statusCode\":\"BAD_REQUEST\",\"dateTime\":\"2026-05-11T13:28:45.367592\"}")
		})}),
        @ApiResponse(responseCode = "500", description = "Internal server error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RestExceptionRendererer.class))})
})
@Parameters({
        @Parameter(name = "lang", description = "Default language to apply to literal columns.", in = ParameterIn.QUERY, schema = @Schema(allowableValues = {"fr", "es", "ru", "de", "it"})),
        @Parameter(name = "noPostProcessings", description = "Don't apply SKOS post-processings after conversion. Set this to true only if you are explicitely generating SKOS taxonomies.", in = ParameterIn.QUERY),
        @Parameter(name = "broaderTransitive", description = "Adds skos:broaderTransitive explicit links.", in = ParameterIn.QUERY),
        @Parameter(name = "format", description = "Mime type of the RDF output format (e.g. \"application/rdf+xml\"). Turtle is returned by default", in = ParameterIn.QUERY, schema = @Schema(allowableValues = {"text/turtle",
				"application/rdf+xml",
				"application/n-triples",
				"application/n-quads",
				"text/n3",
				"application/trig"})),
        @Parameter(name = "skosxl", description = "Apply SKOS-XL post-processings to reify labels.", in = ParameterIn.QUERY),
        @Parameter(name = "skipHidden", description = "Default language to apply to literal columns.", in = ParameterIn.QUERY),
})
public @interface SwaggerRestInfo {

}
