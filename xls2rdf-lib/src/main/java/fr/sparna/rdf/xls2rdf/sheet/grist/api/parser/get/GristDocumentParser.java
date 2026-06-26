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
        JsonNode node = this.getTopNode();
        if (node == null) return null;
        return node.get(DOCUMENT_NAME);
    }

    @Override
    public JsonNode getCreateAtNode() {
        JsonNode node = this.getTopNode();
        if (node == null) return null;
        return node.get(CREATE_AT);
    }

    @Override
    public JsonNode getUpdateAtNode() {
        JsonNode node = this.getTopNode();
        if (node == null) return null;
        return node.get(UPDATE_AT);
    }

    @Override
    public JsonNode getTopNode() {
        JsonNode node = this.getRootNode();
        if (node == null) return null;
        return node;
    }

    @Override
    public JsonNode getIdNode() {
        JsonNode node = this.getTopNode();
        if (node == null) return null;
        return node.get(DOCUMENT_ID);
    }
}
