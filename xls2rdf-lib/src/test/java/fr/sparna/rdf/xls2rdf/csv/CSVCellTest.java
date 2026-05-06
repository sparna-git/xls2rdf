package fr.sparna.rdf.xls2rdf.csv;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.CellType;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVSheet;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static fr.sparna.rdf.xls2rdf.sheet.CellType.ERROR;

public class CSVCellTest {


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
    static final String SHEET_NAME = "A VALIDER";

    List<Row> rows;

    @Before
    public void initProperties() throws Exception {
        this.rows = new ArrayList<>();
        Sheet sheet = CSVWorkbookFactory.open(CSVFormat.DEFAULT, new File(CSV_FILE_URI), Files.newBufferedReader(Path.of(CSV_FILE_URI))).getSheet(0);
        for(int i = 0; i < ROW_COUNT; i++){
            this.rows.add(sheet.getRow(i));
        }
        Assert.assertNotNull("class under test is null.", this.rows);
    }

    @Test
    public void try_get_cell_type(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                CellType type = r.getCell(i).getCellType();
                Assert.assertEquals("Cell's type is not Error.", ERROR, type);
            }
        }
    }

    @Test
    public void try_get_cell_value_is_not_null(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                String value = r.getCell(i).getCellValue();
                Assert.assertNotNull("Cell value is null.", value);
            }
        }
    }

    @Test
    public void try_get_parent_is_not_null(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Row parent = cell.getRow();
                Assert.assertNotNull("parent is null.", parent);
                Assert.assertSame("parent class is not similar.", parent.getClass(), r.getClass());
            }
        }
    }

    @Test
    public void try_get_row_index_is_equal(){
        int indexRow = 0;
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Assert.assertEquals("Row index is not correct", indexRow, cell.getRowIndex());
            }
            indexRow++;
        }
    }

    @Test
    public void try_get_column_index_is_equal(){
        for(Row r: this.rows){
            int indexColumn = 0;
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Assert.assertEquals("Row index is not correct", indexColumn++, cell.getColumnIndex());
            }
        }
    }

    @Test
    public void try_get_sheet_parent_is_not_null(){
        Sheet sheetParent;
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Sheet parent = cell.getSheet();
                Assert.assertNotNull("parent is null.", parent);
                Assert.assertEquals(CSVSheet.class, parent.getClass());
            }
        }
    }

    @Test
    public void try_is_cell_struck_through_false(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Assert.assertFalse("Cell is struck.", cell.isStruckThrough());
            }
        }
    }

    @Test
    public void display_cell_position_type_value() {
        for (Row r : this.rows) {
            System.out.println("Row : " + r.getRowNum());
            for (int i = 0; i < COLUMN_COUNT; i++) {
                Cell c = r.getCell(i);
                System.out.println(c.getCellExcelReference());
            }
            System.out.println("------------");
        }
    }
}
