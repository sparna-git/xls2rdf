package fr.sparna.rdf.xls2rdf;

import java.util.List;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.ExcelRefs;

/**
 * The association between a column header cell and the mapping rule that was declared in it or associated with it.
 */
public class ColumnHeader {
	
	private MappingRule mappingRule;

	/**
	 * The actual cell in which the header was originally declared, from which we can get the column index
	 */
	private Cell headerCell;


	
	public ColumnHeader(Cell headerCell, MappingRule mappingRule) {
		this.headerCell = headerCell;
		this.mappingRule = mappingRule;
	}

	
	public Cell getHeaderCell() {
		return headerCell;
	}

	public MappingRule getMappingRule() {
		return mappingRule;
	}


	/**
	 * Finds the column index based on a column ID reference or an Excel column reference.
	 * Returns -1 if not found.
	 * 
	 * @param headers
	 * @param idRef
	 * @return
	 */
	public static int idRefToColumnIndex(List<ColumnHeader> headers, String idRef) {
		for (ColumnHeader header : headers) {
			if(header.getMappingRule().getId() != null && header.getMappingRule().getId().equals(idRef)) {
				return header.getHeaderCell().getColumnIndex();
			}
		}
		
		// if we haven't found the proper column id, try it as an Excel column reference
		if(idRef.length() <= 2) {
			int idx = ExcelRefs.colLettersToIndex(idRef);
			if(idx != -1) return idx;
		}

		return -1;
	}
	
	/**
	 * Finds the column index based on a reference that can be 
	 * either an ID reference or a property reference or an Excel column reference.
	 * Returns -1 if not found.
	 * 
	 * @param headers
	 * @param idOrPropertyRef the ID or property reference to search
	 * @return
	 */
	public static int idRefOrPropertyRefToColumnIndex(List<ColumnHeader> headers, String idOrPropertyRef) {
		for (ColumnHeader header : headers) {
			if(
					(header.getMappingRule().getId() != null && header.getMappingRule().getId().equals(idOrPropertyRef))
					||
					(header.getMappingRule().getDeclaredProperty() != null && header.getMappingRule().getDeclaredProperty().equals(idOrPropertyRef))
			) {
				return header.getHeaderCell().getColumnIndex();
			}
		}
		
		// if we haven't found the proper column id, try it as an Excel column reference
		if(idOrPropertyRef.length() <= 2) {
			int idx = ExcelRefs.colLettersToIndex(idOrPropertyRef);
			if(idx != -1) return idx;
		}
		
		return -1;
	}
	
	public static ColumnHeader findByColumnIndex(List<ColumnHeader> headers, int columnIndex) {
		for (ColumnHeader header : headers) {
			if(header.getHeaderCell().getColumnIndex() == columnIndex) {
				return header;
			}
		}
		
		return null;
	}

	@Override
	public String toString() {
		return "ColumnHeader [headerCell="+ headerCell + "mappingRule=" + mappingRule + "]";
	}	
	
}


