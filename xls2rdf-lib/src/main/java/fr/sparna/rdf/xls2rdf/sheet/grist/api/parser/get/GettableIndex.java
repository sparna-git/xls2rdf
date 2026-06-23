package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;

public interface GettableIndex extends Gettable {

    JsonNode getNodeFromIndex(int entityIndex);

    JsonNode getFieldsFromIndex(int entityIndex);

    Iterator<String> getFieldNamesIteratorFromIndex(int entityIndex);

    JsonNode getValueFieldFromIndexAndName(int entityIndex, String fieldName);

    JsonNode getIdNode(int index);

}
