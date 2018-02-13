package uk.ac.open.kmi.squire.core4;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.jobs.JobManager;

/**
 * Differs from {@link IQueryRecommendationObserver} in that it is not bound to
 * a job with a token, therefore can also be used if the recommendation is not a
 * Job managed by a {@link JobManager}. It also means it is not aware of the
 * status of the process.
 * 
 * @author alessandro
 *
 */
public interface QueryRecommendationListener {

	public void queryRecommended(Query query, float score, String original);

}
