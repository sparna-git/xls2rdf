package fr.sparna.rdf.xls2rdf.app;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.MergeCsvToXls;

public class Merge implements CliCommandIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public void execute(Object args) throws Exception {
		ArgumentsMerge a = (ArgumentsMerge)args;
		
		Workbook workbookXSL = WorkbookFactory.create(a.getExcel());
		
		MergeCsvToXls merger = new MergeCsvToXls();
		Workbook wb = merger.mergeCsv(a.getCsv(), workbookXSL);

		try (OutputStream fileOut = new FileOutputStream(a.getOutput())) {
			wb.write(fileOut);
			wb.close();
			log.debug("Successfully wrote Excel file in " + a.getOutput().getAbsolutePath());
		}		
	}
	
}
