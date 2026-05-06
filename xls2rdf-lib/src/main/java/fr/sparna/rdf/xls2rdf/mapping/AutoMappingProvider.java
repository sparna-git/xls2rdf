package fr.sparna.rdf.xls2rdf.mapping;

import java.util.List;

import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;

public class AutoMappingProvider implements MappingProvider {

    protected Workbook workbook;

    public AutoMappingProvider(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public List<ColumnMapping> getSheetMapping(String sheetName) {
        Sheet sheet = this.workbook.getSheet(sheetName);
        if(sheet == null) {
            return null;
        }

    }
    
}
