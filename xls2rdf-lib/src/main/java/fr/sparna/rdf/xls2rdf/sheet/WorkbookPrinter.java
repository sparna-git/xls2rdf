package fr.sparna.rdf.xls2rdf.sheet;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility to produce a simple CSV-style dump of a Workbook for debugging purposes.
 */
public final class WorkbookPrinter {

    private static final int MAX_COLS = 200;

    private WorkbookPrinter() {}

    /**
     * Produces a String representation of the given workbook. Each sheet is separated by a line
     * containing three dashes. Each sheet is printed as CSV lines.
     *
     * @param workbook the workbook to print
     * @return the textual dump
     */
    public static String print(Workbook workbook) {
        StringBuilder sb = new StringBuilder();
        if (workbook == null) return "";

        boolean firstSheet = true;
        for (Sheet sheet : workbook) {
            if (!firstSheet) {
                sb.append(System.lineSeparator()).append("---").append(System.lineSeparator());
            }
            firstSheet = false;

            String sheetName = sheet == null ? null : sheet.getSheetName();
            sb.append("Sheet: ").append(sheetName == null ? "(no name)" : sheetName).append(System.lineSeparator());

            if (sheet == null) continue;

            int lastRow = sheet.getLastRowNum();
            for (int rowIndex = 0; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    sb.append(System.lineSeparator());
                    continue;
                }

                // detect last non-empty column in this row (up to MAX_COLS)
                int lastNonEmpty = -1;
                String[] values = new String[MAX_COLS];
                for (int col = 0; col < MAX_COLS; col++) {
                    String v = null;
                    try {
                        v = row.getColumnValue(col);
                    } catch (Exception e) {
                        v = null;
                    }
                    values[col] = v == null ? "" : v;
                    if (!StringUtils.isBlank(values[col])) {
                        lastNonEmpty = col;
                    }
                }

                if (lastNonEmpty == -1) {
                    sb.append(System.lineSeparator());
                    continue;
                }

                for (int col = 0; col <= lastNonEmpty; col++) {
                    if (col > 0) sb.append(',');
                    sb.append(escapeCsv(values[col]));
                }
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        boolean containsSpecial = value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        String v = value;
        if (v.indexOf('"') >= 0) {
            v = v.replace("\"", "\"\"");
        }
        if (containsSpecial) {
            return '"' + v + '"';
        }
        return v;
    }
}
