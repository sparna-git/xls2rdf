package fr.sparna.rdf.xls2rdf.sheet;

/**
 * Represents a workbook containing sheets.
 * A workbook may be identified by its file name or source, and contains sheets at various indices.
 */
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
