package fr.sparna.rdf.xls2rdf.model;

public final class ExcelRefs {
    private ExcelRefs() {}

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

    public static String cellRef(int rowIndexZeroBased, int colIndexZeroBased) {
        return colIndexToLetters(colIndexZeroBased) + Integer.toString(rowIndexZeroBased + 1);
    }

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
}
