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
    private final String filename;

    public CSVWorkbook(CSVDelegate delegate, String filename){
        this.delegate = delegate;
        this.filename = filename;
    }

    @Override
    public Sheet getSheet(int index) {
        if(index != 0){
            throw new IndexOutOfBoundsException("CSV file '"+this.filename+"' has only one sheet at index 0, but index "+index+" was requested.");
        }
        return new CSVSheet(this.delegate, this, filename);
    }

    @Override
    public Sheet getSheet(String name) {
        if(name != null && name.equals(this.filename)){
            return new CSVSheet(this.delegate, this, filename);
        } else {
            log.warn("No sheet found with name '"+name+"' in CSV file '"+this.filename+"'");
            return null;
        }
    }

    @NotNull
    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<>() {
            private int i;
            @Override
            public boolean hasNext() {
                return i == 0;
            }

            @Override
            public Sheet next() {
                return getSheet(i++);
            }
        };
    }

    @Override
    public int size() {
        return 1;
    }

    public CSVDelegate getCSVBridge() {
        return this.delegate;
    }

    public String getFilename() {
        return filename;
    }

}
