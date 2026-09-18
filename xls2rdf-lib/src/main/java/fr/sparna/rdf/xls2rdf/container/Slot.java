package fr.sparna.rdf.xls2rdf.container;

/**
 * A FacadeX-like slot interface to represent a slot, either being a reference by key or by number
 * See https://w3c-facade-x.github.io/facade-x-specs/rdf.html#h-Slot
 */
public interface Slot {
    
    public static enum SlotType {
        SLOTNUMBER,
        SLOTSTRING
    }

    public SlotType getSlotType();

    public ContainerOrValue getValue();

}
