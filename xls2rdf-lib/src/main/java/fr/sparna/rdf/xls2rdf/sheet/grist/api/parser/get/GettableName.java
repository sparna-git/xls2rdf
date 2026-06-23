package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;

public interface GettableName extends Gettable {

    JsonNode getNodeFromName(String entityName);

    JsonNode getFieldsFromName(String entityName);

    Iterator<String> getFieldNamesIteratorFromName(String entityName);

    JsonNode getValueFieldFromName(String entityName, String fieldName);

}
