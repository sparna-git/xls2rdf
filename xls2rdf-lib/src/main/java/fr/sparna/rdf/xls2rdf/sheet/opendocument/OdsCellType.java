package fr.sparna.rdf.xls2rdf.sheet.opendocument;

public enum OdsCellType {

    FLOAT("float"),
    CURRENCY("currency"),
    DATE("date"),
    PERCENTAGE("percentage"),
    STRING("string"),
    TIME("time");


    private final String typeName;
    public String getTypeName(){
        return this.typeName;
    }

    private OdsCellType (String typeName){
        this.typeName = typeName;
    }
}
