package fr.sparna.rdf.xls2rdf.mapping;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import fr.sparna.rdf.xls2rdf.PrefixManager;


public class WorkbookMappingFactory {

    public WorkbookMapping buildFromYamlParser(YamlParser yamlParser){
        WorkbookMapping workbookMapping = new WorkbookMapping();
        PrefixManager prefixManager = new PrefixManager();

        // register declared prefixes in the PrefixManager
        if(yamlParser.getOptionalPrefixes().isPresent()){
            for(Map.Entry<String, String> e : yamlParser.getOptionalPrefixes().get().entrySet()){
                prefixManager.register(e.getKey(), e.getValue());
            }
        }

        // We read the YamlParser to retrieve clients properties and associate them to the sheetMapping with #addMappingRule
        for(YamlParser.YamlSheet r : yamlParser.getOptionalSheets().orElse(Collections.emptyList())){
            SheetMapping s = new SheetMapping(r.getName(), prefixManager);
            for(Map.Entry<String, String> e : r.getRules().entrySet()){
                s.addMappingRule(e.getKey(), e.getValue());
            }

            workbookMapping.getSheetMappingMap().put(r.getName(), s);
        }

        // set the prefixes
        workbookMapping.setPrefixes(yamlParser.getOptionalPrefixes().orElse(Collections.emptyMap()));

        // set the base IRI
        workbookMapping.setBaseIRI(yamlParser.getOptionalBase().orElse(null));

        return workbookMapping;
    }


    public WorkbookMapping buildFromProperties(Properties properties){
        WorkbookMapping workbookMapping = new WorkbookMapping();
        PrefixManager prefixManager = new PrefixManager();

        // We read the Properties to retrieve clients properties and associate them to the sheetMapping with #addMappingRule
        for(Map.Entry<Object, Object> p: properties.entrySet()){
            String key = (String) p.getKey();
            String value = (String) p.getValue();

            String sheetName = key.substring(0, key.indexOf("."));
            String columnName = key.substring(key.indexOf(".") + 1);
            String mappingRule = value;

            
            if(!workbookMapping.getSheetMappingMap().containsKey(sheetName)){
                SheetMapping s = new SheetMapping(sheetName, prefixManager);
                s.addMappingRule(columnName, mappingRule);
                workbookMapping.getSheetMappingMap().put(sheetName, s);
            } else {
                SheetMapping s = workbookMapping.getSheetMappingMap().get(sheetName);
                s.addMappingRule(columnName, mappingRule);
            }
        }

        return workbookMapping;
    }

}
