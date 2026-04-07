package fr.sparna.rdf.xls2rdf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc.MessageCode;

public class ResourceOrLiteralValueGenerator implements ValueProcessorIfc {

	/**
	 * 
	 */
	private final ValueProcessorFactory valueProcessorFactory;
	protected ColumnHeader header;
	protected PrefixManager prefixManager;
	protected Xls2RdfMessageListenerIfc messageListener;
	
	public ResourceOrLiteralValueGenerator(ValueProcessorFactory valueProcessorFactory, ColumnHeader header, PrefixManager prefixManager, Xls2RdfMessageListenerIfc messageListener) {
		super();
		this.valueProcessorFactory = valueProcessorFactory;
		this.header = header;
		this.prefixManager = prefixManager;
		this.messageListener = messageListener;
	}

	@Override
	public List<Statement> processValue(Model model, Resource subject, String value, Cell cell, String language) {
		String theCellValue = this.header.isNormalizeSpace()?ValueProcessorFactory.normalizeSpace(value):value;
		
		if (StringUtils.isBlank(theCellValue)) {
			return null;
		}
		
		IRI headerDatatype = header.getDatatype().orElse(null);
		String headerLanguage = header.getLanguage().orElse(null);

		// if the value starts with http, or uses a known namespace, then try to parse it as a resource
		// only if no datatype or language have been explicitely specified, in which case this will default to a literal
		if(
				headerDatatype == null
				&&
				headerLanguage == null
				&&
				(value.startsWith("http") || value.startsWith("mailto") || prefixManager.usesKnownPrefix(theCellValue))
		) {
			if(!header.isInverse()) {
				Value v = SimpleValueFactory.getInstance().createIRI(prefixManager.uri(theCellValue, false));
				Statement s = SimpleValueFactory.getInstance().createStatement(subject, header.getProperty(), v);
				model.add(s);
				return Collections.singletonList(s);
			} else {
				model.add(SimpleValueFactory.getInstance().createIRI(prefixManager.uri(theCellValue, false)), header.getProperty(),subject);
			}			
		} else if(headerDatatype == null && headerLanguage == null && value.startsWith("(") && value.endsWith(")")) {
			// handle rdf:List
			return this.valueProcessorFactory.turtleParsing(header.getProperty(), prefixManager).processValue(model, subject, value, cell, language);	
		} else if(headerDatatype == null && headerLanguage == null && value.startsWith("[") && value.endsWith("]")) {
			// handle blank nodes
			return this.valueProcessorFactory.turtleParsing(header.getProperty(), prefixManager).processValue(model, subject, value, cell, language);
		} else if(
				value.startsWith("\"")
				&&
				(
						value.contains("\"^^")
						||
						value.contains("\"@")
				)
		) {
			// handle cells that explicitly indicate a datatype or a language
			// in that case it has precedence over the ones indicated in the header
			return this.valueProcessorFactory.turtleParsing(header.getProperty(), prefixManager).processValue(model, subject, value, cell, language);
		} else {
			// if the value is surrounded with quotes, remove them, they were here to escape a URI to be considered as a literal
			String unescapedValue = (value.startsWith("\"") && value.endsWith("\""))?value.substring(1, value.length()-1):value;
			
			// consider it like a literal
			if(headerDatatype != null) {
				Literal l = null;
				
				if(headerDatatype.stringValue().equals(XMLSchema.DATE.stringValue())) {
					try {
						Date d = ExcelHelper.asCalendar(ValueProcessorFactory.normalizeSpace(unescapedValue)).getTime();
						l = SimpleValueFactory.getInstance().createLiteral(
								new SimpleDateFormat("yyyy-MM-dd").format(d),
								XMLSchema.DATE
						);
					} catch (Exception e) {
						// date parsing failed in the case the cell has a string format
						// test if string value has proper string format
						if(
								ValueProcessorFactory.normalizeSpace(unescapedValue).matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")
						) {
							l = SimpleValueFactory.getInstance().createLiteral(ValueProcessorFactory.normalizeSpace(unescapedValue), headerDatatype);
						} 
						
						// let's be smart and try to match french-formatted dates as well
						if(
								ValueProcessorFactory.normalizeSpace(unescapedValue).matches("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]")
						) {
							try {
								SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
								SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
								l = SimpleValueFactory.getInstance().createLiteral(sdf2.format(sdf1.parse(ValueProcessorFactory.normalizeSpace(unescapedValue))), headerDatatype);
							} catch (ParseException e1) {
								e1.printStackTrace();
								this.messageListener.onMessage(MessageCode.WRONG_FORMAT, new CellReference(cell).formatAsString(), "Failed to parse date format for value '"+ value +"'. Is the cell formatted as a date ?");
							}
						} 
						
						if (l == null){
							this.messageListener.onMessage(MessageCode.WRONG_FORMAT, new CellReference(cell).formatAsString(), "Failed to parse date format for value '"+ value +"'. Is the cell formatted as a date ?");
						}
					}
				} else if(headerDatatype.stringValue().equals(XMLSchema.DATETIME.stringValue())) {
					try {
						try {
							l = SimpleValueFactory.getInstance().createLiteral(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)ExcelHelper.asCalendar(ValueProcessorFactory.normalizeSpace(unescapedValue))));
						} catch (DatatypeConfigurationException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						// date parsing failed in the case the cell has a string format - then simply default to a typed literal creation

						// test if string value has proper string format
						if(
								ValueProcessorFactory.normalizeSpace(unescapedValue).matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")
						) {
							l = SimpleValueFactory.getInstance().createLiteral(ValueProcessorFactory.normalizeSpace(unescapedValue), headerDatatype);
						} 
						
						if (l == null) {
							this.messageListener.onMessage(MessageCode.WRONG_FORMAT, new CellReference(cell).formatAsString(), "Failed to parse datetime format for value '"+ value +"'. Is the cell forma");
						}
					}
				} else if(headerDatatype.stringValue().equals(XMLSchema.BOOLEAN.stringValue())) {
					List<String> TRUE_VALUES = Arrays.asList(new String[] { "true", "vrai", "1" });
					List<String> FALSE_VALUES = Arrays.asList(new String[] { "false", "faux", "0" });
					
					if(
							!TRUE_VALUES.contains(unescapedValue.toLowerCase())
							&&
							!FALSE_VALUES.contains(unescapedValue.toLowerCase())
					) {
						this.messageListener.onMessage(MessageCode.WRONG_FORMAT, new CellReference(cell).formatAsString(), "Failed to parse boolean format for value '"+ value +"'");
					} else {
						if(TRUE_VALUES.contains(unescapedValue.toLowerCase())) {
							l = SimpleValueFactory.getInstance().createLiteral("true", headerDatatype);
						} else {
							l = SimpleValueFactory.getInstance().createLiteral("false", headerDatatype);
						}							
					}
				}
				else {
					l = SimpleValueFactory.getInstance().createLiteral(ValueProcessorFactory.normalizeSpace(unescapedValue), headerDatatype);
				}
				
				if(l != null) {
					Statement s = SimpleValueFactory.getInstance().createStatement(subject, header.getProperty(), l);
					model.add(s);
					return Collections.singletonList(s);
				}
			} else if(value.startsWith("_:")) {
				Value v = SimpleValueFactory.getInstance().createBNode(value.substring(2));
				Statement s = SimpleValueFactory.getInstance().createStatement(subject, header.getProperty(), v);
				model.add(s);
				return Collections.singletonList(s);
			} else if(value.startsWith("<") && value.endsWith(">")) {
				Value v = SimpleValueFactory.getInstance().createIRI(prefixManager.relativeUri(value.substring(1, value.length()-1)));
				Statement s = SimpleValueFactory.getInstance().createStatement(subject, header.getProperty(), v);
				model.add(s);
				return Collections.singletonList(s);
			} else {
				return this.valueProcessorFactory.langOrPlainLiteral(header.getProperty()).processValue(model, subject, theCellValue, cell, language);
			}
		}
		
		return null;
	};		
}