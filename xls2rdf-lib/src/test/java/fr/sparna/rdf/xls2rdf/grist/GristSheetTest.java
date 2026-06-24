package fr.sparna.rdf.xls2rdf.grist;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GristSheetTest {


    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};
    final static int LAST_ROW_NUM = 3;


    Workbook workbook;
    List<Sheet> cud;
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
        this.cud = new ArrayList<>();
        Mockito.when(this.mockClient.getTables(Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist_tables.json")));
        this.workbook = new GristWorkbook(DOCUMENT_ID, this.mockClient);
        for(String n : TABLES_NAMES){
            this.mockRecordsClientCall(n);
            this.cud.add(workbook.getSheet(n));
        }
    }

    @Test
    public void try_get_sheet_name() throws IOException {
        this.initProperties();
        for(Sheet s : this.cud){
            System.out.println("Sheet name : " + s.getSheetName());
            Assert.assertTrue("Sheet's name is not recognized.", Arrays.deepToString(TABLES_NAMES).contains(s.getSheetName()));
        }
    }

    @Test
    public void try_get_sheet_last_row_num() throws IOException {
        this.initProperties();
        for(Sheet s : this.cud){
            Assert.assertTrue("Sheet's last row num is not correct..", LAST_ROW_NUM == s.getLastRowNum());
            System.out.println("Last row num for" + s.getSheetName() + " = " + s.getLastRowNum());
        }
    }


    @Test
    public void try_iter_on_sheet() throws IOException {
        this.initProperties();
        for(Sheet s : this.cud){
            Iterator<Row> iter = s.iterator();
            int index = 0;
            while(iter.hasNext()){
                Row r = iter.next();
                System.out.println("Iteration en cours sur les records de = " + r.getSheet().getSheetName());
                Assert.assertNotNull("Row is null.", r);
                Assert.assertEquals("Row num is not correct", index++, r.getRowNum());
            }
        }
    }


}
