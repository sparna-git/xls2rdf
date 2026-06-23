package fr.sparna.rdf.xls2rdf.web.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * <p>Permet d'ajouter des messages d'exception, de rajouter et lever les exceptions
 * grâce à {@link ExceptionManager#throwException} </p>
 */
public enum ExceptionManager {


    FILE_MISSING("Uploaded link file is empty!"),
    URL_MISSING("Url is missing!"),
    INVALID_URL("Url is not valid for conversion!"),
    INVALID_FILE_FORMAT("File format is not supported!"),
    IO_EXCEPTION("Input/Outputstream error! Please try again."),
    GENERIC("Something went wrong while processing your request! Please try again."),
    NULL_POINTER("Null pointer exception."),
    TEST("TEST");


    private final String message;

    public String getMessage(){
        return this.message;
    }    
    
    ExceptionManager(String message){
        this.message = message;
    }

    public static void throwException(Class<? extends Exception> klass, String msg){
			if(Xls2RdfConvertException.class == klass) {
                Xls2RdfConvertException e = new Xls2RdfConvertException(msg);
                e.printStackTrace();
                throw e;
            }
            if(Xls2RdfRestControllerException.class == klass) {
                Xls2RdfRestControllerException e = new Xls2RdfRestControllerException(msg);
                e.printStackTrace();
                throw e;
            }
            //Add other exceptions here ...
	}

    public static String getStackTrace(Throwable t){
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public static ResponseEntity<RestExceptionRendererer> prepareRestExceptionRenderer(Throwable t, HttpStatus status, LocalDateTime dateTime){
        return new ResponseEntity<>(
                new RestExceptionRendererer(t.getClass().getName(), t.getMessage(), status, dateTime, ExceptionManager.getStackTrace(t)), status
        );
    }

    
}
