package fr.sparna.rdf.xls2rdf.container;

public abstract class BaseContainer implements Container {
    
    private String id;
    private String type;
    private Container parent;

    public BaseContainer(String id, String type, Container parent) {
        this.id = id;
        this.type = type;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Container getParent() {
        return this.parent;
    }
    
}
