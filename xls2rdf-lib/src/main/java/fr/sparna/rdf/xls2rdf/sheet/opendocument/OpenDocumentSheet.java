package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.jetbrains.annotations.NotNull;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class OpenDocumentSheet implements Sheet {

    static Logger log = LoggerFactory.getLogger(OpenDocumentSheet.class);
    public final static String HIDDEN_VALUE     = "collapse";

    private final OdfTable delegate;
    private final OpenDocumentWorkbook parentSpreadSheet;

    public OpenDocumentSheet(OdfTable delegate, OpenDocumentWorkbook parentSpreadSheet){
        this.delegate = delegate;
        this.parentSpreadSheet = parentSpreadSheet;
    }

    @Override
    public String getSheetName() {
        return this.delegate.getTableName();
    }

    @Override
    public int getLastRowNum() {
        return this.delegate.getRowCount() - 1;
    }

    @Override
    public Row getRow(int rowIndex) {
        return new OpenDocumentRow(this.delegate.getRowByIndex(rowIndex), this);
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        String strIsHidden = this.delegate.getColumnByIndex(columnIndex).getOdfElement().getTableVisibilityAttribute();
        if(strIsHidden == null) return false;
        return HIDDEN_VALUE.equals(strIsHidden);
    }

    @NotNull
    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            private int index;
            @Override
            public boolean hasNext() {
                return index < getLastRowNum();
            }

            @Override
            public Row next() {
                return getRow(index++);
            }
        };
    }

    public OdfTable getOdfTable(){
        return this.delegate;
    }

    public OpenDocumentWorkbook getParentSpreadSheet(){
        return this.parentSpreadSheet;
    }

}
