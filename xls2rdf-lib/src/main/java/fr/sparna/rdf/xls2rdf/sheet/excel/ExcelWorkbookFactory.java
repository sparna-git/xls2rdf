package fr.sparna.rdf.xls2rdf.sheet.excel;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.WorkbookFactory;

import fr.sparna.rdf.xls2rdf.sheet.Workbook;

public final class ExcelWorkbookFactory {
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
