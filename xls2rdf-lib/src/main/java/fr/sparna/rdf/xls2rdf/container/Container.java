package fr.sparna.rdf.xls2rdf.container;

import java.util.List;
import java.util.Set;

/**
 * A FacadeX-like container interface to represent a hierarchical structure of nodes, where each node can have an ID, a type, a parent container, child nodes, and key-value pairs.
 * See https://sparql-anything.readthedocs.io/stable/Facade-X/
 */
public interface Container extends ContainerOrValue {
    
    /**
     * @return the unique identifier of this container, if any. This can be null if the container does not have a specific ID.
     */
    public String getId();

    /**
     * @return the type of this container, if any. This can be null if the container does not have a specific type.
     */
    public String getType();

    /**
     * @return the parent container of this container, or null if this is a root container.
     */
    public Container getParent();

    public Set<Slot> getSlots();  

    public List<SlotNumber> getSlotNumbers();  

    /**
    * @return the list of keys for this container, if any. This can be null if the container does not have key-value pairs.
    */
    public List<String> listSlotStringKeys();

    /**
     * @return the value associated with the given key, or null if the key is not present. The value is either a String, either another Container.
     */
    public ContainerOrValue getSlotStringvalue(String key);

}
