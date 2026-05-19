package fr.sparna.rdf.xls2rdf.web.exception;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;

public class Xls2RdfRestControllerException extends Xls2RdfException {

    public Xls2RdfRestControllerException() {}

    public Xls2RdfRestControllerException(String message) {
        super(message);
    }

    public Xls2RdfRestControllerException(Throwable cause) {
        super(cause);
    }
}
