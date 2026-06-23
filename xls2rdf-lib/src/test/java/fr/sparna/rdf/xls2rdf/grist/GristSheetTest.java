package fr.sparna.rdf.xls2rdf.grist;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GristSheetTest {


    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};
    final static int LAST_ROW_NUM = 3;


    Workbook workbook;
    List<Sheet> cud;


    public void initProperties_with_cache(){
        this.cud = new ArrayList<>();
        this.workbook = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), true);
            for(String n : TABLES_NAMES){
                this.cud.add(workbook.getSheet(n));
            }
    }

    public void initProperties_with_no_cache(){
        this.cud = new ArrayList<>();
        this.workbook = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), false);
        for(String n : TABLES_NAMES){
            this.cud.add(workbook.getSheet(n));
        }
    }

    @Test
    public void try_get_sheet_name_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : this.cud){
            System.out.println("Sheet name : " + s.getSheetName());
            Assert.assertTrue("Sheet's name is not recognized.", Arrays.deepToString(TABLES_NAMES).contains(s.getSheetName()));
        }
    }

    @Test
    public void try_get_sheet_name_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : this.cud){
            System.out.println("Sheet name : " + s.getSheetName());
            Assert.assertTrue("Sheet's name is not recognized.", Arrays.deepToString(TABLES_NAMES).contains(s.getSheetName()));
        }
    }

    @Test
    public void try_get_sheet_last_row_num_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : this.cud){
            Assert.assertTrue("Sheet's last row num is not correct..", LAST_ROW_NUM == s.getLastRowNum());
            System.out.println("Last row num = " + s.getLastRowNum());
        }
    }

    @Test
    public void try_get_sheet_last_row_num_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : this.cud){
            Assert.assertTrue("Sheet's last row num is not correct..", LAST_ROW_NUM == s.getLastRowNum());
            System.out.println("Last row num = " + s.getLastRowNum());
        }
    }

    @Test
    public void try_iter_on_sheet_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : this.cud){
            Iterator<Row> iter = s.iterator();
            int index = 0;
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                Assert.assertEquals("Row num is not correct", index++, r.getRowNum());
            }
        }
    }

    @Test
    public void try_iter_on_sheet_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : this.cud){
            Iterator<Row> iter = s.iterator();
            int index = 0;
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                Assert.assertEquals("Row num is not correct", index++, r.getRowNum());
            }
        }
    }





}
