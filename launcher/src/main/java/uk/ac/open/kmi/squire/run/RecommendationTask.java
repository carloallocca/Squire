package uk.ac.open.kmi.squire.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.QueryRecommendatorForm4;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
import uk.ac.open.kmi.squire.report.ConsolidatingReporter;
import uk.ac.open.kmi.squire.report.Tracer;

public class RecommendationTask {

	private String query;

	private String sourceEndpoint;

	private String id;

	private boolean withLogging = false;

	private Set<String> targetEndpoints;

	public RecommendationTask(String query, String sourceEndpoint, Set<String> targetEndpoints, String id,
			boolean logging) {
		this.query = query;
		this.sourceEndpoint = sourceEndpoint;
		this.targetEndpoints = targetEndpoints;
		this.withLogging = logging;
		this.id = id;
	}

	public RecommendationTask(String query, String sourceEndpoint, Set<String> targetEndpoints, String id) {
		this(query, sourceEndpoint, targetEndpoints, id, false);
	}

	private Logger log = LoggerFactory.getLogger(getClass());

	public void execute() {
		log.info("Start recommendation for query:\r\n{}", query);
		String dir = "TestResults/";
		new File(dir).mkdir();
		for (String tgt : targetEndpoints) {
			IRDFDataset d1 = new SparqlIndexedDataset(sourceEndpoint), d2 = new SparqlIndexedDataset(tgt);
			QueryRecommendatorForm4 recom = new QueryRecommendatorForm4(query, d1, d2, 1, 1, 1, 1, Integer.toString(1));

			URL uS, uT;
			try {
				uS = new URL(sourceEndpoint);
				uT = new URL(tgt);
			} catch (MalformedURLException e) {
				log.error("Make sure both <{}> and <{}> are URLs.", sourceEndpoint, tgt);
				break;
			}
			String key = uT.getHost().replaceAll("\\.", "_");
			ConsolidatingReporter rep = new ConsolidatingReporter(query, uS, uT);
			String filename = (id != null && !id.trim().isEmpty() ? id.trim() + "__" : "") + key;
			Tracer tracer = null;
			if (withLogging) try {
				tracer = new Tracer(query, uS, uT, new PrintWriter(new FileWriter(dir + filename + ".log")));
				tracer.printHeader();
				recom.addListener(tracer);
			} catch (IOException ex) {
				log.error("Cannot log to file {}", dir + filename + ".log");
			}
			recom.addListener(rep);
			recom.run();
			if (tracer != null) tracer.printFooter();
			try {
				rep.printReport(new PrintWriter(new FileWriter(dir + filename + ".txt")), 50);
			} catch (IOException e) {
				log.error("Cannot write report to file {}", dir + filename + ".txt");
				throw new RuntimeException(e);
			}

		}

	}

}
