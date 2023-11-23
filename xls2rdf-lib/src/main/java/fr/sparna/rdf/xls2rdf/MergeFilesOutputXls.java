package fr.sparna.rdf.xls2rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MergeFilesOutputXls {
		
	static Logger log = LoggerFactory.getLogger(RdfizableSheet.class.getName());

	public Workbook mergeFile(File csvfile, Workbook workbookXLS) throws InvalidFormatException, IOException {
		
		// Parser csv File
		InputStream inFile = new FileInputStream(csvfile);
		CSVParser csvParser = new CSVParser(new InputStreamReader(inFile), CSVFormat.DEFAULT
																		.withFirstRecordAsHeader()
																		.withDelimiter(';'));
		// Create outputXLS Workbook and load data
		Workbook workBookOutput = workbookXLS;
		
		int indexSheet = workBookOutput.getSheetIndex(getSheetActive(workBookOutput));
		int rowIndexTitle = get_rowIndexTitle(workBookOutput);
		int nRow = rowIndexTitle;
		// iterate on all CSV Records
		for (CSVRecord dataRecord : csvParser.getRecords()) {
			nRow++;
			int nCol = 0;
			Row newRow = workBookOutput.getSheetAt(indexSheet).createRow(nRow);
			for (String columnNameCSV : dataRecord.getParser().getHeaderNames()) {
				for (Cell columnName : workBookOutput.getSheetAt(indexSheet).getRow(rowIndexTitle)) {
					if (columnNameCSV.equals(columnName.toString())) {
						// create cell
						Cell newCell = newRow.createCell(nCol++);
						// Save of data value in workbook			
						newCell.setCellValue(dataRecord.get(columnNameCSV));
						break;
					}
					
				}
			}
		}		
		return workBookOutput;		
	}
	
	public PrefixManager getPrefixes(Workbook workbook) throws InvalidFormatException, IOException {
		// Start instance
		PrefixManager prefixManager = new PrefixManager();
		// register prefixes
		for (Sheet sheet : workbook) {
			prefixManager.register(RdfizableSheet.readPrefixes(sheet));
		}
		
		return prefixManager;
	}

	public int get_rowIndexTitle(Workbook wbXslfile) throws InvalidFormatException, IOException {
		
		int titleRowIndex = 0;
		
		int numberOfSheetIndex = wbXslfile.getSheetIndex(getSheetActive(wbXslfile));
		Sheet sheetToMerge = wbXslfile.getSheetAt(numberOfSheetIndex);
		
		// Get all Prefixes from to xls file 
		PrefixManager prefixManager = getPrefixes(wbXslfile);
		
		RdfizableSheet rdfizableSheet = new RdfizableSheet(sheetToMerge, prefixManager);
		
		if (rdfizableSheet.computeTitleRowIndex() == 1) {
			titleRowIndex = rdfizableSheet.computeTitleRowIndex()-1;
		} else {
			titleRowIndex = rdfizableSheet.computeTitleRowIndex();
		}
		
		return titleRowIndex;
	}
	
	public String getSheetActive(Workbook wbInput) throws InvalidFormatException, IOException {
	    
	    // Get all prefix
	    PrefixManager prefixManager = getPrefixes(wbInput);
	    
	    // find the sheet to merger
	    String nameSheet = "";
	    for (Sheet sheet : wbInput) {
	      RdfizableSheet rdfizableSheetActive = new RdfizableSheet(sheet, prefixManager);
	      if (rdfizableSheetActive.canRDFize()) {
	        nameSheet = sheet.getSheetName();
	        break;
	      }
	    }
	    
	    return nameSheet;
	  }
		
}