package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.table;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.AbstractGristEntity;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.document.GristDocument;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristTablesParser;

import java.util.Objects;

public class GristTables extends AbstractGristEntity<GristTablesParser> {

    //PROPERTIES
    private GristDocument gristDocument;

    //CONSTRUCTORS
    public GristTables(GristDocument gristDocument, GristTablesParser parser){
        super(parser);
        this.gristDocument = gristDocument;
    }

    public GristTables(GristTablesParser parser){
        super(parser);
    }

    //GETTERS SETTERS
    public GristDocument getGristDocument() {
        return gristDocument;
    }

    public void setRootNode(JsonNode rootNode){
        this.getParser().setRootNode(rootNode);
    }


    //METHODS
    public JsonNode getTable(String tableName){
        return this.getParser().getNodeFromName(tableName);
    }

    public JsonNode getTable(int tableIndex){
        return this.getParser().getNodeFromIndex(tableIndex);
    }

    public String getTableName(int tableIndex){
        return this.getParser().getIdNode(tableIndex).asText();
    }

    public JsonNode getMetadatas(String tableName){
        return this.getParser().getFieldsFromName(tableName);
    }

    public JsonNode getMetadatas(int tableIndex){
        return this.getParser().getFieldsFromIndex(tableIndex);
    }

    public JsonNode getMetadata(String tableName, String fieldName){
        return this.getParser().getValueFieldFromName(tableName, fieldName);
    }

    public JsonNode getMetadata(int tableIndex, String fieldName){
        return this.getParser().getValueFieldFromIndexAndName(tableIndex, fieldName);
    }

    public int getTablesSize(){
        return this.getParser().getTablesSizes();
    }

    //OVERRIDE OBJECT
    @Override
    public int hashCode() {
        return Objects.hash(this.getParser(), this.gristDocument);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        else if(obj == null) return false;
        else if(!(obj instanceof GristTables)) return false;

        GristTables o = (GristTables) obj;

        return Objects.equals(this.getParser(), o.getParser()) && Objects.equals(this.gristDocument, o.gristDocument);
    }


}
