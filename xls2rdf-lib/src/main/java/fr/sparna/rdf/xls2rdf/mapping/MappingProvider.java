package fr.sparna.rdf.xls2rdf.mapping;

import java.util.List;

public interface MappingProvider {
    
    public List<ColumnMapping> getSheetMapping(String sheetName);

}
