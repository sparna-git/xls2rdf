package fr.sparna.rdf.xls2rdf.container;

/**
 * A FacadeX-like literal interface to represent a leaf node in a hierarchical structure of nodes, where each literal has a text value.
 * See https://sparql-anything.readthedocs.io/stable/Facade-X/
 */
public interface Literal extends Node {
    
    public String getText();

}
