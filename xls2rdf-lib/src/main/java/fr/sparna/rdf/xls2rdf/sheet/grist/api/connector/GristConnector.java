package fr.sparna.rdf.xls2rdf.sheet.grist.api.connector;

import java.net.URI;

public class GristConnector {

    public static final URI GRIST_PERSONAL_URI = URI.create("https://docs.getgrist.com/api/");

    private final String tokenAPI;
    private URI baseAddress;

    public GristConnector(String tokenAPI) {
        this.tokenAPI = tokenAPI;
        this.baseAddress = GRIST_PERSONAL_URI;
    }

    public GristConnector(String APIToken, URI baseAddress){
        this(APIToken);
        if(baseAddress != null) this.baseAddress = baseAddress;
        else this.baseAddress = GRIST_PERSONAL_URI;
    }

    public URI getBaseAddress() {
        return baseAddress;
    }

    public String getTokenAPI() {
        return tokenAPI;
    }




}
