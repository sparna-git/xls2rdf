package fr.sparna.rdf.xls2rdf.app;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;


public class ArgumentsConvert {

	@Parameter(
			names = { "-i", "--input" },
			description = "Input Excel file",
			converter = FileConverter.class,
			required = true
	)
	private File input;
	
	@Parameter(
			names = { "-o", "--output" },
			description = "Output RDF or ZIP file name",
			converter = FileConverter.class,
			required = true
	)
	private File output;
	
	@Parameter(
			names = { "-d", "--directory" },
			description = "Consider output like a directory, and generate separate files in it for each graph."
	)
	private boolean outputAsDirectory = false;
	
	@Parameter(
			names = { "-l", "--lang" },
			description = "Default language to use for literal columns when no language is specified",
			required = false
	)
	private String lang;
	
	@Parameter(
			names = { "-f", "--format" },
			description = "Output RDF format mime type."
	)
	private String rdfFormat = null;
	
	@Parameter(
			names = { "-xl", "--skosxl" },
			description = "XLify labels"
	)
	private boolean xlify = false;

	@Parameter(
			names = { "-bt", "--broaderTransitive" },
			description = "Generate skos:broaderTransitive"
	)
	private boolean broaderTransitiveify = false;

	@Parameter(
			names = { "-def", "--definitions" },
			description = "XLify definitions"
	)
	private boolean xlifyDefinitions = false;

	@Parameter(
			names = { "-g", "--generateGraphs" },
			description = "Generate Virtuoso graph files"
	)
	private boolean generateGraphFiles = false;
	
	@Parameter(
			names = { "-np", "--noPostProcessings" },
			description = "Ignore post processings on sheet data"
	)
	private boolean noPostProcessings = false;
	
	@Parameter(
			names = { "-xd", "--externalData" },
			description = "External support data for reconcile",
			converter = FileConverter.class,
			required = false
	)
	private File externalData;
	
	@Parameter(
			names = { "-nrf", "--noReconcileFail" },
			description = "Does not fait if a value to be reconciled cannot be found in the reconciled data",
			required = false
	)
	private boolean noReconcileFail = false;

	@Parameter(
			names = { "-p", "--pretty" },
			description = "Prettify output by grouping statements. Default is false.",
			required = false
	)
	private boolean pretty = false;
	
	public File getInput() {
		return input;
	}

	public void setInput(File input) {
		this.input = input;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getRdfFormat() {
		return rdfFormat;
	}

	public void setRdfFormat(String rdfFormat) {
		this.rdfFormat = rdfFormat;
	}

	public boolean isXlify() {
		return xlify;
	}

	public void setXlify(boolean xlify) {
		this.xlify = xlify;
	}

	public boolean isXlifyDefinitions() {
		return xlifyDefinitions;
	}

	public void setXlifyDefinitions(boolean xlifyDefinitions) {
		this.xlifyDefinitions = xlifyDefinitions;
	}

	public boolean isBroaderTransitiveify() {
		return broaderTransitiveify;
	}

	public void setBroaderTransitiveify(boolean broaderTransitiveify) {
		this.broaderTransitiveify = broaderTransitiveify;
	}

	public boolean isGenerateGraphFiles() {
		return generateGraphFiles;
	}

	public void setGenerateGraphFiles(boolean generateGraphFiles) {
		this.generateGraphFiles = generateGraphFiles;
	}

	public boolean isOutputAsDirectory() {
		return outputAsDirectory;
	}

	public void setOutputAsDirectory(boolean outputAsDirectory) {
		this.outputAsDirectory = outputAsDirectory;
	}

	public boolean isNoPostProcessings() {
		return noPostProcessings;
	}

	public void setNoPostProcessings(boolean noPostProcessings) {
		this.noPostProcessings = noPostProcessings;
	}

	public File getExternalData() {
		return externalData;
	}

	public void setExternalData(File externalData) {
		this.externalData = externalData;
	}

	public boolean isPretty() {
		return pretty;
	}

	public void setPretty(boolean pretty) {
		this.pretty = pretty;
	}

	public boolean isNoReconcileFail() {
		return noReconcileFail;
	}

	public void setNoReconcileFail(boolean noReconcileFail) {
		this.noReconcileFail = noReconcileFail;
	}
	
}
