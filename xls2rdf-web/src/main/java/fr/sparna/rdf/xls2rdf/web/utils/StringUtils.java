package fr.sparna.rdf.xls2rdf.web.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StringUtils {

    final static Logger log = LoggerFactory.getLogger(StringUtils.class);

    public final static String DEFAULT_FILE_NAME = "xls2rdf";

    private StringUtils(){}

    public static String formatFileName(Object obj, String mediaType){
        if(obj instanceof URL url){
            return  String.format("%s-%s.%s", formatUrlName(url), formatDateISO8601(), mediaType);
        }
        if(obj instanceof MultipartFile file){
            return  String.format("%s-%s.%s", formatFileName(file), formatDateISO8601(), mediaType);
        }
        return DEFAULT_FILE_NAME;
    }



    private static String formatUrlName(URL url){
        String buffer = (!url.getPath().isEmpty()) ? url.getPath() : DEFAULT_FILE_NAME;
        // keep only latest file, after final /
        buffer = (buffer.contains("/")) ? buffer.substring(buffer.lastIndexOf("/") + 1) : buffer;
        // set the output file name to the name of the input file
        buffer = (buffer.contains("."))
                ? buffer.substring(0, buffer.lastIndexOf('.'))
                : buffer;
        return buffer;
    }

    private static String formatFileName(MultipartFile file){
        return  file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.'))
                : file.getOriginalFilename();
    }

    public static String formatDateISO8601(){
        LocalDate date = LocalDate.now();
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}
