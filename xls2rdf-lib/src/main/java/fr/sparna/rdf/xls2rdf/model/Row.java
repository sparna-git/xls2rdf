package fr.sparna.rdf.xls2rdf.model;

public interface Row {
    /**
     * @param columnIndex zero-based column index.
     * @return the cell at the given column index, or null if absent.
     */
    Cell getCell(int columnIndex);

    /**
     * @return the zero-based row number of this row.
     */
    int getRowNum();

    /**
     * @return the parent sheet that contains this row.
     */
    Sheet getSheet();

    /**
     * @return true if this row is hidden (zero height).
     */
    boolean isHidden();
}
