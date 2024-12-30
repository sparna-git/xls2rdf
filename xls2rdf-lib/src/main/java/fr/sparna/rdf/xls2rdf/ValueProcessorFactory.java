package fr.sparna.rdf.xls2rdf;

import static fr.sparna.rdf.xls2rdf.ExcelHelper.getCellValue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
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

			List<Statement> result = new ArrayList<>();
			Arrays.stream(StringUtils.split(value, separator)).forEach(aValue -> {
				List<Statement> statements = delegate.processValue(model, subject, normalizeSpace(aValue), cell, language);
				if(statements != null) {
					result.addAll(statements);
				}
			});
			return result;
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
			
			Statement s = SimpleValueFactory.getInstance().createStatement(subject, property, iri);
			model.add(s);
			return Collections.singletonList(s);
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
				return null;
			}
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
				return delegate.processValue(model, subject, theValue, cell, language);
			}

		};
	}

	public ValueProcessorIfc copyTo(IRI copyTo, ValueProcessorIfc delegate) {
		return (model, subject, value, cell, language) -> {
			List<Statement> statements = delegate.processValue(model, subject, value, cell, language);
			List<Statement> newStatements = new ArrayList<>();
			if(statements != null) {
				statements.stream().forEach(v -> {
					newStatements.add(SimpleValueFactory.getInstance().createStatement(subject, copyTo, v.getObject()));
				});
				model.addAll(newStatements);		
			}
			newStatements.addAll(statements);
			return newStatements;
		};
	}

	public ValueProcessorIfc asList(ColumnHeader header, ValueProcessorIfc delegate) {
		return (model, subject, value, cell, language) -> {
			List<Statement> originalStatements = delegate.processValue(model, subject, value, cell, language);

			Model toAdd = new LinkedHashModel();

			// get all values
			Set<Value> values = originalStatements.stream().map(s -> s.getObject()).collect(Collectors.toSet());

			// aggregate in list
			Resource listHead = Values.bnode();
			RDFCollections.asRDF(values,listHead,toAdd);
			// remove all original triples
			model.removeAll(originalStatements);
			// add instead triple to the list
			toAdd.add(subject, header.getProperty(), listHead);

			model.addAll(toAdd);

			return toAdd.stream().toList();
		};
	}

	public ValueProcessorIfc wrapWithShaclLogicalOperator(ColumnHeader header, IRI logicalOperator, ValueProcessorIfc delegate) {
		return (model, subject, value, cell, language) -> {
			List<Statement> originalStatements = delegate.processValue(model, subject, value, cell, language);

			Model toRemove = new LinkedHashModel();
			Model toAdd = new LinkedHashModel();

			// get all values
			Set<Value> values = originalStatements.stream().map(s -> s.getObject()).collect(Collectors.toSet());

			// join with the boolean operator only if there is more than 1 value
			if(values.size() > 1) {
				// for each values...
				List<BNode> items = new ArrayList<>();
				for(Value v : values) {
					BNode bnode = Values.bnode();
					items.add(bnode);
					toAdd.add(
						SimpleValueFactory.getInstance().createStatement(
							bnode,
							header.getProperty(),
							v
						)
					);
				}

				// aggregate in list
				Resource listHead = Values.bnode();
				// 3rd parameter is a sink
				RDFCollections.asRDF(items,listHead,toAdd);

				toAdd.add(subject, logicalOperator, listHead);

				// remove all original triples
				toRemove.addAll(originalStatements);
			}

			// remove everything that needs to be removed
			model.removeAll(toRemove);
			model.addAll(toAdd);

			return toAdd.stream().collect(Collectors.toList());
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
				return collector.getStatements().stream().collect(Collectors.toList());
			} catch (Exception e) {
				// if anything goes wrong, default to creating a literal
				log.error("Error in parsing Turtle :\n"+turtle);
				e.printStackTrace();
				return langOrPlainLiteral(property).processValue(model, subject, value, cell, language);
			}
			
		};
	}

	public ValueProcessorIfc plainLiteral(IRI property) {
		return (model, subject, value, cell, language) -> {
			Literal literal = SimpleValueFactory.getInstance().createLiteral(value);
			Statement s = SimpleValueFactory.getInstance().createStatement(subject, property, literal);
			model.add(s);
			return Collections.singletonList(s);
		};
	}
	
	public ValueProcessorIfc langOrPlainLiteral(IRI property) {
		return (model, subject, value, cell, language) -> {
			Literal literal;
			if(language != null) {
				literal = SimpleValueFactory.getInstance().createLiteral(value, language);
			} else {
				literal = SimpleValueFactory.getInstance().createLiteral(value);
			}
			Statement s = SimpleValueFactory.getInstance().createStatement(subject, property, literal);
			model.add(s);
			return Collections.singletonList(s);
		};
	}

	public ValueProcessorIfc skosXlLabel(IRI xlLabelProperty, PrefixManager prefixManager) {
		return (model, subject, value, cell, language) -> {
			// String labelUri = ConceptSchemeFromExcel.fixUri(value);
			String labelUri = prefixManager.uri(value, true);
			IRI labelResource = SimpleValueFactory.getInstance().createIRI(labelUri);
			List<Statement> statements = new ArrayList<>();

			statements.add(SimpleValueFactory.getInstance().createStatement(labelResource, RDF.TYPE, SKOSXL.LABEL));
			statements.add(SimpleValueFactory.getInstance().createStatement(subject, xlLabelProperty, labelResource));

			model.addAll(statements);
			return statements;
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
