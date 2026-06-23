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
    @Override
    public JsonNode getNameNode() {
        return this.getTopNode().get(WORKSPACE_NAME);
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
        return this.getTopNode().get(WORKSPACE_ID);
    }

}
