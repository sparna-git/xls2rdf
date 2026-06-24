package fr.sparna.rdf.xls2rdf.sheet.grist.api.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface Client {

    JsonNode getWorkspace(int workspaceId) throws IOException, InterruptedException;

    JsonNode getDocument(String documentId) throws IOException, InterruptedException;

    JsonNode getTables(String documentId) throws IOException, InterruptedException;

    JsonNode getRecords(String documentId, String tableId) throws IOException, InterruptedException;

    JsonNode getColumns(String documentId, String tableId) throws IOException, InterruptedException;

}
