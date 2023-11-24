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


public class MergeCsvToXls {
		
	static Logger log = LoggerFactory.getLogger(RdfizableSheet.class.getName());

	// TODO : prendre en paramètre l'objet CSVParser
	public Workbook mergeCsv(File csvfile, Workbook workbookXLS) throws InvalidFormatException, IOException {
		
		// Parser csv File
		InputStream inFile = new FileInputStream(csvfile);
		
		
		CSVParser csvParser = new CSVParser(
				new InputStreamReader(inFile),				
				CSVFormat.DEFAULT.builder()
				.setDelimiter(';')
				.setSkipHeaderRecord(true).build()
		);
		
		// Create outputXLS Workbook and load data
		Workbook workBookOutput = workbookXLS;
		
	    // Get all prefix
	    PrefixManager prefixManager = getPrefixes(workbookXLS);
		
		int indexSheet = this.getMergeSheetIndex(workBookOutput, prefixManager);
		
		// TODO : test if indexSheet < 0
		
		Sheet targetSheet = workBookOutput.getSheetAt(indexSheet);
		
		
		int rowIndexTitle = new RdfizableSheet(targetSheet, prefixManager).getTitleRowIndex();
		
		
		
		int nRow = rowIndexTitle;
		// iterate on all CSV Records
		for (CSVRecord dataRecord : csvParser.getRecords()) {
			nRow++;
			int nCol = 0;
			Row newRow = targetSheet.createRow(nRow);
			// TODO : insérer les colonnes dans l'ordre, sans essayer de trouver la colonne avec le même titre
			for (String columnNameCSV : dataRecord.getParser().getHeaderNames()) {
				for (Cell columnName : targetSheet.getRow(rowIndexTitle)) {
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

	public int getTitleRowIndex(Sheet targetSheet, PrefixManager prefixManager) throws InvalidFormatException, IOException {	
		RdfizableSheet rdfizableSheet = new RdfizableSheet(targetSheet, prefixManager);
		
		return rdfizableSheet.computeTitleRowIndex();
	}
	
	public Integer getMergeSheetIndex(Workbook wbInput, PrefixManager prefixManager) throws InvalidFormatException, IOException {
	    // find the target sheet
	    for (Sheet sheet : wbInput) {
	      RdfizableSheet rdfizableSheetActive = new RdfizableSheet(sheet, prefixManager);
	      if (rdfizableSheetActive.canRDFize()) {
	        return wbInput.getSheetIndex(sheet.getSheetName());
	      }
	    }
	    
	    // return -1 if not found
	    return -1;
	  }
		
}