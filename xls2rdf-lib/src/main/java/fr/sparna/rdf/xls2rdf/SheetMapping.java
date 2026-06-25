package fr.sparna.rdf.xls2rdf;

import java.util.HashMap;
import java.util.Map;

public class SheetMapping {

    private final Map<String, MappingRule> mappingRule;
    private String subjectColumn;
    private PrefixManager prefixManager;
    private String sheetName;

    public SheetMapping(String sheetName, PrefixManager prefixManager){
        this.mappingRule = new HashMap<>();
        this.sheetName = sheetName;
        this.prefixManager = prefixManager;
    }

    public void addMappingRule(String columnName, String predicate){
        MappingRuleParser parser = new MappingRuleParser(this.prefixManager);
        MappingRule rule = parser.parse(predicate);
        this.mappingRule.put(columnName, rule);
    }


   public Map<String, MappingRule> getMappingRule(){
        return this.mappingRule;
   }

   public void setPrefixManager(PrefixManager prefixManager){
        this.prefixManager = prefixManager;
    }

}
