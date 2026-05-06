package fr.sparna.rdf.xls2rdf.sheet.csv;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class CSVWorkbook implements Workbook {


    static Logger log = LoggerFactory.getLogger(CSVWorkbook.class.getName());

    private final CSVDelegate delegate;

    public CSVWorkbook(CSVDelegate delegate){
        this.delegate = delegate;
    }

    @Override
    public Sheet getSheet(int index) {
        return new CSVSheet(this.delegate, this);
    }

    @Override
    public Sheet getSheet(String name) {
        return new CSVSheet(this.delegate, this);
    }

    @NotNull
    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<>() {
            private int i;
            @Override
            public boolean hasNext() {
                return i++ == 0;
            }

            @Override
            public Sheet next() {
                return getSheet(i);
            }
        };
    }

    public CSVDelegate getCSVBridge() {
        return this.delegate;
    }
}
