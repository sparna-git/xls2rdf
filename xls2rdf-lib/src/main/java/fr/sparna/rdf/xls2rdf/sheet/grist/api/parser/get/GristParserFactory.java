package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

public class GristParserFactory {

    private GristParserFactory(){}

    public static GristWorkspaceParser createGristGetWorkspace(JsonNode workspaceNode) {
         return new GristWorkspaceParser(workspaceNode);
    }

    public static GristDocumentParser createGristGetDocument(JsonNode documentNode){
         return new GristDocumentParser(documentNode);
    }

    public static GristTablesParser createGristGetTables(JsonNode tablesNode){
         return new GristTablesParser(tablesNode);
    }

    public static GristRecordsParser createGristGetRecords(JsonNode recordsNode){
        return new GristRecordsParser(recordsNode);
    }

    public static GristColumnsParser createGristGetColumns(JsonNode columnsNode){
        return new GristColumnsParser(columnsNode);
    }

}
