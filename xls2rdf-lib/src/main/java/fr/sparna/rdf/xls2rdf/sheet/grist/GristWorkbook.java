package fr.sparna.rdf.xls2rdf.sheet.grist;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.GristClient;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.GristEntityFactory;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.entity.table.GristTables;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class GristWorkbook implements Workbook {

    private static final Logger LOGGER = LoggerFactory.getLogger(GristWorkbook.class);

    private final String gristDocumentId;
    private final GristClient client;
    private final GristTables gristTables;

    public GristWorkbook(String gristDocumentId, GristClient client){
        this.client = client;
        this.gristDocumentId = gristDocumentId;
        this.gristTables = GristEntityFactory.getTables(this.client.getGristTables(this.getGristDocumentId()));
    }

    @Override
    public Sheet getSheet(int index) {
        return new GristSheet(this.gristTables.getTable(index),this);
    }
    @Override
    public Sheet getSheet(String name) {
        return new GristSheet(this.gristTables.getTable(name),this);
    }

    @NotNull
    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<Sheet>() {
            private int i;

            @Override
            public boolean hasNext() {
                return i < gristTables.getTablesSize();
            }

            @Override
            public Sheet next() {
                return getSheet(i++);
            }
        };
    }

    public GristClient getClient(){
        return this.client;
    }

    public String getGristDocumentId(){
        return this.gristDocumentId;
    }

}
