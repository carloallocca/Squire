package org.insightcentre.squire.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * Can be attached to a query recommendation generator in order to do
 * record-keeping and reporting during or at the end of computation.
 * 
 * @author alessandro
 *
 */
public class ConsolidatingReporter extends Reporter {

	/**
	 * Inverse sorter that compares by score, higher first.
	 */
	private Comparator<QueryStringScorePair> comparator = new Comparator<QueryStringScorePair>() {
		public int compare(QueryStringScorePair o1, QueryStringScorePair o2) {
			return o1.getScore() < o2.getScore() ? 1 : o1.getScore() > o2.getScore() ? -1 : 0;
		}
	};

	private Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Integer> order = new HashMap<>();

	private ArrayList<QueryStringScorePair> recommendations = new ArrayList<>();

	/**
	 * Keeps track of how many recommended queries have been received since
	 * record-keeping began (or since the reporter was last reset).
	 */
	protected int counter = 0;

	public ConsolidatingReporter(String originalQuery, URL sourceEndpoint, URL targetEndpoint) {
		super(originalQuery, sourceEndpoint, targetEndpoint);
	}

	@Override
	public void generalized(Collection<Query> lgg, String original) {
		// We are not interested in the lgg for the consolidated report.
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
		log.info("Generating consolidating report for top {} recommendations", topK);
		printHeader(out);
		out.println("## Recommendations");
		out.println("Top " + Math.min(topK, recommendations.size()) + " recommended queries follow.");
		log.debug("Sorting recommendations...");
		long before = System.currentTimeMillis();
		recommendations.sort(comparator);
		log.debug("DONE. Sorted {} items in {} ms", recommendations.size(), System.currentTimeMillis() - before);
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
		out.flush();
		printFooter(out);
		log.info("Consolidating report complete.");
	}

	@Override
	public void queryRecommended(Query query, float score, String original) {
		if (originalQuery != null && !original.equals(this.originalQuery)) {
			log.warn("Original query has changed!");
			log.warn(" * Got: {}", original);
			log.warn(" * Expected: {}", this.originalQuery);
		} else
			this.originalQuery = original;
		recommendations.add(new QueryStringScorePair(query.serialize(), score));
		order.put(query.serialize(), ++counter);
	}

	@Override
	public void reset() {
		counter = 0;
		recommendations.clear();
		order.clear();
	}

	@Override
	public void satisfiabilityChecked(Query query, IRDFDataset targetDataset, boolean satisfiable) {
		// We are not including the satisfiability of queries in the consolidated
		// report at the moment.
	}

}
