package fr.sparna.rdf.xls2rdf;

public interface Xls2RdfMessageListenerIfc {
	
	enum MessageCode {
		INVALID_PROPERTY,
		WRONG_FORMAT,
		UNABLE_TO_RECONCILE_VALUE,
		UNABLE_TO_PARSE_SHACL_PATH
	}
	
	public void onMessage(MessageCode code, String cellReference, String message);
	
}
