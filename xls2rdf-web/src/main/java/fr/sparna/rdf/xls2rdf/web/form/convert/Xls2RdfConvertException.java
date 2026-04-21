package fr.sparna.rdf.xls2rdf.web.form.convert;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;


/**
 * <p>Permet de gérer les exceptions pour convertForm</p>
 */
public class Xls2RdfConvertException extends Xls2RdfException{

    public Xls2RdfConvertException() {
	}

	public Xls2RdfConvertException(String message) {
		super(message);
	}

	public Xls2RdfConvertException(Throwable cause) {
		super(cause);
	}
    
}
