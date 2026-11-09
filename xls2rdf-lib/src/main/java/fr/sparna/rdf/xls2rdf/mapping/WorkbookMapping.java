package fr.sparna.rdf.xls2rdf.mapping;

import java.util.HashMap;
import java.util.Map;


public class WorkbookMapping {
    private String baseIRI;
    private Map<String, String> prefixes;
    private Map<String, SheetMapping> sheetMappings;
    

    public WorkbookMapping(){
        this.sheetMappings = new HashMap<>();
    }  

    public Map<String, SheetMapping> getSheetMappings() {
        return sheetMappings;
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
        return this.sheetMappings.get(sheetName);
    }

    public SheetMapping getUniqueSheetMapping() {
        if(this.sheetMappings.size() > 1) {
            throw new IllegalArgumentException("Mapping has more than one sheet specified, cannot provide unique sheet mapping");
        }

        return this.sheetMappings.get(this.sheetMappings.keySet().iterator().next());
    }

}
