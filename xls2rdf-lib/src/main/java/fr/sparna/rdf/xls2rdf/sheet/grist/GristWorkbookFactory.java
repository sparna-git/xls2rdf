package fr.sparna.rdf.xls2rdf.sheet.grist;

import fr.sparna.rdf.xls2rdf.sheet.grist.api.caller.GristCallerFactory;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.GristClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;

public class GristWorkbookFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(GristWorkbookFactory.class);

    private GristWorkbookFactory(){}

    public static GristWorkbook open(String documentId, String tokenAPI, boolean useCache){
        return new GristWorkbook(documentId, GristClient.getInstance(GristCallerFactory.createGristCaller(tokenAPI), useCache));
}

    public static GristWorkbook open(String documentId, String tokenAPI, URI baseAddress, boolean useCache){
        return new GristWorkbook(documentId, GristClient.getInstance(GristCallerFactory.createGristCaller(tokenAPI, baseAddress), useCache));
    }

    public static GristWorkbook open(String documentId, HttpClient client, String tokenAPI, boolean useCache){
        return new GristWorkbook(documentId, GristClient.getInstance(GristCallerFactory.createGristCaller(client, tokenAPI), useCache));
    }

    public static GristWorkbook open(String documentId, HttpClient client, String tokenAPI, URI baseAddress, boolean useCache){
        return new GristWorkbook(documentId, GristClient.getInstance(GristCallerFactory.createGristCaller(client, tokenAPI, baseAddress), useCache));
    }



}
