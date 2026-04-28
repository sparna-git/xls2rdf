package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentRow;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;

import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenDocumentRowTest {


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


    List<Row> cud;

    @Before
    public void initProperties() throws Exception {
        this.cud = new ArrayList<>();
        Sheet sheet = OpenDocumentSpreadSheetFactory.open(new FileInputStream(PATH_NAME)).getSheet(SHEET_NAME);
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
            Assert.assertSame("parent class is not similar.", OpenDocumentTable.class, parent.getClass());
        }
    }

    @Test
    public void try_get_delegate_is_not_null(){
        for(Row r: cud){
            OdfTableRow delegate = ((OpenDocumentRow)r).getRow();
            Assert.assertNotNull("delegate is null,", delegate);
            Assert.assertSame("delegate class is not similar.", OdfTableRow.class, delegate.getClass());
        }
    }

    @Test
    public void try_get_workboot_parent_is_not_null(){
        for(Row r: cud){
            Workbook workbookParent = ((OpenDocumentRow)r).getSpreadSheet();
            Assert.assertNotNull("delegate is null,", workbookParent);
            Assert.assertSame("workbootParent class is not similar.", OpenDocumentSpreadSheet.class, workbookParent.getClass());
        }
    }

    @Test
    public void try_is_row_hidden_false(){
        for(Row r: cud){
            boolean isRowNotHidden = r.isHidden();
            Assert.assertFalse("Row is hidden.", isRowNotHidden);
        }
    }


    @Test
    public void try_is_row_hidden_true(){
        for(Row r: cud){
            ((OpenDocumentRow)r).getRow().getOdfElement().setTableVisibilityAttribute(OpenDocumentRow.HIDDEN_VALUE);
            boolean isRowHidden = r.isHidden();
            Assert.assertTrue("Row is not hidden.", isRowHidden);
        }
    }

}
