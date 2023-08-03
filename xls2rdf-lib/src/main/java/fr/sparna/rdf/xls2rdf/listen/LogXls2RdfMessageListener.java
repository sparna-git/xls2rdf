package fr.sparna.rdf.xls2rdf.listen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc.MessageCode;

public class LogXls2RdfMessageListener implements Xls2RdfMessageListenerIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public void onMessage(MessageCode code, String cellReference, String message) {
		log.info("Cell "+cellReference+" : " +code.name()+" - "+message);
	}
	
}
