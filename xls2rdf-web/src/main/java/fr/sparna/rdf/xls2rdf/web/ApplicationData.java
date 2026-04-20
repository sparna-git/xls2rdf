package fr.sparna.rdf.xls2rdf.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * Everything that needs to be loaded at an application-wide level
 * 
 * @author Thomas Francart
 */
@ApplicationScope
@Component
public class ApplicationData {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	protected String buildVersion;	
	protected String buildTimestamp;
	protected String applicationTitle;


	public ApplicationData() {
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	@Value("${build.version:unknown}")
	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	@Value("${build.timestamp:unknown}")
	public void setBuildTimestamp(String buildTimestamp) {
		this.buildTimestamp = buildTimestamp;
	}

    public String getApplicationTitle() {
        return applicationTitle;
    }

	@Value("${title:unknown}")
    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }
	
}
