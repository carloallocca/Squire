/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Before;
import org.junit.BeforeClass;

import uk.ac.open.kmi.squire.core4.QueryRecommendatorForm4;
import uk.ac.open.kmi.squire.jobs.JobManager;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
import uk.ac.open.kmi.squire.report.ConsolidatingReporter;
import uk.ac.open.kmi.squire.report.Tracer;

/**
 *
 * @author carloallocca
 */
public class TestGoldStandard {

	private class Config {
		float wQueryRootDistance = 1;
		float wQuerySpecificityDistance = 1;
		float wResultSizeSimilarity = 1;
		float wResultTypeSimilarity = 1;
	}

	private static JsonObject testdata;

	@BeforeClass
	public static void setUp() {
		testdata = JSON.parse(TestGoldStandard.class.getResourceAsStream("/goldstandard.json"));
		assertTrue(testdata.isObject());
	}

	private Config config;

	@Before
	public void setUpTest() {
		config = new Config();
	}

	/*
	 * Not repeatable at the moment due to datos.artium.org:8890 being down.
	 */
	// @Test
	public void testArt() throws Exception {
		// the two RDF datasets are very similar to each other...
		// http://datos.artium.org:8890/sparql is not up anymore.
		unpackAndRun("arts_1");
	}

	// @Test
	public void testEducationI() throws Exception {
		unpackAndRun("edu_1");
	}

	// @Test
	public void testEducationII() throws Exception {
		// Generalised is too generic! Aalto keeps us waiting forever on
		// SELECT DISTINCT ?dpt1 ?opt1 ?dpt2
		// WHERE
		// { ?podcast ?dpt1 ?published ;
		// ?dpt2 ?download ;
		// ?opt1 ?this
		// }
		// (and we can't really blame them for it)
		unpackAndRun("edu_2");
	}

	// @Test
	public void testGovernmentOpenData() throws Exception {
		// This is what is happening for the query q3
		// Virtuoso 42000 Error The estimated execution time 1376 (sec) exceeds the
		// limit of 400 (sec).
		// SPARQL query:
		// SELECT DISTINCT ?ct1 ?dpt1 ?opt1 ?opt2
		// WHERE
		// { ?s ?dpt1 ?ct1 .
		// ?s ?opt1 ?id .
		// ?s ?opt2 ?label.
		// }
		unpackAndRun("egov_1");
	}

	// @Test
	public void testMuseum() throws Exception {
		unpackAndRun("museum_1");
	}

	// @Test
	public void testQueryRootDistance() throws Exception {

		System.out.println("getRecommendedQueryList");
		// String qo = "SELECT distinct ?download ?published ?this WHERE {?podcast
		// <http://purl.org/dc/terms/published> ?published .?podcast
		// <http://digitalbazaar.com/media/download> ?download .?podcast
		// <http://purl.org/dc/terms/isPartOf> ?this .}";
		// String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE
		// {?building a <http://vocab.deri.ie/rooms#Building> .?building
		// <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building
		// <http://www.geonames.org/ontology#name> ?buildName .?building
		// <http://www.geonames.org/ontology#postalCode> ?postCode .}";
		// String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE
		// {?building a <http://vocab.deri.ie/rooms#Building> .?building
		// <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building
		// <http://www.geonames.org/ontology#name> ?buildName .?building
		// <http://www.geonames.org/ontology#postalCode> ?postCode .}";

		String qo = "SELECT DISTINCT ?mod ?title ?code WHERE { ?mod a <http://purl.org/vocab/aiiso/schema#Module>. ?mod <http://purl.org/dc/terms/title> ?title . ?mod <http://purl.org/vocab/aiiso/schema#code> ?code . }";

		String source_endpoint = "http://data.open.ac.uk/query";
		String target_endpoint = "https://data.ox.ac.uk/sparql/";
		float resultTypeSimilarityDegree = 1;
		float queryRootDistanceDegree = 1;
		float resultSizeSimilarityDegree = 1;
		float querySpecificityDistanceDegree = 1;

		JobManager jobMan = JobManager.getInstance();

		IRDFDataset d1, d2;

		d1 = new SparqlIndexedDataset(source_endpoint);
		d2 = new SparqlIndexedDataset(target_endpoint);

		QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(qo, d1, d2, resultTypeSimilarityDegree,
				queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
				Integer.toString(1));

		R1.run();

	}

	private void runEvaluation(String source, String target, String[] queries, Config configuration, String key)
			throws Exception {
		IRDFDataset d1 = new SparqlIndexedDataset(source), d2 = new SparqlIndexedDataset(target);
		int nQ = 1;
		String dir = "TestResults/";
		new File(dir).mkdir();
		for (String q : queries) {
			QueryRecommendatorForm4 recom = new QueryRecommendatorForm4(q, d1, d2, configuration.wResultTypeSimilarity,
					configuration.wQueryRootDistance, configuration.wResultSizeSimilarity,
					configuration.wQuerySpecificityDistance, Integer.toString(1));
			ConsolidatingReporter rep = new ConsolidatingReporter(q, new URL(source), new URL(target));
			String filename = key + "_q" + nQ++;
			Tracer tracer = new Tracer(q, new URL(source), new URL(target),
					new PrintWriter(new FileWriter(dir + filename + ".log")));
			tracer.printHeader();
			recom.addListener(rep);
			recom.addListener(tracer);
			recom.run();
			tracer.printFooter();
			rep.printReport(new PrintWriter(new FileWriter(dir + filename + ".txt")), 50);
		}
	}

	private void unpackAndRun(String key) throws Exception {
		JsonObject group = testdata.get(key).getAsObject();
		assertTrue(group.hasKey("source"));
		String endpoint_src = group.get("source").getAsString().value();
		assertTrue(group.hasKey("target"));
		String endpoint_tgt = group.get("target").getAsString().value();
		assertTrue(group.hasKey("queries"));
		List<String> queries = new ArrayList<>();
		for (JsonValue v : group.get("queries").getAsArray()) {
			assertTrue(v.isObject());
			assertTrue(v.getAsObject().hasKey("original"));
			queries.add(v.getAsObject().get("original").getAsString().value());
		}
		runEvaluation(endpoint_src, endpoint_tgt, queries.toArray(new String[0]), this.config, key);
	}

}
