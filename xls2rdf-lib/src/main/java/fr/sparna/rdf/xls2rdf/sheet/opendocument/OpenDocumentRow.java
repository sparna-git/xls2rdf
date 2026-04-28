package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenDocumentRow implements Row {

    static Logger log = LoggerFactory.getLogger(OpenDocumentRow.class);
    public final static String HIDDEN_VALUE = "collapse";

    private final OdfTableRow delegate;
    private final OpenDocumentTable parentTable;

    public OpenDocumentRow(OdfTableRow delegate, OpenDocumentTable parentTable){
        this.delegate = delegate;
        this.parentTable = parentTable;
    }

    @Override
    public Cell getCell(int columnIndex) {
        return new OpenDocumentCell(this.delegate.getCellByIndex(columnIndex), this);
    }

    @Override
    public String getColumnValue(int columnIndex) {
        Cell cell = getCell(columnIndex);
        return cell == null ? null : cell.getCellValue();
    }

    @Override
    public int getRowNum() {
        return this.delegate.getRowIndex();
    }

    @Override
    public Sheet getSheet() {
        return this.parentTable;
    }

    @Override
    public boolean isHidden() {
        return HIDDEN_VALUE.equals(this.delegate.getOdfElement().getTableVisibilityAttribute());
    }

    public OdfTableRow getRow() {
        return this.delegate;
    }

    public Workbook getSpreadSheet(){
        return this.parentTable.getParentSpreadSheet();
    }

}
