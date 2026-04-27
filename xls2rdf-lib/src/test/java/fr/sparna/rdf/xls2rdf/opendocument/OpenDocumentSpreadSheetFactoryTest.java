package fr.sparna.rdf.xls2rdf.opendocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;


import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheet;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentSpreadSheetFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;

public class OpenDocumentSpreadSheetFactoryTest {

    static final URL ODS_FILE_URL = OpenDocumentSpreadSheetFactoryTest.class.getResource("/opendocument/test.ods");
    static final String PATH_NAME;
    static{
        try {
            Assert.assertNotNull(ODS_FILE_URL);
            PATH_NAME = ODS_FILE_URL.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    InputStream odsAsInputStream;
    File odsAsFile;
    OdfSpreadsheetDocument odsDocument;


    @Before
    public void initProperties() throws Exception {
        this.odsAsInputStream = new FileInputStream(PATH_NAME);
        Assert.assertNotNull("odsAsInputStream document is null.", odsAsInputStream);

        this.odsAsFile        = new File(PATH_NAME);
        Assert.assertNotNull("odsAsFile document is null.", odsAsFile);

        this.odsDocument      = OdfSpreadsheetDocument.loadDocument(PATH_NAME);
        Assert.assertNotNull("odsDocument is null.", odsDocument);
    }

    @After
    public void closeIn() throws IOException {
        this.odsAsInputStream.close();
    }


    @Test
    public void openDocumentFactoryIn() throws Exception {
        Workbook doc = OpenDocumentSpreadSheetFactory.open(odsAsInputStream);
       Assert.assertNotNull("openDocumentFactoryIn doc is null.", doc);
    }
    @Test
    public void openDocumentFactoryFile() throws Exception {
        Workbook doc = OpenDocumentSpreadSheetFactory.open(odsAsFile);
        Assert.assertNotNull("openDocumentFactoryFile doc is null.", doc);
    }
    @Test
    public void openDocumentFactoryDoc() throws Exception {
        Workbook doc = OpenDocumentSpreadSheetFactory.open(OdfSpreadsheetDocument.loadDocument(PATH_NAME));
        Assert.assertNotNull("openDocumentFactoryDoc doc is null.", doc);
    }
}
