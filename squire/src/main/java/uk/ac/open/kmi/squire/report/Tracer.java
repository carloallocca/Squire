package uk.ac.open.kmi.squire.report;

import java.io.PrintWriter;
import java.net.URL;

import org.apache.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * Attached to a query recommendation generator, it will log recommended queries
 * as they come.
 * 
 * @author alessandro
 *
 */
public class Tracer extends Reporter {

	private Logger log = LoggerFactory.getLogger(getClass());

	private PrintWriter out;

	private long startTimestamp, latestTimestamp;

	/**
	 * Keeps track of how many recommended queries have been received since
	 * record-keeping began (or since the reporter was last reset).
	 */
	protected int counter;

	public Tracer(String originalQuery, URL sourceEndpoint, URL targetEndpoint, PrintWriter out) {
		super(originalQuery, sourceEndpoint, targetEndpoint);
		this.out = out;
		reset();
	}

	public void printFooter() {
		printFooter(out);
	}

	public void printHeader() {
		printHeader(out, "SQUIRE query recommendation log");
	}

	@Override
	public void queryRecommended(Query query, float score, String original) {
		long timestamp = System.currentTimeMillis();
		if (originalQuery != null && !original.equals(this.originalQuery)) {
			log.warn("Original query has changed!");
			log.warn(" * Got: {}", original);
			log.warn(" * Expected: {}", this.originalQuery);
		} else this.originalQuery = original;
		out.println("=========");
		out.println("Query # " + (++counter));
		out.println("Score = " + score);
		out.println("Computation time = " + (timestamp - latestTimestamp) + " ms (" + (timestamp - startTimestamp)
				+ " ms since process start)");
		out.println("Query:");
		out.println(query);
		out.println();
		out.flush();
		latestTimestamp = timestamp;
	}

	@Override
	public void reset() {
		counter = 0;
		startTimestamp = System.currentTimeMillis();
		latestTimestamp = startTimestamp;
	}

	@Override
	public void satisfiabilityChecked(Query query, IRDFDataset targetDataset, boolean satisfiable) {
		out.println("*****************");
		out.println("**** WARNING ****");
		out.println("*****************");
		out.println("A recommendation candidate was found to be unsatisfiable with the target dataset.");
		out.println("Endpoint : " + targetDataset.getEndPointURL());
		out.println("Query:");
		out.println(query);
		out.println("*****************");
		out.println();
		out.flush();
	}

}
