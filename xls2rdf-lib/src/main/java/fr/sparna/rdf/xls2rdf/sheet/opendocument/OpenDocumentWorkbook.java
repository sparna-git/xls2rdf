package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.jetbrains.annotations.NotNull;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;


public class OpenDocumentWorkbook implements Workbook {

    static Logger log = LoggerFactory.getLogger(OpenDocumentWorkbook.class.getName());

    private final OdfSpreadsheetDocument delegate;

    public OpenDocumentWorkbook(OdfSpreadsheetDocument delegate){
        this.delegate = delegate;
    }


    @Override
    public Sheet getSheet(int index) {
        return new OpenDocumentSheet(this.delegate.getSpreadsheetTables().get(index), this);
    }

    @Override
    public Sheet getSheet(String name) {
        OdfTable table = this.delegate.getTableByName(name);
        return table != null ? new OpenDocumentSheet(table, this) : null;
    }

    @NotNull
    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<Sheet>() {
            private int index;
            @Override
            public boolean hasNext() {
                return index < delegate.getSpreadsheetTables().size();
            }

            @Override
            public Sheet next() {
                return getSheet(index++);
            }
        };
    }

    public OdfSpreadsheetDocument getSpreadsheetDocument(){
        return this.delegate;
    }

}
