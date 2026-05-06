package fr.sparna.rdf.xls2rdf.sheet.csv;

import fr.sparna.rdf.xls2rdf.sheet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVCell implements Cell {

    static Logger log = LoggerFactory.getLogger(CSVCell.class.getName());

    private final String value;
    private final CSVRow parentRow;
    private final int columnIndex;

    public CSVCell(String value, CSVRow parentRow, int columnIndex){
        this.value = value;
        this.parentRow = parentRow;
        this.columnIndex = columnIndex;
    }

    @Override
    public CellType getCellType() {
        return CellType.ERROR;
    }

    @Override
    public String getCellValue() {
        return this.value.trim();
    }

    @Override
    public Row getRow() {
        return this.parentRow;
    }

    @Override
    public int getRowIndex() {
        return this.parentRow.getRowNum();
    }

    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    @Override
    public Sheet getSheet() {
        return this.parentRow.getSheet();
    }

    @Override
    public boolean isStruckThrough() {
        return false;
    }

    @Override
    public String getCellExcelReference() {
        return ExcelRefs.cellRef(this.getRowIndex(), this.getColumnIndex());
    }
}
