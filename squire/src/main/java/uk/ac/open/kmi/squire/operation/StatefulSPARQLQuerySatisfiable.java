package uk.ac.open.kmi.squire.operation;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * A satisfiability checker that checks satisfiability for one dataset only but
 * caches results for that dataset.
 * 
 * @author alessandro
 *
 */
public class StatefulSPARQLQuerySatisfiable extends SPARQLQuerySatisfiable {

	private IRDFDataset dataset;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<Query, Boolean> satisfiables;

	public StatefulSPARQLQuerySatisfiable(IRDFDataset dataset) {
		super();
		this.satisfiables = new HashMap<>();
		this.dataset = dataset;
	}

	public StatefulSPARQLQuerySatisfiable(IRDFDataset dataset, String token) {
		this(dataset);
		this.token = token;
	}

	public IRDFDataset getDataset() {
		return dataset;
	}

	public boolean isSatisfiableWrtResults(Query q) throws ConnectException {
		if (satisfiables.containsKey(q)) {
			log.debug("Satisfiability check already cached, not executing again");
			boolean cond = satisfiables.get(q);
			log.debug("Satisfiable? {}", cond ? "YES" : "NO");
			notifyQuerySatisfiableValue(q, cond);
			return cond;
		}
		boolean b = super.isSatisfiableWrtResults(q, dataset);
		this.satisfiables.put(q, b);
		return b;
	}

}
