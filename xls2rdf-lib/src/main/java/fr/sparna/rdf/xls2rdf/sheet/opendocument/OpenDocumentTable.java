package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.jetbrains.annotations.NotNull;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class OpenDocumentTable implements Sheet {

    static Logger log = LoggerFactory.getLogger(OpenDocumentTable.class);
    public final static String HIDDEN_VALUE     = "collapse";

    private final OdfTable delegate;
    private final OpenDocumentSpreadSheet parentSpreadSheet;

    public OpenDocumentTable(OdfTable delegate, OpenDocumentSpreadSheet parentSpreadSheet){
        this.delegate = delegate;
        this.parentSpreadSheet = parentSpreadSheet;
    }

    @Override
    public String getSheetName() {
        return this.delegate.getTableName();
    }

    @Override
    public int getLastRowNum() {
        return this.delegate.getRowCount();
    }

    @Override
    public Row getRow(int rowIndex) {
        return new OpenDocumentRow(this.delegate.getRowByIndex(rowIndex), this);
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        return HIDDEN_VALUE.equals(this.delegate.getColumnByIndex(columnIndex).getOdfElement().getTableVisibilityAttribute());
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

    public OpenDocumentSpreadSheet getParentSpreadSheet(){
        return this.parentSpreadSheet;
    }

}
