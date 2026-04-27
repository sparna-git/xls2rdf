package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentTable;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

public class OpenDocumentTableTest{


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

    OpenDocumentSpreadSheet cud;


    @Before
    public void initProperties() throws Exception {
        this.cud = OpenDocumentSpreadSheetFactory.open(new FileInputStream(PATH_NAME));
        Assert.assertNotNull("class under test is null.", cud);
    }

    @Test
    public void try_get_sheet_by_name(){
        Sheet sheetOne = this.cud.getSheet("Arthur_Test");
        Assert.assertNotNull("sheet is null.", sheetOne);

        Sheet sheetTwo = this.cud.getSheet("Sheet2");
        Assert.assertNotNull("sheet is null.", sheetTwo);

        Sheet sheetThree = this.cud.getSheet("Sheet3");
        Assert.assertNotNull("sheet is null.", sheetThree);

        Sheet sheetFour = this.cud.getSheet("Sheet4");
        Assert.assertNull("sheet is not null.", sheetFour);
    }

    @Test
    public void try_get_sheet_by_id(){
        Sheet sheetOne = this.cud.getSheet(0);
        Assert.assertNotNull("sheet is null.", sheetOne);

        Sheet sheetTwo = this.cud.getSheet(1);
        Assert.assertNotNull("sheet is null.", sheetTwo);

        Sheet sheetThree = this.cud.getSheet(2);
        Assert.assertNotNull("sheet is null.", sheetThree);

        Sheet sheetFour = this.cud.getSheet(3);

    }

}