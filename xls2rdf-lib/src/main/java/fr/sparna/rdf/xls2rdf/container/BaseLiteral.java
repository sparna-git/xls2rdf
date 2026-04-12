package fr.sparna.rdf.xls2rdf.container;

/**
 * A base implementation of the Literal interface.
 */
public class BaseLiteral implements Literal {
    
    private String text;

    public BaseLiteral(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
    
}
