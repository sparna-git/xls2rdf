package fr.sparna.rdf.xls2rdf;

import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc.MessageCode;
import fr.sparna.rdf.xls2rdf.sheet.Cell;
import fr.sparna.rdf.xls2rdf.sheet.ExcelRefs;
import fr.sparna.rdf.xls2rdf.sheet.Row;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A Sheet in a Workbook that can be turned into RDF.
 * @author Thomas Francart
 *
 */
public class RdfizableSheet {

	static Logger log = LoggerFactory.getLogger(RdfizableSheet.class.getName());
	
	protected Sheet sheet;
	protected PrefixManager prefixManager;

	protected Map<String, MappingRule> mappingRules;
	protected HeaderLine headerLine;

	public RdfizableSheet(
		Sheet sheet,
		PrefixManager prefixManager,
		Map<String, MappingRule> mappingRules
	) {
		super();
		this.sheet = sheet;
		this.prefixManager = prefixManager;
		this.mappingRules = mappingRules;

		int titleRowIndex = computeTitleRowIndex(sheet, prefixManager);
		// if we found a title row somewhere...
		if(titleRowIndex > -1) {
			this.headerLine = new HeaderLine(sheet.getRow(titleRowIndex));
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
		
		String uri = sheet.getRow(0).getColumnValue(1);
		
		if(StringUtils.isBlank(uri)) {
			log.debug(sheet.getSheetName()+" : B1 is empty.");
			return false;
		} else {
			String fixedUri = this.prefixManager.uri(uri, false);
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
		return sheet.getRow(0).getCell(1).getCellValue();
	}	

	public Map<String, MappingRule> getMappingRules() {
		return mappingRules;
	}

	/**
	 * Determines the index of the row containing the column headers.
	 * This is determined by checking if column B and C both contain a URI (full, starting with http://, or abbreviated using one of the declared prefix).
	 * @return
	 */
	public static int computeTitleRowIndex(Sheet sheet, PrefixManager prefixManager) {
		int headerRowIndex = -1;
		
		boolean found = false;
		MappingRuleParser headerParser = new MappingRuleParser(prefixManager);
		// look for it in either the lastRowNum or 200, whichever is smaller - don't scan very large tables below row 200
		for (int rowIndex = headerRowIndex; rowIndex <= Math.min(sheet.getLastRowNum(), 200); rowIndex++) {
			
			int numFound = 0;
			// we start to check on the second column to avoid detecting a column header in ConceptScheme metadata
			for (short colIndex = 1; colIndex < 10; colIndex++) {
				try {
					Cell c = sheet.getRow(rowIndex).getCell(colIndex);
					MappingRule rule = headerParser.parse(c.getCellValue());
					if(rule.getProperty() != null) {
						log.debug("Found proper property in header : "+rule.getProperty().toString());
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
			for (int rowIndex = -1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				// test if we find "URI" in the first column
				if(sheet.getRow(rowIndex) != null) {
					if(
							sheet.getRow(rowIndex).getCell(0) != null
							&&
							(
								sheet.getRow(rowIndex).getCell(0).getCellValue().equals("URI")
								||
								sheet.getRow(rowIndex).getCell(0).getCellValue().equals("IRI")
							)
					) {
						headerRowIndex = rowIndex;
						found = true;
						break;
					}
				}
			}
		}
		
		return headerRowIndex;
	}
	
	/**
	 * A RDFizable sheet has a data section if we found some header line
	 * @return
	 */
	public boolean hasDataSection() {
		return this.headerLine != null;
	}

	/**
	 * @return Attempt to auto-detect mapping rules in a given sheet, by looking up the title row and parsing it
	 */
	public static Map<String, MappingRule> autoDetectMappingRules(Sheet sheet, PrefixManager prefixManager) {
		Map<String, MappingRule> mappingRules = new HashMap<>();

		// first lookup the title row
		int titleRowIndex = computeTitleRowIndex(sheet, prefixManager);

		if(titleRowIndex < 0) return mappingRules;

		Row row = sheet.getRow(titleRowIndex);
		
		MappingRuleParser headerParser = new MappingRuleParser(prefixManager);
		if(row != null) {
			for (short i = 0; true; i++) {
				Cell cell = row.getCell(i);
				if (null == cell) break;
				String columnName = cell.getCellValue();

				// stop at the first empty value
				if (StringUtils.isBlank(columnName)) {
					break;
				}

				mappingRules.put(columnName, headerParser.parse(columnName));
			}
		}
		return mappingRules;
	}

	/**
	 * @return The association between a row number in the header part of the sheet and a mapping rule
	 */
	protected Map<Integer, MappingRule> parseHeaderMappingRules() {
		Map<Integer, MappingRule> mappingRules = new HashMap<>();
		
		MappingRuleParser headerParser = new MappingRuleParser(this.prefixManager);
		int endOfHeader = (this.getHeaderLine() != null)?this.getHeaderLine().getRowIndex():this.sheet.getLastRowNum()+1;
		for (int rowIndex = 1; rowIndex < endOfHeader; rowIndex++) {
			if(sheet.getRow(rowIndex) != null) {
				String key = sheet.getRow(rowIndex).getColumnValue(0);
				String value = sheet.getRow(rowIndex).getColumnValue(1);
				
				// attempt to parse the property
				MappingRule mappingRule = headerParser.parse(key);
				if(
						mappingRule != null
						&&
						mappingRule.getProperty() != null
						&&
						StringUtils.isNotBlank(value)
				) {
					mappingRules.put(rowIndex, mappingRule);
				}
			}
		}
		
		return mappingRules;
	}

	/**
	 * @param title
	 * @return the mapping rule associated to this header
	 */
	public MappingRule findMappingRuleByHeader(String title) {
		return this.mappingRules.get(title);
	}

	public HeaderLine getHeaderLine() {
		return this.headerLine;
	}

	public boolean validateHeaders(Predicate<IRI> propertyValidator, Xls2RdfMessageListenerIfc messageListener) {		
		boolean allValid = true;
		
		// validate also header headers
		for(Map.Entry<Integer, MappingRule> oneMapping : this.parseHeaderMappingRules().entrySet()) {
			MappingRule mappingRule = oneMapping.getValue();
			if(mappingRule.getProperty() != null) {
				log.debug("Validating header property "+mappingRule.getProperty()+" (originally declared as "+mappingRule.getDeclaredProperty()+")");
				boolean valid = propertyValidator.test(mappingRule.getProperty());
				if(!valid) {
					String cellReference = "A"+oneMapping.getKey();
					String message = "Property "+mappingRule.getProperty()+" is not valid, in cell "+cellReference;
					log.error(message);
					messageListener.onMessage(
							MessageCode.INVALID_PROPERTY,
						    cellReference,
							message
					);
					allValid = false;
				} else {
					log.debug("Property "+mappingRule.getProperty()+" is valid.");
				}
			}

		}
		
		if(this.mappingRules != null) {
			for(Map.Entry<String, MappingRule> oneMapping : this.mappingRules.entrySet()) {
				MappingRule mappingRule = oneMapping.getValue();
				if(oneMapping.getValue().getProperty() != null) {
					log.debug("Validating header property "+mappingRule.getProperty()+" (originally declared as "+mappingRule.getDeclaredProperty()+")");
					boolean valid = propertyValidator.test(mappingRule.getProperty());
					if(!valid) {
						String message = "Property "+mappingRule.getProperty()+" is not valid, in mapping "+mappingRule.getOriginalValue();
						log.error(message);
						messageListener.onMessage(
								MessageCode.INVALID_PROPERTY,
								mappingRule.getOriginalValue(),
								message
						);
						allValid = false;
					} else {
						log.debug("Property "+mappingRule.getProperty()+" is valid.");
					}
				}
			}
		}
		
		
		return allValid;
	}

		/**
	 * Finds the column index based on a column ID reference or an Excel column reference.
	 * Returns -1 if not found.
	 * 
	 * @param headers
	 * @param idRef
	 * @return
	 */
	public static int idRefToColumnIndex(RdfizableSheet rdfizableSheet, String idRef) {
		// iterate on all headers in the file...
		for(int i = 0; i < rdfizableSheet.getHeaderLine().getHeaders().size(); i++) {
			String oneHeader = rdfizableSheet.getHeaderLine().getHeaders().get(i);
			// find corresponding mapping rule for this header
			MappingRule mappingRule = rdfizableSheet.findMappingRuleByHeader(oneHeader);
			if(
					mappingRule != null
					&&
					(mappingRule.getId() != null && mappingRule.getId().equals(idRef))
			) {
				return i;
			}
		}
		
		// if we haven't found the proper column id, try it as an Excel column reference
		if(idRef.length() <= 2) {
			int idx = ExcelRefs.colLettersToIndex(idRef);
			if(idx != -1) return idx;
		}

		return -1;
	}
	
	/**
	 * Finds the column index based on a reference that can be 
	 * either an ID reference or a property reference or an Excel column reference.
	 * Returns -1 if not found.
	 * 
	 * @param rdfizableSheet the sheet that contains the headers + the association between headers and mapping rules
	 * @param idOrPropertyRef the ID or property reference to search
	 * @return
	 */
	public static int idRefOrPropertyRefToColumnIndex(RdfizableSheet rdfizableSheet, String idOrPropertyRef) {
		// iterate on all headers in the file...
		for(int i = 0; i < rdfizableSheet.getHeaderLine().getHeaders().size(); i++) {
			String oneHeader = rdfizableSheet.getHeaderLine().getHeaders().get(i);
			// find corresponding mapping rule for this header
			MappingRule mappingRule = rdfizableSheet.findMappingRuleByHeader(oneHeader);
			if(
					mappingRule != null
					&&
					(
						(mappingRule.getId() != null && mappingRule.getId().equals(idOrPropertyRef))
						||
						(mappingRule.getDeclaredProperty() != null && mappingRule.getDeclaredProperty().equals(idOrPropertyRef))
					)
			) {
				return i;
			}
		}
		
		// if we haven't found the proper column id, try it as an Excel column reference
		if(idOrPropertyRef.length() <= 2) {
			int idx = ExcelRefs.colLettersToIndex(idOrPropertyRef);
			if(idx != -1) return idx;
		}
		
		return -1;
	}
}
