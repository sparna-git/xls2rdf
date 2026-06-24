package fr.sparna.rdf.xls2rdf.sheet.grist;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.Client;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristRecordsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GristRow implements Row {

    private static final Logger LOGGER = LoggerFactory.getLogger(GristRow.class);

    private final Sheet parentSheet;
    private final JsonNode rowNode;
    private final List<String> columnNames;


    public GristRow(JsonNode rowNode, List<String> columnNames, GristSheet delegate){
        this.parentSheet = delegate;
        this.rowNode = rowNode;
        this.columnNames = columnNames;
    }

    @Override
    public Cell getCell(int columnIndex) {
        if(columnIndex >= this.columnNames.size()) return null;
        return new GristCell(this.rowNode.get(GristRecordsParser.FIELDS_ID).get(this.columnNames.get(columnIndex)), columnIndex, this.columnNames.get(columnIndex), this);
    }

    @Override
    public String getColumnValue(int columnIndex) {
        if(columnIndex >= this.columnNames.size()) return null;
        System.out.print("ColumnName: " + this.columnNames.get(columnIndex) + " Value === ");// <---- a retirer juste pour les tests
        JsonNode cellNode = this.rowNode.get(GristRecordsParser.FIELDS_ID).get(this.columnNames.get(columnIndex));
        String convertResult = GristCellConverter.getInstance().convertIf(cellNode);
        if(convertResult != null) return convertResult;
        else return cellNode.asText();
    }

    @Override
    public int getRowNum() {
        return this.rowNode.get(GristRecordsParser.RECORD_POSITION).asInt() - 1;
    }

    @Override
    public Sheet getSheet() {
        return this.parentSheet;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    public Workbook getWorkbook(){
        return ((GristSheet)this.getSheet()).getParentWorkbook();
    }

    public Client getGristClient(){
        return ((GristSheet)this.parentSheet).getGristClient();
    }

}
