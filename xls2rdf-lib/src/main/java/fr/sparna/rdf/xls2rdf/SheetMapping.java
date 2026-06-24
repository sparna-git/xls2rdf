package fr.sparna.rdf.xls2rdf;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SheetMapping {

    private Map<String, String> mapper;
    private Map<String, ColumnHeader> columnHeaders;
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

   public ColumnHeader parseColumnHeader(String sheetName, String columnName){
        ColumnHeaderParser parser = new ColumnHeaderParser(this.prefixManager);
        ColumnHeader columnHeader = parser.parse(this.mappingProperties.getProperty(sheetName + "." + columnName), null);
        if(columnHeader.getProperty() == null)
        this.columnHeaders.put(columnName, columnHeader);
        return columnHeader;
   }

   public Map<String, ColumnHeader> getColumnHeaders(){
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
