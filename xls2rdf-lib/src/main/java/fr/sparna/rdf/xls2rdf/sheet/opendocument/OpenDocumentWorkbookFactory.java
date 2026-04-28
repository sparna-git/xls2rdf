package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;

import java.io.File;
import java.io.InputStream;

public class OpenDocumentWorkbookFactory {

    private OpenDocumentWorkbookFactory(){}

    public static Workbook open(OdfSpreadsheetDocument document){
        return new OpenDocumentWorkbook(document);
    }

    public static Workbook open(File file) throws Exception {
        return new OpenDocumentWorkbook(OdfSpreadsheetDocument.loadDocument(file));
    }

    public static Workbook open(InputStream in) throws Exception {
        return new OpenDocumentWorkbook(OdfSpreadsheetDocument.loadDocument(in));
    }
}
