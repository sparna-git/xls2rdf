package fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get;

import com.fasterxml.jackson.databind.JsonNode;

public class GristWorkspaceParser extends AbstractGristParser implements GettableWorkspace {

    public static final String WORKSPACE_ID   = "id";
    public static final String WORKSPACE_NAME = "name";
    public static final String CREATE_AT      = "createAt";
    public static final String UPDATE_AT      = "updateAt";

    public GristWorkspaceParser(JsonNode rootNode) {
        super(rootNode);
    }

    public GristWorkspaceParser(){
        super();
    }

    //GET METHODS

    //GET METHODS
    @Override
    public JsonNode getNameNode() {
        JsonNode node = this.getTopNode();
        if (node == null) return null;
        return node.get(WORKSPACE_NAME);
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
        return node.get(WORKSPACE_ID);
    }

}

