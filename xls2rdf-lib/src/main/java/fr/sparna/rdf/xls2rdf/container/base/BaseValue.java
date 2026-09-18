package fr.sparna.rdf.xls2rdf.container.base;

import fr.sparna.rdf.xls2rdf.container.Value;

/**
 * A base implementation of the Value interface.
 */
public class BaseValue implements Value {
    
    private String text;

    public BaseValue(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
    
}
