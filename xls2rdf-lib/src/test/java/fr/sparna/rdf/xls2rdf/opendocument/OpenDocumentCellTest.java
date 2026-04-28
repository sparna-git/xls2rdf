package fr.sparna.rdf.xls2rdf.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.CellType;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentCell;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentRow;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentTable;
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
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
                    case BLANK   -> Assert.assertEquals(BLANK, type);
                    case STRING  -> Assert.assertEquals(STRING, type);
                    case NUMERIC -> Assert.assertEquals(NUMERIC, type);
                    case BOOLEAN -> Assert.assertEquals(BOOLEAN, type);
                    case FORMULA -> Assert.assertEquals(FORMULA, type);
                    default      -> Assert.assertEquals(ERROR, type);
                }
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
                Assert.assertTrue("parent class is not similar.", parent == r);
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
                Assert.assertEquals(OpenDocumentTable.class, parent.getClass());
            }
        }
    }

    @Test
    public void try_get_delegate_is_not_null(){
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
                applyStruckThrough.accept(cell, false);
                Assert.assertFalse("Cell is struck.", cell.isStruckThrough());
            }
        }
    }

    @Test
    public void try_is_cell_struck_through_true(){
        for(Row r: this.rows){
            for(int i = 0; i < COLUMN_COUNT; i++){
                Cell cell = r.getCell(i);
                applyStruckThrough.accept(cell, true);
                Assert.assertTrue("Cell is not struck.", cell.isStruckThrough());
            }
        }
    }

    BiConsumer<Cell, Boolean> applyStruckThrough = (cell, b) -> {
        OdfTableCell c = ((OpenDocumentCell)cell).getCell();
        //On récupére le nom du style appliqué pour la cell courante
        String styleName = c.getStyleName();
        //On récupére l'objet OdfStyle associé à la cell courante
        OdfStyle style = c.getOdfElement().getAutomaticStyles().getStyle(styleName, OdfStyleFamily.TableCell);
        //On récupére l'objet OdfStyleProperty associé à text-line-through-style
        OdfStyleProperty styleProperty = OdfStyleProperty
                .get(OdfStylePropertiesSet.TextProperties, OdfName.getOdfName(
                        OdfNamespace.getNamespace(OdfDocumentNamespace.STYLE.getUri()),
                        "text-line-through-style"));
        //On associe la propriété de style text-line-through-style, soit solid soit none
        if (b) {
            style.setProperty(styleProperty, "solid");
        } else {
            style.setProperty(styleProperty, "none");
        }
    };


    @Test
    public void display_cell_position_type_value() {
        for (Row r : this.rows) {
            System.out.println("Row : " + r.getRowNum());
            for (int i = 0; i < COLUMN_COUNT; i++) {
                Cell c = r.getCell(i);
                System.out.println(c.getCellExcelReference() + " type = " + c.getCellType() + " value = " + c.getCellValue());
            }
            System.out.println("------------");
        }
    }


}
