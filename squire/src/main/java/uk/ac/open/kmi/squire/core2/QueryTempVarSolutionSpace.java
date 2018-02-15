/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.VarNameVarValuePair;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;

/**
 *
 * @author carloallocca
 *
 *         This class transfor a given query q with template variable into
 *         another query q' whose project variables are the template variables
 *         Ex: q = SELECT DISTINCT ?mod ?title ?code WHERE { ?mod rdf:type ?ct1
 *         ; <http://purl.org/dc/terms/title> ?title ; ?dpt1 ?code } into
 *
 *         q' = SELECT DISTINCT ?ct1 ?dpt1 WHERE { ?mod rdf:type ?ct1 ;
 *         <http://purl.org/dc/terms/title> ?title ; ?dpt1 ?code }
 *
 */
public class QueryTempVarSolutionSpace {

	private static Set<Var> getQueryTemplateVariableSet(Query qR) {
		SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getQueryTemplateVariableSet();

	}

	private static Query rewriteQueryWithTemplateVar(Query qR) {
		Set<Var> templateVarSet = getQueryTemplateVariableSet(qR);
		Element elem = qR.getQueryPattern();
		Query qT = QueryFactory.make();
		qT.setDistinct(true);
		qT.setQueryPattern(elem);
		qT.setQuerySelectType();
		for (Var tv : templateVarSet) {
			qT.addResultVar(tv.getName());
		}
		return qT;
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	public List<QuerySolution> computeTempVarSolutionSpace(Query qChild, IRDFDataset rdfd2) {
		// 0. Check if the input query has aany template variable, otherwise qTsol is
		// empty
		Set<Var> templateVarSet = getQueryTemplateVariableSet(qChild);
		if (templateVarSet.size() > 0) {
			try {
				// 1. Transform the the query qChild into a query containg the template variable
				// only.
				Query qT = rewriteQueryWithTemplateVar(qChild);
				// 2. Compute the QuerySolution for qT;

				// qT.setLimit(1000);
				// log.info("[QueryTempVarSolutionSpace::compute]qT.setLimit(1000); " +qT);
				List<QuerySolution> qTsol = computeSolutionSpace(qT, rdfd2);
				log.info("[QueryTempVarSolutionSpace::compute]qTsol size; " + qTsol.size());
				return qTsol;
			} catch (ConnectException ex) {
				log.error("Connection failed while checking solution space.", ex);
				log.error("Returning empty solution space.");
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
	}

	private List<QuerySolution> computeSolutionSpace(Query q, IRDFDataset rdfd2) throws ConnectException {
		log.info("This is the subquery: " + q.toString());
		int timeout = 30;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		try {
			String encodedQuery = URLEncoder.encode(q.toString(), "UTF-8");
			String GET_URL = rdfd2.getEndPointURL() + "?query=" + encodedQuery;
			log.trace(" * Full request URI: {}", GET_URL);
			HttpGet getRequest = new HttpGet(GET_URL);
			getRequest.addHeader("accept", "application/sparql-results+json");
			HttpResponse response = client.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200)
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			String result = "";
			while ((output = br.readLine()) != null)
				result = result + output;
			log.debug("Raw query result follows:");
			log.debug("{}", result);
			List<QuerySolution> resultList = asJenaQuerySolutions(result, q.getProjectVars());
			// log.info("This is the solution space query: " + q.toString());
			log.info("solution space result size " + resultList.size());
			return resultList;
		} catch (ClientProtocolException e) {
			throw new ConnectException("Caused by org.apache.http.client.ClientProtocolException : " + e.getMessage());
		} catch (IOException e) {
			throw new ConnectException("Caused by java.io.IOException : " + e.getMessage());
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				log.warn("Failed to close HTTP client. This isn't usually fatal but you may want to check why.", e);
			}
		}
	}
	// based on jena
	// private List<QuerySolution> computeSolutionSpace(Query q, IRDFDataset rdfd2)
	// throws java.net.ConnectException {
	// try{
	// QueryExecution qexec = new QueryEngineHTTP((String) rdfd2.getEndPointURL(),
	// q);
	// ResultSet results = qexec.execSelect();
	//
	// List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(,
	// results, q);
	// log.info("[computeSolutionSpace]solList " +solList.size());
	//
	// return solList;
	// }catch(Exception ex){
	// throw ex;
	// }
	// //return new ArrayList<>();
	// }

	private boolean isValidateURI(String cleanedVarValue) {
		final IRIFactory u = IRIFactory.iriImplementation();
		try {
			u.create(cleanedVarValue);// = IRIResolver(cleanedVarValue);
			return true;
		} catch (Exception e1) {
			return false;
		}

	}

	private void printQuerySolutionSpaceMap(Map<Var, Set<RDFNode>> tmpMap) {

		System.out.println("[QueryTempVarSolutionSpace::printQuerySolutionSpaceMap]");

		if (tmpMap != null) {
			Iterator<Map.Entry<Var, Set<RDFNode>>> iter = tmpMap.entrySet().iterator();

			while (iter.hasNext()) {
				Map.Entry<Var, Set<RDFNode>> entry = iter.next();
				System.out.println("Var= " + entry.getKey().asNode().getName());
				Set<RDFNode> valuList = entry.getValue();
				for (RDFNode value : valuList) {
					System.out.println("Value= " + value.toString());
				}
			}

		}

	}

	private Map<Var, Set<RDFNode>> tranformToMap(List<QuerySolution> qTsol, Query qChild) {

		if (qTsol != null && qTsol.size() > 0) {

			// System.out.println("[QuerySpecializer::printQuerySolutionSpace] There are " +
			// qTsolList.size() + " solutions");
			Map<Var, Set<RDFNode>> map = new HashMap<>();

			// Set<Var> tempVarSet = qRScoreMaxNode.getqRTemplateVariableSet();
			Set<Var> tempVarSet = getQueryTemplateVariableSet(qChild);

			for (Var vt : tempVarSet) {
				Set<RDFNode> valueList = new HashSet<>();
				// System.out.println("[QuerySpecializer::printQuerySolutionSpace] " +
				// vt.getName() + "=" + sol.get(vt.getName()).toString());
				for (QuerySolution sol : qTsol) {

					valueList.add(sol.get(vt.getName()));
				}
				map.put(vt, valueList);
			}
			return map;
		}
		return new HashMap<>();
	}

	private List<QuerySolution> asJenaQuerySolutions(String resultString, List<Var> projected) {

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
			ArrayList<VarNameVarValuePair> varNameVarValuePairList = new ArrayList<>();

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
				if (varNameAndvarValueParts.length < 2) {
					continue;
				}

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
				} else {
					extractedVarName = varNamePart;
				}
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

					if (isValidateURI(cleanedVarValue)) {
						final URI uri = URI.create(cleanedVarValue);
						RDFNode rdfNode = new ResourceImpl(cleanedVarValue);
						qs.add(cleanedVarName, rdfNode);
					}

				}
				if (varNameVarValuePairList.size() == projected.size()) {
					output.add(qs);
				}

			} catch (Exception e1) {
				log.info(e1.getMessage());
			}
		}
		return output;
	}

}
