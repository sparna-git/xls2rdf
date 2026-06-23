package fr.sparna.rdf.xls2rdf.grist;

import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import org.junit.Assert;
import org.junit.Test;

public class GristWorkbookFactoryTest {

    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";

    GristWorkbook cud;


    @Test
    public void try_get_workbook_is_not_null_with_no_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), false);
        Assert.assertNotNull("Class under test is null.", this.cud);
    }

    @Test
    public void try_get_workbook_is_not_null_with_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), true);
        Assert.assertNotNull("Class under test is null.", this.cud);
    }


}
