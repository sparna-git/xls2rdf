package fr.sparna.rdf.xls2rdf.model.excel;

import fr.sparna.rdf.xls2rdf.model.Cell;
import fr.sparna.rdf.xls2rdf.model.Row;
import fr.sparna.rdf.xls2rdf.model.Sheet;

public class ExcelRow implements Row {
    private final org.apache.poi.ss.usermodel.Row delegate;
    private final ExcelSheet parentSheet;

    public ExcelRow(org.apache.poi.ss.usermodel.Row delegate, ExcelSheet parentSheet) {
        this.delegate = delegate;
        this.parentSheet = parentSheet;
    }

    @Override
    public Cell getCell(int columnIndex) {
        org.apache.poi.ss.usermodel.Cell c = delegate.getCell(columnIndex);
        return c == null ? null : new ExcelCell(c, this);
    }

    @Override
    public int getRowNum() {
        return delegate.getRowNum();
    }

    @Override
    public Sheet getSheet() {
        return parentSheet;
    }

    @Override
    public boolean isHidden() {
        return delegate.getZeroHeight();
    }

    ExcelWorkbook getWorkbook() {
        return parentSheet.getWorkbook();
    }

    org.apache.poi.ss.usermodel.Row getPoiRow() {
        return delegate;
    }
}
