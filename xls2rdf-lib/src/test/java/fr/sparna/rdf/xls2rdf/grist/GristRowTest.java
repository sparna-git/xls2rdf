package fr.sparna.rdf.xls2rdf.grist;

import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GristRowTest {

    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};
    final static int LAST_ROW_NUM = 3;


    Workbook workbook;
    List<Sheet> sheets;


    public void initProperties_with_cache(){
        this.sheets = new ArrayList<>();
        this.workbook = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), true);
        for(String n : TABLES_NAMES){
            this.sheets.add(workbook.getSheet(n));
        }
    }

    public void initProperties_with_no_cache(){
        this.sheets = new ArrayList<>();
        this.workbook = GristWorkbookFactory.open(DOCUMENT_ID, System.getProperty("gristToken"), false);
        for(String n : TABLES_NAMES){
            this.sheets.add(workbook.getSheet(n));
        }
    }

    @Test
    public void try_get_cell_value_at_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : sheets){
            int index = 0;
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                System.out.println("--------------------");
                System.out.println("ROW NUMBER ==== " + index++);
                System.out.println("--------------------");
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    System.out.println(r.getColumnValue(i));
                }
            }
        }
    }

    @Test
    public void try_get_cell_value_at_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : sheets){
            int index = 0;
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                System.out.println("--------------------");
                System.out.println("ROW NUMBER ==== " + index++);
                System.out.println("--------------------");
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    System.out.println(r.getColumnValue(i));
                }
            }
        }
    }

    @Test
    public void try_get_row_num_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                System.out.println("row num = " + r.getRowNum());

            }
        }
    }

    @Test
    public void try_get_row_num_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                System.out.println("row num = " + r.getRowNum());
            }
        }
    }

}
