package fr.sparna.rdf.xls2rdf.container.sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.ColumnHeader;
import fr.sparna.rdf.xls2rdf.ColumnHeaderParser;
import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.container.BaseLiteral;
import fr.sparna.rdf.xls2rdf.container.Container;
import fr.sparna.rdf.xls2rdf.container.Node;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;

public class SheetContainer implements Container {

    static Logger log = LoggerFactory.getLogger(SheetContainer.class.getName());

    public static final String TYPE = "SHEET";

    private Sheet sheet;
    private WorkbookContainer parent;

    private int titleRowIndex = -1;
    private List<String> columnNames = null;
    private Map<String, Node> keyValuePairs;

    public SheetContainer(Sheet sheet, WorkbookContainer parent) {
        this.sheet = sheet;
        this.parent = parent;
        this.titleRowIndex = computeTitleRowIndex();
        this.columnNames = computeColumnNames();
        this.keyValuePairs = computeKeyValuePairs();
    }

    @Override
    public String getId() {
        String b1Value = sheet.getRow(0).getCell(1).getCellValue();
        String fixedUri = new PrefixManager(this.parent.getPrefixes()).uri(b1Value, false);
        return fixedUri;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        if(this.titleRowIndex == -1) {
            return children;
        }

        for (int rowIndex = (this.titleRowIndex + 1); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if(row != null) {
                children.add(new RowContainer(row, this));
            }
        }
        return children;
    }

    @Override
    public List<String> getKeys() {
        return this.keyValuePairs.keySet().stream().toList();
    }

    @Override
    public Node getValue(String key) {
        return this.keyValuePairs.get(key);
    }

    @Override
    public Map<String, Node> getKeyValuePairs() {
        return this.keyValuePairs;
    }

    public Map<String, Node> computeKeyValuePairs() {
        Map<String, Node> keyValuePairs = new HashMap<>();

        int headerEnd = this.titleRowIndex;
        // si la ligne d'entete n'a pas été trouvée, on ne génère que la ressource d'entête
		if(headerEnd < 0) {
			log.info("Could not find header row index in sheet "+sheet.getSheetName()+", will parse header object until end of sheet (last rowNum = "+ sheet.getLastRowNum() +")");
			headerEnd = sheet.getLastRowNum()+1;
		}

        ColumnHeaderParser headerParser = new ColumnHeaderParser(new PrefixManager(this.parent.getPrefixes()));
        for (int rowIndex = 1; rowIndex < headerEnd; rowIndex++) {
            if(sheet.getRow(rowIndex) != null) {
                Row row = sheet.getRow(rowIndex);
                Cell cellKey = row.getCell(0);
                String key = (cellKey != null) ? cellKey.getCellValue() : null;
                
                // parse the property	
				ColumnHeader header = headerParser.parse(key, cellKey);
				if(
						header != null
						&&
						header.getProperty() != null
				) {		
                    if(row.getCell(1) != null) {
                        keyValuePairs.put(key, new BaseLiteral(row.getCell(1).getCellValue()));
                    } else {
                        keyValuePairs.put(key, new BaseLiteral(""));
                    }
                    
                }
            }
        }

        return keyValuePairs;
    }

    /**
	 * Determines the index of the row containing the column headers.
	 * This is determined by checking if column B and C both contain a URI (full, starting with http://, or abbreviated using one of the declared prefix).
	 * @return
	 */
	private int computeTitleRowIndex() {
		int headerRowIndex = -1;
		
		boolean found = false;
		ColumnHeaderParser headerParser = new ColumnHeaderParser(new PrefixManager(this.parent.getPrefixes()));
		for (int rowIndex = headerRowIndex; rowIndex <= this.sheet.getLastRowNum(); rowIndex++) {
			
			int numFound = 0;
			// we start to check on the second column to avoid detecting a column header in ConceptScheme metadata
			for (short colIndex = 1; colIndex < 10; colIndex++) {
				try {
					Cell c = this.sheet.getRow(rowIndex).getCell(colIndex);
					ColumnHeader header = headerParser.parse(c.getCellValue(), c);
					if(header.getProperty() != null) {
						log.debug("Found proper property in header : "+header.getProperty().toString());
						numFound++;
					}
				} catch (Exception e) {
					// we prevent anything to go wrong in the parsing at this stage, since the parsing
					// tests cells for which we are unsure of the format.
					log.trace("Unable to parse a cell content while auto-detecting title row : "+e.getMessage());
				}
			}
			
			// we check if we find 2 columns header in the first 10 columns
			if(numFound >= 2) {
				log.info("Found at least 2 headers with proper property declaration in the first 10 columns, title row is at index "+rowIndex);
				headerRowIndex = rowIndex;
				found = true;
				break;
			}
		}
		
		if(!found) {
			for (int rowIndex = headerRowIndex; rowIndex <= this.sheet.getLastRowNum(); rowIndex++) {
				// test if we find "URI" in the first column
				if(this.sheet.getRow(rowIndex) != null) {
					ColumnHeader headerA = null;
					try {
						Cell c = this.sheet.getRow(rowIndex).getCell(0);
						headerA = headerParser.parse(c.getCellValue(), c);
					} catch (Exception e) {
						// we prevent anything to go wrong in the parsing at this stage, since the parsing
						// tests cells for which we are unsure of the format.
						log.trace("Unable to parse a cell content while auto-detecting title row : "+e.getMessage());
					}
					
					if(headerA != null) {
						if(
								this.sheet.getRow(rowIndex).getCell(0).getCellValue().equals("URI")
								||
								this.sheet.getRow(rowIndex).getCell(0).getCellValue().equals("IRI")
						) {
							headerRowIndex = rowIndex;
							found = true;
							break;
						}
					}
				}
			}
		}
		
		return headerRowIndex;
	} 

    
    
    public List<String> getColumnNames() {
        return columnNames;
    }

    private java.util.List<String> computeColumnNames() {
        List<String> columnNames = new ArrayList<>();
        if(this.titleRowIndex == -1) {
            return columnNames;
        }
		Row row = this.sheet.getRow(this.titleRowIndex);
		
		if(row != null) {
			for (short i = 0; true; i++) {
				Cell cell = row.getCell(i);
				if (null == cell) break;
				String columnName = cell.getCellValue();
                if (StringUtils.isBlank(columnName)) {
                    // Look ahead in the next 5 columns
                    boolean nonEmptyLookahead = false;
                    for (int k = 1; k <= 5; k++) {
                        Cell nextCell = row.getCell(i + k);
                        if (nextCell != null && !StringUtils.isBlank(nextCell.getCellValue())) {
                            nonEmptyLookahead = true;
                            break;
                        }
                    }
                    // lookahead failed, we consider this column as the last one and stop the loop
                    if (!nonEmptyLookahead) {
                        break;
                    } else {
                        // lookahead succeeded, we add this column with an empty name
                        columnNames.add("");
                    }
                } else {
                    columnNames.add(columnName);
                }

				columnNames.add(columnName);
			}
		}

		return columnNames;
    }
    
}
