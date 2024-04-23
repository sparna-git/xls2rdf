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
import org.apache.commons.io.FileUtils;
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

		// Copy file output from excel
		FileUtils.copyFile(a.getExcel(), a.getOutput());
		// Create workbook instance
		Workbook workbookXSL = WorkbookFactory.create(a.getOutput());
		
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
		List<CSVRecord> cvsRecords = csvParser.getRecords();
				
		MergeCsvToXls merger = new MergeCsvToXls();
		Workbook wb = merger.mergeCsv(cvsRecords, workbookXSL);		
		if (wb != null) {
			
			// Delete output file ????????
			if (a.getOutput().exists()) {
				a.getOutput().delete();				
			}
			
			try (OutputStream outputFile = new FileOutputStream(a.getOutput())) {
				wb.write(outputFile);
				log.debug("Successfully wrote Excel file in " + a.getOutput().getAbsolutePath());
			} catch (Exception e) {
				// TODO: handle exception
				
			}
		}	
	}	
}
