package fr.sparna.rdf.xls2rdf.container;

/**
 * A FacadeX-like value interface to represent a leaf node in a hierarchical structure of nodes, where each value has a text value.
 * See https://sparql-anything.readthedocs.io/stable/Facade-X/
 */
public interface Value extends ContainerOrValue {
    
    public String getText();

}
