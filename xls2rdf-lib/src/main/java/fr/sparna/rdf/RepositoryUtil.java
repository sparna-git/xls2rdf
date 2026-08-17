package fr.sparna.rdf;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class RepositoryUtil {

    public static void mergeRepositories(Repository sourceRepo, Repository targetRepo) {
    	try (RepositoryConnection sourceConn = sourceRepo.getConnection();
			RepositoryConnection targetConn = targetRepo.getConnection()) {

			// Copy all statements (including default and named graphs)
			targetConn.add(sourceConn.getStatements(null, null, null, true));

			// Copy all namespace prefixes
			for (Namespace ns : sourceConn.getNamespaces()) {
				targetConn.setNamespace(ns.getPrefix(), ns.getName());
			}
		}
	}
    
}
