package fr.sparna.rdf.xls2rdf.csv;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVSheet;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class CSVSheetTest {


    static final URL CSV_FILE_URL = fr.sparna.rdf.xls2rdf.opendocument.OpenDocumentWorkbookFactoryTest.class.getResource("/csv/test.csv");
    static final URI CSV_FILE_URI;
    static{
        try {
            Assert.assertNotNull(CSV_FILE_URL);
            CSV_FILE_URI = CSV_FILE_URL.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    static final int ROW_COUNT = 12;
    static final String SHEET_NAME = "Csv Sheet";

    Sheet cud;

    @Before
    public void initProperties() throws Exception {
        this.cud = CSVWorkbookFactory.open(CSVFormat.DEFAULT, new InputStreamReader(new FileInputStream(CSV_FILE_URI.getPath()))).getSheet(0);
        Assert.assertNotNull("class under test is null.", cud);
    }

    @Test
    public void try_get_sheet_name_is_equal(){
        Assert.assertEquals("Sheet's name is different.", SHEET_NAME, this.cud.getSheetName());
    }

    @Test
    public void try_get_sheet_name_is_not_equal(){
        Assert.assertNotEquals("Sheet's name is the same.", "Test another sheet name which does not exist.", this.cud.getSheetName());
    }

    @Test
    public void try_get_last_row_num_is_equal() {
        Assert.assertEquals("Sheet's row count is different.", ROW_COUNT - 1, this.cud.getLastRowNum());
    }

    @Test
    public void try_get_rows_are_not_null() {
        for(int i = 0; i < ROW_COUNT; i++){
            Row currentRow = this.cud.getRow(i);
            Assert.assertNotNull("The current row is null.", currentRow);
        }
    }

    @Test
    public void try_iterator_on_rows_are_not_null(){
        for(Row r : this.cud){
            Assert.assertNotNull("The iterator return null value for the row.", r);
        }
    }

    @Test
    public void try_get_delegate_is_not_null(){
        CSVFormat delegate = ((CSVSheet)this.cud).getCSVAdapter().getFormat();
        Assert.assertNotNull("delegate object is null.", delegate);
        Assert.assertSame("delegate class is not similar.", CSVFormat.class, delegate.getClass());
    }

    @Test
    public void try_get_workboot_parent_is_not_null(){
        Workbook parent = ((CSVSheet)this.cud).getWorkbook();
        Assert.assertNotNull("parent object is null.", parent);
        Assert.assertSame("parent class is not similar.", CSVWorkbook.class, parent.getClass());
    }

    @Test
    public void try_is_column_hidden_false(){
        for(int i = 0; i < ROW_COUNT; i++){
            boolean isColumnNotHidden = this.cud.isColumnHidden(i);
            Assert.assertFalse("Column is hidden.", isColumnNotHidden);
        }
    }

}
