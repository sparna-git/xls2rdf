package fr.sparna.rdf.xls2rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeFilesOutputXls {
	
	static Logger log = LoggerFactory.getLogger(RdfizableSheet.class.getName());
	
	private File csvfile;
	private File xlsinput;
	private File outputfile;
	
	
	public File getCsvfile() {
		return csvfile;
	}
	public void setCsvfile(File csvfile) {
		this.csvfile = csvfile;
	}
	public File getXlsinput() {
		return xlsinput;
	}
	public void setXlsinput(File xlsinput) {
		this.xlsinput = xlsinput;
	}
	public File getOutputfile() {
		return outputfile;
	}
	public void setOutputfile(File outputfile) {
		this.outputfile = outputfile;
	}
	
	/*
	 *  @Param csvfile
	 * 
	 */	
	public void mergeFile(File csvfile, File xlsfile, File Output) throws InvalidFormatException, IOException {
		
		
		/*
		 *  Convert the files in WoorkBook
		 */
		
		// Convert xls file to Workbook object
		Workbook workbookXSL = WorkbookFactory.create(xlsfile);
		
		/*
		 *  Convert the csv file to a xls format and return in Workbook Object
		 *  
		 */
		Workbook workbookCSV = convert_csv_to_xls(csvfile);
		
		// merge CSV after the line at titleRowIndex
		/*
		 *  Section of merge the workbooks objects csv and xls, in only sheet 
		 * 
		 */
		Workbook wbMerge =  merge_files(workbookCSV, workbookXSL);
		
		//Test
		if (Output.isFile()) {
			try (OutputStream fileOut = new FileOutputStream(Output)) {
			    wbMerge.write(fileOut);
			    wbMerge.close();
			}
			
			log.debug(xlsfile.getName()+" "+"is merge ........");
		}
		
	}
	
	public PrefixManager readWorkbook(Workbook workbook) throws InvalidFormatException, IOException {
		// Start instance
		PrefixManager prefixManager = new PrefixManager();
		// register prefixes
		for (Sheet sheet : workbook) {
			prefixManager.register(RdfizableSheet.readPrefixes(sheet));
		}
		
		return prefixManager;
	}

	public Workbook convert_csv_to_xls(File inputCSV) throws IOException {
		
		// Instance of workbook
		XSSFWorkbook wb = new XSSFWorkbook();
		// Create sheet style
		XSSFSheet wbSheet = wb.createSheet("default");
		// Store in memory 
		BufferedReader br = new BufferedReader(new FileReader(inputCSV));
		// Loop for get all data
		String currentLine = null;
		int RowNum = 0;
		while ((currentLine = br.readLine()) != null) {
            String str[] = currentLine.split(";");
            RowNum++;
            XSSFRow currentRow=wbSheet.createRow(RowNum);
            for(int i=0;i<str.length;i++){
                currentRow.createCell(i).setCellValue(str[i]);
            }
        }    
 		
		return wb;
	}
	
	public int get_rowTitle(Workbook wbXslfile) throws InvalidFormatException, IOException {
		
		int titleRowIndex = 0;
		
		Sheet sheetToMerge = wbXslfile.getSheetAt(1);
		
		// Get all Prefixes from to xls file 
		PrefixManager prefixManager = readWorkbook(wbXslfile);
		
		RdfizableSheet rdfizableSheet = new RdfizableSheet(sheetToMerge, prefixManager);
		
		if (rdfizableSheet.computeTitleRowIndex() == 1) {
			titleRowIndex = rdfizableSheet.computeTitleRowIndex()-1;
		} else {
			titleRowIndex = rdfizableSheet.computeTitleRowIndex();
		}
		
		return titleRowIndex;
	}
	
	/*
	 * this section validate the columns positions
	 
	public Workbook validate_source(Workbook wbCsvfile,Workbook wbXslfile) throws InvalidFormatException, IOException {
		
		Workbook wb = null;
				
		// return last row of a title in xsl source
		int rowTitleIndex = get_rowTitle(wbXslfile);
		
		// get the title row		
		List<String> titleHeaderCSV = new ArrayList<>();
		Iterator<Row> nRowTitle = wbXslfile.getSheetAt(1).iterator();
		while (nRowTitle.hasNext()) {
			Row rowTitle = nRowTitle.next();
			
			// loop for each cell (Column)
			Iterator<Cell> cellTitle = rowTitle.cellIterator();
			while (cellTitle.hasNext()) {
				Cell cellTitleHeader = cellTitle.next();				
				// Title name
				titleHeaderCSV.add(cellTitleHeader.getStringCellValue());				
			}			
		}
		
		
		//find title in the xsl from to merger
		List<String> titleHeaderXSL = new ArrayList<>();
		Iterator<Cell> cellSourceData = (Iterator<Cell>) wbXslfile.getSheetAt(1).getRow(rowTitleIndex);
		while (cellSourceData.hasNext()) {
			Cell cellSourceTitle = cellSourceData.next();
			
			titleHeaderXSL.add(cellSourceTitle.getStringCellValue());				
		}
		
		
		if (titleHeaderCSV.equals(titleHeaderXSL)) {
			wb = wbCsvfile;			
		}
		// create the process for new wb
		
		return wb;		
	}
	*/
	
	
	public Workbook merge_files(Workbook wbCsvfile,Workbook wbXslfile) throws InvalidFormatException, IOException {
		
		// Get all prefix
		PrefixManager prefixManager = readWorkbook(wbXslfile);
		
		// find the sheet to merger
		String nameSheet = "";
		for (Sheet sheet : wbXslfile) {
			RdfizableSheet rdfizableSheetActive = new RdfizableSheet(sheet, prefixManager);
			if (rdfizableSheetActive.canRDFize()) {
				nameSheet = sheet.getSheetName();
				break;
			}
		}	
		// number of index sheet
		int numberOfSheet = wbXslfile.getSheetIndex(nameSheet);
		
		// last row of sheet 		
		int rowTitleIndex = get_rowTitle(wbXslfile);
		// number of line for write output data
		int nRow = rowTitleIndex++;
		/*
		 *  Merge
		 */
		
		// 1. Read file csv		
		Iterator<Row> sourceCSV = wbCsvfile.getSheetAt(0).iterator();
		while (sourceCSV.hasNext()) {
			Row rowdata = sourceCSV.next();
			
			if (rowdata.getRowNum() != 1) {
				
				Iterator<Cell> dataCell = rowdata.iterator();
				
				// create new row
				nRow++;
				Row nSourceRow = wbXslfile.getSheetAt(numberOfSheet).createRow(nRow);
				while (dataCell.hasNext()) {
					Cell cellData = dataCell.next();
					
					// write in xsl from to merger
					Cell cellsource =  nSourceRow.createCell(cellData.getColumnIndex());
					cellsource.setCellValue(cellData.getStringCellValue());
									
				}				
			}
		}
	
		return wbXslfile;
	}
	
}
