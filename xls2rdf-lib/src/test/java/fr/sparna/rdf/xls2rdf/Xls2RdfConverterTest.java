package fr.sparna.rdf.xls2rdf;

import junit.framework.TestSuite;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/**
 * Don't rename this class , it has to end with *Test to be picked up my Maven surfefire plugin
 *
 */
@RunWith(AllTests.class)
public class Xls2RdfConverterTest {

    public static TestSuite suite() {
        //TEST FOR EXCEL
        TestSuite suite = new TestSuite();
        File xlsDir = new File("src/test/resources/excel/suite");
        List<File> sortedList = Arrays.asList(xlsDir.listFiles());
        Collections.sort(sortedList);
        for (File aDir : sortedList) {
        	if(aDir.isDirectory()) {
        		suite.addTest(new Xls2RdfConverterTestExecution(aDir));
        	}
		}    
        

        //TEST FOR ODS
        File odsDir = new File("src/test/resources/opendocument/suite");
        List<File> sortedOds = Arrays.asList(odsDir.listFiles());
        for (File aDir : sortedOds) {
            if(aDir.isDirectory()) {
                suite.addTest(new Xls2RdfConverterTestExecution(aDir));
            }
        }

        /*
        //TEST FOR CSV
        File csvDir = new File("src/test/resources/csv/suite");
        List<File> sortedCsv = Arrays.asList(csvDir.listFiles());
        for (File aDir : sortedCsv) {
            if(aDir.isDirectory()) {
                suite.addTest(new Xls2RdfConverterTestExecution(aDir));
            }
        }
        */

        return suite;
    }

}
