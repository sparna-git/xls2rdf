package fr.sparna.rdf.xls2rdf.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseArrayContainer extends BaseContainer {
    
    public static String TYPE = "ARRAY";

    private List<Node> values;

    /**
     * Factory method to create a BaseArrayContainer from a list of string values.
     */
    public static BaseArrayContainer of(List<String> stringValues, Container parent) {
        List<Node> nodes = new ArrayList<Node>(stringValues.stream().map(BaseLiteral::new).toList());
        return new BaseArrayContainer(null, nodes, parent);
    }

    /**
     * Factory method to create a BaseArrayContainer from an array of string values.
     */
    public static BaseArrayContainer of(String[] stringValues, Container parent) {
        return of(List.of(stringValues), parent);
    }

    public BaseArrayContainer(String id, List<Node> values, Container parent) {
        super(id, TYPE, parent);
        this.values = values;
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getKeys() {
        return this.values.stream().map(n -> this.values.indexOf(n)).map(i -> String.valueOf(i)).toList();
    }

    @Override
    public Node getValue(String key) {
        try {
            int index = Integer.parseInt(key);
            return this.values.get(index);
        } catch (NumberFormatException e) {
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public Map<String, Node> getKeyValuePairs() {
        Map<String, Node> map = new HashMap<>();
        for (int i = 0; i < this.values.size(); i++) {
            map.put(String.valueOf(i), this.values.get(i));
        }
        return map;
    }

    
}
