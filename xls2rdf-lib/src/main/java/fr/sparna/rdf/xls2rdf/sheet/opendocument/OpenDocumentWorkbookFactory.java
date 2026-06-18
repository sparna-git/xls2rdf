package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class OpenDocumentWorkbookFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(OpenDocumentWorkbookFactory.class.getName());

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
