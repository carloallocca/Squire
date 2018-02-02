/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package some.tests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.*;
//import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author carloallocca
 */
public class ApacheHttpClientExample {

	// public static void main(String[] args) throws IOException {
	// sendGET();
	// System.out.println("GET DONE");
	//
	// HttpClient client = HttpClients.custom().se;
	//
	// QueryExecution execution = QueryExecutionFactory.sparqlService("", "",
	// client);//.sparqlService(, httpClient)
	//
	// }
	public static void main(String[] args) {

		// String GET_URL =
		// "http://opendatacommunities.org/sparql/?query=SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
		// String GET_URL =
		// "http://data.admin.ch/query/?query=SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
		// String GET_URL =
		// "http://data.admin.ch/query?query=SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
		// String GET_URL =
		// "http://opendatacommunities.org/sparql?query=SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
		try {
			// String query="SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";

			String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointlistNew";

			FileInputStream fstream = null;
			// Open the file
			fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			int i = 1;
			try {

				while ((strLine = br.readLine()) != null) {
					System.out.println(+i + strLine);
					i++;
					System.out.println("class set");
					selectClassSet(strLine);
					System.out.println("obj set");
					selectObjectPropertySet(strLine);

					System.out.println("dp set");
					selectDatatypePropertySet(strLine);

					System.out.println("");
					System.out.println("");
					System.out.println("");
				}
			} catch (IOException ex) {
				Logger.getLogger(ApacheHttpClientExample.class.getName()).log(Level.SEVERE, null, ex);
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(ApacheHttpClientExample.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private static void selectClassSet(String endpointURI) {
		try {
			String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
					+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where " + "{ "
					+ " ?ind a ?class . " + "}";
			String encodedQuery = URLEncoder.encode(qString, "UTF-8");
			String GET_URL = endpointURI + "?query=" + encodedQuery;
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
			parseSparqlResultsJson(result, "class");

			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void selectObjectPropertySet(String endpointURI) {
		try {

			// I need to filter our uri with this namespace:
			// http://www.w3.org/1999/02/22-rdf-syntax-ns#
			String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
					+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?op where " + "{ "
					+ " ?s ?op ?o . " + " FILTER (isURI(?o)) " + "}";

			String encodedQuery = URLEncoder.encode(qString, "UTF-8");
			String GET_URL = endpointURI + "?query=" + encodedQuery;

			// set the connection timeout value to 30 seconds (30000 milliseconds)
			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 300000000);
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			// DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpGet getRequest = new HttpGet(GET_URL);

			getRequest.addHeader("accept", "application/sparql-results+json");
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {

				String reason = response.getStatusLine().getReasonPhrase();
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());

			}
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			String result = "";
			while ((output = br.readLine()) != null) {
				result = result + output;
			}
			parseSparqlResultsJson(result, "op");

			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void selectDatatypePropertySet(String endpointURI) {
		try {
			String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
					+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?p  where " + "{ "
					+ " ?s ?p ?o . " + " FILTER (isLiteral(?o)) " + "} LIMIT 30";

			String encodedQuery = URLEncoder.encode(qString, "UTF-8");
			String GET_URL = endpointURI + "?query=" + encodedQuery;
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
			parseSparqlResultsJson(result, "p");

			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void parseSparqlResultsJson(String result, String varString) {

		JsonParser jsonParser = new JsonParser();
		JsonArray results = jsonParser.parse(result).getAsJsonObject().get("results").getAsJsonObject()
				.getAsJsonArray("bindings");
		for (JsonElement result1 : results) {
			JsonObject _class = result1.getAsJsonObject().getAsJsonObject(varString);
			String value = _class.get("value").getAsString();
			try {
				URI valueURI = new URI(value);
				System.out.println(valueURI);
			} catch (URISyntaxException ex) {
				Logger.getLogger(ApacheHttpClientExample.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}

	// this use the 3.1 commons-httpclient that is commented in the pom.
	// public static void main(String[] args) {
	//
	// //from http://hc.apache.org/httpclient-3.x/tutorial.html
	// //String url = "http://www.apache.org/";
	//// String url =
	// "http://opendatacommunities.org/sparql.xml?querySELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
	//// String url =
	// "http://opendatacommunities.org/sparql.json?query=SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
	// String url =
	// "http://edan.si.edu/saam/sparql?query=SELECT+%2A+WHERE+%7B%3Fs+%3Fp+%3Fo%7D+LIMIT+10";
	//
	//
	//
	// // Create an instance of HttpClient.
	// HttpClient client = new HttpClient();
	//
	// // Create a method instance.
	// GetMethod method = new GetMethod(url);
	//
	// // Provide custom retry handler is necessary
	// method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
	// new DefaultHttpMethodRetryHandler(3, false));
	//
	//
	// try {
	// // Execute the method.
	// int statusCode = client.executeMethod(method);
	//
	// if (statusCode != HttpStatus.SC_OK) {
	// System.err.println("Method failed: " + method.getStatusLine());
	// }
	//
	// // Read the response body.
	// byte[] responseBody = method.getResponseBody();
	//
	//
	//
	//
	// // Deal with the response.
	// // Use caution: ensure correct character encoding and is not binary data
	// System.out.println(new String(responseBody));
	//
	// } catch (HttpException e) {
	// System.err.println("Fatal protocol violation: " + e.getMessage());
	// e.printStackTrace();
	// } catch (IOException e) {
	// System.err.println("Fatal transport error: " + e.getMessage());
	// e.printStackTrace();
	// } finally {
	// // Release the connection.
	// method.releaseConnection();
	// }
	// }
	// private static void sendGET() throws IOException {
	// CloseableHttpClient httpClient = HttpClients.createDefault();
	// HttpGet httpGet = new HttpGet(GET_URL);
	// httpGet.addHeader("User-Agent", USER_AGENT);
	// CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
	//
	// System.out.println("GET Response Status:: "
	// + httpResponse.getStatusLine().getStatusCode());
	//
	// BufferedReader reader = new BufferedReader(new InputStreamReader(
	// httpResponse.getEntity().getContent()));
	//
	// String inputLine;
	// StringBuffer response = new StringBuffer();
	//
	// while ((inputLine = reader.readLine()) != null) {
	// response.append(inputLine);
	// }
	// reader.close();
	//
	// // print result
	// System.out.println(response.toString());
	// httpClient.close();
	// }
}
