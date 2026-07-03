package fr.sparna.rdf.xls2rdf.sheet.grist;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.*;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.Client;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.GristColumns;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristColumnsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GristCell implements Cell {

    private static final Logger LOGGER = LoggerFactory.getLogger(GristCell.class);

    private final JsonNode cellNode;
    private final Row parentRow;
    private final int columnIndex;
    private final String columnName;
    private GristColumns gristColumns;

    public GristCell(JsonNode cellNode, int columnIndex, String columnName, GristRow parentRow, GristColumns gristColumns){
        this.parentRow = parentRow;
        this.cellNode = cellNode;
        this.columnIndex = columnIndex;
        this.columnName = columnName;
        this.gristColumns = gristColumns;
    }

    @Override
    public CellType getCellType() {
        return CellType.STRING;
    }

    @Override
    public String getCellValue() {
        String type = this.gristColumns.getMetadata(this.columnName, GristColumnsParser.TYPE).asText();
        String convertResult = GristCellConverter.getInstance().convertIf(cellNode, type);
        if(convertResult != null) return convertResult;
        else return "";
    }

    @Override
    public Row getRow() {
        return this.parentRow;
    }

    @Override
    public int getRowIndex() {
        return this.getRow().getRowNum();
    }

    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    @Override
    public Sheet getSheet() {
        return this.parentRow.getSheet();
    }

    @Override
    public boolean isStruckThrough() {
        return false;
    }

    @Override
    public String getCellExcelReference() {
        return ExcelRefs.cellRef(this.getRowIndex(), this.getColumnIndex());
    }

    public Workbook getWorkbook(){
        return ((GristRow)this.parentRow).getWorkbook();
    }

    public Client getGristClient(){
        return ((GristRow)this.parentRow).getGristClient();
    }


}
