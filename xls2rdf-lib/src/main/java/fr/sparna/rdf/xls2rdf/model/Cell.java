package fr.sparna.rdf.xls2rdf.model;

public interface Cell {
    /**
     * @return the {@link CellType} of this cell (string, numeric, boolean, etc.).
     */
    CellType getCellType();

    /**
     * @return the cell value as text; implementations may normalize whitespace.
     */
    String getCellValue();

    /**
     * @return the parent row containing this cell.
     */
    Row getRow();

    /**
     * @return the zero-based row index of this cell.
     */
    int getRowIndex();

    /**
     * @return the zero-based column index of this cell.
     */
    int getColumnIndex();

    /**
     * @return the parent sheet of this cell.
     */
    Sheet getSheet();

    /**
     * @return true if the cell text is displayed with a strike-through font.
     */
    boolean isStruckThrough();

    /**
     * @return the Excel reference of this cell, e.g. "A1", "B2", etc.
     */
    String getCellExcelReference();
}
