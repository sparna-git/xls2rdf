package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;

import java.io.File;
import java.io.InputStream;

public class OpenDocumentSpreadSheetFactory {

    private OpenDocumentSpreadSheetFactory(){}

    public static Workbook open(OdfSpreadsheetDocument document){
        return new OpenDocumentSpreadSheet(document);
    }

    public static Workbook open(File file) throws Exception {
        return new OpenDocumentSpreadSheet(OdfSpreadsheetDocument.loadDocument(file));
    }

    public static Workbook open(InputStream in) throws Exception {
        return new OpenDocumentSpreadSheet(OdfSpreadsheetDocument.loadDocument(in));
    }
}
