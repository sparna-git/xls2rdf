package fr.sparna.rdf.xls2rdf.app;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;

@Parameters(commandDescription = "Merge an input CSV file into an Excel file that declares the header and columns to be converted to RDF.")
public class ArgumentsMerge {

	@Parameter(
			names = { "-e", "--excel" },
			description = "Excel file containing the header and column definitions, in which the CSV file will be merged.",
			converter = FileConverter.class,
			validateWith = FileExistsValidator.class,
			required = true
	)
	private File excel;
	
	@Parameter(
			names = { "-o", "--output" },
			description = "Output Excel file",
			converter = FileConverter.class,
			required = true
	)
	private File output;
	
	@Parameter(
			names = { "-c", "--csv" },
			description = "Input csv file",
			converter = FileConverter.class,
			validateWith = FileExistsValidator.class,
			required = false
	)
	private File csv;
	
		
	public File getCsv() {
		return csv;
	}

	public void setCsv(File csv) {
		this.csv = csv;
	}

	public File getExcel() {
		return excel;
	}

	public void setExcel(File excel) {
		this.excel = excel;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}
	
}
