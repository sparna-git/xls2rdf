package fr.sparna.rdf.xls2rdf.sheet;

import java.io.File;
import java.io.FileInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.sheet.csv.CSVWorkbookFactory;
import fr.sparna.rdf.xls2rdf.sheet.excel.ExcelWorkbookFactory;
import fr.sparna.rdf.xls2rdf.sheet.opendocument.OpenDocumentWorkbookFactory;

public class WorkbookFactory {
    
    private static Logger log = LoggerFactory.getLogger(WorkbookFactory.class.getName());

    public static Workbook createWorkbook(File input) throws Exception {
        log.info("Building workbook from file " + input.getAbsolutePath() + "...");
        Workbook workbook;
        String extension = "";
        //Le pattern récupére l'extension du fichier soit le dernier .xxx
        Pattern p = Pattern.compile("\\.[^.]+$");
        //On applique le pattern sur le nom du fichier
        Matcher m = p.matcher(input.getName().trim());
        //Si c'est bien un fichier on trouve une extension qu'on récupére
        if(m.find()) extension = m.group();
        workbook = switch (extension){
            //Voir https://support.microsoft.com/fr-fr/office/formats-de-fichier-pris-en-charge-dans-excel-0943ff2c-6014-4e8d-aaea-b83d51d46247
            case ".xls", ".xlsx", ".xlsm" -> ExcelWorkbookFactory.open(input);
            case ".ods" -> OpenDocumentWorkbookFactory.open(input);
            case ".csv" -> CSVWorkbookFactory.open(CSVFormat.DEFAULT, new FileInputStream(input), input.getName());
            default -> null;
        };

        return workbook;
    }

}
