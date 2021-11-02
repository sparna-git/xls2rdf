package fr.sparna.rdf.xls2rdf.reconcile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.query.AbstractTupleQueryResultHandler;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlReconcileService implements ReconcileServiceIfc {

	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	private Repository repository;

	public SparqlReconcileService(Repository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Map<String, ReconcileResultIfc> reconcile(Map<String, ReconcileQueryIfc> queries) {
		Map<String, ReconcileResultIfc> result = new HashMap<String, ReconcileResultIfc>();
		
		try(RepositoryConnection c = this.repository.getConnection()) {
			for (Map.Entry<String, ReconcileQueryIfc> anEntry : queries.entrySet()) {
				ReconcileQueryIfc aQuery = anEntry.getValue();
				
				String sparql = ""
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+"\n"
						+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+"\n"
						+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+"\n"
						+ "PREFIX dct: <http://purl.org/dc/terms/> "+"\n"
						+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "+"\n"
						+ "PREFIX schema: <http://schema.org/> "+"\n"
						+ "SELECT ?x WHERE { "+"\n"
						+ " ?x rdfs:label|skos:prefLabel|skos:altLabel|skos:notation|foaf:name|dct:title|dc:title|dct:identifier|dc:identifier|schema:name ?literal ."+"\n"
						+ " FILTER(LCASE(STR(?literal)) = LCASE(\"%s\") )"+"\n"
						+ " %s "+"\n"
						+ "}"
						;
				
				String finalSparql = String.format(
						sparql,
						aQuery.getQuery(), 
						((aQuery.getTypes() != null && aQuery.getTypes().size() > 0)?" ?x rdf:type|skos:inScheme <"+aQuery.getTypes().get(0)+"> .":"")
				);
				
				log.trace("Executing reconcile SPARQL : "+finalSparql);
				// System.out.println("Executing reconcile SPARQL : "+finalSparql);
				TupleQuery query = c.prepareTupleQuery(finalSparql);
				query.evaluate(new AbstractTupleQueryResultHandler() {
					
					@Override
					public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
						// always put an empty result for every query
						// that can be overwritten if there is a match
						// there must be one result in the output for every sent query
						result.put(
								anEntry.getKey(),
								new SimpleReconcileResult()
						);
					}

					@Override
					public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
						result.put(
								anEntry.getKey(),
								new SimpleReconcileResult(
										bindingSet.getBinding("x").getValue().toString(),
										null,
										null
								)
						);
					}
					
				});
				

			}
		}
		

		return result;	
	}
	
	
	
}
