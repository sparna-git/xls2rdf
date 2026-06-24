package fr.sparna.rdf.xls2rdf.grist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@RunWith(MockitoJUnitRunner.class)
public class GristWorkbookTest {

    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};

    Workbook cud;
    @Mock
    Client mockClient;
    ObjectMapper m = new ObjectMapper();


    @Before
    public void initMock() throws IOException {
        InputStream node = this.getClass().getResourceAsStream("/grist/grist_tables.json");
        JsonNode jsonNode = this.m.readTree(node);
        //On initie le comportement du mock avant d'être ajouté dans le GristWorkbook
        Mockito.when(mockClient.getTables(Mockito.anyString())).thenReturn(jsonNode);
        cud = new GristWorkbook(DOCUMENT_ID, mockClient);
        Mockito.verify(this.mockClient).getTables(DOCUMENT_ID);

    }

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

    @Test
    public void try_get_sheet_by_name() throws IOException {
      Assert.assertNotNull("Class under test is null.", this.cud);
            for (int i = 0; i < TABLES_NAMES.length ; i++) {
                this.mockRecordsClientCall(TABLES_NAMES[i]);
                Sheet s = this.cud.getSheet(TABLES_NAMES[i]);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_index() throws IOException {
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            this.mockRecordsClientCall(TABLES_NAMES[i]);
            Sheet s = this.cud.getSheet(i);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_iter_on_sheet() throws IOException {
        Iterator<Sheet> iter = this.cud.iterator();
        while(iter.hasNext()){
            Mockito.when(mockClient.getRecords(Mockito.anyString(), Mockito.anyString())).thenReturn(this.m.readTree(this.getClass().getResourceAsStream("/grist/grist_records_test.json")));
            Sheet s = iter.next();
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

}
