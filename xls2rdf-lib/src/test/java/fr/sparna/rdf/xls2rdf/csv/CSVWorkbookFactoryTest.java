package fr.sparna.rdf.xls2rdf.csv;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;

public class CSVWorkbookFactoryTest {


    static final URL CSV_FILE_URL = fr.sparna.rdf.xls2rdf.opendocument.OpenDocumentWorkbookFactoryTest.class.getResource("/csv/test.csv");
    static final String PATH_NAME;
    static{
        try {
            Assert.assertNotNull(CSV_FILE_URL);
            PATH_NAME = CSV_FILE_URL.toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    BufferedReader csvAsReader;
    File csvAsFile;
    CSVFormat csvFormat;

    @Before
    public void initProperties() throws Exception {

        this.csvAsFile = new File(PATH_NAME);
        Assert.assertNotNull("csvAsFile document is null.", csvAsFile);

        this.csvAsReader = new BufferedReader(new FileReader(csvAsFile));
        Assert.assertNotNull("csvAsInputStream document is null.", csvAsReader);

        this.csvFormat = CSVFormat.DEFAULT;
        Assert.assertNotNull("csvFormat is null.", csvFormat);
    }

    @Test
    public void csvFactoryIn() throws Exception {
        Workbook doc = CSVWorkbookFactory.open(this.csvFormat, this.csvAsFile, this.csvAsReader);
        Assert.assertNotNull("csvFactoryIn doc is null.", doc);
    }

}
