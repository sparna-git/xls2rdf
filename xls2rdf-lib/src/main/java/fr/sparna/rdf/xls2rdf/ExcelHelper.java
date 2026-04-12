package fr.sparna.rdf.xls2rdf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.ExcelRefs;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;

public class ExcelHelper {

	private static Logger log = LoggerFactory.getLogger(ExcelHelper.class.getName());

	// getCellValue methods are no longer needed; value retrieval is provided by model.Cell
	
	
	public static Calendar asCalendar(String value) {
		Calendar calendar = DateUtil.getJavaCalendar(Double.valueOf(value));
		calendar.setTimeZone(TimeZone.getTimeZone("CEST"));
		return calendar;
	}
	
	public static Row columnLookup(String value, Sheet sheet, int columnIndex, boolean ignoreCase) {
		List<Row> foundRows = new ArrayList<>();
		for(Row r : sheet) {
		   Cell c = r.getCell(columnIndex);
		   if(c != null) {
			  String cellValue = c.getCellValue();
		      if(
		    		(!ignoreCase && cellValue.trim().equals(value.trim()))
		    	  	||
		    	  	(ignoreCase && cellValue.trim().equalsIgnoreCase(value.trim()))		    	  
		      ) {
		    	  foundRows.add(r);
		      }
		   }
		}
		
		if(foundRows.size() == 0) {
			// not found
			return null;
		} else if(foundRows.size() > 1) {
			// found multiple times
			String references = foundRows.stream().map(r -> ExcelRefs.cellRef(r.getRowNum(), columnIndex)).collect(Collectors.joining(" "));
			throw new Xls2RdfException("Ambiguous reference : found value '"+value+"' "+foundRows.size()+" times ("+references+"). Fix the values to garantee they are unique.");
		} else {
			// single value
			return foundRows.get(0);
		}
		
		
	}


}
