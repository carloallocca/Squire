package uk.ac.open.kmi.squire.core4;

import java.util.List;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.evaluation.Measures;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * A processor that, given a SPARQL {@link Query} that is satisfiable on an
 * {@link IRDFDataset} (called the <em>source dataset</em>), is able to generate
 * recommendations of other similar queries that satisfiable with another
 * dataset (the <em>target dataset</em>). The recommendations are ranked based
 * on a set of measures computed over some relevant {@link Measures}.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public interface QueryRecommender {

	/**
	 * Starts the actual recommendation process, whose results will eventually
	 * become available on the next call to
	 * {@link QueryRecommender#getRecommendations()}.
	 */
	public void buildRecommendation();

	public Measures getMetrics();

	/**
	 * Returns the original SPARQL query that is assumed to be satisfiable on the
	 * RDF dataset returned by a call to
	 * {@link QueryRecommender#getSourceDataset()}.
	 * 
	 * @return the source query
	 */
	public Query getQuery();

	public IRDFDataset getSourceDataset();

	public IRDFDataset getTargetDataset();

	/**
	 * If {@link QueryRecommender#buildRecommendation()} was called earlier and has
	 * terminated, this method will return the result.
	 * 
	 * @return an ordered list of SPARQL queries, highest-scored first.
	 */
	public List<QueryScorePair> getRecommendations();

}
