package fr.sparna.rdf.xls2rdf.sheet.excel;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.CellType;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;

public class ExcelCell implements Cell {
    private final org.apache.poi.ss.usermodel.Cell delegate;
    private final ExcelRow parentRow;

    public ExcelCell(org.apache.poi.ss.usermodel.Cell delegate, ExcelRow parentRow) {
        this.delegate = delegate;
        this.parentRow = parentRow;
    }

    @Override
    public CellType getCellType() {
        if(delegate.getCellType() == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            return mapType(delegate.getCachedFormulaResultType());
        }

        return mapType(delegate.getCellType());
    }

    private static CellType mapType(org.apache.poi.ss.usermodel.CellType t) {
        return switch (t) {
            case BLANK    -> CellType.BLANK;
            case ERROR    -> CellType.ERROR;
            case STRING   -> CellType.STRING;
            case NUMERIC  -> CellType.NUMERIC;
            case BOOLEAN  -> CellType.BOOLEAN;
            case FORMULA  -> CellType.FORMULA;
            default       -> CellType.ERROR;
        };
    }

    @Override
    public String getCellValue() {
		return getCellValue(this.getCellType());
    }

	public String getCellValue(CellType type) {

        //String test = switch(type){
        //   case BLANK, ERROR -> "";
        //   case STRING       -> this.delegate.getStringCellValue().trim();
        //   case BOOLEAN      -> Boolean.toString(this.delegate.getBooleanCellValue());
        //   case NUMERIC      -> {
        //       Double numericValue = this.delegate.getNumericCellValue();
        //       if(numericValue % 1 == 0) yield Integer.toString(numericValue.intValue());
        //       else yield numericValue.toString();
        //   }
        //   case FORMULA      -> getCellValue(mapType(this.delegate.getCachedFormulaResultType()));
        //   default           -> throw new Xls2RdfException("Cell type unknown or unsupported ({}) at Sheet '{}', row {}, column {}", type.name(), this.delegate.getSheet().getSheetName(), this.delegate.getRowIndex(), this.delegate.getColumnIndex());
        //};

		// blank or error cells give an empty value
		if (type == CellType.BLANK || type == CellType.ERROR) {
			return "";
		} else if (type == CellType.STRING) {
			return this.delegate.getStringCellValue().trim();
        } else if (type == CellType.NUMERIC) {
        	double d = this.delegate.getNumericCellValue();
			if((d % 1) == 0) {
				// return it as an int without the dot to avoid values like "1.0"
				return "" + Double.valueOf(d).intValue();
			} else {
				return "" + d;
			} 
        } else if (type == CellType.BOOLEAN) {
        	return Boolean.toString(this.delegate.getBooleanCellValue());
        } else if (type == CellType.FORMULA) {
            // Re-run based on the formula type
            return getCellValue(mapType(this.delegate.getCachedFormulaResultType()));
        } else {
        	throw new Xls2RdfException("Cell type unknown or unsupported ({}) at Sheet '{}', row {}, column {}", type.name(), this.delegate.getSheet().getSheetName(), this.delegate.getRowIndex(), this.delegate.getColumnIndex());
        }
	}

    @Override
    public Row getRow() {
        return parentRow;
    }

    @Override
    public int getRowIndex() {
        return delegate.getRowIndex();
    }

    @Override
    public int getColumnIndex() {
        return delegate.getColumnIndex();
    }

    @Override
    public Sheet getSheet() {
        return parentRow.getSheet();
    }

    @Override
    public boolean isStruckThrough() {
        int fontIdx = delegate.getCellStyle().getFontIndex();
        return parentRow.getWorkbook().getPoiWorkbook().getFontAt(fontIdx).getStrikeout();
    }

    @Override
    public String getCellExcelReference() {
        return new org.apache.poi.ss.util.CellReference(delegate).formatAsString();
    }

    public org.apache.poi.ss.usermodel.Cell getPoiCell() {
        return delegate;
    }
}
