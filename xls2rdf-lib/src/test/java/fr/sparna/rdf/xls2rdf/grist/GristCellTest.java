package fr.sparna.rdf.xls2rdf.grist;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.grist.GristWorkbookFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GristCellTest {

    final static String   DOCUMENT_ID   = "nh8zUVxuPVGRC4cgHWC9bg";
    final static String[] TABLES_NAMES  = {"NodeShapes", "PropertyShapes", "ShaclGraph", "Prefixes"};
    final static int LAST_ROW_NUM = 3;


    Workbook workbook;
    List<Sheet> sheets;
    int columnSize;


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
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println(c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_cell_value_at_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println(c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_row_index_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println("row index is" + c.getRowIndex() + " for " + c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_row_index_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println("row index is" + c.getRowIndex() + " for " + c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_column_index_wih_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println("column index is " + c.getColumnIndex() + " for " + c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_column_index_wih_cache(){
        this.initProperties_with_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 30 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println("column index is" + c.getColumnIndex() + " for " + c.getCellValue());
                }
            }
        }
    }

    @Test
    public void try_get_cell_ref_with_no_cache(){
        this.initProperties_with_no_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 10 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println(c.getCellExcelReference());
                }
            }
        }
    }

    @Test
    public void try_get_cell_ref_with_cache(){
        this.initProperties_with_cache();
        for(Sheet s : sheets){
            Iterator<Row> iter = s.iterator();
            System.out.println("--------------------");
            System.out.println("SHEET = " + s.getSheetName());
            System.out.println("--------------------");
            while(iter.hasNext()){
                Row r = iter.next();
                Assert.assertNotNull("Row is null.", r);
                for (int i = 0; i < 10 ; i++) {
                    Cell c = r.getCell(i);
                    if(c == null) continue;
                    System.out.println(c.getCellExcelReference());
                }
            }
        }
    }
}
