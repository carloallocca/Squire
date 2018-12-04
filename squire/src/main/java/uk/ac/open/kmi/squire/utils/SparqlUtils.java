package uk.ac.open.kmi.squire.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonParseException;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utility methods for executing SPARQL queries or for
 * manipulating them or their results.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
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

	public static class SparqlResultException extends Exception {

		private static final long serialVersionUID = -7102834742711938343L;

		private String responseBody;

		public SparqlResultException(String message, String responseBody) {
			super(message);
			this.responseBody = responseBody;
		}

		public SparqlResultException(Throwable cause, String responseBody) {
			super(cause);
			this.responseBody = responseBody;
		}

		public String getResponseBody() {
			return responseBody;
		}

	}

	private static Logger log = LoggerFactory.getLogger(SparqlUtils.class);

	/**
	 * Equivalent to calling {@link SparqlUtils#doRawQuery(String, String, int)}
	 * with a timeout of two minutes.
	 * 
	 * @param queryString
	 * @param endpoint
	 * @return
	 * @throws SparqlException
	 */
	public static String doRawQuery(String queryString, String endpoint) throws SparqlException {
		return doRawQuery(queryString, endpoint, 120);
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
		if (timeout < 1)
			throw new IllegalArgumentException(
					"Sorry, waiting forever is disallowed. Timeout must be a positive integer.");
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

	public static String[][] extractSelectValuePairs(String sparqlResultJson, String var1, String var2)
			throws SparqlException {
		final String[][] output;
		JsonObject ob;
		try {
			ob = JSON.parse(sparqlResultJson);
		} catch (JsonParseException ex) {
			log.warn("Error parsing JSON object.", ex);
			log.warn("Dumping content below:\r\n{}", sparqlResultJson);
			throw new RuntimeException(ex);
		}
		if (ob == null) {
			log.warn("Parsing returned null JSON object.");
			return new String[0][0];
		}
		JsonArray results = ob.get("results").getAsObject().get("bindings").getAsArray();
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

	public static List<String> extractSelectVariableValues(String sparqlResultJson, String variable)
			throws SparqlResultException {
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
	public static List<String> extractSelectVariableValues(String sparqlResultJson, String variable, boolean mustBeUris)
			throws SparqlResultException {
		List<String> output = new ArrayList<>();
		JsonObject jResp = JSON.parse(sparqlResultJson);
		if (!jResp.hasKey("results"))
			throw new SparqlResultException(
					"Key 'results' missing from reponse body expected to be in application/sparql-results+json",
					sparqlResultJson);
		JsonArray results = jResp.get("results").getAsObject().get("bindings").getAsArray();
		for (JsonValue res : results) {
			JsonObject bind = res.getAsObject().get(variable).getAsObject();
			String value = bind.get("value").getAsString().value();
			try {
				if (mustBeUris)
					new URI(value); // To test the syntax
				output.add(value);
			} catch (URISyntaxException ex) {
				log.error("Bad URI synax for string '{}'", value);
			}
		}
		// Make unique list using Java 8 Stream API
		return output.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * * A utility method to expand a SPARQL SELECT result set with the values of
	 * variables mapped to existing ones. This is useful if, for example, the query
	 * was reduced before sending it to an endpoint. In that case, this function
	 * will rebuild the solution space as the cross product between the reduced
	 * variables and the "surviving" ones (i.e. either not reduced at all or
	 * retained by the reduction).
	 * 
	 * @param solution
	 *            the original result set
	 * @param reducedVars
	 *            a mapping of retained variables to set of reduced ones
	 * @return the reconstructed result set
	 */
	public static List<QuerySolution> inflateResultSet(List<QuerySolution> solution, Map<Var, Set<Var>> reducedVars) {
		log.debug("Inflating a result set of size {}", solution.size());
		log.debug("Reduction map follows:");
		log.debug("{}", reducedVars);

		final List<QuerySolution> inflated = new ArrayList<>();

		// Example : { p1: "A" } -> [ { y1: "X" } , { y1: "Y" } ]
		// (assuming y2 is reduced into y1)
		// Cannot directly use QuerySolution because equivalence does not seem to be
		// implemented for that class. That sucks but what can we do.
		Map<Map<String, RDFNode>, Set<Map<String, RDFNode>>> fixed2kept = new HashMap<>();

		// Run a full scan to populate the above
		for (QuerySolution sol : solution) {
			QuerySolutionMap fixedSlice = new QuerySolutionMap();
			QuerySolutionMap keptSlice = new QuerySolutionMap();
			for (Iterator<String> it = sol.varNames(); it.hasNext();) {
				String v = it.next();
				if (reducedVars.containsKey(Var.alloc(v)))
					keptSlice.add(v, sol.get(v));
				else
					fixedSlice.add(v, sol.get(v));
			}
			if (!fixed2kept.containsKey(fixedSlice.asMap()))
				fixed2kept.put(fixedSlice.asMap(), new HashSet<>());
			fixed2kept.get(fixedSlice.asMap()).add(keptSlice.asMap());
		}

		for (Map<String, RDFNode> fixed : fixed2kept.keySet()) {
			log.trace("Fixed solution: {}", fixed);
			// Expand the other part of each reduced solution
			for (Map<String, RDFNode> kepts : fixed2kept.get(fixed)) {
				QuerySolutionMap solNu = new QuerySolutionMap();
				// Add the part of the solution that is not reduced
				for (Entry<String, RDFNode> entry : fixed.entrySet()) {
					solNu.add(entry.getKey(), entry.getValue());
					log.trace(" ..... added: {} - {}", entry.getKey(), entry.getValue());
				}
				log.trace(" ... kept: {}", kepts);
				if (kepts.isEmpty())
					inflated.add(solNu);
				else
					// Process every "kept" variable from the expandable part
					for (Entry<String, RDFNode> entry : kepts.entrySet()) {
						// First add the kept value
						solNu.add(entry.getKey(), entry.getValue());
						log.trace(" ..... added: {} - {}", entry.getKey(), entry.getValue());
						// Then iteratively expand every reduced variable over the kept one
						inflateSolution(solNu, reducedVars, fixed2kept, fixed, inflated);
					}
			}
		}
		log.debug("DONE. Inflated to size {}", inflated.size());
		return inflated;
	}

	public static boolean isValidUri(String uri) {
		try {
			IRIFactory.iriImplementation().create(uri); // = IRIResolver(uri);
			return true;
		} catch (Exception e1) {
			return false;
		}
	}

	/**
	 * The recursive call of the {@link #inflateResultSet(List, Map)} function,
	 * operating on a single solution.
	 * 
	 * @param solution
	 *            the (possibly partial) SPARQL SELECT solution being inflated
	 * @param kept
	 *            the variable (retained, i.e. not reduced) to be inspected for
	 *            reductions
	 * @param reduced
	 *            the variable (reduced to kept) being inspected: determines the
	 *            recursive step
	 * @param reducedVars
	 *            the mapping from kept variables to sets of reduced variables, used
	 *            to call further recursions and check when the process is complete
	 * @param expansionPlan
	 *            this is actually the entire result set, but split into a mapping
	 *            from "fixed" parts of solutions (i.e. neither kept not reduced) to
	 *            the kept ones
	 * @param fixedPart
	 *            the key of the expansionPlan being inspected
	 * @param result
	 *            the list of {@link QuerySolution}s where the computation is being
	 *            written to
	 */
	private static void inflateSolution(QuerySolutionMap solution, Map<Var, Set<Var>> reducedVars,
			Map<Map<String, RDFNode>, Set<Map<String, RDFNode>>> expansionPlan, Map<String, RDFNode> fixedPart,
			final List<QuerySolution> result) {
		log.trace("Solution:");
		log.trace("{}", solution);

		// If the solution can no longer be expanded, add it to the final list...
		boolean complete = true;
		for (Entry<Var, Set<Var>> entry : reducedVars.entrySet()) {
			if (!solution.contains(entry.getKey().getName())) {
				complete = false;
				break;
			}
			for (Var v : entry.getValue()) {
				if (!solution.contains(v.getName())) {
					complete = false;
					break;
				}
			}
		}
		if (complete) {
			log.trace("... is complete, so adding to result set.");
			result.add(solution);
			return;
		}
		// TODO can we reduce this O(n^4) complexity, even though n is small for most?
		// Iterate over the (pre-processed) variable-value bindings of the result set.
		for (Map<String, RDFNode> keptBind : expansionPlan.get(fixedPart))
			for (Entry<String, RDFNode> entry : keptBind.entrySet()) {
				String var = entry.getKey();
				// Add the retained binding if not present.
				// Iterate over e.g. { y1:X } , { y2:Y }
				log.trace(" ... processing binding: {} - {}", var, entry.getValue());
				if (!solution.contains(var))
					solution.add(var, entry.getValue());
				// Recursively expand the solution by re-adding the value for the variables that
				// were reduced into the kept one.
				for (Var kept : reducedVars.keySet()) {
					log.trace("... - kept variable : {}", kept);
					for (Var reduced : reducedVars.get(kept)) {
						// Each reduced variable generates a new (cloned) solution, so recurse into it
						log.trace("... - reduced variable : {}", reduced);
						if (solution.contains(kept.getName()) && !solution.contains(reduced.getName())
								&& reducedVars.get(kept).contains(reduced)) {
							log.trace(" ... performing expansion ...");
							QuerySolutionMap newSol = new QuerySolutionMap();
							newSol.addAll(solution);
							newSol.add(reduced.getName(), entry.getValue());
							log.trace(" ..... added: {} - {}", reduced.getName(), entry.getValue());
							inflateSolution(newSol, reducedVars, expansionPlan, fixedPart, result);
						}
					}
				}

			}

	}

}
