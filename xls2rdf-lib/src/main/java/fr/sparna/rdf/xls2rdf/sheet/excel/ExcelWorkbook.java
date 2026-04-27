package fr.sparna.rdf.xls2rdf.sheet.excel;

import java.util.Iterator;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;

import org.slf4j.Logger;

public class ExcelWorkbook implements Workbook {

    static Logger log = LoggerFactory.getLogger(ExcelWorkbook.class.getName());

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

    public org.apache.poi.ss.usermodel.Workbook getPoiWorkbook() {
        return this.delegate;
    }


}
