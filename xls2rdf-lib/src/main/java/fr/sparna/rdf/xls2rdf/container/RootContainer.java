package fr.sparna.rdf.xls2rdf.container;

import java.util.Map;

/**
 * A FacadeX-like root container interface to represent the top-level container in a hierarchical structure of nodes,
 * which can also contain global information such as prefixes and base IRI.
 */
public interface RootContainer extends Container {
   
    public Map<String, String> getPrefixes();

    public String getBaseIri();

}
