package fr.sparna.rdf.xls2rdf.model.excel;

import java.util.Iterator;
import java.util.List;

import fr.sparna.rdf.xls2rdf.model.Sheet;
import fr.sparna.rdf.xls2rdf.model.Workbook;

public class ExcelWorkbook implements Workbook {
    private final org.apache.poi.ss.usermodel.Workbook delegate;

    public ExcelWorkbook(org.apache.poi.ss.usermodel.Workbook delegate) {
        this.delegate = delegate;
    }

    @Override
    public Sheet getSheet(int index) {
        return new ExcelSheet(delegate.getSheetAt(index), this);
    }

    @Override
    public Sheet getSheet(String name) {
        return new ExcelSheet(delegate.getSheet(name), this);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<Sheet>() {
            private int i = 0;

            @Override
            public boolean hasNext() { return i < delegate.getNumberOfSheets(); }

            @Override
            public Sheet next() { return getSheet(i++); }
        };
    }

    org.apache.poi.ss.usermodel.Workbook getPoiWorkbook() {
        return delegate;
    }
}
