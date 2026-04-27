package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.CellType;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentCell;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentRow;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.NamespaceName;
import org.odftoolkit.odfdom.pkg.OdfAttribute;
import org.odftoolkit.odfdom.pkg.OdfName;
import org.odftoolkit.odfdom.pkg.OdfNamespace;

import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static fr.sparna.rdf.xls2rdf.sheet.CellType.*;

public class OpenDocumentCellTest {


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

    List<Row> rows;

    @Before
    public void initProperties() throws Exception {
        this.rows = new ArrayList<>();
        Sheet sheet = OpenDocumentSpreadSheetFactory.open(new FileInputStream(PATH_NAME)).getSheet(SHEET_NAME);
        for(int i = 0; i < ROW_COUNT; i++){
            this.rows.add(sheet.getRow(i));
        }
    }

    @Test
    public void try_get_cell_type(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                CellType type = r.getCell(i).getCellType();
                switch(type){
                    case BLANK -> Assert.assertEquals(BLANK, type);
                    case ODS_DATE -> Assert.assertEquals(ODS_DATE, type);
                    case ODS_CURRENCY -> Assert.assertEquals(ODS_CURRENCY, type);
                    case ODS_PERCENTAGE -> Assert.assertEquals(ODS_PERCENTAGE, type);
                    case ODS_FLOAT -> Assert.assertEquals(ODS_FLOAT, type);
                    case ODS_TIME -> Assert.assertEquals(ODS_TIME, type);
                    case ODS_STRING -> Assert.assertEquals(ODS_STRING, type);
                }
            }
        }
    }

    @Test
    public void try_get_cell_value(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                String value = r.getCell(i).getCellValue();
                Assert.assertNotNull("Cell value is null.", value);
            }
        }
    }

    @Test
    public void try_get_parent(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Row parent = cell.getRow();
                Assert.assertTrue(parent == r);
            }
        }
    }

    @Test
    public void try_get_row_index(){
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
    public void try_get_column_index(){
        for(Row r: this.rows){
            int indexColumn = 0;
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                Assert.assertEquals("Row index is not correct", indexColumn++, cell.getColumnIndex());
            }
        }
    }

    @Test
    public void try_get_sheet_parent(){
        Sheet sheetParent;
        for(Row r: this.rows){
            sheetParent = r.getSheet();
            Assert.assertNotNull("sheetParent is null.", sheetParent);
        }
    }

    @Test
    public void try_get_delegate(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                OdfTableCell delegate = ((OpenDocumentCell)cell).getCell();
                Assert.assertNotNull("delegate is null", delegate);
                Assert.assertSame("delegate class is not similar.", OdfTableCell.class, delegate.getClass());
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
    public void try_is_cell_struck_through_true(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                String styleName = ((OpenDocumentCell)cell).getCell().getStyleName();
                OdfStyle style = ((OpenDocumentCell)cell).getCell().getOdfElement().getAutomaticStyles().getStyle(styleName, OdfStyleFamily.TableCell);
                System.out.println(style.getProperty(OdfStyleProperty.get(OdfStylePropertiesSet.TextProperties, OdfName.newName("style:"+OpenDocumentCell.LINE_THROUGH_ATTRIBUTE))));
                style.setAttribute(OpenDocumentCell.LINE_THROUGH_ATTRIBUTE, OpenDocumentCell.LINE_THROUGH_VALUE);
                Assert.assertTrue("Cell is not struck.", cell.isStruckThrough());
            }
        }
    }
}
