package fr.sparna.rdf.xls2rdf;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Don't rename this class , it has to end with *Test to be picked up my Maven surfefire plugin
 *
 */


public class Xls2RdfConverterTest {

    @Test
    public void runExcel() {
        //TEST FOR EXCEL
        File xlsDir = new File("src/test/resources/excel/suite");
        List<File> sortedList = Arrays.asList(xlsDir.listFiles());
        Collections.sort(sortedList);
        for (File aDir : sortedList) {
        	if(aDir.isDirectory()) {
        		new Xls2RdfConverterTestExecution(aDir).run();
        	}
		}
    }

    @Test
    public void runOds() {
        //TEST FOR ODS
        File odsDir = new File("src/test/resources/opendocument/suite");
        List<File> sortedOds = Arrays.asList(odsDir.listFiles());
        for (File aDir : sortedOds) {
            if(aDir.isDirectory()) {
                new Xls2RdfConverterTestExecution(aDir).run();
            }
        }
    }

    @Test
    public void runCsv() {
        //TEST FOR CSV
        File csvDir = new File("src/test/resources/csv/suite");
        List<File> sortedCsv = Arrays.asList(csvDir.listFiles());
        for (File aDir : sortedCsv) {
            if(aDir.isDirectory()) {
                new Xls2RdfConverterTestExecution(aDir).run();
            }
        }
    }



}
