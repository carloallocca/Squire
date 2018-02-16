package uk.ac.open.kmi.squire.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.VarNameVarValuePair;

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

	/**
	 * A remain from the legacy code we should get rid of.
	 * 
	 * @param resultString
	 * @param projected
	 * @return
	 */
	public static List<QuerySolution> asJenaQuerySolutions(String resultString, List<Var> projected) {

		for (Var v : projected)
			log.info("var " + v.getName());

		// TO MANAGE:
		// [QueryTempVarSolutionSpace]: ::result{ "head": { "vars": [ "opt2" , "opt1" ]
		// } , "results": { "bindings": [ ] }}
		// [INFO ]
		log.info("::result" + resultString);
		ArrayList<QuerySolution> output = new ArrayList<>();
		String[] resBindings = resultString.split("bindings");

		try {
			// it could be " [ ] }}"
			log.info("empty bindings " + resBindings[1]);
		} catch (Exception ex) {
			log.info(ex.getMessage());
		}

		// this is true for the EDUCATION I
		// String[] solutionAsStrings = resBindings[1].split(" } } ,");
		String[] solutionAsStrings;
		if (resBindings[1].contains("]")) {
			solutionAsStrings = resBindings[1].substring(0, resBindings[1].indexOf("]") - 1).split("}\\s*}\\s*\\,?");
			// String[] solutionAsStrings = resBindings[1].split("}},");

		} else {
			solutionAsStrings = resBindings[1].substring(0, resBindings[1].length()).split("}\\s*}\\s*\\,?");
		}
		log.info("solutionAsStrings size " + solutionAsStrings.length);
		// we get up to -1 as the last one could be broken, and we don't want to process
		// it.
		for (int i = 0; i <= solutionAsStrings.length - 1; i++) {
			// for (int i = 0; i <= 4000; i++) {
			log.info(solutionAsStrings[i]);
			// This is working for EDUCATION I
			// String[] solValueArray = solutionAsStrings[i].split("} , ");
			String[] solValueArray = solutionAsStrings[i].split("}\\s*\\,?");

			// if (solValueArray.length == 1) {
			// log.info("solValueArray.length==0");
			// }
			List<VarNameVarValuePair> varNameVarValuePairList = new ArrayList<>();

			for (String solValueArray1 : solValueArray) {
				log.info(solValueArray1);
				// this is working with EDUCATION I
				// String[] varNameAndvarValueParts = solValueArray1.split("\"type\": \"uri\" ,
				// \"value\":");
				// String[] varNameAndvarValueParts =
				// solValueArray1.split("\"type\"\\s*:\\s*\"uri\"\\s*,\\s*\"value\\s*\":");
				String[] varNameAndvarValueParts = solValueArray1
						.split("\"type\"\\s*:\\s*\"uri\"\\s*,\\s*\"value\\s*\"\\s*:");
				// the regex is not for this case // "ct1": { "type": "typed-literal",
				// "datatype": "http://www.w3.org/2001/XMLSche
				if (varNameAndvarValueParts.length < 2) continue;

				// if(varNameAndvarValueParts.length==2){
				// log.info("::2" +varNameAndvarValueParts[0] +varNameAndvarValueParts[1]);
				// }
				// if(varNameAndvarValueParts.length==1){
				// log.info("::1" +varNameAndvarValueParts[0]);
				// }
				//
				// TODO: (DONE) i need to add the code to extract each part of the solution...
				String varNamePart = varNameAndvarValueParts[0];// .split(":")[0];
				String varValuePart = varNameAndvarValueParts[1];
				// log.info("varNamePart " +varNamePart.substring(varNamePart.indexOf("\"")+1,
				// varNamePart.lastIndexOf("\"")));
				String extractedVarName = "";
				if (varNamePart.contains("[")) {
					String[] varNameNew = varNamePart.split("\\[");
					extractedVarName = varNameNew[1];
				} else extractedVarName = varNamePart;

				String cleanedVarName = StringUtils.substringBetween(extractedVarName, "\"", "\"");
				String cleanedVarValue = StringUtils.substringBetween(varValuePart, "\"", "\"");

				VarNameVarValuePair newPairItem = new VarNameVarValuePair(cleanedVarName, cleanedVarValue);
				varNameVarValuePairList.add(newPairItem);
				log.info("varName " + cleanedVarName);
				log.info("varValue " + cleanedVarValue);
			}
			try {
				QuerySolutionMap qs = new QuerySolutionMap();
				for (VarNameVarValuePair v : varNameVarValuePairList) {
					String cleanedVarName = v.getVarName();
					String cleanedVarValue = v.getVarValue();
					if (isValidUri(cleanedVarValue)) qs.add(cleanedVarName, new ResourceImpl(cleanedVarValue));
				}
				if (varNameVarValuePairList.size() == projected.size()) output.add(qs);
			} catch (Exception e1) {
				log.warn("", e1);
			}
		}
		return output;
	}

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
		return output;
	}

	private static boolean isValidUri(String cleanedVarValue) {
		try {
			IRIFactory.iriImplementation().create(cleanedVarValue);// = IRIResolver(cleanedVarValue);
			return true;
		} catch (Exception e1) {
			return false;
		}
	}

}
