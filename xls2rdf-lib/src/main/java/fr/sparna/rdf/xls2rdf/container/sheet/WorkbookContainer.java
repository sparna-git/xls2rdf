package fr.sparna.rdf.xls2rdf.container.sheet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.container.Container;
import fr.sparna.rdf.xls2rdf.container.Node;
import fr.sparna.rdf.xls2rdf.container.RootContainer;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.excel.ExcelWorkbook;

public class WorkbookContainer implements RootContainer {
    
    static Logger log = LoggerFactory.getLogger(ExcelWorkbook.class.getName());

    public static final String TYPE = "WORKBOOK";

    private Workbook workbook;
    private transient Map<String, String> prefixes = null;
    private transient String baseIri = null;

    public WorkbookContainer(Workbook workbook) {
        this.workbook = workbook;
        this.prefixes = computePrefixes();
        this.baseIri = computeBaseIri();
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Container getParent() {
        return null;
    }

    @Override
    public List<Node> getChildren() {
        return StreamSupport.stream(this.workbook.spliterator(), false)
        .filter(sheet -> this.canConvert(sheet))
        .map(sheet -> (Node)new SheetContainer(sheet, this)).toList();
    }

    @Override
    public List<String> getKeys() {
        return null;
    }

    @Override
    public Node getValue(String key) {
        return null;
    }

    @Override
    public Map<String, Node> getKeyValuePairs() {
        return null;
    }

    @Override
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    @Override
    public String getBaseIri() {
        return baseIri;
    }

    /**
	 * Reads the prefixes declared in the workbook. The prefixes are read in the top 100 rows, when column A contains "PREFIX" or "@prefix" (ignoring case).
	 * @return the map of prefixes
	 */
	private Map<String, String> computePrefixes() {
		Map<String, String> prefixes = new HashMap<String, String>();
		
        for(Sheet sheet : this.workbook) {
            // read the prefixes in the top 100 rows	(including the first one for cases where all prefixes are grouped in the first sheet)
            int maxRowToCheck = Math.min(100, sheet.getLastRowNum());
            for (int rowIndex = 0; rowIndex <= maxRowToCheck; rowIndex++) {
                if(sheet.getRow(rowIndex) != null) {
                    Row row = sheet.getRow(rowIndex);
                    String prefixKeyword = row.getColumnValue(0);
                    // if we have the "prefix" keyword...
                    // note : we add a null check here because there are problems with some sheets
                    if(prefixKeyword != null && (prefixKeyword.equalsIgnoreCase("PREFIX") || prefixKeyword.equalsIgnoreCase("@prefix"))) {

                        // and we have the prefix and namespaces defined...
                        String prefix = row.getColumnValue(1);
                        if(StringUtils.isNotBlank(prefix)) {
                            if(prefix.charAt(prefix.length()-1) == ':') {
                                prefix = prefix.substring(0, prefix.length()-1);
                            }

                            String namespace = row.getColumnValue(2);
                            if(StringUtils.isNotBlank(namespace)) {
                                log.debug("Found prefix : "+prefix+" : <"+namespace+">");
                                prefixes.put(prefix, namespace);
                            }
                        }
                    }
                }
            }
        }

		return prefixes;
	}

    public String computeBaseIri() {
        for(Sheet sheet : this.workbook) {
            // read the base IRI in the top 100 rows	(including the first one for cases where all prefixes are grouped in the first sheet)
            for (int rowIndex = 0; rowIndex <= 100; rowIndex++) {
                if(sheet.getRow(rowIndex) != null) {
                    Row row = sheet.getRow(rowIndex);

                    String baseKeyword = row.getColumnValue(0);
                    // if we have the "base" keyword...
                    // note : we add a null check here because there are problems with some sheets
                    if(baseKeyword != null && (baseKeyword.equalsIgnoreCase("BASE") || baseKeyword.equalsIgnoreCase("@base"))) {
                        // and we have the prefix and namespaces defined...
                        String baseIri = row.getColumnValue(1);
                        if(StringUtils.isNotBlank(baseIri)) {
                            log.debug("Found base IRI : "+baseIri);
                            return baseIri;
                        }
                    }
                }
            }
        }

        return null;
    }

        /**
	 * A Sheet can be converted to RDF if :
	 * <ol>
	 *   <li>The first row is not empty</li>
	 *   <li>Cell B1 contains a value</li>
	 *   <li>The value is a URI starting with http:// or using one of the declared prefixes in the file</li>
	 * </ol>
	 * @return true if this sheet can be converted in RDF by the converter, false otherwise.
	 */
    public boolean canConvert(Sheet sheet) {
		if(sheet.getRow(0) == null) {
			log.debug(sheet.getSheetName()+" : First row is empty.");
			return false;
		}
		
		String uri = sheet.getRow(0).getColumnValue(1);
		
		if(StringUtils.isBlank(uri)) {
			log.debug(sheet.getSheetName()+" : B1 is empty.");
			return false;
		} else {
			String fixedUri = new PrefixManager(this.getPrefixes()).uri(uri, false);
			try {
				new URI(fixedUri);
			} catch (URISyntaxException e) {
				log.debug(sheet.getSheetName()+" : B1 is not a valid URI ('"+uri+"').");
				return false;
			} catch (NullPointerException e) {
				log.debug("Cannot build a valid URI from '"+uri+"'.");
				return false;
			}
		}
		
		return true;
    }

}
