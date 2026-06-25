package fr.sparna.rdf.xls2rdf.sheet.grist;

import fr.sparna.rdf.xls2rdf.sheet.*;

import java.util.List;

public class GristHeaderCell implements Cell {

    private List<String> columnNames;
    private int columnIndex;
    private int rowIndex;
    private Row row;

    public GristHeaderCell(List<String> columnNames, int columnIndex, int rowIndex, Row row) {
        this.columnNames = columnNames;
        this.columnIndex = columnIndex;
        this.row = row;
    }

    @Override
    public CellType getCellType() {
        return CellType.STRING;
    }

    @Override
    public String getCellValue() {
        if(columnIndex >= this.columnNames.size()) return null;
        return this.columnNames.get(this.columnIndex);
    }

    @Override
    public Row getRow() {
        return this.row;
    }

    @Override
    public int getRowIndex() {
        return this.rowIndex;
    }

    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    @Override
    public Sheet getSheet() {
        return this.getRow().getSheet();
    }

    @Override
    public boolean isStruckThrough() {
        return false;
    }

    @Override
    public String getCellExcelReference() {
        return ExcelRefs.cellRef(this.rowIndex, this.columnIndex);
    }
}
