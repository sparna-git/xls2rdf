package fr.sparna.rdf.xls2rdf.model.excel;

import java.util.Iterator;

import fr.sparna.rdf.xls2rdf.model.Row;
import fr.sparna.rdf.xls2rdf.model.Sheet;

public class ExcelSheet implements Sheet {
    private final org.apache.poi.ss.usermodel.Sheet delegate;
    private final ExcelWorkbook parentWorkbook;

    public ExcelSheet(org.apache.poi.ss.usermodel.Sheet delegate, ExcelWorkbook parentWorkbook) {
        this.delegate = delegate;
        this.parentWorkbook = parentWorkbook;
    }

    @Override
    public String getSheetName() {
        return delegate.getSheetName();
    }

    @Override
    public int getLastRowNum() {
        return delegate.getLastRowNum();
    }

    @Override
    public Row getRow(int rowIndex) {
        org.apache.poi.ss.usermodel.Row r = delegate.getRow(rowIndex);
        return r == null ? null : new ExcelRow(r, this);
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        return delegate.isColumnHidden(columnIndex);
    }

    @Override
    public Iterator<Row> iterator() {
        final Iterator<org.apache.poi.ss.usermodel.Row> it = delegate.iterator();
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() { return it.hasNext(); }

            @Override
            public Row next() { return new ExcelRow(it.next(), ExcelSheet.this); }
        };
    }

    ExcelWorkbook getWorkbook() {
        return parentWorkbook;
    }

    org.apache.poi.ss.usermodel.Sheet getPoiSheet() {
        return delegate;
    }
}
