package fr.sparna.rdf.xls2rdf.model.excel;

import java.io.File;
import java.io.InputStream;

import fr.sparna.rdf.xls2rdf.model.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public final class ExcelWorkbookFactory {
    private ExcelWorkbookFactory() {}

    public static Workbook open(File file) throws Exception {
        return new ExcelWorkbook(WorkbookFactory.create(file));
    }

    public static Workbook open(InputStream in) throws Exception {
        return new ExcelWorkbook(WorkbookFactory.create(in));
    }
}
