package uk.ac.open.kmi.squire.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlUtils {

	public static class SparqlException extends Exception {

		private static final long serialVersionUID = -8338307511565961723L;

		private String queryString, endpoint;

		public SparqlException(String message, String queryString, String endpoint) {
			super(message);
			this.queryString = queryString;
			this.endpoint = endpoint;
		}

		public SparqlException(Throwable cause, String queryString, String endpoint) {
			super(cause);
			this.queryString = queryString;
			this.endpoint = endpoint;
		}

		public String getEndpoint() {
			return endpoint;
		}

		public String getQueryString() {
			return queryString;
		}

	}

	private static Logger log = LoggerFactory.getLogger(SparqlUtils.class);

	public static String doRawQuery(String queryString, String endpoint) throws SparqlException {
		return doRawQuery(queryString, endpoint, 30);
	}

	/**
	 * Attempts an HTTP GET with the URLencoded query in the 'query' GET parameter
	 * and 'application/sparql-results+json' as the Accept header.
	 * 
	 * @param queryString
	 * @param endpoint
	 * @param timeout
	 * @return the raw, unparsed response (expected to be JSON)
	 * @throws SparqlException
	 *             if anything happens other than receiving a HTTP 200 OK
	 */
	public static String doRawQuery(String queryString, String endpoint, int timeout) throws SparqlException {
		log.debug("About to execute the following:");
		log.debug(" * endpoint: {}", endpoint);
		log.debug(" * query: {}", queryString.replaceAll("\\s+", " "));
		String encodedQuery;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		try {
			encodedQuery = URLEncoder.encode(queryString, "UTF-8");
			String url = endpoint + "?query=" + encodedQuery;
			log.trace(" ... Full request URI: {}", url);
			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("Accept", "application/sparql-results+json");
			HttpResponse response = httpClient.execute(getRequest);
			int stcode = response.getStatusLine().getStatusCode();
			if (200 == stcode) {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output;
				String result = "";
				while ((output = br.readLine()) != null)
					result += output;
				log.trace("Raw query result follows:");
				log.trace("{}", result);
				return result;
			} else {
				String reason = response.getStatusLine().getReasonPhrase();
				throw new SparqlException(reason, queryString, endpoint);
			}
		} catch (IOException e) {
			throw new SparqlException(e, queryString, endpoint);
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				log.warn("Failed to close HTTP client after sending query."
						+ " This is generally recoverable but you may want to check why it happened.", e);
			}
		}
	}

	public static List<QuerySolution> extractProjectedValues(String rawJson, List<Var> vars) {
		List<QuerySolution> filtered = new ArrayList<>();
		for (ResultSet parsed = ResultSetFactory.fromJSON(new ByteArrayInputStream(rawJson.getBytes())); parsed
				.hasNext();) {
			QuerySolution qs = parsed.next();
			for (Var v : vars) // One bound variable is enough
				if (qs.contains(v.getName())) {
					filtered.add(qs);
					break;
				}
		}
		return filtered;
	}

	public static String[][] extractSelectValuePairs(String sparqlResultJson, String var1, String var2) {
		String[][] output;
		JsonArray results = JSON.parse(sparqlResultJson).get("results").getAsObject().get("bindings").getAsArray();
		output = new String[results.size()][2];
		for (int i = 0; i < results.size(); i++) {
			JsonObject bind = results.get(i).getAsObject();
			if (bind.hasKey(var1)) {
				output[i][0] = bind.get(var1).getAsObject().get("value").getAsString().value();
				output[i][1] = bind.hasKey(var2) ? bind.get(var2).getAsObject().get("value").getAsString().value()
						: null;
			}
		}
		return output;
	}

	public static List<String> extractSelectVariableValues(String sparqlResultJson, String variable) {
		return extractSelectVariableValues(sparqlResultJson, variable, false);
	}

	/**
	 * Extract a list of values from a SPARQL SELECT result string in JSON and a
	 * given variable name that appears in the projection (SELECT clause).
	 * 
	 * @param sparqlResultJson
	 *            A string that is expected to be in sparql-results+json
	 * @param variable
	 *            the name of the variable to extract bindings from.
	 * @return
	 */
	public static List<String> extractSelectVariableValues(String sparqlResultJson, String variable,
			boolean mustBeUris) {
		List<String> output = new ArrayList<>();
		JsonArray results = JSON.parse(sparqlResultJson).get("results").getAsObject().get("bindings").getAsArray();
		for (JsonValue res : results) {
			JsonObject bind = res.getAsObject().get(variable).getAsObject();
			String value = bind.get("value").getAsString().value();
			try {
				if (mustBeUris) new URI(value); // To test the syntax
				output.add(value);
			} catch (URISyntaxException ex) {
				log.error("Bad URI synax for string '{}'", value);
			}
		}
		// Make unique list using Java 8 Stream API
		return output.stream().distinct().collect(Collectors.toList());
	}

	public static boolean isValidUri(String uri) {
		try {
			IRIFactory.iriImplementation().create(uri);// = IRIResolver(uri);
			return true;
		} catch (Exception e1) {
			return false;
		}
	}

}
