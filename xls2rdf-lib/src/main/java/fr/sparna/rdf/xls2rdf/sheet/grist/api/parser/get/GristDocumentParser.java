package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

public class GristDocumentParser extends AbstractGristParser implements GettableDocument {

    public static final String DOCUMENT_ID   = "id";
    public static final String DOCUMENT_NAME = "name";
    public static final String CREATE_AT     = "createAt";
    public static final String UPDATE_AT     = "updateAt";

    public GristDocumentParser(JsonNode rootNode) {
        super(rootNode);
    }

    public GristDocumentParser(){
        super();
    }

    //GET METHODS
    @Override
    public JsonNode getNameNode() {
        return this.getTopNode().get(DOCUMENT_NAME);
    }

    @Override
    public JsonNode getCreateAtNode() {
        return this.getTopNode().get(CREATE_AT);
    }

    @Override
    public JsonNode getUpdateAtNode() {
        return this.getTopNode().get(UPDATE_AT);
    }

    @Override
    public JsonNode getTopNode() {
        return this.getRootNode();
    }

    @Override
    public JsonNode getIdNode() {
        return this.getTopNode().get(DOCUMENT_ID);
    }
}
