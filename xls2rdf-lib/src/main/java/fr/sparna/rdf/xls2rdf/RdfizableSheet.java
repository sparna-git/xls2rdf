package fr.sparna.rdf.xls2rdf;

import static fr.sparna.rdf.xls2rdf.ExcelHelper.getCellValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc.MessageCode;

/**
 * A Sheet in a Workbook that can be turned into RDF.
 * @author Thomas Francart
 *
 */
public class RdfizableSheet {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	protected Sheet sheet;
	protected Xls2RdfConverter converter;
	protected int titleRowIndex;
	protected List<ColumnHeader> columnHeaders;

	public RdfizableSheet(
			Sheet sheet,
			Xls2RdfConverter converter
	) {
		super();
		this.sheet = sheet;
		this.converter = converter;
	}
	
	public void init() {
		this.titleRowIndex = this.computeTitleRowIndex();
		if(hasDataSection()) {
			this.columnHeaders = this.computeColumnHeaders(this.titleRowIndex);
		}
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
	public boolean canRDFize() {
		if(sheet.getRow(0) == null) {
			log.debug(sheet.getSheetName()+" : First row is empty.");
			return false;
		}
		
		String uri = getCellValue(sheet.getRow(0).getCell(1));
		
		if(StringUtils.isBlank(uri)) {
			log.debug(sheet.getSheetName()+" : B1 is empty.");
			return false;
		} else {
			String fixedUri = converter.prefixManager.uri(uri, false);
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
	
	public String getSchemeOrGraph() {
		return getCellValue(sheet.getRow(0).getCell(1));
	}
	
	public int getTitleRowIndex() {
		return titleRowIndex;
	}
	
	public List<ColumnHeader> getColumnHeaders() {
		return columnHeaders;
	}

	/**
	 * Determines the index of the row containing the column headers.
	 * This is determined by checking if column B and C both contain a URI (full, starting with http://, or abbreviated using one of the declared prefix).
	 * @return
	 */
	protected int computeTitleRowIndex() {
		int headerRowIndex = 1;
		
		boolean found = false;
		ColumnHeaderParser headerParser = new ColumnHeaderParser(converter.prefixManager);
		for (int rowIndex = headerRowIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			
			int numFound = 0;
			// we start to check on the second column to avoid detecting a column header in ConceptScheme metadata
			for (short colIndex = 1; colIndex < 10; colIndex++) {
				try {
					Cell c = sheet.getRow(rowIndex).getCell(colIndex);
					ColumnHeader header = headerParser.parse(getCellValue(c), c);
					if(header.getProperty() != null) {
						log.info("Found proper property in header : "+header.getProperty().toString());
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
			for (int rowIndex = headerRowIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				// test if we find "URI" in the first column
				if(sheet.getRow(rowIndex) != null) {
					ColumnHeader headerA = null;
					try {
						Cell c = sheet.getRow(rowIndex).getCell(0);
						headerA = headerParser.parse(getCellValue(sheet.getRow(rowIndex).getCell(0)), c);
					} catch (Exception e) {
						// we prevent anything to go wrong in the parsing at this stage, since the parsing
						// tests cells for which we are unsure of the format.
						log.trace("Unable to parse a cell content while auto-detecting title row : "+e.getMessage());
					}
					
					if(headerA != null) {
						if(
								getCellValue(sheet.getRow(rowIndex).getCell(0)).equals("URI")
								||
								getCellValue(sheet.getRow(rowIndex).getCell(0)).equals("IRI")
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
	
	/**
	 * A RDFizable sheet has a data section if the title row is greater than 1
	 * @return
	 */
	public boolean hasDataSection() {
		return titleRowIndex > 1;
	}
	
	/**
	 * Parses the ColumnHeader from the title row index.
	 * @param rowNumber the index of the row containing the column headers
	 * @return
	 */
	protected List<ColumnHeader> computeColumnHeaders(int rowNumber) {
		List<ColumnHeader> columnNames = new ArrayList<>();
		Row row = this.sheet.getRow(rowNumber);
		
		ColumnHeaderParser headerParser = new ColumnHeaderParser(converter.prefixManager);
		if(row != null) {
			for (short i = 0; true; i++) {
				Cell cell = row.getCell(i);
				if (null == cell) break;
				String columnName = cell.getStringCellValue();
				// stop at the first empty value
				if (StringUtils.isBlank(columnName)) {
					break;
				}
				columnNames.add(headerParser.parse(columnName, cell));
			}
		}
		return columnNames;
	}
	
	protected List<ColumnHeader> getHeaderColumnHeaders() {
		List<ColumnHeader> headerColumnHeaders = new ArrayList<>();
		
		ColumnHeaderParser headerParser = new ColumnHeaderParser(converter.prefixManager);
		for (int rowIndex = 1; rowIndex < this.getTitleRowIndex(); rowIndex++) {
			if(sheet.getRow(rowIndex) != null) {
				String key = getCellValue(sheet.getRow(rowIndex).getCell(0));
				Cell cell = sheet.getRow(rowIndex).getCell(1);
				String value = getCellValue(cell);
				
				// parse the property
				ColumnHeader header = headerParser.parse(key, sheet.getRow(rowIndex).getCell(0));
				if(
						header != null
						&&
						header.getProperty() != null
						&&
						StringUtils.isNotBlank(value)
				) {
					headerColumnHeaders.add(header);
				}
			}
		}
		
		return headerColumnHeaders;
	}
	
	/**
	 * Reads the prefixes declared in the sheet. The prefixes are read in the top 20 rows, when column A contains "PREFIX" or "@prefix" (ignoring case).
	 * @return the map of prefixes
	 */
	public Map<String, String> readPrefixes() {
		Map<String, String> prefixes = new HashMap<String, String>();
		
		// read the prefixes in the top 20 rows		
		for (int rowIndex = 1; rowIndex <= 20; rowIndex++) {
			if(sheet.getRow(rowIndex) != null) {
				String prefixKeyword = getCellValue(sheet.getRow(rowIndex).getCell(0));
				// if we have the "prefix" keyword...
				// note : we add a null check here because there are problems with some sheets
				if(prefixKeyword != null && (prefixKeyword.equalsIgnoreCase("PREFIX") || prefixKeyword.equalsIgnoreCase("@prefix"))) {
					// and we have the prefix and namespaces defined...
					String prefix = getCellValue(sheet.getRow(rowIndex).getCell(1));
					if(StringUtils.isNotBlank(prefix)) {
						if(prefix.charAt(prefix.length()-1) == ':') {
							prefix = prefix.substring(0, prefix.length()-1);
						}
						String namespace = getCellValue(sheet.getRow(rowIndex).getCell(2));
						if(StringUtils.isNotBlank(namespace)) {
							log.info("Found prefix : "+prefix+" : <"+namespace+">");
							prefixes.put(prefix, namespace);
						}
					}
				}
			}
		}
		
		return prefixes;
	}

	public boolean validateHeaders(Predicate<IRI> propertyValidator, Xls2RdfMessageListenerIfc messageListener) {		
		boolean allValid = true;
		
		// validate also header headers
		for (ColumnHeader columnHeader : this.getHeaderColumnHeaders()) {
			if(columnHeader.getProperty() != null) {
				log.debug("Validating header property "+columnHeader.getProperty()+" (originally declared as "+columnHeader.getDeclaredProperty()+")");
				boolean valid = propertyValidator.test(columnHeader.getProperty());
				if(!valid) {
					String message = "Property "+columnHeader.getProperty()+" is not valid, in cell "+new CellReference(columnHeader.getHeaderCell()).formatAsString();
					log.error(message);
					messageListener.onMessage(
							MessageCode.INVALID_PROPERTY,
							new CellReference(columnHeader.getHeaderCell()).formatAsString(),
							message
					);
					allValid = false;
				} else {
					log.debug("Property "+columnHeader.getProperty()+" is valid.");
				}
			}
		}
		
		if(this.columnHeaders != null) {
			for (ColumnHeader columnHeader : this.columnHeaders) {
				if(columnHeader.getProperty() != null) {
					log.debug("Validating header property "+columnHeader.getProperty()+" (originally declared as "+columnHeader.getDeclaredProperty()+")");
					boolean valid = propertyValidator.test(columnHeader.getProperty());
					if(!valid) {
						String message = "Property "+columnHeader.getProperty()+" is not valid, in cell "+new CellReference(columnHeader.getHeaderCell()).formatAsString();
						log.error(message);
						messageListener.onMessage(
								MessageCode.INVALID_PROPERTY,
								new CellReference(columnHeader.getHeaderCell()).formatAsString(),
								message
						);
						allValid = false;
					} else {
						log.debug("Property "+columnHeader.getProperty()+" is valid.");
					}
				}
			}
		}
		
		
		return allValid;
	}
}
