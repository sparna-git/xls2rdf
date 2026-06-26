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
        JsonNode node = this.getNodeFromIndex(index);
        if (node == null) return null;
        return node.get(TABLE_NAME);
    }

    @Override
    public JsonNode getValueFieldFromIndexAndName(int entityIndex, String fieldName) {
        JsonNode node = this.getFieldsFromIndex(entityIndex);
        if (node == null) return null;
        return node.get(fieldName);
    }

    @Override
    public JsonNode getTopNode() {
        JsonNode node = this.getRootNode();
        if (node == null) return null;
        return node.get(TABLES_ID);
    }

    @Override
    public JsonNode getNodeFromIndex(int entityIndex) {
        JsonNode node = this.getTopNode();
        if (node == null) return null;
        return node.get(entityIndex);
    }

    @Override
    public JsonNode getFieldsFromIndex(int entityIndex) {
        JsonNode node = this.getNodeFromIndex(entityIndex);
        if (node == null) return null;
        return node.get(FIELDS_ID);
    }

    @Override
    public Iterator<String> getFieldNamesIteratorFromIndex(int entityIndex) {
        JsonNode node = this.getFieldsFromIndex(entityIndex);
        if (node == null) return null;
        return node.fieldNames();
    }

    @Override
    public JsonNode getNodeFromName(String entityName) {
        JsonNode node = this.getTopNode();
        if (node == null) return null;

        for (JsonNode n : node) {
            if (n.get(TABLE_NAME).asText().equals(entityName)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public JsonNode getFieldsFromName(String entityName) {
        JsonNode node = this.getNodeFromName(entityName);
        if (node == null) return null;
        return node.get(FIELDS_ID);
    }

    @Override
    public Iterator<String> getFieldNamesIteratorFromName(String entityName) {
        JsonNode node = this.getFieldsFromName(entityName);
        if (node == null) return null;
        return node.fieldNames();
    }

    @Override
    public JsonNode getValueFieldFromName(String entityName, String fieldName) {
        JsonNode node = this.getFieldsFromName(entityName);
        if (node == null) return null;
        return node.get(fieldName);
    }

    @Override
    public int getTablesSizes() {
        JsonNode node = this.getTopNode();
        if (node == null) return 0;
        return node.size();
    }
}
