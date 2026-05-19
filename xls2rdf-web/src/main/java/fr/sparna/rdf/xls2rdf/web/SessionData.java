package fr.sparna.rdf.xls2rdf.web;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

@SessionScope
@Component
public class SessionData implements Serializable {

	@Serial
    private static final long serialVersionUID = -1;
	
	// The user Locale
	private Locale userLocale;
	private String baseUrl;

	public Locale getUserLocale() {
		return this.userLocale;
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
