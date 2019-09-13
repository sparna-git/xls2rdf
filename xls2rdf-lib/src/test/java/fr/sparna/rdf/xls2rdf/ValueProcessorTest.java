package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Test;

import fr.sparna.rdf.xls2rdf.ColumnHeaderParser;
import fr.sparna.rdf.xls2rdf.PrefixManager;
import fr.sparna.rdf.xls2rdf.ValueProcessorFactory;
import fr.sparna.rdf.xls2rdf.ValueProcessorIfc;
import junit.framework.Assert;

public class ValueProcessorTest {

	private SimpleValueFactory vf = SimpleValueFactory.getInstance();
	private Resource subject;
	private Model model;

	private ColumnHeaderParser parser;
	private PrefixManager prefixManager;
	
	private ValueProcessorFactory factory;
	
	
	@Before
	public void before() {
		this.subject = vf.createIRI("http://sparna.fr");
		this.model = new LinkedHashModelFactory().createEmptyModel();
		
		this.prefixManager = new PrefixManager();
		this.prefixManager.register("skos", SKOS.NAMESPACE);
		this.prefixManager.register("xsd", XMLSchema.NAMESPACE);
		parser = new ColumnHeaderParser(this.prefixManager);
		
		factory = new ValueProcessorFactory(new LogXls2RdfMessageListener());
	}
	
	@Test
	public void plainLiteralTest() {
		ValueProcessorIfc vg = factory.plainLiteral(SKOS.NOTATION);		
		vg.processValue(model, subject, "1", null, null);		
		Assert.assertTrue(model.contains(subject, SKOS.NOTATION, vf.createLiteral("1")));
	}
	
	@Test
	public void langOrPlainLiteralTest() {
		ValueProcessorIfc vg = factory.langOrPlainLiteral(SKOS.PREF_LABEL);		
		vg.processValue(model, subject, "sparna", null, null);
		vg.processValue(model, subject, "SPARNA", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.PREF_LABEL, vf.createLiteral("sparna")));
		Assert.assertTrue(model.contains(subject, SKOS.PREF_LABEL, vf.createLiteral("SPARNA", "fr")));
	}
	
	@Test
	public void resourceOrLiteralTest() {
		ValueProcessorIfc vg = factory.resourceOrLiteral(this.parser.parse("skos:prefLabel^^xsd:string", null), prefixManager);		
		vg.processValue(model, subject, "sparna", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.PREF_LABEL, vf.createLiteral("sparna", XMLSchema.STRING)));
	}
	
	@Test
	public void overwriteLangTest() {
		ValueProcessorIfc vg = factory.resourceOrLiteral(this.parser.parse("skos:prefLabel@en", null), prefixManager);		
		vg.processValue(model, subject, "sparna", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.PREF_LABEL, vf.createLiteral("sparna", "fr")));
	}
	
	@Test
	public void splitLangLiteralTest() {
		ValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:altLabel", null), prefixManager),
				","
		);
		vg.processValue(model, subject, "sparna, SPARNA", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.ALT_LABEL, vf.createLiteral("sparna", "fr")));
		Assert.assertTrue(model.contains(subject, SKOS.ALT_LABEL, vf.createLiteral("SPARNA", "fr")));
	}
	
	@Test
	public void splitDatatypeLiteralTest() {
		ValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:altLabel^^xsd:string", null), prefixManager),
				","
		);
		vg.processValue(model, subject, "sparna, SPARNA", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.ALT_LABEL, vf.createLiteral("sparna", XMLSchema.STRING)));
		Assert.assertTrue(model.contains(subject, SKOS.ALT_LABEL, vf.createLiteral("SPARNA", XMLSchema.STRING)));
	}
	
	@Test
	public void splitFullUriTest() {
		ValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:exactMatch", null), prefixManager),
				","
		);
		vg.processValue(model, subject, "http://blog.sparna.fr, http://SPARNA.fr", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.EXACT_MATCH, vf.createIRI("http://blog.sparna.fr")));
		Assert.assertTrue(model.contains(subject, SKOS.EXACT_MATCH, vf.createIRI("http://SPARNA.fr")));
	}
	
	@Test
	public void splitPrefixedUriTest() {
		ValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:exactMatch", null), prefixManager),
				","
		);
		vg.processValue(model, subject, "skos:notation, skos:prefLabel", null, "fr");
		Assert.assertTrue(model.contains(subject, SKOS.EXACT_MATCH, SKOS.PREF_LABEL));
		Assert.assertTrue(model.contains(subject, SKOS.EXACT_MATCH, SKOS.NOTATION));
	}
}
