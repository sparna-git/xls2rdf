package fr.sparna.rdf.xls2rdf.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
		// Parser csv File
		InputStream inFile = new FileInputStream(a.getCsv());
		CSVParser csvParser = new CSVParser(
				new InputStreamReader(inFile),				
				CSVFormat.DEFAULT.builder()
				.setDelimiter(";")
				.setSkipHeaderRecord(true)
				.build()
		);
		// get Records values
		List<CSVRecord> cvsRecord = csvParser.getRecords();
		
		
		MergeCsvToXls merger = new MergeCsvToXls();
		Workbook wb = merger.mergeCsv(cvsRecord, workbookXSL);
		
		if (wb != null) {
			try (OutputStream fileOut = new FileOutputStream(a.getOutput())) {
				wb.write(fileOut);
				wb.close();
				log.debug("Successfully wrote Excel file in " + a.getOutput().getAbsolutePath());
			}
		}	
	}
	
}
