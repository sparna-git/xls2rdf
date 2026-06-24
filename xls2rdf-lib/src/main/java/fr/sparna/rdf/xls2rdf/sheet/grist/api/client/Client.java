package fr.sparna.rdf.xls2rdf.sheet.grist.api.client;

import com.fasterxml.jackson.databind.JsonNode;

public interface Client {

    JsonNode getWorkspace(int workspaceId);

    JsonNode getDocument(String documentId);

    JsonNode getTables(String documentId);

    JsonNode getRecords(String documentId, String tableId);

    JsonNode getColumns(String documentId, String tableId);

}
