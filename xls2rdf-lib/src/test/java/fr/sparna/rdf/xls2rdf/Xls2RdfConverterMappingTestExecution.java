package fr.sparna.rdf.xls2rdf;

import fr.sparna.rdf.xls2rdf.mapping.WorkbookMapping;
import fr.sparna.rdf.xls2rdf.mapping.WorkbookMappingFactory;
import fr.sparna.rdf.xls2rdf.mapping.YamlParser;
import fr.sparna.rdf.xls2rdf.sheet.Workbook;
import fr.sparna.rdf.xls2rdf.sheet.WorkbookFactory;
import fr.sparna.rdf.xls2rdf.sheet.WorkbookPrinter;
import fr.sparna.rdf.xls2rdf.write.RepositoryModelWriter;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RepositoryUtil;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.trig.TriGWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Don't rename this class otherwise it could be picked up by Maven plugin to execute test.
 * @author thomas
 *
 */
public class Xls2RdfConverterMappingTestExecution implements Test {

	protected File testFolder;
	protected Xls2RdfConverter converter;
	private Repository outputRepository;
	
	public Xls2RdfConverterMappingTestExecution(File testFolder) {
		super();
		this.testFolder = testFolder;
		this.outputRepository = new SailRepository(new MemoryStore());
		this.outputRepository.init();
		
		this.converter = new Xls2RdfConverter(new RepositoryModelWriter(outputRepository));
	}

	@Override
	public int countTestCases() {
		return 1;
	}

	@Override
	public void run(TestResult result) {
		result.startTest(this);
		File input = new File(this.testFolder, "input.xls");
		if(!input.exists()) {
			input = new File(this.testFolder, "input.xlsx");
		}
		if(!input.exists()) {
			input = new File(this.testFolder, "input.xlsm");
		}
		if(!input.exists()) {
			input = new File(this.testFolder, "input.ods");
		}
		if(!input.exists()) {
			input = new File(this.testFolder, "input.csv");
		}
		
		final File expected = new File(this.testFolder, "expected.ttl");
		System.out.println("Testing " + input.getAbsolutePath());

		final File mapping = new File(this.testFolder, "mapping.yaml");

		// build mapping object
		
		WorkbookMapping workbookMapping;
		try {
			WorkbookMappingFactory factory = new WorkbookMappingFactory();
			workbookMapping = factory.buildFromYamlParser(YamlParser.getInstance(new FileInputStream(mapping)));
		} catch (FileNotFoundException e) {
			result.addError(this, e);
			throw new IllegalArgumentException("Problem reading mapping.yaml file in "+this.testFolder.getName(), e);
		}

		try {
			Workbook wb = WorkbookFactory.createWorkbook(input);
			System.out.println("Converting workbook:\n"+WorkbookPrinter.print(wb));		
			// convert
			this.converter.processWorkbook(wb, workbookMapping);
		} catch (Exception e) {
			result.addError(this, e);
			throw new IllegalArgumentException("Cannot init workbook", e);
		}
		
		// reput everything in flat repositories for proper comparisons without the graphs
		Repository outputRepositoryToCompare = new SailRepository(new MemoryStore());
		outputRepositoryToCompare.init();
		
		Model outputModel = new LinkedHashModelFactory().createEmptyModel();
		try(RepositoryConnection connection = outputRepository.getConnection()) {
			// print result in ttl (notes: prints all graphs)
			connection.export(new TriGWriter(System.out));
			connection.export(new StatementCollector(outputModel));
			
			try {
				final File output = new File(this.testFolder, "output.trig");
				if(!output.exists()) {
					output.createNewFile();
				}
				FileOutputStream out = new FileOutputStream(output);
				connection.export(new TriGWriter(out));
			} catch (Exception e) {
				result.addError(this, e);
			}
			
			try(RepositoryConnection connectionToCompare = outputRepositoryToCompare.getConnection()) {
				connectionToCompare.add(outputModel, (Resource)null);
				try {
					final File output = new File(this.testFolder, "output.ttl");
					if(!output.exists()) {
						output.createNewFile();
					}
					FileOutputStream out = new FileOutputStream(output);
					connection.export(new TurtleWriter(out));
				} catch (Exception e) {
					result.addError(this, e);
				}
			}
		}
		
		if(expected.exists()) {
			Repository expectedRepository = new SailRepository(new MemoryStore());
			expectedRepository.init();
			try(RepositoryConnection expectedConnection = expectedRepository.getConnection()) {
				expectedConnection.add(Rio.parse(new FileInputStream(expected), expected.toURI().toURL().toString(), RDFFormat.TURTLE));
			} catch (Exception e) {
				result.addError(this, e);
				throw new IllegalArgumentException("Problem with expected.ttl in unit test "+this.testFolder.getName(), e);
			}
			
			// test if isomorphic				
			if(!RepositoryUtil.equals(outputRepositoryToCompare, expectedRepository)) {
				result.addFailure(this, new AssertionFailedError("Test failed on "+this.testFolder+":"
						+ "\nStatements in output not in expected:\n"+prettyPrint(RepositoryUtil.difference(outputRepositoryToCompare, expectedRepository))
						+ "\nStatements in expected missing in output:\n"+prettyPrint(RepositoryUtil.difference(expectedRepository, outputRepositoryToCompare))
				));
			}
		} 
		
		result.endTest(this);
	}

	@Override
	public String toString() {
		return testFolder.getName();
	}
	
	private static String prettyPrint(Collection<? extends Statement> statements) {
		StringBuffer sb = new StringBuffer();
		
		for (Iterator iterator = statements.iterator(); iterator.hasNext();) {
			Statement statement = (Statement) iterator.next();
			sb.append(statement.toString()+"\n");
		}
		
		return sb.toString();
	}

}
