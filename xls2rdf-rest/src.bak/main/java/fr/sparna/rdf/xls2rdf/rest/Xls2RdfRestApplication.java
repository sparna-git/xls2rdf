package fr.sparna.rdf.xls2rdf.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
// @EnableDiscoveryClient
public class Xls2RdfRestApplication {
  public static void main(String[] args) {
    SpringApplication.run(Xls2RdfRestApplication.class, args);
  }
}
