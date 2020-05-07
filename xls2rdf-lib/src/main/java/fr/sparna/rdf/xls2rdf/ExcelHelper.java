package fr.sparna.rdf.xls2rdf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelHelper {

	private static Logger log = LoggerFactory.getLogger(ExcelHelper.class.getName());
	
	private ExcelHelper() {
	}

	public static String getCellValue(Cell cell) {
		if(cell == null) return null;
		return getCellValue(cell.getCellTypeEnum(), cell);
	}
	
	private static String getCellValue(CellType type, Cell cell) {
		// blank or error cells give an empty value
		if (type == CellType.BLANK || type == CellType.ERROR) {
			return "";
		} else if (type == CellType.STRING) {
			return cell.getStringCellValue().trim();
        } else if (type == CellType.NUMERIC) {
        	double d = cell.getNumericCellValue();
			if((d % 1) == 0) {
				// return it as an int without the dot to avoid values like "1.0"
				return "" + new Double(d).intValue();
			} else {
				return "" + d;
			} 
        } else if (type == CellType.BOOLEAN) {
        	return Boolean.toString(cell.getBooleanCellValue());
        } else if (type == CellType.FORMULA) {
            // Re-run based on the formula type
            return getCellValue(cell.getCachedFormulaResultTypeEnum(), cell);
        } else {
        	throw new Xls2RdfException("Cell type unknown or unsupported ({}) at Sheet '{}', row {}, column {}", type.name(), cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex());
        }
	}
	
	
	public static Calendar asCalendar(String value) {
		Calendar calendar = DateUtil.getJavaCalendar(Double.valueOf(value));
		calendar.setTimeZone(TimeZone.getTimeZone("CEST"));
		return calendar;
	}
	
	public static Row columnLookup(String value, Sheet sheet, int columnIndex) {
		List<Row> foundRows = new ArrayList<>();
		for(Row r : sheet) {
		   Cell c = r.getCell(columnIndex);
		   if(c != null) {
		      String cellValue = getCellValue(c);
		      if(cellValue.trim().equals(value.trim())) {
		    	  foundRows.add(r);
		      }
		   }
		}
		
		if(foundRows.size() == 0) {
			// not found
			return null;
		} else if(foundRows.size() > 1) {
			// found multiple times
			String references = foundRows.stream().map(r -> new CellReference(r.getRowNum(), columnIndex).formatAsString()).collect(Collectors.joining(" "));
			throw new Xls2RdfException("Ambiguous reference : found value '"+value+"' "+foundRows.size()+" times ("+references+"). Fix the values to garantee they are unique.");
		} else {
			// single value
			return foundRows.get(0);
		}
		
		
	}


}
