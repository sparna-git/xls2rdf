package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractGristParser {

    private final ObjectMapper mapper;
    private JsonNode rootNode;

    protected AbstractGristParser(JsonNode rootNode){
        this.rootNode = rootNode;
        this.mapper = new ObjectMapper();
    }

    protected AbstractGristParser(){
        this.mapper = new ObjectMapper();
    }

    public ObjectMapper getMapper(){
        return this.mapper;
    }

    public JsonNode getRootNode(){
        return this.rootNode;
    }

    public void setRootNode(JsonNode rootNode){
        this.rootNode = rootNode;
    }

}
