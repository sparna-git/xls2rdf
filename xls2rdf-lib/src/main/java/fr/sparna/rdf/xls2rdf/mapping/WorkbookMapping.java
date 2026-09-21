package fr.sparna.rdf.xls2rdf.mapping;

import java.util.HashMap;
import java.util.Map;


public class WorkbookMapping {
    private String baseIRI;
    private Map<String, String> prefixes;
    private Map<String, SheetMapping> sheetMappingMap;
    

    public WorkbookMapping(){
        this.sheetMappingMap = new HashMap<>();
    }  

    public Map<String, SheetMapping> getSheetMappingMap() {
        return sheetMappingMap;
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(Map<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    public void setBaseIRI(String baseIRI) {
        this.baseIRI = baseIRI;
    }

    public String getBaseIRI() {
        return baseIRI;
    }

    public SheetMapping getSheetMappingFor(String sheetName){
        return this.sheetMappingMap.get(sheetName);
    }

    public SheetMapping getUniqueSheetMapping() {
        if(this.sheetMappingMap.size() > 1) {
            throw new IllegalArgumentException("Mapping has more than one sheet specified, cannot provide unique sheet mapping");
        }

        return this.sheetMappingMap.get(this.sheetMappingMap.keySet().iterator().next());
    }

}
