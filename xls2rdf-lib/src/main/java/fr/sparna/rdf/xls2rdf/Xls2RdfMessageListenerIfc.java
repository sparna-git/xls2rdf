package fr.sparna.rdf.xls2rdf;

public interface Xls2RdfMessageListenerIfc {
	
	enum MessageCode {
		INVALID_PROPERTY,
		WRONG_FORMAT,
		UNABLE_TO_RECONCILE_VALUE
	}
	
	public void onMessage(MessageCode code, String cellReference, String message);
	
}
