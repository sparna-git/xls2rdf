package fr.sparna.rdf.xls2rdf.web.exception;


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
    
    private ExceptionManager(String message){
        this.message = message;
    }

    public static void throwException(Class<? extends Exception> klass, String msg){
			if(Xls2RdfConvertException.class == klass) {
                Xls2RdfConvertException e = new Xls2RdfConvertException(msg);
                printStackTrace(e);
                throw e;
            }
            if(Xls2RdfRestControllerException.class == klass) {
                Xls2RdfRestControllerException e = new Xls2RdfRestControllerException(msg);
                printStackTrace(e);
                throw e;
            }
            //Add other exceptions here ...
	}
    
    public static void printStackTrace(Throwable ex){
        ex.printStackTrace();
    }

    
}
