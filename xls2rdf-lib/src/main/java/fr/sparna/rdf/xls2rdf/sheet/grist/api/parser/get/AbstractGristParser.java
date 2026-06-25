package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractGristParser {

    private JsonNode rootNode;

    protected AbstractGristParser(JsonNode rootNode){
        this.rootNode = rootNode;

    }

    protected AbstractGristParser(){
    }

    public JsonNode getRootNode(){
        return this.rootNode;
    }

    public void setRootNode(JsonNode rootNode){
        this.rootNode = rootNode;
    }

}
