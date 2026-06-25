package fr.sparna.rdf.xls2rdf;

import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HeaderLine {

    private int rowIndex;
    private List<String> headers;

    public HeaderLine(int rowIndex, List<String> headers) {
        this.rowIndex = rowIndex;
        this.headers = headers;
    }

    public HeaderLine(Row row) {
        this.rowIndex = row.getRowNum();
        this.headers = new ArrayList<String>();
        for (short i = 0; true; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) break;
            String columnName = cell.getCellValue();

            // stop at the first empty value
            if (StringUtils.isBlank(columnName)) {
                break;
            }
            this.headers.add(columnName);
        }
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public List<String> getHeaders() {
        return headers;
    }
}
