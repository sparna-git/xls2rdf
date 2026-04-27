package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;


public class OpenDocumentSpreadSheet implements Workbook {

    static Logger log = LoggerFactory.getLogger(OpenDocumentSpreadSheet.class.getName());

    private final OdfSpreadsheetDocument delegate;

    public OpenDocumentSpreadSheet(OdfSpreadsheetDocument delegate){
        this.delegate = delegate;
    }


    @Override
    public Sheet getSheet(int index) {
        return new OpenDocumentTable(this.delegate.getSpreadsheetTables().get(index), this);
    }

    @Override
    public Sheet getSheet(String name) {
        Optional<OdfTable> table = this.delegate.getSpreadsheetTables()
                .stream().
                filter(odfTable -> odfTable.getTableName().equals(name))
                .findFirst();
        return table.map(odfTable -> new OpenDocumentTable(odfTable, this)).orElse(null);
    }

    @NotNull
    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<Sheet>() {
            private int index = 0;
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
