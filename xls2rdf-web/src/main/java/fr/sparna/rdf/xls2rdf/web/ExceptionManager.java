package fr.sparna.rdf.xls2rdf.web;

import fr.sparna.rdf.xls2rdf.web.exception.Xls2RdfConvertException;

/**
 * <p>Permet d'ajouter des messages d'exception, de rajouter et lever les exceptions
 * grâce à {@link ExceptionManager#throwException} </p>
 */
public enum ExceptionManager {


    FILE_MISSING("Uploaded link file is empty!"),
    URL_MISSING("Url is missing!"),
    INVALID_URL("Url is not valid for conversion!"),
    INVALID_FILE_FORMAT("File format is not supported!"),
    IO_EXCEPTION("Url is not valid for conversion or file format is not supported!"),
    GENRERIC("Something went wrong while processing your request! Please try again.");


    private final String message;

    public String getMessage(){
        return this.message;
    }    
    
    private ExceptionManager(String message){
        this.message = message;
    }


    
    public static void throwException(Class<? extends Exception> ex, ExceptionManager msg){
			if(Xls2RdfConvertException.class == ex) throw new Xls2RdfConvertException(msg.getMessage()); 
            //Add other exceptions here ...         
	}

    
}
