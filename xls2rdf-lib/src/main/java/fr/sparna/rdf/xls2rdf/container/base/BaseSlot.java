package fr.sparna.rdf.xls2rdf.container.base;

import fr.sparna.rdf.xls2rdf.container.ContainerOrValue;
import fr.sparna.rdf.xls2rdf.container.Slot;

/**
 * A base implementation of the Slot interface.
 */
public abstract class BaseSlot implements Slot {
    
    private SlotType slotType;
    private ContainerOrValue value;

    public BaseSlot(SlotType slotType, ContainerOrValue value) {
        this.slotType = slotType;
        this.value = value;
    }

    @Override
    public SlotType getSlotType() {
        return slotType;
    }

    @Override
    public ContainerOrValue getValue() {
        return value;
    }
    
}
