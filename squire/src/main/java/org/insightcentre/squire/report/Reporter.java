package org.insightcentre.squire.report;

import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.ac.open.kmi.squire.core4.QueryRecommendationListener;

/**
 * Objects that listen to query recommendations being fired with the objective
 * of printing a log or report will instantiate an extension of this class.
 * 
 * @author alessandro
 *
 */
public abstract class Reporter implements QueryRecommendationListener {

	protected String originalQuery;

	protected URL sourceEndpoint, targetEndpoint;

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
	 * Tells this reporter to start keeping records from scratch. This operation is
	 * expected to preserve the original SPARQL query and the endpoints.
	 */
	public abstract void reset();

	/*
	 * TODO make private and support end of computation with another event type.
	 */
	protected void printFooter(PrintWriter out) {
		out.println("=========");
		out.println("This report generated: "
				+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		out.println("END REPORT");
		out.flush();
	}

	protected void printHeader(PrintWriter out) {
		printHeader(out, "SQUIRE query recommendation report");
	}

	/*
	 * TODO make private and support start of computation with another event type.
	 */
	protected void printHeader(PrintWriter out, String title) {
		out.println("# " + title);
		out.println();
		out.println("Original query:");
		out.println(this.originalQuery);
		out.println("Satisfiable on endpoint: " + this.sourceEndpoint);
		out.println("Reformulated for endpoint: " + this.targetEndpoint);
		out.println();
		out.flush();
	}

}
