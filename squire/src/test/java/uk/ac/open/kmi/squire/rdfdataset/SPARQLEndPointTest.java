/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.utils.SparqlUtils;

/**
 *
 * @author carloallocca
 *
 *         see https://github.com/ncbo/sparql-code-examples/tree/master/java see
 *         SPARQL Web-Querying Infrastructure: Ready for Action?
 *
 */
public class SPARQLEndPointTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Test of setDatasetPath method, of class SPARQLEndPointBasedRDFDataset.
	 *
	 *
	 */
	// @Test
	// public void testPair1() {
	//
	// System.out.println("setDatasetPath");
	//
	// // String urlAddress = "http://dbpedia.org/sparql";
	//// String urlAddress = "http://sparql.data.southampton.ac.uk/";
	//
	// // Unexpected error making the query:
	// org.apache.http.conn.HttpHostConnectException: Connection to
	// http://sparql.linkedopendata.it refused
	//// String urlAddress = "http://193.145.57.56:8890/sparql";
	//
	// // Unexpected error making the query:
	// org.apache.http.conn.HttpHostConnectException: Connection to
	// http://sparql.linkedopendata.it refused
	//// String urlAddress = "http://sparql.linkedopendata.it/scuole";
	//
	// String urlAddress = "http://data.linkedu.eu/don/sparql";
	//
	//
	//
	//
	//
	// String graphName = "";
	// SPARQLEndPointBasedRDFDataset instance = new
	// SPARQLEndPointBasedRDFDataset(urlAddress, graphName);
	//
	// System.out.println("SPARQL endpoint " + urlAddress);
	//// System.out.println("getClassSet " +instance.getClassSet().toString());
	//// System.out.println("cardinality " +instance.getClassSet().size());
	//
	// //System.out.println("getObjectPropertySet "
	// +instance.getObjectPropertySet().toString());
	// //System.out.println("getDatatypePropertySet "
	// +instance.getDatatypePropertySet().toString());
	// //System.out.println("getLiteralSet " +instance.getLiteralSet().toString());
	// //System.out.println("getPropertySet "
	// +instance.getPropertySet().toString());
	// //System.out.println("getRDFVocabulary "
	// +instance.getRDFVocabulary().toString());
	// }
	/**
	 * Test of setDatasetPath method, of class SPARQLEndPointBasedRDFDataset.
	 *
	 * String urlAddress1 = "http://dbpedia.org/sparql"; String urlAddress2 =
	 * "http://sparql.data.southampton.ac.uk/";
	 */
	// @Test
	// public void testPair2() {
	//
	// // NOT WORKING SPARQL END POINT :
	// //String urlAddress = "http://sparql.data.southampton.ac.uk/";
	// //String urlAddress = "http://linkedgeodata.org/sparql/";
	//
	// //HTTP 503 error making the query: Service Unavailable
	// //String urlAddress = "http://www4.wiwiss.fu-berlin.de/eurostat/sparql";
	//
	// //String urlAddress = "https://query.wikidata.org/sparql";
	//
	// // Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	// // String urlAddress ="http://data.nature.com/query";
	//
	// //HTTP 504 error making the query: GATEWAY_TIMEOUT
	// //String urlAddress =
	// "http://data.ordnancesurvey.co.uk/datasets/os-linked-data/apis/sparql";
	// // Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	// //String urlAddress = "http://collection.britishart.yale.edu/sparql/";
	// // Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	// //String urlAddress = "http://publicspending.net/endpoint";
	// //Unexpected error making the query:
	// org.apache.http.conn.HttpHostConnectException: Connection to
	// http://sparql.asn.desire2learn.com:8890 refused
	// //String urlAddress = "http://sparql.asn.desire2learn.com:8890/sparql";
	// // https://chem2bio2rdf.wikispaces.com/multiple+sources
	// //Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	// //String urlAddress = "http://chem2bio2rdf.org/bindingdb/sparql";
	// // String urlAddress = "http://ldf.fi/finlex/sparql";
	// // Not a JSON object START: [null]
	// // String urlAddress = "http://finance.data.gov.uk/sparql/finance/query";
	// // Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	// //String urlAddress = "http://openuplabs.tso.co.uk/sparql/gov-reference";
	//// String urlAddress = "http://lsd.taxonconcept.org/sparql";
	// // classSet cardinality 552
	// // objectPropertySet cardinality so far: 450
	// // HTTP 502 error making the query: Proxy Error
	//// String urlAddress = "http://semantic.eea.europa.eu/sparql";
	// //String urlAddress = "http://ecowlim.tfri.gov.tw/sparql/query";
	// //classSet cardinality 1163
	//// String urlAddress = "http://linkedgeodata.org/sparql";
	// //NOT available
	//// String urlAddress =
	// "http://data.linkededucation.org/request/lak-conference/sparql";
	// // Failed to connect to SPARQL endpoint , http://seek.rkbexplorer.com/sparql/
	// //String urlAddress = "http://leonard:7002/sparql/";
	// // cardinality 98
	// //objectPropertySet cardinality so far: 50
	// // Time elapsed: 41.503 sec <<< ERROR!
	//// String urlAddress = "http://services.data.gov.uk/education/sparql";
	//// String urlAddress = "http://sparql.linkedopendata.it/musei";
	//// String urlAddress = "http://collection.britishmuseum.org/Sparql";
	//
	// // Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	//// String urlAddress = "http://openuplabs.tso.co.uk/sparql/gov-crime";
	//
	//// String urlAddress5 = "http://data.szepmuveszeti.hu/sparql";
	//// String urlAddress = "http://visualdataweb.infor.uva.es/sparql";
	//// String urlAddress = "http://aliada.scanbit.net:8891/sparql"; //connession
	// refused
	//// String urlAddress = "http://setaria.oszk.hu/sparql";
	//// String urlAddress = "http://data.uni-muenster.de/sparql";
	// // I stopped at datatypePropertySet cardinality so far: 2400
	// // String urlAddress = "http://serendipity.utpl.edu.ec/lod/sparql";
	//
	// //String urlAddress11 = "http://collection.britishmuseum.org/sparql";
	//
	// // Endpoint returned Content-Type: text/html which is not currently supported
	// for SELECT queries
	// //String urlAddress ="http://data.nature.com/sparql";
	//
	//
	//
	//
	// //SPARQL ENDPOINT DATASET
	//
	// //1. HIGH EDUCTATION
	//
	// // classSet cardinality 152
	// // ObjectProperty cardinality 595
	// // datatypePropertySet cardinality 264
	// String urlAddress1 = "http://data.open.ac.uk/query";
	// // classSet cardinality 139
	// // ObjectProperty cardinality 220
	// // datatypePropertySet cardinality 134
	// String urlAddress2 = "https://data.ox.ac.uk/sparql/"; //oxford univeristy
	//
	// // 2. GOVERNMENT STATISTICS 1
	//
	// // classSet cardinality 11
	// // ObjectProperty cardinality 21
	// // datatypePropertySet cardinality 5
	// String urlAddress3 = "http://eurostat.linked-statistics.org/sparql";
	//
	//
	// // classSet cardinality 39
	// // ObjectProperty cardinality 70
	// // datatypePropertySet cardinality 82
	// String urlAddress4 = "http://www.europeandataportal.eu/sparql";
	//
	//
	//
	// // 3. POLICE-CRIME
	//
	// // classSet cardinality 3
	// // ObjectProperty cardinality 10
	// // datatypePropertySet cardinality 30
	// String urlAddress5 = "http://greek-lod.auth.gr/police/sparql";
	// // classSet cardinality 12
	// // ObjectProperty cardinality 4
	// // datatypePropertySet cardinality 4
	// String urlAddress6 = "http://gov.tso.co.uk/crime/sparql"; // This is very
	// good, it as query examples
	//
	// // http://gov.tso.co.uk/crime/sparql
	//
	// // 4. GEO
	// // classSet cardinality 38
	// // ObjectProperty cardinality 41
	// // datatypePropertySet cardinality 24
	// String urlAddress7 = "http://os.services.tso.co.uk/geo/sparql"; // This is
	// very good, it as query examples
	//
	// // classSet cardinality 190
	// // ObjectProperty cardinality 60
	// // datatypePropertySet cardinality 80
	// String urlAddress8="http://geo.linkeddata.es/sparql";
	//
	// //String urlAddress111 = "http://linkedgeodata.org/sparql";
	//
	// // 5. GOVERNMENT STATISTICS 2
	//
	// // classSet cardinality 11
	// // ObjectProperty cardinality 21
	// // datatypePropertySet cardinality 5
	// String urlAddress9 = "http://eurostat.linked-statistics.org/sparql";
	//
	//
	// // classSet cardinality 4
	// // ObjectProperty cardinality 50
	// // datatypePropertySet cardinality 8
	// String urlAddress10 = "http://gov.tso.co.uk/statistics/sparql"; // This is
	// very good, it as query examples
	//
	//
	//
	//
	//
	//
	//
	//
	// // 6. MUSEUM AND ART
	//
	// // classSet cardinality 107
	// // ObjectProperty cardinality 122
	// // datatypePropertySet cardinality 106
	// // Time = 5.475 sec
	// String urlAddress11 = "http://datos.artium.org/sparql";
	//
	// // DOES NOT MANAGE "ORDER BY"
	// // classSet cardinality 35
	// // ObjectProperty cardinality ?
	// // datatypePropertySet cardinality ?
	// String urlAddress12 =
	// "http://rijksmuseum.sealinc.eculture.labs.vu.nl/sparql/";
	//
	//
	//
	//
	// // 7. BIO DOMAIN
	//
	// // classSet cardinality ?
	// // ObjectProperty cardinality ?
	// // datatypePropertySet cardinality ?
	// String urlAddress13 = "http://wwwdev.ebi.ac.uk/rdf/services/atlas/sparql";
	//
	// // classSet cardinality 30
	// // ObjectProperty cardinality 44
	// // datatypePropertySet cardinality 25
	// String urlAddress14 = "http://edan.si.edu/saam/sparql";
	//
	// //parser
	// String urlAddress15 = "http://chem2bio2rdf.org/bindingdb/sparql";
	//
	//
	// // 8. EDUCATION
	//
	// // classSet cardinality 93
	// // ObjectProperty cardinality 58
	// // datatypePropertySet cardinality 149
	// String urlAddress16 = "http://gov.tso.co.uk/education/sparql"; // This is
	// very good, it as query examples
	//
	// // classSet cardinality 98
	// // ObjectProperty cardinality ?
	// // datatypePropertySet cardinality ?
	// String urlAddress17 = "http://services.data.gov.uk/education/sparql";
	//
	//
	//
	// // 9. ARCHAEOLOGICAL DOMAIN AND World War I as Linked Open Data (CIDOC-CRM)
	//
	// // classSet cardinality 84
	// // ObjectProperty cardinality 93
	// // datatypePropertySet cardinality 75
	// String urlAddress18 = "http://ldf.fi/ww1lod/sparql";
	// // classSet cardinality 99
	// // ObjectProperty cardinality 88
	// // datatypePropertySet cardinality 66
	// String urlAddress19 = "http://linkedarc.net/sparql";
	//
	//
	//
	// // 10. ENVIRONMENT
	//
	// // classSet cardinality 67
	// // ObjectProperty cardinality 68
	// // datatypePropertySet cardinality 53
	// String urlAddress20 = "http://gov.tso.co.uk/environment/sparql"; // This is
	// very good, it as query examples
	//
	// // classSet cardinality 552
	// // ObjectProperty cardinality ?
	// // datatypePropertySet cardinality ?
	//
	// // OK, but I need manually filter " FILTER
	// (!regex(str(?p),'http://www.w3.org/1999/02/22-rdf-syntax-ns') )"+
	// //" FILTER (!regex(str(?p),'http://www.w3.org/2000/01/rdf-schema') )"+
	// String urlAddress21 = "http://semantic.eea.europa.eu/sparql";
	//
	//
	//
	//
	// // 11. UK-GOV-TRANSPORT
	// // classSet cardinality 107
	// // ObjectProperty cardinality ?
	// // datatypePropertySet cardinality ?
	// String urlAddress22 = "http://gov.tso.co.uk/transport/sparql"; // This is
	// very good, it as query examples
	//
	// // 12. UK-GOV-PATENTS
	// // classSet cardinality 8
	// // ObjectProperty cardinality 10
	// // datatypePropertySet cardinality 12
	// String urlAddress23 = "http://gov.tso.co.uk/patents/sparql"; // This is very
	// good, it as query examples
	//
	// // 13. UK-GOV-BUSINESS
	// // classSet cardinality 3
	// // ObjectProperty cardinality 7
	// // datatypePropertySet cardinality 22
	// String urlAddress24 = "http://gov.tso.co.uk/business/sparql"; // This is very
	// good, it as query examples
	//
	// // 14. UK-GOV-RESEARCH
	// // classSet cardinality 14
	// // ObjectProperty cardinality 13
	// // datatypePropertySet cardinality 31
	// String urlAddress25 = "http://gov.tso.co.uk/research/sparql"; // This is very
	// good, it as query examples
	//
	//
	// // 15.
	//
	// String urlAddress26 = "http://worldbank.270a.info/sparql";
	// String urlAddress27 = "http://uis.270a.info/sparql";
	//
	//
	// // HTTP 502 error making the query: Proxy Error
	//// String urlAddress =
	// "http://openlifedata.org/ncbigene/sparql/";//http://semantic.eea.europa.eu/sparql";
	//
	//// String urlAddress = "http://sparql.data.southampton.ac.uk/";
	//
	//
	//
	// // Service Temporarily Unavailable
	//// String urlAddress
	// ="http://resrev.ilrt.bris.ac.uk/data-server-workshop/sparql";
	//
	// // Aalto University
	//// String urlAddress ="http://data.aalto.fi/sparql";
	//
	// //String urlAddress ="http://linked.opendata.cz/sparql";
	//
	//// String urlAddress ="http://data.upf.edu/en/sparql"; // GOOD FOR
	// http://data.upf.edu/en/sparql_examples
	// ///http://linkeduniversities.org/lu/index.php/datasets-and-endpoints/
	//
	//
	// //
	//// String urlAddress = "http://biomodels.bio2rdf.org/sparql"; //
	//// String urlAddress = "http://bioportal.bio2rdf.org/sparql"; //
	//
	//
	//
	//
	//
	//// String[] urlAddressPair = {urlAddress1, urlAddress2, urlAddress4,
	// urlAddress5, urlAddress6,
	//// urlAddress7, urlAddress8, urlAddress9, urlAddress10, urlAddress27};
	//
	// String[] urlAddressPair = {urlAddress7};
	//// String[] urlAddressPair = {urlAddress9};
	//// String[] urlAddressPair = {urlAddress1};
	//// String[] urlAddressPair = {urlAddress27};
	// // String[] urlAddressPair = {urlAddress26};
	//
	//
	//// String[] urlAddressPair = { "http://gov.tso.co.uk/patents/sparql",
	// "http://reference.data.gov.uk/organograms/sparql",
	// "http://gov.tso.co.uk/education/sparql"};
	//
	//// String[] urlAddressPair = {urlAddress3,urlAddress1, urlAddress2,
	//// urlAddress4, urlAddress5, urlAddress6, urlAddress7, urlAddress8,
	// urlAddress9};
	//
	// for (int i = 0; i < urlAddressPair.length; i++) {
	// System.out.println(" ");
	// System.out.println("[testPair2] SPARQL endpoint " + urlAddressPair[i]);
	// String graphName = "";
	// SPARQLEndPointBasedRDFDataset instance = new
	// SPARQLEndPointBasedRDFDataset(urlAddressPair[i], graphName);
	// System.out.println(" ");
	//
	// }
	//
	//
	////
	//// for (int i = 0; i < urlAddressPair.length; i++) {
	//// System.out.println(" ");
	//// System.out.println("[testPair2] SPARQL endpoint Getting the Signature " +
	// urlAddressPair[i]);
	//// String graphName = "";
	//// SPARQLEndPointBasedRDFDataset instance = new
	// SPARQLEndPointBasedRDFDataset(urlAddressPair[i], graphName);
	////
	//// System.out.println("getClassSet " +instance.getClassSet().toString());
	//// System.out.println("getObjectPropertySet "
	// +instance.getObjectPropertySet().toString());
	//// System.out.println("getDatatypePropertySet "
	// +instance.getDatatypePropertySet().toString());
	////
	////
	////
	////
	//// System.out.println(" ");
	////
	////
	//// }
	//
	//
	//
	// }
	//
	//

	// @Test
	// public void testAddSPARQLEndPointSignature() {
	// FileInputStream fstream = null;
	// org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
	//
	// try {
	//// RDFDatasetIndexerII indexerInstance = new RDFDatasetIndexerII(indexDir);
	//
	// System.out.println("[SPARQLEndPointBasedRDFDatasetTest::testAddSPARQLEndPointSignature]");
	// //String indexDir="/Users/carloallocca/Desktop/KMi/KMi Started
	// 2015/KMi2015Development/Led2Pro/SPARQEndPointIndex";
	// String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started
	// 2015/KMi2015Development/WebSquire/endpointlist";
	//
	// // Open the file
	// fstream = new FileInputStream(fileName);
	// try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream)))
	// {
	// String strLine;
	// try {
	// //Read File Line By Line
	// int i = 1;
	// while ((strLine = br.readLine()) != null) {
	// System.out.println("EndPoint " + strLine);
	// System.out.println(i);
	// i++;
	// computeClassSet(strLine, "");
	//
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// }
	// } catch (IOException ex) {
	// Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE,
	// "TEST:CARLO 1", ex);
	// } catch (Exception ex) {
	//
	// Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE,
	// "TEST:CARLO 2", ex);
	// }
	// }
	// } catch (FileNotFoundException ex) {
	// Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE,
	// "TEST:CARLO 3", ex);
	// } catch (IOException ex) {
	//
	// Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE,
	// "TEST:CARLO 4", ex);
	// } finally {
	// try {
	// if (fstream != null) {
	// fstream.close();
	// }
	// } catch (IOException ex) {
	// Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE,
	// "TEST:CARLO 7", ex);
	// }
	// }
	//
	// }

	// @Test
	public void testAddSPARQLEndPointSignature3() {
		ParameterizedSparqlString qs = new ParameterizedSparqlString(
				"" + "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" + "\n" + "select ?resource where {\n"
						+ "  ?resource rdfs:label ?label\n" + "}");

		String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where " + "{ "
				+ " ?ind a ?class . " + "}";

		// Literal london = ResourceFactory..createLangLiteral( "London", "en" );
		// qs.setParam( "label", london );

		System.out.println(qs);

		// QueryExecution exec = QueryExecutionFactory.sparqlService(
		// "http://dbpedia.org/sparql", qs.asQuery() );
		QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qString);

		// Normally you'd just do results = exec.execSelect(), but I want to
		// use this ResultSet twice, so I'm making a copy of it.
		ResultSet results = ResultSetFactory.copyResults(exec.execSelect());

		while (results.hasNext()) {
			// As RobV pointed out, don't use the `?` in the variable
			// name here. Use *just* the name of the variable.
			System.out.println(results.next().get("resource"));
		}

		// A simpler way of printing the results.
		ResultSetFormatter.out(results);
	}

	// @Test
	public void testAddSPARQLEndPointSignature1() {
		String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where " + "{ "
				+ " ?ind a ?class . " + "}";
		// QueryExecution qexec =
		// QueryExecutionFactory.sparqlService("http://data.open.ac.uk/query", qString,
		// "");
		// QueryExecution qexec = new QueryEngineHTTP("http://dbpedia.org/sparql",
		// qString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://datos.artium.org:8890/sparql", qString);
		ResultSet results = qexec.execSelect();
		String a = ResultSetFormatter.asText(results);
		System.out.println("#Class " + a);
	}

	// @Test
	public void testAddSPARQLEndPointSignature2() {

		try {
			String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
					+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where " + "{ "
					+ " ?ind a ?class . " + "}";

			String endEndPoint = "http://opendatacommunities.org/sparql";
			// "https://dbpedia.org/sparql"

			String encodedQuery = URLEncoder.encode(qString, "UTF-8");
			String GET_URL = endEndPoint + "?query=" + encodedQuery;
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(GET_URL);
			getRequest.addHeader("accept", "application/sparql-results+json");
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			String result = "";
			while ((output = br.readLine()) != null) {
				result = result + output;
			}
			List<String> classList = SparqlUtils.getValuesFromSparqlJson(result, "class");

			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testAddSPARQLEndPointSignature() {
		FileInputStream fstream = null;
		// org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		try {
			// RDFDatasetIndexerII indexerInstance = new RDFDatasetIndexerII(indexDir);

			System.out.println("[SPARQLEndPointBasedRDFDatasetTest::testAddSPARQLEndPointSignature]");
			// String indexDir="/Users/carloallocca/Desktop/KMi/KMi Started
			// 2015/KMi2015Development/Led2Pro/SPARQEndPointIndex";
			// String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started
			// 2015/KMi2015Development/WebSquire/endpointlist";
			String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointTestlist";

			// Open the file
			fstream = new FileInputStream(fileName);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
				String strLine;
				try {
					// Read File Line By Line
					int i = 1;
					while ((strLine = br.readLine()) != null) {
						System.out.println("EndPoint  " + strLine);
						System.out.println(i);
						i++;

						try {
							computeClassSet(strLine, "");
							computeObjectPropertySet(strLine, "");
							computeDataTypePropertySet(strLine, "");

						} catch (Exception ex) {
							log.error("{}", ex);
						}

						System.out.println(" ");
						System.out.println(" ");
						System.out.println(" ");
						System.out.println(" ");
						System.out.println(" ");
					}
				} catch (IOException ex) {
					log.error("TEST:CARLO 1", ex);
				} catch (Exception ex) {
					log.error("TEST:CARLO 2", ex);
				}
			}
		} catch (FileNotFoundException ex) {
			log.error("TEST:CARLO 3", ex);
		} catch (IOException ex) {

			log.error("TEST:CARLO 4", ex);
		} finally {
			try {
				if (fstream != null) {
					fstream.close();
				}
			} catch (IOException ex) {
				log.error("TEST:CARLO 7", ex);
			}
		}

	}

	private void computeClassSet(String datasetPath, String graphName) throws Exception {
		String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where " + "{ "
				+ " ?ind a ?class . " + "}";

		QueryExecution qexec = QueryExecutionFactory.sparqlService(datasetPath, qString, graphName);
		// QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath,
		// qString);

		ResultSet results = qexec.execSelect();

		String a = ResultSetFormatter.asText(results);

		// List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(,
		// results, q);
		System.out.println("#Class " + a);

		// for (QuerySolution sol : solList) {
		// if (sol.get("class").asResource().getURI() != null) {
		//// this.classSet.add(sol.get("class").asResource().getURI());
		// System.out.println(sol.get("class").asResource().getURI());
		// }
		// }
	}

	private void computeObjectPropertySet(String datasetPath, String graphName) throws Exception {
		String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?op where " + "{ "
				+ " ?s ?op ?o . " + " FILTER (isURI(?o)) " + "}";

		QueryExecution qexec = QueryExecutionFactory.sparqlService(datasetPath, qString, graphName);
		// QueryExecution qexec = new QueryEngineHTTP(datasetPath, qString);

		ResultSet results = qexec.execSelect();
		String a = ResultSetFormatter.asText(results);

		// List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(,
		// results, q);
		System.out.println("#ObjectProperty " + a);

		// ResultSetFormatter.out(results);

		// for (QuerySolution sol : solList) {
		// if (sol.get("op").asResource().getURI() != null) {
		// System.out.println(sol.get("op").asResource().getURI());
		//
		// }
		// }

	}

	private void computeDataTypePropertySet(String datasetPath, String graphName) throws Exception {

		String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?p  where " + "{ "
				+ " ?s ?p ?o . " + " FILTER (isLiteral(?o)) " + "}";

		// QueryExecution qexec = QueryExecutionFactory.sparqlService((String)
		// this.datasetPath, qString, this.graphName);
		QueryExecution qexec = new QueryEngineHTTP(datasetPath, qString);

		ResultSet results = qexec.execSelect();

		String a = ResultSetFormatter.asText(results);

		// List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(,
		// results, q);
		System.out.println("#DatatypeProperty " + a);

		// List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(,
		// results, q);
		// for (QuerySolution sol : solList) {
		// if (sol.get("p").asResource().getURI() != null) {
		// System.out.println(sol.get("p").asResource().getURI());
		//
		// }
		// }

	}

}
