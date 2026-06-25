package fr.sparna.rdf.xls2rdf.sheet.grist;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;

import java.util.List;

public class GristHeaderRow implements Row {

    private List<String> columnNames;
    private Sheet sheet;

    public GristHeaderRow(List<String> columnNames, Sheet sheet){
        this.columnNames = columnNames;
        this.sheet = sheet;
    }

    @Override
    public Cell getCell(int columnIndex) {
        return new GristHeaderCell(columnNames, columnIndex, this.getRowNum(), this);
    }

    @Override
    public String getColumnValue(int columnIndex) {
        if(columnIndex >= this.columnNames.size()) return null;
        return this.columnNames.get(columnIndex);
    }

    @Override
    public int getRowNum() {
        return 0;
    }

    @Override
    public Sheet getSheet() {
        return this.sheet;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
