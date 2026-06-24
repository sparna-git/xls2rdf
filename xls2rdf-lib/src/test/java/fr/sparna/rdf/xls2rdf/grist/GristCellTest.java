package fr.sparna.rdf.xls2rdf.grist;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.Client;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GristCellTest {

    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};
    final static int LAST_ROW_NUM = 3;


    Workbook workbook;
    List<Sheet> sheets;
    @Mock
    Client mockClient;
    ObjectMapper m = new ObjectMapper();


    public void mockRecordsClientCall(String tableName) throws IOException {

        switch (tableName){
            case "NodeShapes" -> {
                Mockito.when(mockClient.getRecords(Mockito.anyString(), Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist_NodeShapes_records.json")));
            }
            case "PropertyShapes" -> {
                Mockito.when(mockClient.getRecords(Mockito.anyString(), Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist.PropertyShapes_records.json")));
            }
            case "Prefixes" -> {
                Mockito.when(mockClient.getRecords(Mockito.anyString(), Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist_Prefiixes_records.json")));
            }
            case "ShaclGraph" -> {
                Mockito.when(mockClient.getRecords(Mockito.anyString(), Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist_ShaclGraph_records.json")));
            }
        }
    }

    public void initProperties() throws IOException {
        this.sheets = new ArrayList<>();
        Mockito.when(this.mockClient.getTables(Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist_tables.json")));
        this.workbook = new GristWorkbook(DOCUMENT_ID, this.mockClient);
        for(String n : TABLES_NAMES){
            this.mockRecordsClientCall(n);
            this.sheets.add(workbook.getSheet(n));
        }
    }

    @Test
    public void try_get_cell_value_at() throws IOException {
        this.initProperties();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println(c.getCellValue());
                }
            }
        }
    }


    @Test
    public void try_get_row_index() throws IOException {
        this.initProperties();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println("row index is " + c.getRowIndex() + " for " + c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_column_index() throws IOException {
        this.initProperties();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println("column index is " + c.getColumnIndex() + " for " + c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_cell_ref() throws IOException {
        this.initProperties();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 10 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println(c.getCellExcelReference());
                }
            }
        }
    }

}
