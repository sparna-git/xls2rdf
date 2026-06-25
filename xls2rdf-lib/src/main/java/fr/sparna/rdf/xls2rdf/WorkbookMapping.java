package fr.sparna.rdf.xls2rdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class WorkbookMapping {

    private Map<String, SheetMapping> sheetMappingMap;
    private Properties properties;
    private PrefixManager prefixManager;

    public WorkbookMapping(Properties properties){
        this.sheetMappingMap = new HashMap<>();
        this.properties = properties;
    }

    public SheetMapping doSheetMappingFor(String sheetName){
        SheetMapping s = new SheetMapping(sheetName, prefixManager);
        for(Map.Entry<Object, Object> p: this.properties.entrySet()){
            if(((String)p.getKey()).equals("URI")){
                s.addMappingRule((String)p.getKey(), ((String)p.getValue()));
            }
            if(((String)p.getKey()).startsWith(sheetName)){
                s.addMappingRule(((String)((String) p.getKey()).substring(sheetName.length() + 1)), ((String)p.getValue()));
            }
        }
        this.sheetMappingMap.put(sheetName, s);
        return s;
    }

    public void setPrefixManager(PrefixManager prefixManager){
        this.prefixManager = prefixManager;
    }

    public SheetMapping getSheetMappingFor(String sheetName){
        return this.sheetMappingMap.get(sheetName);
    }

}
