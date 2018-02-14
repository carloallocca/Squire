package uk.ac.open.kmi.squire.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.QueryRecommendationListener;

/**
 * Can be attached to a query recommendation generator in order to do
 * record-keeping and reporting during or at the end of computation.
 * 
 * @author alessandro
 *
 */
public class Reporter implements QueryRecommendationListener {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ArrayList<QueryStringScorePair> recommendations = new ArrayList<>();

	private Map<String, Integer> order = new HashMap<>();

	private String originalQuery;

	private URL sourceEndpoint, targetEndpoint;

	/**
	 * Keeps track of how many recommended queries have been received since
	 * record-keeping began (or since the reporter was last reset).
	 */
	private int counter = 0;

	/**
	 * Inverse sorter that compares by score, higher first.
	 */
	private Comparator<QueryStringScorePair> comparator = new Comparator<QueryStringScorePair>() {
		public int compare(QueryStringScorePair o1, QueryStringScorePair o2) {
			return o1.getScore() < o2.getScore() ? 1 : o1.getScore() > o2.getScore() ? -1 : 0;
		}
	};

	public Reporter(String originalQuery, URL sourceEndpoint, URL targetEndpoint) {
		if (originalQuery == null || originalQuery.isEmpty())
			throw new IllegalArgumentException("Original query must be non-null and non-empty.");
		this.originalQuery = originalQuery;
		if (sourceEndpoint == null || targetEndpoint == null)
			throw new IllegalArgumentException("Source and target endpoint cannot be null or empty.");
		this.sourceEndpoint = sourceEndpoint;
		this.targetEndpoint = targetEndpoint;
	}

	/**
	 * Prints a report of the current recommendation records for up to the top 20
	 * recommended queries.
	 * 
	 * @param out
	 *            the writer to print to
	 * @throws IOException
	 */
	public void printReport(PrintWriter out) throws IOException {
		printReport(out, Math.min(recommendations.size(), 20));
	}

	/**
	 * Prints a report of the current recommendation records for the top k
	 * recommended queries (or less if the total number of recommendations is
	 * lower).
	 * 
	 * @param out
	 *            the writer to print to
	 * @param topK
	 *            the number of highest-scored queries to show in the report
	 * @throws IOException
	 */
	public void printReport(PrintWriter out, int topK) throws IOException {
		out.println("# SQUIRE query recommendation report");
		out.println();
		out.println("Original query:");
		out.println(this.originalQuery);
		out.println("Satisfiable on endpoint: " + this.sourceEndpoint);
		out.println("Reformulated for endpoint: " + this.targetEndpoint);
		out.println();
		out.println("## Recommendations");
		out.println("Top " + Math.min(topK, recommendations.size()) + " recommended queries follow.");
		recommendations.sort(comparator);
		int total = recommendations.size();
		for (int i = 0; i < topK && i < total; i++) {
			out.println("=========");
			out.println("Rank = " + (i + 1));
			out.println("Score = " + recommendations.get(i).getScore());
			out.println("Position on arrival = " + order.get(recommendations.get(i).getQuery()).intValue() + " of "
					+ total);
			out.println("Query:");
			out.println(recommendations.get(i).getQuery());
			out.println();
		}
		out.println("=========");
		out.println("This report generated: "
				+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		out.println("END REPORT");
		out.flush();
	}

	@Override
	public void queryRecommended(Query query, float score, String original) {
		if (originalQuery != null && !original.equals(this.originalQuery)) {
			log.warn("Original query has changed!");
			log.warn(" * Got: {}", original);
			log.warn(" * Expected: {}", this.originalQuery);
		} else this.originalQuery = original;
		recommendations.add(new QueryStringScorePair(query.serialize(), score));
		order.put(query.serialize(), ++counter);
	}

	/**
	 * Tells this reporter to start keeping records from scratch, without changing
	 * the original SPARQL query or the endpoints.
	 */
	public void reset() {
		counter = 0;
		recommendations.clear();
		order.clear();
	}

}
