package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.AbstractGristParser;

public abstract class AbstractGristEntity<R extends AbstractGristParser> {

    private final R parser;

    protected AbstractGristEntity(R parser){
        this.parser = parser;
    }

    public void setRootNode(JsonNode rootNode){
        this.parser.setRootNode(rootNode);
    }

    public R getParser(){
        return this.parser;
    }

}
