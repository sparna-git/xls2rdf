package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

public interface GettableWorkspace extends Gettable {

        JsonNode getNameNode();

        JsonNode getCreateAtNode();

        JsonNode getUpdateAtNode();

        JsonNode getIdNode();

}
