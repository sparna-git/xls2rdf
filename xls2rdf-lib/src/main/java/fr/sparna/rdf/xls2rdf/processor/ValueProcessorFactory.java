package fr.sparna.rdf.xls2rdf.processor;

import fr.sparna.rdf.xls2rdf.*;
import fr.sparna.rdf.xls2rdf.listen.LogXls2RdfMessageListener;
import fr.sparna.rdf.xls2rdf.mapping.MappingRule;
import fr.sparna.rdf.xls2rdf.processor.manchester.ManchesterClassExpressionParserProcessor;
import fr.sparna.rdf.xls2rdf.reconcile.ReconciliableValueSetIfc;
import fr.sparna.rdf.xls2rdf.sheet.Sheet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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

	public static Pair<List<Statement>, List<Statement>> toPair(List<Statement> left) {
		return toPair(left, new ArrayList<Statement>());
	}

	public static Pair<List<Statement>, List<Statement>> toPair(List<Statement> left, List<Statement> right) {
		return new ImmutablePair<List<Statement>,List<Statement>>(left, right);
	}

	public RepositoryValueProcessorIfc split(RepositoryValueProcessorIfc delegate, String separator) {
		return new SplitValueProcessor(delegate, separator);
	}
	
	public RepositoryValueProcessorIfc resource(IRI property, PrefixManager prefixManager) {
		return new ResourceValueProcessor(property, prefixManager);
	}
	
	public RepositoryValueProcessorIfc lookup(MappingRule mappingRule, Sheet sheet, int lookupColumn, int uriColumn, PrefixManager prefixManager) {
		return new LookupValueProcessor(this, mappingRule, sheet, lookupColumn, uriColumn, prefixManager, messageListener);
	}
	
	public RepositoryValueProcessorIfc reconcile(MappingRule mappingRule, PrefixManager prefixManager, ReconciliableValueSetIfc reconciledValues) {
		return new ReconcileValueProcessor(this, mappingRule, prefixManager, reconciledValues, messageListener);
	}

	@Deprecated
	public RepositoryValueProcessorIfc reconcileLocal(MappingRule mappingRule, PrefixManager prefixManager, IRI reconcileOn, Repository supportRepository) {
		return new ReconcileLocalValueProcessor(this, mappingRule, prefixManager, reconcileOn, supportRepository, messageListener);
	}
	
	public RepositoryValueProcessorIfc ignoreIfParenthesis(RepositoryValueProcessorIfc delegate) {
		return new IgnoreIfParenthesisValueProcessor(delegate);
	}

	public RepositoryValueProcessorIfc copyTo(IRI copyTo, RepositoryValueProcessorIfc delegate) {
		return new CopyToValueProcessor(copyTo, delegate);
	}

	public RepositoryValueProcessorIfc asList(MappingRule mappingRule, RepositoryValueProcessorIfc delegate) {
		return new AsListValueProcessor(mappingRule, delegate);
	}

	public RepositoryValueProcessorIfc wrapWithShaclLogicalOperator(MappingRule mappingRule, IRI logicalOperator, RepositoryValueProcessorIfc delegate) {
		return new WrapWithShaclLogicalOperatorValueProcessor(mappingRule, logicalOperator, delegate);
	}
	
	
	public RepositoryValueProcessorIfc resourceOrLiteral(MappingRule mappingRule, PrefixManager prefixManager) {
		ResourceOrLiteralValueProcessor g = new ResourceOrLiteralValueProcessor(this, mappingRule, prefixManager, messageListener);
		return g;
	}

	public RepositoryValueProcessorIfc turtleParsing(MappingRule mappingRule, IRI property, PrefixManager prefixManager) {
		return new TurtleParsingValueProcessor(this, mappingRule, property, prefixManager, messageListener);
	}

	public RepositoryValueProcessorIfc plainLiteral(IRI property) {
		return new PlainLiteralValueProcessor(property);
	}
	
	public RepositoryValueProcessorIfc langOrPlainLiteral(IRI property, String language) {
		return new LangOrPlainLiteralValueProcessor(property, language);
	}

	public RepositoryValueProcessorIfc skosXlLabel(IRI xlLabelProperty, PrefixManager prefixManager) {
		return new SkosXlLabelValueProcessor(xlLabelProperty, prefixManager);
	}

	public RepositoryValueProcessorIfc manchesterClassExpressionParser(MappingRule mappingRule, PrefixManager prefixManager) {
		ManchesterClassExpressionParserProcessor p = new ManchesterClassExpressionParserProcessor(mappingRule, prefixManager, messageListener);
		return p;
	}
	
	public static String normalizeSpace(String s) {
		return s.replaceAll("\\h+"," ").trim();
		// return s.replaceAll("(^\\h*)|(\\h*$)", " ").trim();
	}
	
}
