package fr.sparna.rdf.xls2rdf.sheet;

/**
 * Utilities for converting between Excel A1-style references and indexes or vice versa.
 */
public final class ExcelRefs {
    private ExcelRefs() {}

    /**
     * @param index zero-based column index (0 -> A, 25 -> Z, 26 -> AA).
     * @return Excel column letters for the given index.
     */
    public static String colIndexToLetters(int index) {
        StringBuilder sb = new StringBuilder();
        int i = index;
        do {
            int rem = i % 26;
            sb.append((char)('A' + rem));
            i = i / 26 - 1;
        } while (i >= 0);
        return sb.reverse().toString();
    }

    /**
     * @param col Excel column letters, e.g., "A", "Z", "AA".
     * @return zero-based column index, or -1 if input is invalid.
     */
    public static int colLettersToIndex(String col) {
        if (col == null || col.isEmpty()) return -1;
        int idx = 0;
        for (int i = 0; i < col.length(); i++) {
            char ch = Character.toUpperCase(col.charAt(i));
            if (ch < 'A' || ch > 'Z') return -1;
            idx = idx * 26 + (ch - 'A' + 1);
        }
        return idx - 1;
    }

    /**
     * @param rowIndexZeroBased zero-based row index (0 -> 1).
     * @param colIndexZeroBased zero-based column index (0 -> A).
     * @return A1-style cell reference, e.g., "A1", "B2".
     */
    public static String cellRef(int rowIndexZeroBased, int colIndexZeroBased) {
        return colIndexToLetters(colIndexZeroBased) + Integer.toString(rowIndexZeroBased + 1);
    }


}
