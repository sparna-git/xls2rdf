package fr.sparna.rdf.xls2rdf.sheet.opendocument;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.CellType;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfName;
import org.odftoolkit.odfdom.pkg.OdfNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static fr.sparna.rdf.xls2rdf.sheet.CellType.ERROR;
import static fr.sparna.rdf.xls2rdf.sheet.CellType.FORMULA;

public class OpenDocumentCell implements Cell{

    static Logger log = LoggerFactory.getLogger(OpenDocumentCell.class);

    public final static String LINE_THROUGH_ATTRIBUTE = "text-line-through-style";
    public final static String LINE_THROUGH_VALUE     = "solid";

    private final OdfTableCell delegate;
    private final OpenDocumentRow parentRow;

    public OpenDocumentCell(OdfTableCell delegate, OpenDocumentRow parentRow){
        this.delegate = delegate;
        this.parentRow = parentRow;
    }

    @Override
    public CellType getCellType() {
        return mapType(this.delegate.getValueType());
    }

    private CellType mapType(String type){
        /*
         * Get the value type of this cell.
         * The returned value can be
         * "boolean", "currency", "date", "float", "percentage", "string" or "time". If no value type is set, null will be returned
         */
        if(type == null) return ERROR;
        /*
         *If the cell does not contain a formula, null will be returned
         */
        if(this.delegate.getFormula() != null) return FORMULA;
        return this.strToType.apply(type);
    }

    private Function<String, CellType> strToType = (type) -> {
        return switch (type) {
            case ""        -> CellType.BLANK;
            case "string"  -> CellType.STRING;
            case "float"   -> CellType.NUMERIC;
            case "boolean" -> CellType.BOOLEAN;
            default        -> CellType.ERROR;
        };
    };

    @Override
    public String getCellValue() {
        return this.getCellValue(this.getCellType());
    }

    public String getCellValue(CellType type){
        return switch(type){
            case BLANK, ERROR -> "";
            case STRING       -> this.delegate.getStringValue().trim();
            case BOOLEAN      -> Boolean.toString(this.delegate.getBooleanValue());
            case NUMERIC      -> {
                Double numericValue = this.delegate.getDoubleValue();
                if(numericValue % 1 == 0) yield Integer.toString(numericValue.intValue());
                else yield numericValue.toString();
            }
            case FORMULA      -> this.getCellValue(this.strToType.apply(this.delegate.getValueType()));
            default           -> throw new Xls2RdfException("Cell type unknown or unsupported ({}) at Sheet '{}', row {}, column {}", type.name(), this.getSheet().getSheetName(), this.delegate.getRowIndex(), this.delegate.getColumnIndex());
        };
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
        //On récupére le style courant de la cellule

        OdfStyle cellStyle = this.delegate.getOdfElement().getOrCreateAutomaticStyles().getStyle(this.delegate.getStyleName(), this.delegate.getOdfElement().getStyleFamily());
        if(cellStyle == null) return false;
        //On crée OdfStyleProperty associé à style:text-line-through-style
        OdfStyleProperty styleProperty = OdfStyleProperty
                .get(OdfStylePropertiesSet.TextProperties, OdfName.getOdfName(OdfNamespace.getNamespace(OdfDocumentNamespace.STYLE.getUri()), OpenDocumentCell.LINE_THROUGH_ATTRIBUTE));
        //On récupére la valeur str associée à styleProperty->'style:text-line-through-style' soit SOLID soit NONE
        String strPropertyValue = cellStyle.getStyleProperties().get(styleProperty);
        if(strPropertyValue == null) return false;
       return strPropertyValue.equals(LINE_THROUGH_VALUE);
    }

    @Override
    public String getCellExcelReference() {
        return "row:" + this.delegate.getRowIndex() + "-" + "column:" + this.delegate.getColumnIndex();
    }

    public OdfTableCell getCell(){
        return this.delegate;
    }
}