package fr.sparna.rdf.xls2rdf.sheet.excel;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public final class ExcelWorkbookFactory {

    static Logger log = LoggerFactory.getLogger(ExcelWorkbookFactory.class.getName());

    private ExcelWorkbookFactory() {}

    public static Workbook open(org.apache.poi.ss.usermodel.Workbook wb) {
        return new ExcelWorkbook(wb);
    }

    public static Workbook open(File file) throws Exception {
        return new ExcelWorkbook(WorkbookFactory.create(file));
    }

    public static Workbook open(InputStream in) throws Exception {
        return new ExcelWorkbook(WorkbookFactory.create(in));
    }
}
