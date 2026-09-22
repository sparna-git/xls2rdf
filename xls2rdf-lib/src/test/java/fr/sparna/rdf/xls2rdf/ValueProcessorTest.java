package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.sparna.rdf.xls2rdf.listen.LogXls2RdfMessageListener;
import fr.sparna.rdf.xls2rdf.mapping.MappingRuleParser;
import fr.sparna.rdf.xls2rdf.processor.ValueProcessorFactory;
import junit.framework.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ValueProcessorTest {

	private SimpleValueFactory vf = SimpleValueFactory.getInstance();
	private Resource subject;
	private IRI workingGraph;

	private MappingRuleParser parser;
	private PrefixManager prefixManager;
	
	private ValueProcessorFactory factory;
	
	private Repository repository;
	private RepositoryConnection connection;
	
	
	@Before
	public void before() {
		this.subject = vf.createIRI("http://sparna.fr");
		this.workingGraph = vf.createIRI("http://graph");
		
		this.repository = new SailRepository(new MemoryStore());
		this.connection = repository.getConnection();
		
		this.prefixManager = new PrefixManager();
		this.prefixManager.register("skos", SKOS.NAMESPACE);
		this.prefixManager.register("xsd", XMLSchema.NAMESPACE);
		parser = new MappingRuleParser(this.prefixManager);
		
		factory = new ValueProcessorFactory(new LogXls2RdfMessageListener());
	}
	
	@After
	public void after() throws Exception {
		if (connection != null) {
			connection.close();
		}
		if (repository != null) {
			repository.shutDown();
		}
	}
	
	private boolean hasStatement(Resource s, IRI p, org.eclipse.rdf4j.model.Value o, IRI context) {
		return connection.hasStatement(s, p, o, false, context);
	}
	
	@Test
	public void plainLiteralTest() {
		RepositoryValueProcessorIfc vg = factory.plainLiteral(SKOS.NOTATION);		
		vg.init(connection, workingGraph);
		vg.processValue(subject, "1", null);
		
		Assert.assertTrue(hasStatement(subject, SKOS.NOTATION, vf.createLiteral("1"), workingGraph));
	}
	
	@Test
	public void langOrPlainLiteralTest() {
		RepositoryValueProcessorIfc vg = factory.langOrPlainLiteral(SKOS.PREF_LABEL, "fr");		
		vg.init(connection, workingGraph);
		vg.processValue(subject, "sparna", null);
		vg.processValue(subject, "SPARNA", null);
		Assert.assertTrue(hasStatement(subject, SKOS.PREF_LABEL, vf.createLiteral("sparna", "fr"), workingGraph));
		Assert.assertTrue(hasStatement(subject, SKOS.PREF_LABEL, vf.createLiteral("SPARNA", "fr"), workingGraph));
	}
	
	@Test
	public void resourceOrLiteralTest() {
		RepositoryValueProcessorIfc vg = factory.resourceOrLiteral(this.parser.parse("skos:prefLabel^^xsd:string"), prefixManager);		
		vg.init(connection, workingGraph);
		vg.processValue(subject, "sparna", null);
		Assert.assertTrue(hasStatement(subject, SKOS.PREF_LABEL, vf.createLiteral("sparna", XMLSchema.STRING), workingGraph));
	}
	
	
	@Test
	public void splitLangLiteralTest() {
		RepositoryValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:altLabel@fr"), prefixManager),
				","
		);
		vg.init(connection, workingGraph);
		vg.processValue(subject, "sparna, SPARNA", null);
		Assert.assertTrue(hasStatement(subject, SKOS.ALT_LABEL, vf.createLiteral("sparna", "fr"), workingGraph));
		Assert.assertTrue(hasStatement(subject, SKOS.ALT_LABEL, vf.createLiteral("SPARNA", "fr"), workingGraph));
	}
	
	@Test
	public void splitDatatypeLiteralTest() {
		RepositoryValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:altLabel^^xsd:string"), prefixManager),
				","
		);
		vg.init(connection, workingGraph);
		vg.processValue(subject, "sparna, SPARNA", null);
		Assert.assertTrue(hasStatement(subject, SKOS.ALT_LABEL, vf.createLiteral("sparna", XMLSchema.STRING), workingGraph));
		Assert.assertTrue(hasStatement(subject, SKOS.ALT_LABEL, vf.createLiteral("SPARNA", XMLSchema.STRING), workingGraph));
	}
	
	@Test
	public void splitFullUriTest() {
		RepositoryValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:exactMatch"), prefixManager),
				","
		);
		vg.init(connection, workingGraph);
		vg.processValue(subject, "http://blog.sparna.fr, http://SPARNA.fr", null);
		Assert.assertTrue(hasStatement(subject, SKOS.EXACT_MATCH, vf.createIRI("http://blog.sparna.fr"), workingGraph));
		Assert.assertTrue(hasStatement(subject, SKOS.EXACT_MATCH, vf.createIRI("http://SPARNA.fr"), workingGraph));
	}
	
	@Test
	public void splitPrefixedUriTest() {
		RepositoryValueProcessorIfc vg = factory.split(
				factory.resourceOrLiteral(this.parser.parse("skos:exactMatch"), prefixManager),
				","
		);
		vg.init(connection, workingGraph);
		vg.processValue(subject, "skos:notation, skos:prefLabel", null);
		Assert.assertTrue(hasStatement(subject, SKOS.EXACT_MATCH, SKOS.PREF_LABEL, workingGraph));
		Assert.assertTrue(hasStatement(subject, SKOS.EXACT_MATCH, SKOS.NOTATION, workingGraph));
	}
}
