package fr.sparna.rdf.xls2rdf.sheet.csv;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;

public class CSVWorkbookFactory {

    static Logger log = LoggerFactory.getLogger(CSVWorkbookFactory.class.getName());

    private CSVWorkbookFactory(){}

    public static Workbook open(CSVFormat format, Reader reader) throws Exception {
        return new CSVWorkbook(new CSVDelegate(format, reader));
    }

}
