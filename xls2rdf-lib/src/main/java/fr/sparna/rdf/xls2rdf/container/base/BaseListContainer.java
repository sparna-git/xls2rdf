package fr.sparna.rdf.xls2rdf.container.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.sparna.rdf.xls2rdf.container.Container;
import fr.sparna.rdf.xls2rdf.container.ContainerOrValue;
import fr.sparna.rdf.xls2rdf.container.Slot;
import fr.sparna.rdf.xls2rdf.container.SlotNumber;

public class BaseListContainer extends BaseContainer {
    
    public static String TYPE = "ARRAY";

    private List<ContainerOrValue> values;

    /**
     * Factory method to create a BaseArrayContainer from a list of string values.
     */
    public static BaseListContainer of(List<String> stringValues, Container parent) {
        List<ContainerOrValue> nodes = new ArrayList<ContainerOrValue>(stringValues.stream().map(BaseValue::new).toList());
        return new BaseListContainer(null, nodes, parent);
    }

    /**
     * Factory method to create a BaseArrayContainer from an array of string values.
     */
    public static BaseListContainer of(String[] stringValues, Container parent) {
        return of(List.of(stringValues), parent);
    }

    public BaseListContainer(String id, List<ContainerOrValue> values, Container parent) {
        super(id, TYPE, parent);
        this.values = values;
    }

    @Override
    public List<SlotNumber> getSlotNumbers() {
        List<SlotNumber> slotNumbers = new ArrayList<>();
        for (int i = 0; i < this.values.size(); i++) {
            slotNumbers.add(new BaseSlotNumber(i, this.values.get(i)));
        }
        return slotNumbers;
    }

    @Override
    public ContainerOrValue getSlotStringvalue(String key) {
        return null;
    }

    @Override
    public List<String> listSlotStringKeys() {
        return new ArrayList<>();
    }

    @Override
    public Set<Slot> getSlots() {
        return this.getSlotNumbers().stream().collect(java.util.stream.Collectors.toSet());
    }

    
}
