package fr.sparna.rdf.xls2rdf.sheet.csv;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVRow implements Row {

    static Logger log = LoggerFactory.getLogger(CSVRow.class.getName());

    private final CSVRecord delegate;
    private final CSVSheet parentSheet;

    public CSVRow(CSVRecord delegate, CSVSheet parentSheet){
        this.delegate = delegate;
        this.parentSheet = parentSheet;
    }

    @Override
    public Cell getCell(int columnIndex) {
        return new CSVCell(this.getColumnValue(columnIndex), this, columnIndex);
    }

    @Override
    public String getColumnValue(int columnIndex) {
        try{
            return this.delegate.values()[columnIndex].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            log.info("Index {} for column is not found. ArrayIndex Exception raised but ignored.", columnIndex);
        }
        return "";
    }

    @Override
    public int getRowNum() {
        return (int)this.delegate.getRecordNumber() - 1;
    }

    @Override
    public Sheet getSheet() {
        return this.parentSheet;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    public CSVWorkbook getWorkbook(){
        return this.parentSheet.getWorkbook();
    }

    public CSVRecord getCSVRecord() {
        return this.delegate;
    }
}
