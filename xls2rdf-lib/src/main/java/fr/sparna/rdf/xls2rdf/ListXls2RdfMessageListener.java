package fr.sparna.rdf.xls2rdf;

import java.util.ArrayList;
import java.util.List;

public class ListXls2RdfMessageListener implements Xls2RdfMessageListenerIfc {

	private List<Message> messages = new ArrayList<>();

	@Override
	public void onMessage(MessageCode code, String cellReference, String message) {
		messages.add(new Message(code, cellReference, message));
	}
	
	public List<Message> getMessages() {
		return messages;
	}

	public class Message {
		MessageCode code;
		String cellReference;
		String message;
		public Message(MessageCode code, String cellReference, String message) {
			super();
			this.code = code;
			this.cellReference = cellReference;
			this.message = message;
		}
		public MessageCode getCode() {
			return code;
		}
		public String getCellReference() {
			return cellReference;
		}
		public String getMessage() {
			return message;
		}
	}
	
}
