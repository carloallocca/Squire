package uk.ac.open.kmi.squire.core4;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import uk.ac.open.kmi.squire.evaluation.Measures;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * The base implementation of a {@link QueryRecommender}.
 * 
 * @author alessandro
 *
 */
public abstract class AbstractQueryRecommender extends AbstractQueryRecommendationObservable
		implements QueryRecommender, IQueryRecommendationObserver {

	protected final Measures metrics;

	protected final Query q0;

	protected final IRDFDataset rdfD1, rdfD2;

	public AbstractQueryRecommender(Query query, IRDFDataset d1, IRDFDataset d2, Measures metrics) {
		q0 = QueryFactory.create(query.toString());
		rdfD1 = d1;
		rdfD2 = d2;
		this.metrics = metrics;
	}

	@Override
	public Measures getMetrics() {
		return this.metrics;
	}

	@Override
	public Query getQuery() {
		return this.q0;
	}

	@Override
	public IRDFDataset getSourceDataset() {
		return this.rdfD1;
	}

	@Override
	public IRDFDataset getTargetDataset() {
		return this.rdfD2;
	}

	@Override
	public void updateDatasetSimilarity(float simScore, String token) {
		// Nothing to do
	}

	@Override
	public void updateQueryRecommendated(Query qR, float score, String token) {
		this.notifyQueryRecommendation(qR, score); // Just propagate
	}

	@Override
	public void updateQueryRecommendationCompletion(Boolean finished, String token) {
		this.notifyQueryRecommendationCompletion(finished); // Just propagate
	}

	@Override
	public void updateSatisfiableValue(Query query, boolean value, String token) {
		// Nothing to do
	}

}
