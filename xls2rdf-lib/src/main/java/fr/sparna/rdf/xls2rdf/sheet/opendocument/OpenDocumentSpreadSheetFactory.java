package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;

import java.io.File;
import java.io.InputStream;

public class OpenDocumentSpreadSheetFactory {

    private OpenDocumentSpreadSheetFactory(){}

    public static OpenDocumentSpreadSheet open(OdfSpreadsheetDocument document){
        return new OpenDocumentSpreadSheet(document);
    }

    public static OpenDocumentSpreadSheet open(File file) throws Exception {
        return new OpenDocumentSpreadSheet(OdfSpreadsheetDocument.loadDocument(file));
    }

    public static OpenDocumentSpreadSheet open(InputStream in) throws Exception {
        return new OpenDocumentSpreadSheet(OdfSpreadsheetDocument.loadDocument(in));
    }
}
