package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class OpenDocumentSpreadSheetTest {


    static final List<String> SHEETS = List.of("Sheet1", "Sheet2", "Sheet3");
    static final URL ODS_FILE_URL = OpenDocumentSpreadSheetFactoryTest.class.getResource("/opendocument/test.ods");
    static final String PATH_NAME;
    static{
        try {
            Assert.assertNotNull(ODS_FILE_URL);
            PATH_NAME = ODS_FILE_URL.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    Workbook cud;


    @Before
    public void initProperties() throws Exception {
        this.cud = OpenDocumentSpreadSheetFactory.open(new FileInputStream(PATH_NAME));
        Assert.assertNotNull("class under test is null.", cud);
    }

    @Test
    public void try_get_sheet_by_name_is_not_null(){
        for(String s: SHEETS){
            Sheet sheet = this.cud.getSheet(s);
            Assert.assertNotNull("sheet is null.", sheet);
        }
    }

    @Test
    public void try_get_sheet_by_name_is_null(){
        Sheet sheetFour = this.cud.getSheet("Sheet4IsNotPresent");
        Assert.assertNull("sheet is not null.", sheetFour);
    }

    @Test
    public void try_get_sheet_by_id_is_not_null(){
        int size = SHEETS.size();
        for(int i = 0; i < size; i++){
            Sheet sheet = this.cud.getSheet(i);
            Assert.assertNotNull("sheet is null.", sheet);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void try_get_sheet_by_id_is_out_of_array(){
        Sheet sheetFour = this.cud.getSheet(1_000_000);
    }

    @Test
    public void try_get_delegate(){
        OdfSpreadsheetDocument ods = ((OpenDocumentSpreadSheet)this.cud).getSpreadsheetDocument();
        Assert.assertNotNull("Delegate object is null.", ods);
    }

    @Test
    public void try_iterator_on_sheets(){
        for(Sheet s: this.cud){
            Assert.assertNotNull("The iterator return null value for the sheet.", s);
        }
    }


}