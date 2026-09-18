package fr.sparna.rdf.xls2rdf.container.base;

import fr.sparna.rdf.xls2rdf.container.ContainerOrValue;
import fr.sparna.rdf.xls2rdf.container.SlotNumber;

/**
 * A base implementation of the SlotNumber interface.
 */
public class BaseSlotNumber  extends BaseSlot implements SlotNumber{
    
    private int index;

    public BaseSlotNumber(int index, ContainerOrValue value) {
        super(SlotType.SLOTNUMBER, value);
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }
    
}
