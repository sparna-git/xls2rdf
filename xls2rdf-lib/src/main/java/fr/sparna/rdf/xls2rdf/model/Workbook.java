package fr.sparna.rdf.xls2rdf.model;


public interface Workbook extends Iterable<Sheet> {

    /**
     * @param index zero-based sheet index.
     * @return the sheet at the given index.
     * @throws IndexOutOfBoundsException if index is invalid.
     */
    Sheet getSheet(int index);

    /**
     * @param name sheet name.
     * @return the sheet with the given name, or null if not found.
     */
    Sheet getSheet(String name);

}
