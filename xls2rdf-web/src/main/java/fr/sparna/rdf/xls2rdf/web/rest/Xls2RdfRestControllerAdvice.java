package fr.sparna.rdf.xls2rdf.web.rest;

import fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.RestExceptionRendererer;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfRestControllerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class Xls2RdfRestControllerAdvice {

    @ExceptionHandler(exception = {Xls2RdfRestControllerException.class}, produces = {"application/json"})
    public ResponseEntity<RestExceptionRendererer> xls2RdfRestControllerExceptionHandler(Xls2RdfRestControllerException ex) {
        return ExceptionManager.prepareRestExceptionRenderer(ex, HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }

    //Permet de catch toutes les erreurs non attrapées par le serveur / spring ou autre.
    @ExceptionHandler(exception = {Exception.class}, produces = {"application/json"})
    public ResponseEntity<RestExceptionRendererer> exceptionHandler(Exception ex) {
        return ExceptionManager.prepareRestExceptionRenderer(ex, HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now());
    }

}