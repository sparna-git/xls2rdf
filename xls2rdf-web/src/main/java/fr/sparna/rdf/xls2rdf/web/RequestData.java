package fr.sparna.rdf.xls2rdf.web;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class RequestData {

	// error messages to display in alerts
	private String errorMessage;
	// default language of the user to initialize the language selection
	private String defaultLanguage;

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
	
}
