package fr.sparna.rdf.xls2rdf.mapping;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class YamlParser {

    private Map<String, String> prefixes;
    private List<YamlSheet> sheets;
    private String base;


    public YamlParser(){}

    public Optional<List<YamlSheet>> getOptionalSheets() {
        return Optional.ofNullable(sheets);
    }

    public Optional<Map<String, String>> getOptionalPrefixes() {
        return Optional.of(prefixes);
    }

    public Optional<String> getOptionalBase(){
        return Optional.ofNullable(base);
    }

    public void setPrefixes(Map<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setSheets(List<YamlSheet> sheets) {
        this.sheets = sheets;
    }

    public static YamlParser getInstance(InputStream in){
        Yaml yaml = new Yaml(new Constructor(YamlParser.class, new LoaderOptions()));
        //TypeDescription permet d'indiquer les types génériques d'une propriété de la classe concernée
        TypeDescription description = new TypeDescription(YamlParser.class);// <-------- Ici on indique une description pour YamlParser.class
        description.addPropertyParameters("prefixes", String.class, String.class);//<------ Pour la propriété prefixes elle est de type Map<String, String>, sinon YamlSnake ne sait pas
        description.addPropertyParameters("sheets", YamlParser.YamlSheet.class);//<---------Pour la propriété sheets elle est de type List<YamlSheet>
        description.addPropertyParameters("base", String.class);
        yaml.addTypeDescription(description);//<-------- On ajoute la description à yaml
        return yaml.load(in); //<------ On charge la configuration dans Yaml et cela nous retourne une nouvelle instance de YamlParser avec les propriétés chargées
    }

    /*
     *************************
     * YAML RULE ATTRIBUTE   *
     * ***********************
     */
    public static class YamlSheet{

        private String name;
        private Map<String, String> rules;

        public YamlSheet(){}

        public Map<String, String> getRules() {
            return this.rules;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String sheetName) {
            this.name = sheetName;
        }

        public void setRules(Map<String, String> rule) {
            this.rules = rule;
        }
    }

}
