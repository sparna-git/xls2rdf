package fr.sparna.rdf.xls2rdf.grist;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class GristWorkbookTest {

    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};

    GristWorkbook cud;


    @Test
    public void try_get_sheet_by_name_with_no_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), false);
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(TABLES_NAMES[i]);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_name_with_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), true);
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(TABLES_NAMES[i]);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_index_with_no_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), false);
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(i);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_get_sheet_by_index_with_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), true);
        Assert.assertNotNull("Class under test is null.", this.cud);
        for (int i = 0; i < TABLES_NAMES.length ; i++) {
            Sheet s = this.cud.getSheet(i);
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_iter_on_sheet_with_no_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), false);
        Iterator<Sheet> iter = this.cud.iterator();
        while(iter.hasNext()){
            Sheet s = iter.next();
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

    @Test
    public void try_iter_on_sheet_with_cache(){
        this.cud = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), true);
        Iterator<Sheet> iter = this.cud.iterator();
        while(iter.hasNext()){
            Sheet s = iter.next();
            Assert.assertNotNull("Sheet is null.", s);
            System.out.println(s.getSheetName());
        }
    }

}
