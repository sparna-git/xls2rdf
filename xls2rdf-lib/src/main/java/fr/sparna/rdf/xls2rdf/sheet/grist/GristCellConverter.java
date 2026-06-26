package fr.sparna.rdf.xls2rdf.sheet.grist;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class GristCellConverter {

    private static final GristCellConverter GRIST_CELL_CONVERTER = new GristCellConverter();

    private GristCellConverter(){}

    public static GristCellConverter getInstance(){
        return GRIST_CELL_CONVERTER;
    }

    public String convertIf(JsonNode node) {
        if(node == null) return "";
        if (node.isArray()) {
            if ("D".equals(node.get(0).asText())) {
                //dans le second emplacement du tableau se trouve le timestamp de Grist en second sous la forme 1.xxxxxx * 10^n
                double timestamp = node.get(1).asDouble();
                //dans le troisième emplacement du tableau se trouve la timeZone
                String timeZone = node.get(2).asText();
                //Grist fournit un timestamp de n secondes, Instant.ofEpcohMilli attend en milliseconds
                //donc 1sec = 1000 millisecondes
                //on multiplie donc le timestamp de grist par 1000
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) (timestamp * 1000)), ZoneId.of(timeZone));
                return dateTime.format(DateTimeFormatter.ISO_INSTANT);
                }
            else if ("L".equals(node.get(0).asText())) {
                StringJoiner j = new StringJoiner(",");
                //On parse à partir de l'index 1 car l'indice 0 représente le type de données du tablea "L" pour List.
                for (int i = 1; i < node.size(); i++) {
                    j.add(node.get(i).asText());
                }
                return j.toString();
            }
        }
        return null;
    }

}
