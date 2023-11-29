package fr.sparna.rdf.xls2rdf;

import java.io.IOException;
import java.util.List;

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

	public Workbook mergeCsv(List<CSVRecord> csvRecords, Workbook workbookXLS) throws InvalidFormatException, IOException {
		
		// Get all prefix
	    PrefixManager prefixManager = getPrefixes(workbookXLS);		
		int indexSheet = this.getMergeSheetIndex(workbookXLS , prefixManager);
				
		// TODO : test if indexSheet < 0
		if (indexSheet == -1) {
			log.debug("Warning: Not found a sheetstyle in the document....");
			return null;
		} else {
			Sheet targetSheet = workbookXLS.getSheetAt(indexSheet);
			int rowIndexTitle = new RdfizableSheet(targetSheet, prefixManager).computeTitleRowIndex();			
			int nRow = rowIndexTitle;
			
			// get all data values
			for (int i = 1; i < csvRecords.size(); i++) {				
				CSVRecord r = csvRecords.get(i); 
				nRow++;
				// create row in the sheet
				Row newRow = targetSheet.createRow(nRow);
				for (int j = 0; j < r.values().length; j++) {
					Cell newCell = newRow.createCell(j);
					// write in column
					String value = r.get(j);
					newCell.setCellValue(value);
				}
			}
			return workbookXLS;
		}
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