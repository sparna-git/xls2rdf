package fr.sparna.rdf.xls2rdf.csv;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class CSVWorkbookTest {

    static final URL CSV_FILE_URL = fr.sparna.rdf.xls2rdf.opendocument.OpenDocumentWorkbookFactoryTest.class.getResource("/csv/test.csv");
    static final URI CSV_FILE_URI;
    static{
        try {
            Assert.assertNotNull(CSV_FILE_URL);
            CSV_FILE_URI = CSV_FILE_URL.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    Workbook cud;

    @Before
    public void initProperties() throws Exception {
        this.cud = CSVWorkbookFactory.open(CSVFormat.DEFAULT, new InputStreamReader(new FileInputStream(Paths.get(CSV_FILE_URI).toFile())));
        Assert.assertNotNull("class under test is null.", cud);
    }

    @Test
    public void try_get_sheet_by_name_is_not_null(){
            Sheet sheet = this.cud.getSheet("ANY NAME");
            Assert.assertNotNull("sheet is null.", sheet);

    }

    @Test
    public void try_get_sheet_by_id_is_not_null(){
            Sheet sheet = this.cud.getSheet(57);
            Assert.assertNotNull("sheet is null.", sheet);

    }

    @Test
    public void try_get_delegate_is_not_null(){
        CSVFormat delegate = ((CSVWorkbook)this.cud).getCSVBridge().getFormat();
        Assert.assertNotNull("delegate object is null.", delegate);
        Assert.assertSame("delegate class is not similar.", CSVFormat.class, delegate.getClass());
    }

    @Test
    public void try_iterator_on_sheets_are_not_null(){
        for(Sheet s: this.cud){
            Assert.assertNotNull("The iterator returned null value for the sheet.", s);
        }
    }

}
