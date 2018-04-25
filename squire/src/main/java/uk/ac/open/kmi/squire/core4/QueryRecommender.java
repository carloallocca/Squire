package uk.ac.open.kmi.squire.core4;

import java.util.List;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.evaluation.Metrics;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

public interface QueryRecommender {

	public void buildRecommendation();

	public Metrics getMetrics();

	public Query getQuery();

	public IRDFDataset getSourceDataset();

	public IRDFDataset getTargetDataset();

	public List<QueryScorePair> getRecommendations();

}
