package fr.sparna.rdf.xls2rdf.model.excel;

import fr.sparna.rdf.xls2rdf.model.Cell;
import fr.sparna.rdf.xls2rdf.model.CellType;
import fr.sparna.rdf.xls2rdf.model.Row;
import fr.sparna.rdf.xls2rdf.model.Sheet;

public class ExcelCell implements Cell {
    private final org.apache.poi.ss.usermodel.Cell delegate;
    private final ExcelRow parentRow;

    public ExcelCell(org.apache.poi.ss.usermodel.Cell delegate, ExcelRow parentRow) {
        this.delegate = delegate;
        this.parentRow = parentRow;
    }

    @Override
    public CellType getCellType() {
        if(delegate.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            return mapType(delegate.getCachedFormulaResultType());
        }

        return mapType(delegate.getCellType());
    }

    @Override
    public String getCellValue() {
        return delegate.getStringCellValue();
    }

    @Override
    public double getCellValueAsDouble() {
        return delegate.getNumericCellValue();
    }

    @Override
    public boolean getCellValueAsBoolean() {
        return delegate.getBooleanCellValue();
    }

    @Override
    public Row getRow() {
        return parentRow;
    }

    @Override
    public int getRowIndex() {
        return delegate.getRowIndex();
    }

    @Override
    public int getColumnIndex() {
        return delegate.getColumnIndex();
    }

    @Override
    public Sheet getSheet() {
        return parentRow.getSheet();
    }

    @Override
    public boolean isStruckThrough() {
        int fontIdx = delegate.getCellStyle().getFontIndex();
        return parentRow.getWorkbook().getPoiWorkbook().getFontAt(fontIdx).getStrikeout();
    }

    @Override
    public String getCellExcelReference() {
        return new org.apache.poi.ss.util.CellReference(delegate).formatAsString();
    }

    org.apache.poi.ss.usermodel.Cell getPoiCell() {
        return delegate;
    }

    private static CellType mapType(org.apache.poi.ss.usermodel.CellType t) {
        switch (t) {
            case BLANK: return CellType.BLANK;
            case ERROR: return CellType.ERROR;
            case STRING: return CellType.STRING;
            case NUMERIC: return CellType.NUMERIC;
            case BOOLEAN: return CellType.BOOLEAN;
            case FORMULA: return CellType.FORMULA;
            default: return CellType.ERROR;
        }
    }
}
