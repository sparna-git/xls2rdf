package fr.sparna.rdf.xls2rdf.opendocument;

import java.io.File;
import java.io.IOException;

import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.junit.Test;

public class JOpenDocumentTest {

    final static String ODS_FILE_NAME = "src/test/resources/opendocument/test.ods";
    final static File odsFile = new File(ODS_FILE_NAME);
    static ODPackage CUD;
    static{
        try{
           CUD =  ODPackage.createFromFile(odsFile); 
        }catch(IOException ex){
            
        }
    }


    @Test
    public void test1() throws IOException{
        System.out.println("hello world");
        System.out.println(CUD.getSpreadSheet().getSheet(0).getCellAt(0, 0).getValue());
        System.out.println(CUD.getSpreadSheet().getSheet(0).getRowCount());
        int rowCount = CUD.getSpreadSheet().getSheet(0).getRowCount();

        SpreadSheet sh = SpreadSheet.createFromFile(odsFile);
        Sheet s = sh.getSheet(0);
        s.getmvn

        
    }
    
}
