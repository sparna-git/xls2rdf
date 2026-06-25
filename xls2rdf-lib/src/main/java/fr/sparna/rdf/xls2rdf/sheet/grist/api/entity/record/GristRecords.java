package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.record;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.AbstractGristEntity;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.table.GristTables;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristRecordsParser;

import java.util.Iterator;
import java.util.Objects;

public class GristRecords extends AbstractGristEntity<GristRecordsParser> {

    //PROPERTIES
    private GristTables gristTables;

    //CONSTRUCTORS
    public GristRecords(GristTables gristTables, GristRecordsParser parser) {
        super(parser);
        this.gristTables = gristTables;
    }

    public GristRecords(GristRecordsParser parser) {
        super(parser);
    }

    //GETTERS SETTERS
    public GristTables getGristTables() {
        return this.gristTables;
    }

    public void setRootNode(JsonNode rootNode){
        this.getParser().setRootNode(rootNode);
    }

    public JsonNode getRecord(int recordIndex){
        return this.getParser().getNodeFromIndex(recordIndex - 1);
    }

    public JsonNode getCells(int recordIndex){
        return this.getParser().getFieldsFromIndex(recordIndex);
    }

    public JsonNode getCellValue(int recordIndex, String cellName){
        return this.getParser().getValueFieldFromIndexAndName(recordIndex, cellName);
    }

    public JsonNode getCellValue(String recordId, String cellName){
        return this.getParser().getValueFieldFromName(recordId, cellName);
    }

    public int getRecordsSize(){
        return this.getParser().getRecordsSize();
    }

    public Iterator<String> getColumnNames(int recordIndex){
        return this.getParser().getFieldNamesIteratorFromIndex(recordIndex);
    }

    public int getRecordId(int recordIndex){
        return this.getParser().getIdNode(recordIndex).asInt();
    }

    //OVERRIDE
    @Override
    public int hashCode() {
        return Objects.hash(this.getParser(), this.gristTables);
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj) return true;
        else if(obj == null) return false;
        else if(!(obj instanceof GristRecords)) return false;

        GristRecords o = (GristRecords) obj;

        return Objects.equals(this.getParser(), o.getParser()) && Objects.equals(this.gristTables, o.gristTables);
    }

}
