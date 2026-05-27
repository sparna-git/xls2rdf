package fr.sparna.rdf.xls2rdf.csv;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVRow;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVSheet;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CSVRowTest {


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
    static final int COLUMN_COUNT = 3;


    List<Row> cud;

    @Before
    public void initProperties() throws Exception {
        this.cud = new ArrayList<>();
        Sheet sheet = CSVWorkbookFactory.open(CSVFormat.DEFAULT, new InputStreamReader(new FileInputStream(CSV_FILE_URI.getPath()))).getSheet(0);
        for(int i = 0; i < ROW_COUNT; i++){
            this.cud.add(sheet.getRow(i));
        }
        Assert.assertNotNull("class under test is null.", cud);
    }

    @Test
    public void try_get_cell_by_index_is_not_null(){
        for(Row r: cud){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Assert.assertNotNull("Cell is null.", cell);
            }
        }
    }

    @Test
    public void try_get_cell_value_is_not_null(){
        for(Row r: cud){
            for(int i = 0; i < COLUMN_COUNT; i++){
                String cellValue = r.getColumnValue(i);
                Assert.assertNotNull("Cell value is null.", cellValue);
            }
        }
    }

    @Test
    public void try_get_cell_row_count_equal_row_count(){
        int indexStart = 0;
        for(Row r: cud){
            int currentRowCount = r.getRowNum();
            Assert.assertEquals("currentRowCount is not equal to ROW_COUNT.", indexStart++, currentRowCount);
        }
    }

    @Test
    public void try_get_sheet_parent_is_not_null(){
        for(Row r: cud){
            Sheet parent = r.getSheet();
            Assert.assertNotNull("parent is null,", parent);
            Assert.assertSame("parent class is not similar.", CSVSheet.class, parent.getClass());
        }
    }

    @Test
    public void try_get_delegate_is_not_null(){
        for(Row r: cud){
            CSVRecord delegate = ((CSVRow)r).getCSVRecord();
            Assert.assertNotNull("delegate is null,", delegate);
            Assert.assertSame("delegate class is not similar.", CSVRecord.class, delegate.getClass());
        }
    }

    @Test
    public void try_get_workboot_parent_is_not_null(){
        for(Row r: cud){
            Workbook workbookParent = ((CSVRow)r).getWorkbook();
            Assert.assertNotNull("delegate is null,", workbookParent);
            Assert.assertSame("workbootParent class is not similar.", CSVWorkbook.class, workbookParent.getClass());
        }
    }

    @Test
    public void try_is_row_hidden_false(){
        for(Row r: cud){
            boolean isRowNotHidden = r.isHidden();
            Assert.assertFalse("Row is hidden.", isRowNotHidden);
        }
    }



}
