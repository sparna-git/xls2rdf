package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;


public class GristRecordsParser extends AbstractGristParser implements GettableRecords {

    public final static String RECORDS_ID      = "records";
    public final static String RECORD_POSITION = "id";
    public static final String FIELDS_ID       = "fields";

    public GristRecordsParser(JsonNode rootNode) {
        super(rootNode);
    }

    public GristRecordsParser(){
        super();
    }

    //GET METHODS
    @Override
    public int getRecordsSize() {
        return this.getTopNode().size();
    }

    @Override
    public JsonNode getTopNode() {
        return this.getRootNode().get(RECORDS_ID);
    }

    @Override
    public JsonNode getIdNode(int index) {
        JsonNode node = this.getNodeFromIndex(index);
        if(node == null) return null;
        return node.get(RECORD_POSITION);
    }

    @Override
    public JsonNode getValueFieldFromIndexAndName(int entityIndex, String fieldName) {
        JsonNode node = this.getFieldsFromIndex(entityIndex);
        if(node == null) return null;
        return node.get(fieldName);
    }

    @Override
    public JsonNode getNodeFromIndex(int entityIndex) {
        return this.getTopNode().get(entityIndex);
    }

    @Override
    public JsonNode getFieldsFromIndex(int entityIndex) {
        JsonNode node = this.getNodeFromIndex(entityIndex);
        if(node == null) return null;
        return node.get(FIELDS_ID);
    }

    @Override
    public Iterator<String> getFieldNamesIteratorFromIndex(int index) {
        JsonNode node = this.getFieldsFromIndex(index);
        if(node == null) return null;
        return node.fieldNames();
    }

    @Override
    public JsonNode getNodeFromName(String entityName) {
        for(JsonNode n : this.getTopNode()){
            if(n.get(RECORD_POSITION).asText().equals(entityName)) return n;
        }
        return null;
    }

    @Override
    public JsonNode getFieldsFromName(String entityName) {
        JsonNode node = this.getNodeFromName(entityName);
        if(node == null) return null;
        return node.get(FIELDS_ID);
    }

    @Override
    public Iterator<String> getFieldNamesIteratorFromName(String entityName) {
        JsonNode node = this.getFieldsFromName(entityName);
        if(node == null) return null;
        return node.fieldNames();
    }

    @Override
    public JsonNode getValueFieldFromName(String entityName, String fieldName) {
        JsonNode node = this.getFieldsFromName(entityName);
        if(node == null) return null;
        return node.get(fieldName);
    }
}
