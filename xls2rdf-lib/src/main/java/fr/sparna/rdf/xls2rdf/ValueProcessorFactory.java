package fr.sparna.rdf.xls2rdf;

import static fr.sparna.rdf.xls2rdf.ExcelHelper.getCellValue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.listen.LogXls2RdfMessageListener;
import fr.sparna.rdf.xls2rdf.reconcile.ReconciliableValueSetIfc;

public final class ValueProcessorFactory {
	
	private static Logger log = LoggerFactory.getLogger(ValueProcessorFactory.class.getName());
	
	/**
	 * Message listener for messages that need to be send to the outside world
	 */
	private Xls2RdfMessageListenerIfc messageListener = new LogXls2RdfMessageListener();
	
	public ValueProcessorFactory(Xls2RdfMessageListenerIfc messageListener) {
		super();
		this.messageListener = messageListener;
	}

	public ValueProcessorIfc split(ValueProcessorIfc delegate, String separator) {
		return (model, subject, value, cell, language) -> {
			
			if (StringUtils.isBlank(value)) {
				return null;
			}

			Arrays.stream(StringUtils.split(value, separator)).forEach(
				aValue -> delegate.processValue(model, subject, normalizeSpace(aValue), cell, language)
			);
			return null;
		};
	}
	
	public ValueProcessorIfc resource(IRI property, PrefixManager prefixManager) {
		return (model, subject, value, cell, language) -> {
			
			if (StringUtils.isBlank(value)) {
				return null;
			}
			
			IRI iri = SimpleValueFactory.getInstance().createIRI(prefixManager.uri(normalizeSpace(value), true));
			
			// can be null if we expected an IRI but we had a literal
			if(iri == null) {
				throw new Xls2RdfException("Expected a URI but got '"+normalizeSpace(value)+"'");
			}
			
			model.add(subject, property, iri);
			return null;
		};
	}
	
	public ValueProcessorIfc lookup(ColumnHeader header, Sheet sheet, int lookupColumn, int uriColumn, PrefixManager prefixManager) {
		return (model, subject, value, cell, language) -> {
			String lookupValue = value;
			
			if(lookupValue.equals("")) {
				return null;
			}
			
			Row foundRow = ExcelHelper.columnLookup(lookupValue, sheet, lookupColumn, true);
			
			if(foundRow != null) {				
				ResourceOrLiteralValueGenerator g = new ResourceOrLiteralValueGenerator(this, header, prefixManager, messageListener);
				return g.processValue(model, subject, getCellValue(foundRow.getCell(uriColumn)), cell, language);				
			} else {
				// throw Exception if a reference was not found
				log.error(new CellReference(cell.getRowIndex(), cell.getColumnIndex()).formatAsString()+" Unable to find value '"+lookupValue+"' in column "+CellReference.convertNumToColString(lookupColumn)+", while trying to generate property "+header.getProperty());
				// keep the triple as a literal with special predicate ?				
				// throw new Xls2SkosException("Unable to find value '"+lookupValue+"' in column of index "+lookupColumn+", while trying to generate property "+property);
			}
			
			return null;
		};
	}
	
	public ValueProcessorIfc reconcile(ColumnHeader header, PrefixManager prefixManager, ReconciliableValueSetIfc reconciledValues) {
		return (model, subject, value, cell, language) -> {
			String lookupValue = normalizeSpace(value);
			
			if(lookupValue.equals("")) {
				return null;
			}
			
			IRI result = reconciledValues.getReconciledValue(value);
			if(result != null) {
				ResourceOrLiteralValueGenerator g = new ResourceOrLiteralValueGenerator(this, header, prefixManager, messageListener);
				return g.processValue(model, subject, result.toString(), cell, language);
			} else {
				log.error("Unable to find value '"+lookupValue+"'@"+language+" in reconciled values");
			}
			
//			if(filteredStatements.size() == 1) {
//			ResourceOrLiteralValueGenerator g = new ResourceOrLiteralValueGenerator(header, prefixManager);
//			return g.addValue(model, subject, filteredStatements.get(0).getSubject().toString(), language);		
//		} else if(filteredStatements.size() > 1) {
//			log.error("Found multiple values for '"+lookupValue+"' in type/scheme '"+reconcileOn+"' : "+filteredStatements.stream().map(s -> s.getSubject().toString()).collect(Collectors.joining(", ")));
//		} else {
//			log.error("Unable to find value '"+lookupValue+"'@"+language+" in a type/scheme '"+ reconcileOn +"' in the model");
//		}
			
//			try(RepositoryConnection c = supportRepository.getConnection()) {
//				// look for every value in any predicate
//				List<Statement> statementsWithValue = Iterations.asList(c.getStatements(null, null, SimpleValueFactory.getInstance().createLiteral(lookupValue, language)));
//				
//				List<Statement> filteredStatements = new ArrayList<Statement>();
//				// filter with the reconcileOn if present
//				if(reconcileOn != null) {
//					for (Statement s : model) {
//						filteredStatements.addAll(Iterations.asList(
//								c.getStatements(s.getSubject(), RDF.TYPE, reconcileOn)
//						));
//						filteredStatements.addAll(Iterations.asList(
//								c.getStatements(s.getSubject(), SKOS.IN_SCHEME, reconcileOn)
//						));
//					}
//				} else {
//					filteredStatements = statementsWithValue;
//				}
//				
//				if(filteredStatements.size() == 1) {
//					ResourceOrLiteralValueGenerator g = new ResourceOrLiteralValueGenerator(header, prefixManager);
//					return g.addValue(model, subject, filteredStatements.get(0).getSubject().toString(), language);		
//				} else if(filteredStatements.size() > 1) {
//					log.error("Found multiple values for '"+lookupValue+"' in type/scheme '"+reconcileOn+"' : "+filteredStatements.stream().map(s -> s.getSubject().toString()).collect(Collectors.joining(", ")));
//				} else {
//					log.error("Unable to find value '"+lookupValue+"'@"+language+" in a type/scheme '"+ reconcileOn +"' in the model");
//				}
//			}		
			
			return null;
		};
	}

	@Deprecated
	public ValueProcessorIfc reconcileLocal(ColumnHeader header, PrefixManager prefixManager, IRI reconcileOn, Repository supportRepository) {
		return (model, subject, value, cell, language) -> {
			String lookupValue = normalizeSpace(value);
			
			if(lookupValue.equals("")) {
				return null;
			}
			
			try(RepositoryConnection c = supportRepository.getConnection()) {
				// look for every value in any predicate
				List<Statement> statementsWithValue = Iterations.asList(c.getStatements(null, null, SimpleValueFactory.getInstance().createLiteral(lookupValue, language)));
				
				List<Statement> filteredStatements = new ArrayList<Statement>();
				// filter with the reconcileOn if present
				if(reconcileOn != null) {
					for (Statement s : model) {
						filteredStatements.addAll(Iterations.asList(
								c.getStatements(s.getSubject(), RDF.TYPE, reconcileOn)
						));
						filteredStatements.addAll(Iterations.asList(
								c.getStatements(s.getSubject(), SKOS.IN_SCHEME, reconcileOn)
						));
					}
				} else {
					filteredStatements = statementsWithValue;
				}
				
				if(filteredStatements.size() == 1) {
					ResourceOrLiteralValueGenerator g = new ResourceOrLiteralValueGenerator(this, header, prefixManager, messageListener);
					return g.processValue(model, subject, filteredStatements.get(0).getSubject().toString(), cell, language);		
				} else if(filteredStatements.size() > 1) {
					log.error("Found multiple values for '"+lookupValue+"' in type/scheme '"+reconcileOn+"' : "+filteredStatements.stream().map(s -> s.getSubject().toString()).collect(Collectors.joining(", ")));
				} else {
					log.error("Unable to find value '"+lookupValue+"'@"+language+" in a type/scheme '"+ reconcileOn +"' in the model");
				}
			}		
			
			return null;
		};
	}
	
	public ValueProcessorIfc ignoreIfParenthesis(ValueProcessorIfc delegate) {
		return (model, subject, value, cell, language) -> {
			
			String theValue = normalizeSpace(value);
			if (theValue.startsWith("(") && theValue.endsWith(")")) {
				return null;
			} else {
				delegate.processValue(model, subject, theValue, cell, language);
				return null;
			}

		};
	}

	public ValueProcessorIfc copyTo(IRI copyTo, ValueProcessorIfc delegate) {
		return (model, subject, value, cell, language) -> {
			Value v = delegate.processValue(model, subject, value, cell, language);
			if(v != null) {
				model.add(subject, copyTo, v);
			}
			return null;
		};
	}
	
	
	public ValueProcessorIfc resourceOrLiteral(ColumnHeader header, PrefixManager prefixManager) {
		ResourceOrLiteralValueGenerator g = new ResourceOrLiteralValueGenerator(this, header, prefixManager, messageListener);
		return g;
	}

	public ValueProcessorIfc turtleParsing(IRI property, PrefixManager prefixManager) {
		return (model, subject, value, cell, language) -> {
			// create a small piece of Turtle by concatenating...
			StringBuffer turtle = new StringBuffer();
			// ... the prefixes				
			turtle.append(prefixManager.getPrefixesTurtleHeader());
			// ... the subject and the predicate
			turtle.append("<"+subject.stringValue()+">"+" "+"<"+property.stringValue()+"> ");
			// ... the value (blank node or list or value with datatype or language)
			turtle.append(value);
			// ... and a final dot if there is not one already at the end
			if(!normalizeSpace(value).endsWith(".")) {
				turtle.append(".");
			}
			
			// to debug created turtle
			// System.out.println(turtle);
			
			// now parse the Turtle String and collect the statements in a StatementCollector
			StatementCollector collector = new StatementCollector();
			RDFParser parser = RDFParserRegistry.getInstance().get(RDFFormat.TURTLE).get().getParser();
			parser.setRDFHandler(collector);
			try {
				parser.parse(new StringReader(turtle.toString()), RDF.NS.toString());
				// then add all the resulting statements to the final Model
				model.addAll(collector.getStatements());
				return null;
			} catch (Exception e) {
				// if anything goes wrong, default to creating a literal
				log.error("Error in parsing Turtle :\n"+turtle);
				e.printStackTrace();
				return langOrPlainLiteral(property).processValue(model, subject, value, cell, language);
			}
			
		};
	}
	
	public ValueProcessorIfc dateLiteral(IRI property) {
		return (model, subject, value, cell, language) -> {
			
			if (StringUtils.isBlank(value)) return null;

			Literal literal = null; 
			try {
				literal = SimpleValueFactory.getInstance().createLiteral(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)ExcelHelper.asCalendar(value)));
				model.add(subject, property,literal);
			}
			catch (NumberFormatException ignore) {
			}
			catch (DatatypeConfigurationException ignore) {
				ignore.printStackTrace();
			}
			return literal;
		};
	}

	public ValueProcessorIfc plainLiteral(IRI property) {
		return (model, subject, value, cell, language) -> {
			Literal literal = SimpleValueFactory.getInstance().createLiteral(normalizeSpace(value));
			model.add(subject, property, literal);
			return literal;
		};
	}
	
	public ValueProcessorIfc langOrPlainLiteral(IRI property) {
		return (model, subject, value, cell, language) -> {
			Literal literal;
			if(language != null) {
				literal = SimpleValueFactory.getInstance().createLiteral(normalizeSpace(value), language);
			} else {
				literal = SimpleValueFactory.getInstance().createLiteral(normalizeSpace(value));
			}
			model.add(subject, property, literal);
			return literal;
		};
	}

	public ValueProcessorIfc skosXlLabel(IRI xlLabelProperty, PrefixManager prefixManager) {
		return (model, subject, value, cell, language) -> {
			// String labelUri = ConceptSchemeFromExcel.fixUri(value);
			String labelUri = prefixManager.uri(value, true);
			IRI labelResource = SimpleValueFactory.getInstance().createIRI(labelUri);
			model.add(labelResource, RDF.TYPE, SKOSXL.LABEL);
			model.add(subject, xlLabelProperty, labelResource);
			return labelResource;
		};
	}

	public ValueProcessorIfc manchesterClassExpressionParser(ColumnHeader header, PrefixManager prefixManager) {
		ManchesterClassExpressionParser p = new ManchesterClassExpressionParser(header, prefixManager, messageListener);
		return p;
	}
	
	public static String normalizeSpace(String s) {
		return s.replaceAll("\\h+"," ").trim();
		// return s.replaceAll("(^\\h*)|(\\h*$)", " ").trim();
	}
	
}
