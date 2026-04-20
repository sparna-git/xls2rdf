package fr.sparna.rdf.xls2rdf.web;

import java.io.Serializable;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@SessionScope
@Component
public class SessionData implements Serializable {

	static final long serialVersionUID = -1;
	
	// The user Locale
	protected Locale userLocale;
	protected String baseUrl;

	public Locale getUserLocale() {
		return userLocale;
	}

	public void setUserLocale(Locale userLocale) {
		this.userLocale = userLocale;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}	
	
}
