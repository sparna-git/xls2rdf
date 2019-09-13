package fr.sparna.rdf.xls2rdf.reconcile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.rdf.xls2rdf.Xls2RdfException;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc;
import fr.sparna.rdf.xls2rdf.Xls2RdfMessageListenerIfc.MessageCode;

public class DynamicReconciliableValueSet implements ReconciliableValueSetIfc {

	private static Logger log = LoggerFactory.getLogger(DynamicReconciliableValueSet.class.getName());
	
	private transient ReconcileServiceIfc reconcileService;
	private IRI reconcileType;
	private boolean failOnNoMatch = true;
	private Xls2RdfMessageListenerIfc messageListener;
	
	private Map<String, IRI> reconciledValues;
	
	
	public DynamicReconciliableValueSet(
			ReconcileServiceIfc reconcileService,
			IRI reconcileType,
			boolean failOnNoMatch,
			Xls2RdfMessageListenerIfc messageListener
	) {
		this.reconcileService = reconcileService;
		this.failOnNoMatch = failOnNoMatch;
		this.reconcileType = reconcileType;
		this.messageListener = messageListener;
		this.reconciledValues = new HashMap<String, IRI>();
	}
	
	/* (non-Javadoc)
	 * @see fr.sparna.rdf.skos.xls2skos.reconcile.ReconciliableValueSetIfc#getReconciledValue(java.lang.String)
	 */
	@Override
	public IRI getReconciledValue(String value) {
		if(!reconciledValues.containsKey(value)) {
			Map<String, IRI> result = reconcileSingleValue(value, this.reconcileType);
			this.reconciledValues.putAll(result);
		}
		
		return this.reconciledValues.get(value);
	}
	
	private Map<String, IRI> reconcileSingleValue(String value, IRI reconcileType) {
		
		// build the queries Map
		Map<String, ReconcileQueryIfc> queries = new HashMap<String, ReconcileQueryIfc>();
		queries.put("q0", new SimpleReconcileQuery(
				value,
				(reconcileType != null)?Collections.singletonList(reconcileType.toString()):null
		));
		
		// call ReconcileService
		Map<String, ReconcileResultIfc> reconcileResults = this.reconcileService.reconcile(queries);
		
		// parse results
		Map<String, IRI> result = new HashMap<String, IRI>();
		for (Map.Entry<String, ReconcileResultIfc> anEntry : reconcileResults.entrySet()) {
			
			String initialValue = queries.get(anEntry.getKey()).getQuery();
			
			if(anEntry.getValue().getMatches() == null || anEntry.getValue().getMatches().size() == 0) {
				// no reconciliation result for this value
				String message = "Unable to reconcile value '"+ initialValue +"' on type/scheme <"+ reconcileType +">";
				if(this.failOnNoMatch) {
					throw new Xls2RdfException(message);
				} else {
					this.messageListener.onMessage(MessageCode.UNABLE_TO_RECONCILE_VALUE, "??", message);
					log.error(message);
				}
			} else {
				// pick the first one, assuming only one result for now
				String matchResult = anEntry.getValue().getMatches().get(0).getId();
				log.debug("Value '"+initialValue+"' reconciled to <"+matchResult+">");
				result.put(initialValue, SimpleValueFactory.getInstance().createIRI(anEntry.getValue().getMatches().get(0).getId()));
			}
		}
		
		return result;
	}
	
	
}
