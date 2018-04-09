/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.nio.channels.ClosedByInterruptException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.operation.SPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.RDFDatasetSimilarity;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendatorForm4 extends AbstractQueryRecommendationObservable
		implements IQueryRecommendationObserver, Runnable {

	private Set<QueryRecommendationListener> listeners = new HashSet<>();

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final float queryRootDistanceDegree, querySpecificityDistanceDegree, resultSizeSimilarityDegree,
			resultTypeSimilarityDegree;

	private final String queryString;

	private final IRDFDataset rdfd1, rdfd2;

	public QueryRecommendatorForm4(String qString, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree,
			String token) {
		this.token = token;
		this.queryString = qString;
		this.rdfd1 = d1;
		this.rdfd2 = d2;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
	}

	public void addListener(QueryRecommendationListener listener) {
		listeners.add(listener);
	}

	public boolean removeListener(QueryRecommendationListener listener) {
		return listeners.remove(listener);
	}

	@Override
	public void run() {
		try {
			recommendWithToken(this.token);
		} catch (Exception ex) {
			if (ex instanceof ClosedByInterruptException)
				log.warn(" A task with token {} was interrupted. This may have been requested by a client.", token);
			else log.error("Caught exception of type " + ex.getClass().getName() + " : " + ex.getMessage()
					+ " - doing nothing with it.", ex);
		}
	}

	@Override
	public void updateDatasetSimilarity(float simScore, String token) {
		notifyDatatsetSimilarity(simScore);
	}

	@Override
	public void updateQueryRecommendated(Query qR, float score, String token) {
		fireQueryRecommended(qR, score);
		notifyQueryRecommendation(qR, score);
	}

	@Override
	public void updateQueryRecommendationCompletion(Boolean finished, String token) {
		notifyQueryRecommendationCompletion(finished);

	}

	@Override
	public void updateSatisfiableValue(Query query, boolean value, String token) {
		fireSatisfiabilityChecked(query, value);
	}

	private List<QueryStringScorePair> recommendWithToken(String token) {
		Query query;
		log.info("Started recommendation process.");
		log.debug(" (token={})", token);
		try {
			query = QueryFactory.create(queryString);
			log.info(" === Original query follows === ");
			log.info("{}", query);
			log.info(" ============================== ");
		} catch (QueryParseException ex) { // QueryParseException
			throw new QueryParseException("Failed to parse source query as SPARQL.", -1, -1);
		}

		// Phase 1 : check query satisfiability
		SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable(this.token);
		qs.register(this);
		boolean satisfiable = false;
		log.info("Checking satisfiability against source dataset <{}>", rdfd1.getEndPointURL());
		satisfiable = qs.isSatisfiableWrtResults(query, rdfd1);
		log.info(" ... is satisfiable? {}", satisfiable);
		fireSatisfiabilityChecked(query, satisfiable);
		notifyQuerySatisfiableValue(query, satisfiable); // FIXME legacy
		// Phase 2 : check dataset similarity
		if (satisfiable) try {
			// Phase 3 : recommend
			QueryRecommendator4 qR = new QueryRecommendator4(query, rdfd1, rdfd2, resultTypeSimilarityDegree,
					queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree);
			RDFDatasetSimilarity querySim = new RDFDatasetSimilarity(this.token);
			querySim.register(this);
			float score = querySim.computeSim(rdfd1, rdfd2);
			qR.register(this);

			log.info("Building recommended query tree");
			qR.buildRecommendation();
		} catch (Exception ex) {
			log.error("Exception caught while building recommendation.", ex);
		}
		return Collections.emptyList();
	}

	protected void fireQueryRecommended(Query query, float score) {
		for (QueryRecommendationListener listener : listeners)
			listener.queryRecommended(query, score, queryString);
	}

	protected void fireSatisfiabilityChecked(Query query, boolean satisfiable) {
		for (QueryRecommendationListener listener : listeners)
			listener.satisfiabilityChecked(query, rdfd2, satisfiable);
	}

}
