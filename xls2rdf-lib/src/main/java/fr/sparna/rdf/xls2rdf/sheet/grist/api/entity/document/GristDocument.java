package fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.document;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.AbstractGristEntity;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.workspace.GristWorkspace;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristDocumentParser;

import java.time.LocalDateTime;
import java.util.Objects;

public class GristDocument extends AbstractGristEntity<GristDocumentParser> {

    //PROPRIETES
    private GristWorkspace gristWorkspace;

    //CONSTRUCTORS
    public GristDocument(GristDocumentParser parser) {
        super(parser);
    }

    public GristDocument(GristWorkspace gristWorkspace, GristDocumentParser parser) {
        this(parser);
        this.gristWorkspace = gristWorkspace;
    }

    //GETTERS SETTERS
    public GristWorkspace getGristWorkspace() {
        return gristWorkspace;
    }

    public void setRootNode(JsonNode rootNode){
        this.getParser().setRootNode(rootNode);
    }

    public String getDocumentId(){
        return this.getParser().getIdNode().asText();
    }

    public String getDocumentName(){
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
        return Objects.hash(this.getParser());
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj) return true;
        else if(obj == null) return false;
        else if(!(obj instanceof GristDocument)) return false;

        GristDocument o = (GristDocument) obj;

        return Objects.equals(this.getParser(), o.getParser());
    }

}
