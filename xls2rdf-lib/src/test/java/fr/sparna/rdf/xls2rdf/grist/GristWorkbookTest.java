package fr.sparna.rdf.xls2rdf.grist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.api.caller.CallableGrist;
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
    CallableGrist mockCaller;
    @Mock
    Client mockClient;


    @Before
    public void initMock() throws IOException, InterruptedException {
        ObjectMapper m = new ObjectMapper();
        InputStream node = this.getClass().getResourceAsStream("/grist/grist_document.json");
        JsonNode jsonNode = m.readTree(node);
        //On initie le comportement du mock avant d'être ajouté dans le GristWorkbook
        Mockito.when(mockClient.getTables(Mockito.anyString())).thenReturn(jsonNode);
        cud = Mockito.spy(new GristWorkbook(DOCUMENT_ID, mockClient));

    }

    @Test
    public void try_get_sheet_by_name_with_no_cache(){
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(TABLES_NAMES[i]);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_name_with_cache(){
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(TABLES_NAMES[i]);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_index_with_no_cache(){
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(i);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_index_with_cache(){
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(i);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_iter_on_sheet_with_no_cache(){;
        Iterator<Sheet> iter = this.cud.iterator();
        while(iter.hasNext()){
            Sheet s = iter.next();
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_iter_on_sheet_with_cache(){
        Iterator<Sheet> iter = this.cud.iterator();
        while(iter.hasNext()){
            Sheet s = iter.next();
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

}
