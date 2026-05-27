package fr.sparna.rdf.xls2rdf.sheet.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.List;

/**
 * <p>Permet d'encapsuler {@link CSVFormat}, {@link List<CSVRecord>}, {@link BufferedReader} et {@link File},
 * afin de pouvoir travailler sur des enregistrements CSV en mémoire: {@link CSVDelegate#records}, voir:
 * <ul>
 *     <li>{@link CSVWorkbook}</li>
 *     <li>{@link CSVSheet}</li>
 *     <li>{@link CSVRow}</li>
 *     <li>{@link CSVCell}</li>
 * </ul>
 *
 * La méthode {@link CSVDelegate#init()} enregistre les lignes dans une List depuis {@link CSVFormat#parse(Reader)}
 * et est récupérable par {@link CSVDelegate#getRecords()}.</p>
 */
public class CSVDelegate {

    // Default format -> https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html#DEFAULT
    private CSVFormat format;
    private final Reader reader;
    private List<CSVRecord> records;

    public CSVDelegate(CSVFormat format, Reader reader) throws IOException {
        this.format = format;
        this.format = this.format.builder().get();
        this.reader = reader;
        this.init();
    }


    private void init(){
        try {
            //On associe le reader à un CSVParser et on enregistre les records en mémoire pour pouvoir les utiliser plusieurs fois
            //sinon lecture unique une fois le reader parcouru par le parser. voir -> https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVParser.html -> Parsing into memory
            CSVParser parser = this.format.parse(this.reader);
            this.records = parser.getRecords();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CSVRecord> getRecords() {
        return this.records;
    }

    public int getRecordsSize(){
        return this.getRecords().size() - 1;
    }

    public CSVRecord getRecordByIndex(int index){
        return this.getRecords().get(index);
    }

    public CSVFormat getFormat(){
        return this.format;
    }
}
