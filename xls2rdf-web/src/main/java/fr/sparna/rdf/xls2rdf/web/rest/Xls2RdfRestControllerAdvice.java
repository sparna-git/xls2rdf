package fr.sparna.rdf.xls2rdf.web.rest;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.web.exception.ExceptionManager;
import fr.sparna.rdf.xls2rdf.web.exception.RestExceptionRendererer;
import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfRestControllerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = {"fr.sparna.rdf.xls2rdf.web.rest"})
public class Xls2RdfRestControllerAdvice {

    @ExceptionHandler(exception = {Xls2RdfRestControllerException.class}, produces = {"application/json"})
    public ResponseEntity<RestExceptionRendererer> xls2RdfRestControllerExceptionHandler(Xls2RdfRestControllerException ex) {
        return this.prepareExceptionResponse(ex, HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }

    @ExceptionHandler(exception = {Xls2RdfException.class}, produces = {"application/json"})
    public ResponseEntity<RestExceptionRendererer> xls2RdfExceptionHandler(Xls2RdfException ex, Model model) {
        return this.prepareExceptionResponse(ex, HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }

    @ExceptionHandler(exception = {Exception.class}, produces = {"application/json"})
    public ResponseEntity<RestExceptionRendererer> exceptionHandler(Exception ex) {

        return this.prepareExceptionResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now());
    }

    @ExceptionHandler(exception = {Error.class}, produces = {"application/json"})
    public ResponseEntity<RestExceptionRendererer> exceptionHandler(Error ex) {
        return this.prepareExceptionResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now());
    }

    private ResponseEntity<RestExceptionRendererer> prepareExceptionResponse(Throwable t, HttpStatus status, LocalDateTime dateTime){
        return new ResponseEntity<>(
                new RestExceptionRendererer(t.getClass().getName(), t.getMessage(), status, dateTime, ExceptionManager.getStackTrace(t)), status
        );
    }
}