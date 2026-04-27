package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.CellType;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fr.sparna.rdf.xls2rdf.sheet.CellType.*;

public class OpenDocumentCell implements Cell{

    static Logger log = LoggerFactory.getLogger(OpenDocumentCell.class);

    public final static String LINE_THROUGH_ATTRIBUTE = "style:text-line-through-style";
    public final static String LINE_THROUGH_VALUE     = "solid";

    private final OdfTableCell delegate;
    private final OpenDocumentRow parentRow;

    public OpenDocumentCell(OdfTableCell delegate, OpenDocumentRow parentRow){
        this.delegate = delegate;
        this.parentRow = parentRow;
    }

    @Override
    public CellType getCellType() {
        //FROM JAVADOC
        /**
         * Get the value type of this cell.
         * The returned value can be
         * "boolean",
         * "currency",
         * "date",
         * "float",
         * "percentage",
         * "string" or
         * "time".
         * If no value type is set, null will be returned.
         */
       return mapType(this.delegate.getValueType());
    }

    private CellType mapType(String type){
        switch (CellType.valueOf("ODS_" + type.toUpperCase())){
            case ODS_DATE -> {return ODS_DATE;}
            case ODS_PERCENTAGE -> {return ODS_PERCENTAGE;}
            case ODS_FLOAT -> {return ODS_FLOAT;}
            case ODS_STRING -> {return ODS_STRING;}
            case ODS_TIME -> {return ODS_TIME;}
            case ODS_CURRENCY -> {return ODS_CURRENCY;}
            default -> {return null;}
        }
    }

    @Override
    public String getCellValue() {
        return this.delegate.getStringValue().trim();
    }

    @Override
    public Row getRow() {
        return this.parentRow;
    }

    @Override
    public int getRowIndex() {
        return this.delegate.getRowIndex();
    }

    @Override
    public int getColumnIndex() {
        return this.delegate.getColumnIndex();
    }

    @Override
    public Sheet getSheet() {
        return this.parentRow.getSheet();
    }

    @Override
    public boolean isStruckThrough() {
        return LINE_THROUGH_VALUE.equals(this.delegate.getOdfElement().getDocumentStyle().getAttribute(LINE_THROUGH_ATTRIBUTE));
    }

    @Override
    public String getCellExcelReference() {
        return "";
    }

    public OdfTableCell getCell(){
        return this.delegate;
    }
}