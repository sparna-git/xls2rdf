package fr.sparna.rdf.xls2rdf.sheet.grist.api.caller;


import fr.sparna.rdf.xls2rdf.sheet.grist.api.connector.GristConnector;

import java.net.URI;
import java.net.http.HttpClient;

public class GristCallerFactory {

    private GristCallerFactory(){}

    public static CallableGrist createGristCaller(String tokenAPI){
        GristConnector connector = new GristConnector(tokenAPI);
        return new GristCaller(HttpClient.newHttpClient(), connector);
    }

    public static CallableGrist createGristCaller(String tokenAPI, URI baseAddress){
        GristConnector connector = new GristConnector(tokenAPI, baseAddress);
        return new GristCaller(HttpClient.newHttpClient(), connector);
    }

    public static CallableGrist createGristCaller(HttpClient client, String tokenAPI){
        GristConnector connector = new GristConnector(tokenAPI);
        return new GristCaller(client, connector);
    }

    public static CallableGrist createGristCaller(HttpClient client, String tokenAPI, URI baseAddress){
        GristConnector connector = new GristConnector(tokenAPI, baseAddress);
        return new GristCaller(client, connector);
    }
}
