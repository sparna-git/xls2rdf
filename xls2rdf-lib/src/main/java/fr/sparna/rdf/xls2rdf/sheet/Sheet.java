package fr.sparna.rdf.xls2rdf.sheet;

/**
 * Represents a sheet in a workbook. A sheet may be identified by its name and contains rows at various indices.
 */
public interface Sheet extends Iterable<Row> {
    
    /**
     * @return the display name of this sheet.
     * This can return null depending on the implementation, e.g. a CSV implementation may choose to return null.
     */
    String getSheetName();

    /**
     * @return the zero-based index of the last logical row.
     */
    int getLastRowNum();

    /**
     * @param rowIndex zero-based row index.
     * @return the row at the given index, or null if absent.
     */
    Row getRow(int rowIndex);

    /**
     * @param columnIndex zero-based column index.
     * @return true if the column is hidden.
     */
    boolean isColumnHidden(int columnIndex);

}
