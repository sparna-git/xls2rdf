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
    private final OpenDocumentSheet parentSheet;

    public OpenDocumentRow(OdfTableRow delegate, OpenDocumentSheet parentSheet){
        this.delegate = delegate;
        this.parentSheet = parentSheet;
    }

    @Override
    public Cell getCell(int columnIndex) {
        // if(columnIndex < 0 || columnIndex > this.delegate.getCellCount()) {
        //     return null;
        // }
        // getCellByIndex creates a new cell if it doesn't exist, but we don't want that, so we check the cell count first
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
        return this.parentSheet;
    }

    @Override
    public boolean isHidden() {
        String strIsHidden = this.delegate.getOdfElement().getTableVisibilityAttribute();
        if(strIsHidden == null) return false;
        return HIDDEN_VALUE.equals(strIsHidden);
    }

    public OdfTableRow getRow() {
        return this.delegate;
    }

    public Workbook getWorkbook(){
        return this.parentSheet.getWorkbook();
    }

}
