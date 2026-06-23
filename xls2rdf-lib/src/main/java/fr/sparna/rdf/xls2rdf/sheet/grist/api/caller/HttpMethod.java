package fr.sparna.rdf.xls2rdf.sheet.grist.api.caller;

public enum HttpMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS");

    private final String verb;

    private HttpMethod(String verb) {
        this.verb = verb;
    }

    public String getMethod() {
        return this.verb;
    }


}
