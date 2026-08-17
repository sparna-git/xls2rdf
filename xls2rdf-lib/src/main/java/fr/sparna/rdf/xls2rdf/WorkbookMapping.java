package fr.sparna.rdf.xls2rdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class WorkbookMapping {

    //register each sheet's name with her corresponding SheetMapping
    private Map<String, SheetMapping> sheetMappingMap;
    private Properties properties;
    private PrefixManager prefixManager;
    private YamlParser yamlParser;

    public WorkbookMapping(){}

    public WorkbookMapping(Properties properties){
        this.sheetMappingMap = new HashMap<>();
        this.properties = properties;
    }

    public WorkbookMapping(YamlParser yamlParser){
        this.sheetMappingMap = new HashMap<>();
        this.yamlParser = yamlParser;
    }

    //Call this method after the WorkbookMapping has been sent to Xls2RdfConverter in order to process the ruleMapping for each sheet.
    public SheetMapping doSheetMappingFromProperties(String sheetName){
        SheetMapping s = new SheetMapping(sheetName, prefixManager);
        for(Map.Entry<Object, Object> p: this.properties.entrySet()){
            if(((String)p.getKey()).startsWith(sheetName)){
                s.addMappingRule((((String) p.getKey()).substring(sheetName.length() + 1)), ((String)p.getValue()));
            }
        }
        this.sheetMappingMap.put(sheetName, s);
        return s;
    }

    public SheetMapping doSheetMappingFromYaml(String sheetName){
        //create the SheetMapping first, give the sheetName and the prefixManager
        SheetMapping s = new SheetMapping(sheetName, prefixManager);
        //We parse the YamlParser to retrieve clients properties and associate them to the sheetMapping with #addMappingRule
        for(YamlParser.YamlSheet r : this.yamlParser.getSheets()){
               if(r.getName().equals(sheetName)){
                   for(Map.Entry<String, String> e : r.getRules().entrySet()){
                       s.addMappingRule(e.getKey(), e.getValue());
                   }
               }
        }
        this.sheetMappingMap.put(sheetName, s);
        return s;
    }


    //Must be set before using doSheetMappingFor because it requieres a prefixManager
    public void setPrefixManager(PrefixManager prefixManager){
        this.prefixManager = prefixManager;
    }

    public SheetMapping getSheetMappingFor(String sheetName){
        return this.sheetMappingMap.get(sheetName);
    }

    //register all given prefixes
    public void registerPrefixes(){
        this.prefixManager.register(this.yamlParser.getPrefixes());
    }

    public void registerPrefix(String prefix, String uri){
        this.prefixManager.register(prefix, uri);
    }

    public String getBaseIRI(){
        return this.yamlParser.getBase();
    }


}
