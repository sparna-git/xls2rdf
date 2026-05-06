package fr.sparna.rdf.xls2rdf.sheet.csv;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class CSVSheet implements Sheet {

    static Logger log = LoggerFactory.getLogger(CSVSheet.class.getName());

    private final CSVWorkbook parentWorkbook;
    private final CSVDelegate delegate;


    public CSVSheet(CSVDelegate delegate, CSVWorkbook parentWorkbook){
        this.delegate = delegate;
        this.parentWorkbook = parentWorkbook;
    }


    @Override
    public String getSheetName() {
        return this.delegate.getFileName();
    }

    @Override
    public int getLastRowNum() {
        return this.delegate.getRecordsSize();
    }

    @Override
    public Row getRow(int rowIndex){
        if(rowIndex > this.getLastRowNum()){
            return new CSVRow(this.delegate.getRecordByIndex(this.getLastRowNum()), this);
        }
        return new CSVRow(this.delegate.getRecordByIndex(rowIndex), this);
    }


    @Override
    public boolean isColumnHidden(int columnIndex) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<>() {
            private int i;
            @Override
            public boolean hasNext() {
                return i < getLastRowNum();
            }

            @Override
            public Row next() {
                return getRow(i++);
            }
        };
    }

    public CSVWorkbook getWorkbook() {
        return this.parentWorkbook;
    }

    public CSVDelegate getCSVAdapter() {
        return this.delegate;
    }
}
