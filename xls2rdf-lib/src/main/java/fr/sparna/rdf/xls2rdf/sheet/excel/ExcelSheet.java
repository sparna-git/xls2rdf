package fr.sparna.rdf.xls2rdf.sheet.excel;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class ExcelSheet implements Sheet {

    static Logger log = LoggerFactory.getLogger(ExcelSheet.class.getName());

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

    public ExcelWorkbook getWorkbook() {
        return parentWorkbook;
    }

    public org.apache.poi.ss.usermodel.Sheet getPoiSheet() {
        return delegate;
    }
}
