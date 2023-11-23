package fr.sparna.rdf.xls2rdf.app;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.MergeFilesOutputXls;

public class Merge implements CliCommandIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	public void execute(Object args) throws Exception {
		ArgumentsMerge a = (ArgumentsMerge)args;
		
		MergeFilesOutputXls merger = new MergeFilesOutputXls();
		Workbook wb = merger.mergeFile(a.getCsv(), a.getExcel());

		//Test
		try (OutputStream fileOut = new FileOutputStream(a.getOutput())) {
			wb.write(fileOut);
			wb.close();
		}		
	}
	
}
