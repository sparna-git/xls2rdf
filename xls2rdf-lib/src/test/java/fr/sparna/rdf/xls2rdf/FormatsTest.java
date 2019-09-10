package fr.sparna.rdf.xls2rdf;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.sparna.rdf.xls2rdf.ColumnHeader;
import fr.sparna.rdf.xls2rdf.ColumnHeaderParser;
import fr.sparna.rdf.xls2rdf.PrefixManager;

public class FormatsTest {

	protected Xls2RdfConverter converter;
	private Repository outputRepository;
	private ListXls2RdfMessageListener messageListener;
	
	@Before
	public void before() {
		this.outputRepository = new SailRepository(new MemoryStore());
		this.outputRepository.init();
		
		this.converter = new Xls2RdfConverter(new RepositoryModelWriter(outputRepository), "fr");
		this.converter.setPostProcessors(Collections.singletonList(new SkosPostProcessor()));
		this.messageListener = new ListXls2RdfMessageListener();
		this.converter.setMessageListener(messageListener);
	}
	
	@Test
	public void test_formatsTest() {
		File testFolder = new File("src/test/resources/suite/_30_formatsTest");
		File input = new File(testFolder, "input.xls");
		if(!input.exists()) {
			input = new File(testFolder, "input.xlsx");
		}
		if(!input.exists()) {
			input = new File(testFolder, "input.xlsm");
		}
		
		// convert
		this.converter.processFile(input);
		
		System.out.println(this.messageListener.getMessages());
	}
	

}
