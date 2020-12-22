package fr.sparna.rdf.xls2rdf.rest;

import fr.sparna.rdf.xls2rdf.ModelWriterFactory;
import fr.sparna.rdf.xls2rdf.ModelWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverter;
import fr.sparna.rdf.xls2rdf.Xls2RdfConverterFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Service
public class Xls2RdfService {

	public List<String> convert(
					InputStream input,
					OutputStream output,
					String lang,
					RDFFormat format,
					boolean skosxl,
					boolean broaderTransitive,
					boolean ignorePostProc
	) {
		ModelWriterFactory factory = new ModelWriterFactory(false, format, false);
		
		ModelWriterIfc modelWriter = factory.buildNewModelWriter(output);
		
		Xls2RdfConverterFactory converterFactory = new Xls2RdfConverterFactory(!ignorePostProc, skosxl, skosxl, broaderTransitive);
		
		Xls2RdfConverter converter = converterFactory.newConverter(modelWriter, lang);
		
		converter.processInputStream(input);
		return converter.getConvertedVocabularyIdentifiers();
	}
	
	
}
