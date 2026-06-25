package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;

public class GristTablesParser extends AbstractGristParser implements GettableTables {

    public static final String TABLES_ID   = "tables";
    public static final String TABLE_NAME  = "id";
    public static final String FIELDS_ID   = "fields";

    public GristTablesParser(JsonNode rootNode) {
        super(rootNode);
    }

    public GristTablesParser(){
        super();
    }

    //GET METHODS
    @Override
    public JsonNode getIdNode(int index) {
        return this.getNodeFromIndex(index).get(TABLE_NAME);
    }

    @Override
    public JsonNode getValueFieldFromIndexAndName(int entityIndex, String fieldName) {
        return this.getFieldsFromIndex(entityIndex).get(fieldName);
    }

    @Override
    public JsonNode getTopNode() {
        return this.getRootNode().get(TABLES_ID);
    }

    @Override
    public JsonNode getNodeFromIndex(int entityIndex) {
        return this.getTopNode().get(entityIndex);
    }

    @Override
    public JsonNode getFieldsFromIndex(int entityIndex) {
        return this.getNodeFromIndex(entityIndex).get(FIELDS_ID);
    }

    @Override
    public Iterator<String> getFieldNamesIteratorFromIndex(int entityIndex) {
        return this.getFieldsFromIndex(entityIndex).fieldNames();
    }

    @Override
    public JsonNode getNodeFromName(String entityName) {
        for(JsonNode n : this.getTopNode()){
            if(n.get(TABLE_NAME).asText().equals(entityName)) return n;
        }
        return null;
    }

    @Override
    public JsonNode getFieldsFromName(String entityName) {
        return this.getNodeFromName(entityName).get(FIELDS_ID);
    }

    @Override
    public Iterator<String> getFieldNamesIteratorFromName(String entityName) {
        return this.getFieldsFromName(entityName).fieldNames();
    }

    @Override
    public JsonNode getValueFieldFromName(String entityName, String fieldName) {
        return this.getFieldsFromName(entityName).get(fieldName);
    }

    @Override
    public int getTablesSizes() {
        return this.getTopNode().size();
    }

}
