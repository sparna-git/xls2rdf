package fr.sparna.rdf.xls2rdf.sheet;

public enum CellType {

    BLANK("blank"),
    ERROR("error"),
    STRING("string"),
    NUMERIC("numeric"),
    BOOLEAN("boolean"),
    FORMULA("formula"),

    ODS_FLOAT("float"),
    ODS_CURRENCY("currency"),
    ODS_DATE("date"),
    ODS_PERCENTAGE("percentage"),
    ODS_STRING("string"),
    ODS_TIME("time");


    private final String typeName;
    public String getTypeName(){
        return this.typeName;
    }

    private CellType (String typeName){
        this.typeName = typeName;
    }
}
