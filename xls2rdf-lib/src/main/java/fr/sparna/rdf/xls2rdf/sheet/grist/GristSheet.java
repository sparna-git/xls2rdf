package fr.sparna.rdf.xls2rdf.sheet.grist;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.Client;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.GristEntityFactory;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.column.GristColumns;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.record.GristRecords;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.parser.get.GristTablesParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GristSheet implements Sheet {

    private static final Logger LOGGER = LoggerFactory.getLogger(GristSheet.class);

    private final Workbook parentWorkbook;
    private final JsonNode tableNode;
    private final GristRecords gristRecords;
    private final GristColumns gristColumns;
    private final List<String> columnNames;

    public GristSheet(JsonNode tableNode, GristWorkbook delegate){
        this.tableNode = tableNode;
        this.parentWorkbook = delegate;
        this.gristRecords = GristEntityFactory.getRecords(this.getGristClient().getRecords(((GristWorkbook)this.parentWorkbook).getGristDocumentId(), this.getSheetName()));
        this.gristColumns = GristEntityFactory.getColumns(this.getGristClient().getColumns(((GristWorkbook)this.parentWorkbook).getGristDocumentId(), this.getSheetName()));
        this.columnNames = new ArrayList<>();
        Iterator<String> iter = this.gristRecords.getColumnNames(0);
        iter.forEachRemaining(columnNames::add);
    }


    @Override
    public String getSheetName() {
        return this.tableNode.get(GristTablesParser.TABLE_NAME).asText();
    }

    @Override
    public int getLastRowNum() {
        return this.gristRecords.getRecordsSize();
    }

    @Override
    public Row getRow(int rowIndex) {
        if(rowIndex == 0) return new GristHeaderRow(this.columnNames, this);
        return new GristRow(this.gristRecords.getRecord(rowIndex), this.columnNames, this);
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
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


    public Workbook getParentWorkbook(){
        return this.parentWorkbook;
    }

    public Client getGristClient(){
        return ((GristWorkbook)this.parentWorkbook).getClient();
    }

}
