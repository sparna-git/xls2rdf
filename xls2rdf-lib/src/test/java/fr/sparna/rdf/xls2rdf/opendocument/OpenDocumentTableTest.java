package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentRow;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;

import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class OpenDocumentTableTest {

    static final URL ODS_FILE_URL = OpenDocumentSpreadSheetFactoryTest.class.getResource("/opendocument/test.ods");
    static final String PATH_NAME;
    static final String SHEET_NAME = "Sheet1";
    static final int ROW_COUNT = 7;
    static final int COLUMN_COUNT = 6;
    static{
        try {
            Assert.assertNotNull(ODS_FILE_URL);
            PATH_NAME = ODS_FILE_URL.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    Sheet cud;


    @Before
    public void initProperties() throws Exception {
        this.cud = OpenDocumentSpreadSheetFactory.open(new FileInputStream(PATH_NAME)).getSheet(SHEET_NAME);
        Assert.assertNotNull("class under test is null.", cud);
    }

    @Test
    public void try_get_sheet_name_is_equal(){
        Assert.assertEquals("Sheet's name is different.", SHEET_NAME, this.cud.getSheetName());
    }

    @Test
    public void try_get_sheet_name_is_not_equal(){
        Assert.assertNotEquals("Sheet's name is the same.", SHEET_NAME + "/test another sheet name./", this.cud.getSheetName());
    }

    @Test
    public void try_get_last_row_num_is_equal(){
        Assert.assertEquals("Sheet's row count is different.", ROW_COUNT, this.cud.getLastRowNum());
    }

    @Test
    public void try_get_rows_are_not_null(){
        for(int i = 0; i < ROW_COUNT; i++){
            Row currentRow = this.cud.getRow(i);
            Assert.assertNotNull("The current row is null.", currentRow);
        }
    }

    @Test
    public void try_iterator_on_rows(){
        for(Row r : this.cud){
            Assert.assertNotNull("The iterator return null value for the row.", r);
        }
    }

    @Test
    public void try_get_delegate(){
        OdfTable delegate = ((OpenDocumentTable)this.cud).getOdfTable();
        Assert.assertNotNull("Delegate object is null.", delegate);
        Assert.assertSame("delegate class is not similar.", OdfTable.class, delegate.getClass());
    }

    @Test
    public void try_get_workboot_parent(){
        Workbook parent = ((OpenDocumentTable)this.cud).getParentSpreadSheet();
        Assert.assertNotNull("Parent object is null.", parent);
    }

    @Test
    public void try_is_column_hidden_false(){
        for(int i = 0; i < ROW_COUNT; i++){
            boolean isColumnNotHidden = this.cud.isColumnHidden(i);
            Assert.assertFalse("Column is hidden.", isColumnNotHidden);
        }
    }


    @Test
    public void try_is_column_hidden_true(){
        for(int i = 0; i < COLUMN_COUNT; i++){
            ((OpenDocumentTable)this.cud).getOdfTable().getColumnByIndex(i).getOdfElement().setTableVisibilityAttribute(OpenDocumentTable.HIDDEN_VALUE);
            boolean isColumnHidden = this.cud.isColumnHidden(i);
            Assert.assertTrue("Column is not hidden.", isColumnHidden);
        }
    }
}
