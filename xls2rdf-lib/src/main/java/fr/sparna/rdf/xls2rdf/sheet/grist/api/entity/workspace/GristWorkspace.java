package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.workspace;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.AbstractGristEntity;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristWorkspaceParser;

import java.time.LocalDateTime;
import java.util.Objects;

public class GristWorkspace extends AbstractGristEntity<GristWorkspaceParser> {

    //CONSTRUCTORS
    public GristWorkspace(GristWorkspaceParser parser) {
        super(parser);
    }

    //GETTERS SETTERS
    public void setRootNode(JsonNode rootNode){
        this.getParser().setRootNode(rootNode);
    }

    //METHODS
    public int getWorkspaceId(){
        return this.getParser().getIdNode().asInt();
    }

    public String getWorkspaceName(){
        return this.getParser().getNameNode().asText();
    }

    public LocalDateTime getCreateAt(){
        return LocalDateTime.parse(this.getParser().getCreateAtNode().asText());
    }

    public LocalDateTime getUpdateAt(){
        return LocalDateTime.parse(this.getParser().getUpdateAtNode().asText());
    }

    //OVERRIDE
    @Override
    public int hashCode() {
        return Objects.hashCode(this.getParser());
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj) return true;
        else if(obj == null) return false;
        else if(!(obj instanceof GristWorkspace)) return false;

        GristWorkspace o = (GristWorkspace) obj;

        return Objects.equals(this.getParser(), o.getParser());
    }

}
