package fr.sparna.rdf.xls2rdf;

import org.eclipse.rdf4j.repository.Repository;

public interface RepositoryWriterIfc {

	public void beginWorkbook();
	
	public void saveRepository(Repository repository, String baseIri);

	public void endWorkbook();	
	

}