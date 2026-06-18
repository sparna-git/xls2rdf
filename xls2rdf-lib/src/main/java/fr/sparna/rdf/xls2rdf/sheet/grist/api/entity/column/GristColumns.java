package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.column;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.AbstractGristEntity;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.table.GristTables;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristColumnsParser;

import java.util.Objects;

public class GristColumns extends AbstractGristEntity<GristColumnsParser> {

    //PROPERTIES
    private GristTables gristTables;

    //CONSTRUCTORS
    public GristColumns(GristTables gristTable, GristColumnsParser parser) {
        super(parser);
        this.gristTables = gristTable;
    }

    public GristColumns(GristColumnsParser parser) {
        super(parser);
    }

    //GETTERS SETTERS
    public GristTables getGristTable() {
        return gristTables;
    }

    public void setRootNode(JsonNode rootNode){
        this.getParser().setRootNode(rootNode);
    }

    public JsonNode getColumnName(int columnIndex) {
        return this.getParser().getIdNode(columnIndex);
    }

    public JsonNode getColumnFromIndex(int columnIndex) {
        return this.getParser().getNodeFromIndex(columnIndex);
    }

    public JsonNode getMetadatas(String columnName){
        return this.getParser().getFieldsFromName(columnName);
    }

    public JsonNode getMetadatas(int columnIndex){
        return this.getParser().getFieldsFromIndex(columnIndex);
    }

    public JsonNode getMetadata(String columnName, String fieldName){
        return this.getParser().getValueFieldFromName(columnName, fieldName);
    }

    public JsonNode getMetadata(int columnIndex, String fieldName){
        return this.getParser().getValueFieldFromIndexAndName(columnIndex, fieldName);
    }

    //OVERRIDE
    @Override
    public int hashCode() {
        return Objects.hash(this.gristTables, this.getParser());
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj) return true;
        else if(obj == null) return false;
        else if(!(obj instanceof GristColumns)) return false;

        GristColumns o = (GristColumns) obj;

        return Objects.equals(this.gristTables, o.gristTables) && Objects.equals(this.getParser(), o.getParser());
    }
}
