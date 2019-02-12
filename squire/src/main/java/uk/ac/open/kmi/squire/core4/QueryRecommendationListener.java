package uk.ac.open.kmi.squire.core4;

import java.util.Collection;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.jobs.JobManager;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * Differs from {@link IQueryRecommendationObserver} in that it is not bound to
 * a job with a token, therefore can also be used if the recommendation is not a
 * Job managed by a {@link JobManager}.
 * 
 * It also means it is not aware of the status of the recommendation process:
 * although implementations can be made stateful, the interface itself is
 * stateless.
 * 
 * @author alessandro
 *
 */
public interface QueryRecommendationListener {

	/**
	 * Fired after a least general generalization has been computed
	 * 
	 * @param lgg
	 * @param original
	 */
	public void generalized(Collection<Query> lgg, String original);

	/**
	 * Fired after a new query recommendation has been issued.
	 * 
	 * @param query
	 * @param score
	 * @param original
	 */
	public void queryRecommended(Query query, float score, String original);

	/**
	 * Fired after the satisfiability of a query for a dataset has been checked.
	 * 
	 * @param query
	 * @param targetDataset
	 * @param satisfiable
	 */
	public void satisfiabilityChecked(Query query, IRDFDataset targetDataset, boolean satisfiable);

}
