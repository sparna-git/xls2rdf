package fr.sparna.rdf.xls2rdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SheetMapping {

    private Map<String, String> mapper;
    private Map<String, MappingRule> columnHeaders;
    private Properties mappingProperties;
    private String subjectColumn;
    private PrefixManager prefixManager;

    public SheetMapping(Properties mappingProperties){
        this();
        this.mappingProperties = mappingProperties;
    }

    public SheetMapping(){
        this.mapper = new HashMap<>();
        this.columnHeaders = new HashMap<>();
    }

   public MappingRule parseColumnHeader(String sheetName, String columnName){
        MappingRuleParser parser = new MappingRuleParser(this.prefixManager);
        MappingRule columnHeader = parser.parse(this.mappingProperties.getProperty(sheetName + "." + columnName));
        if(columnHeader.getProperty() == null)
        this.columnHeaders.put(columnName, columnHeader);
        return columnHeader;
   }

   public Map<String, MappingRule> getColumnHeaders(){
        return this.columnHeaders;
   }

   public void setPrefixManager(PrefixManager prefixManager){
        this.prefixManager = prefixManager;}


    public int getColumnNameIndex(String columnName){
        return 0;
    }

    public void clearCache(){
        this.columnHeaders.clear();
    }
}
