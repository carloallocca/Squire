package uk.ac.open.kmi.squire.core4;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import uk.ac.open.kmi.squire.evaluation.Metrics;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

public abstract class AbstractQueryRecommender extends AbstractQueryRecommendationObservable
		implements QueryRecommender, IQueryRecommendationObserver {

	protected final Metrics metrics;

	protected final Query q0;

	protected final IRDFDataset rdfD1, rdfD2;

	public AbstractQueryRecommender(Query query, IRDFDataset d1, IRDFDataset d2, Metrics metrics) {
		q0 = QueryFactory.create(query.toString());
		rdfD1 = d1;
		rdfD2 = d2;
		this.metrics = metrics;
	}

	@Override
	public Metrics getMetrics() {
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
