package fr.sparna.rdf.xls2rdf.sheet.grist.api.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface Client {

    JsonNode getGristWorkspace(int workspaceId) throws IOException, InterruptedException;

    JsonNode getGristDocument(String documentId) throws IOException, InterruptedException;

    JsonNode getGristTables(String documentId) throws IOException, InterruptedException;

    JsonNode getGristRecords(String documentId, String tableId) throws IOException, InterruptedException;

    JsonNode getGristColumns(String documentId, String tableId) throws IOException, InterruptedException;

}
