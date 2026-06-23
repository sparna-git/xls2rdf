package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.column.GristColumns;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.document.GristDocument;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.record.GristRecords;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.table.GristTables;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.workspace.GristWorkspace;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristParserFactory;

public class GristEntityFactory {

    private GristEntityFactory(){}

    public static GristWorkspace getWorkspace(JsonNode workspaceNode){
        return new GristWorkspace(GristParserFactory.createGristGetWorkspace(workspaceNode));
    }

    public static GristDocument getDocument(JsonNode documentNode){
        return new GristDocument(GristParserFactory.createGristGetDocument(documentNode));
    }

    public static GristTables getTables(JsonNode tablesNode){
        return new GristTables(GristParserFactory.createGristGetTables(tablesNode));
    }

    public static GristRecords getRecords(JsonNode recordsNode){
        return new GristRecords(GristParserFactory.createGristGetRecords(recordsNode));
    }

    public static GristColumns getColumns(JsonNode columnsNode){
        return new GristColumns(GristParserFactory.createGristGetColumns(columnsNode));
    }

}
