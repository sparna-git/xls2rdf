package fr.sparna.rdf.xls2rdf.container.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.sparna.rdf.xls2rdf.container.BaseLiteral;
import fr.sparna.rdf.xls2rdf.container.Container;
import fr.sparna.rdf.xls2rdf.container.Node;
import fr.sparna.rdf.xls2rdf.sheet.Row;

public class RowContainer implements Container {

    public static final String TYPE = "ROW";

    private Row row;
    private SheetContainer parent;

    public RowContainer(Row row, SheetContainer parent) {
        this.row = row;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return this.row.getRowNum() + "";
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getKeys() {
        return this.parent.getColumnNames();
    }

    @Override
    public Node getValue(String key) {
        int columnIndex = this.parent.getColumnNames().indexOf(key);
        if(columnIndex == -1) {
            return null;
        }
        return new BaseLiteral(this.row.getCell(columnIndex).getCellValue());
    }

    @Override
    public Map<String, Node> getKeyValuePairs() {
        Map<String, Node> keyValuePairs = new java.util.HashMap<>();
        for(String key : this.getKeys()) {
            keyValuePairs.put(key, this.getValue(key));
        }
        return keyValuePairs;
    }
    
}
