package fr.sparna.rdf.xls2rdf.servlet.filter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sparna.rdf.xls2rdf.web.SessionData;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class SessionFilter implements Filter {


	private SessionData sessionData;
	
	@Autowired
	public SessionFilter(SessionData sessionData){
		this.sessionData = sessionData;
	}

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(
			ServletRequest request,
			ServletResponse response,
			FilterChain chain)
	throws IOException, ServletException {
		 // Check type request.
        if (request instanceof HttpServletRequest) {
            // Cast back to HttpServletRequest.
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Parse HttpServletRequest.
            HttpServletRequest parsedRequest = filterRequest(httpRequest);

            // Continue with filter chain.
            chain.doFilter(parsedRequest, response);
        } else {
            // Not a HttpServletRequest.
            chain.doFilter(request, response);
        }
		
	}

	private HttpServletRequest filterRequest(HttpServletRequest request) {

		if(this.sessionData.getUserLocale() == null || this.sessionData.getBaseUrl() == null) {
			log.debug("No session data present. Will create it.");
			//sessionData.store(session);
			this.sessionData.setUserLocale(request.getLocale());
	

			try {
				// init base URL
				URL baseURL = new URL("http"+((!request.getServerName().equals("localhost"))?"s":"")+"://"+request.getServerName()+((request.getServerPort() != 80)?":"+request.getServerPort():"")+request.getContextPath());
				log.debug("Setting the base URL to "+baseURL.toString());
				sessionData.setBaseUrl(baseURL.toString());		
			
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(request.getParameter("lang") != null) {
			log.debug("Detected 'lang' param. Will set a new user locale.");
			sessionData.setUserLocale(new Locale(request.getParameter("lang")));
		}
		return request;
	}

		
}
