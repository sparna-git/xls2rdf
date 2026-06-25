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
        return this.getNodeFromIndex(index).get(RECORD_POSITION);
    }

    @Override
    public JsonNode getValueFieldFromIndexAndName(int entityIndex, String fieldName) {
        return this.getFieldsFromIndex(entityIndex).get(fieldName);
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
    public Iterator<String> getFieldNamesIteratorFromIndex(int index) {
        return this.getFieldsFromIndex(index).fieldNames();
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
}
