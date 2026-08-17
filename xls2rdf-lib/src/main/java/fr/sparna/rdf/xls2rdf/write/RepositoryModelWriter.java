package fr.sparna.rdf.xls2rdf.write;

import fr.sparna.rdf.xls2rdf.RepositoryWriterIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.util.RDFInserter;


/**
 * @author Thomas Francart
 *
 */
public class RepositoryModelWriter implements RepositoryWriterIfc {
	
	private Repository outputRepository;
	
	public RepositoryModelWriter(Repository outputRepository) {
		super();
		this.outputRepository = outputRepository;
	}

	@Override
	public void saveRepository(Repository repository, String baseIri) {
		try {
			try(RepositoryConnection target = this.outputRepository.getConnection()) {
				try(RepositoryConnection source = repository.getConnection()) {
					// register the prefixes
					source.getNamespaces().forEach(rr -> target.setNamespace(rr.getPrefix(), rr.getName()));
					// dump triples
					RDFInserter inserter = new RDFInserter(target);
					target.begin();
    				source.export(inserter);   // exports ALL statements from ALL contexts
    				target.commit();
				}
			}
		} catch(Exception e) {
			throw Xls2RdfException.rethrow(e);
		}
	}

	@Override
	public void beginWorkbook() {
		// nothing
	}

	@Override
	public void endWorkbook() {
		// nothing
	}

}
