package fr.sparna.rdf.xls2rdf.app;

import fr.sparna.rdf.xls2rdf.Xls2RdfConverterBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Convert implements CliCommandIfc {

	private final Logger log = LoggerFactory.getLogger(Convert.class.getName());

	@Override
	public void execute(Object args) throws Exception {

		//Cast args to ArgumentsConvert
		ArgumentsConvert arg = (ArgumentsConvert)args;

		Xls2RdfConverterBuilder builder = Xls2RdfConverterBuilder.getInstance()
						.withLanguage(arg.getLang())
						.withApplyPostProcessing(arg.isNoPostProcessings())
						.withGenerateXl(arg.isXlify())
						.withGenerateXlDefinitions(arg.isXlifyDefinitions())
						.withFailOnReconcile(arg.isNoReconcileFail())
						.withGenerateBroaderTransitive(arg.isBroaderTransitiveify())
						.withSkipHidden(arg.isSkipHidden());

		//if the option -i and -o and -w are present
		if(arg.isWatch() && arg.getOutput() != null && arg.getInput() != null){
			//Run the thread parsing
			builder
					.withFormat(arg.getRdfFormat())
					.withModelWriterFactory(false, arg.isGenerateGraphFiles(), arg.isPretty());
			DirectoryWatcher watcher = new DirectoryWatcher(arg.getInput(), arg.getOutput(), builder);
			watcher.runWatchService();
		}

		//verify is -i and -o are present to run conversion process
		else if(arg.getInput() != null && arg.getOutput() != null){
			if(!arg.getInput().exists()) {
				log.error("Given input file {} does not exist.", arg.getInput().getAbsolutePath());
				return;
			}
			// if user asked for graph files, but without outputting in a directory or in a zip, this is an error
			if(arg.isGenerateGraphFiles() && !(arg.getOutput().getName().endsWith("zip") || arg.isOutputAsDirectory())) {
				log.error("If you need to generate graph files please use the option to output in a directory, or provide an output file with .zip extension.");
				return;
			}

			builder.withFormat(() -> {
						if(arg.getRdfFormat() != null) return RDFWriterRegistry.getInstance().getFileFormatForMIMEType(arg.getRdfFormat()).orElse(RDFFormat.TURTLE);
						else return RDFWriterRegistry.getInstance().getFileFormatForFileName(arg.getOutput().getName()).orElse(RDFFormat.TURTLE);
					})
					.withModelWriterFactory(arg.getOutput().getName().endsWith("zip"), arg.isGenerateGraphFiles(), arg.isPretty())
					.withModelWriterIfc(arg.getOutput(), arg.isOutputAsDirectory())
					.withSupportRepository(arg.getExternalData());

			//Is input is a directory, look for all xls files to process
			if(!arg.getInput().isFile()){
				// sort files to guarantee alphabetical processing
				List<File> files = new ArrayList<>();
				Collections.addAll(files, arg.getInput().listFiles(this::fileFilter));
				files.sort((Comparator.comparing(File::getName)));
				// process each file, and add resulting data in supportRepository
				for (File f : files) {
						try(RepositoryConnection connection = builder.getSupportRepository().getConnection()) {
							List<Model> result = builder.buildConverter().processFile(f);
							for (Model m : result) {
								connection.add(m);
							}
					}
				}
			}
			//if it's file just process for the input file
			else {
				try(InputStream in = new FileInputStream(arg.getInput());){
					log.debug("Will use ModelWriter : {}", builder.getModelWriter().getClass().getName());
					builder.buildConverter().processInputStream(in);
				}
			}
		}
	}

	private boolean fileFilter(File file){
		String p = file.toPath().toString();
		//si besoin rajout d'extensions
		if(p.endsWith(".xls") || p.endsWith(".xlsx") || p.endsWith(".xlsm")) return true;
		else if(p.endsWith(".ods")) return true;
		else if(p.endsWith(".csv")) return true;
		else return false;
	}

}
