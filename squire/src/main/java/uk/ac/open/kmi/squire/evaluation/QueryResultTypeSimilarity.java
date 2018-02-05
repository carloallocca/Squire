/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.VarNameVarValuePair;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryResultTypeSimilarity {

	private static final String TYPE_OF_URI = "uri";
	private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

	public QueryResultTypeSimilarity() {
		super();

	}

	public float computeQueryResultTypeDistance(Query qOri, IRDFDataset d1, Query qRec, IRDFDataset d2) {
		float dist;
		List<VarTypeMap> qOri_d1_signature = computeQueryVariableSignature(qOri, d1);

		// log.info("[QueryResultTypeSimilarity::computeQueryResultTypeDistance]
		// qOri_d1_signature" +qOri_d1_signature.size());
		List<VarTypeMap> qRec_d2_signature = computeQueryVariableSignature(qRec, d2);

		// log.info("[QueryResultTypeSimilarity::computeQueryResultTypeDistance]
		// qRec_d2_signature" +qRec_d2_signature.size());
		dist = 1 - computeRTQOverlapRate(qOri_d1_signature, qRec_d2_signature);
		return dist;
	}

	private float computeRTQOverlapRate(List<VarTypeMap> qOri_d1_signature, List<VarTypeMap> qRec_d2_signature) {
		float overlapRate = 0;
		int cardSignatureQo = qOri_d1_signature.size();
		int cardSignatureQr = qRec_d2_signature.size();
		if (!(cardSignatureQo == 0) && !(cardSignatureQr == 0)) {
			int intersection = 0;
			for (VarTypeMap map : qOri_d1_signature) {
				if (contains(qRec_d2_signature, map)) {
					intersection++;
				}
			}
			// dist =(float) (1.0*(((1.0*qOvarList.size())/(1.0*qRvarList.size()))));
			int cardUnionSignature = computeUnionCardinality(qOri_d1_signature, qRec_d2_signature, intersection);
			overlapRate = (float) ((1.0 * intersection) / (1.0 * cardUnionSignature));
			return overlapRate;
		}
		return overlapRate;
	}

	private List<VarTypeMap> computeQueryVariableSignature(Query qOri, IRDFDataset d1) {
		// List<VarTypeMap> signature = new ArrayList<>();
		// if (d1 instanceof FileBasedRDFDataset) {
		// signature = computeFileBasedQVS(qOri, d1);
		// } else if (d1 instanceof SPARQLEndPoint) {
		// signature = computeSPARQLEndPointBasedQVS(qOri, d1);
		// return signature;
		// }
		// return signature;
		// return computeSPARQLEndPointBasedQVS(qOri, d1);
		return computeSPARQLEndPointBasedQVS1(qOri, d1);

	}

	private List<VarTypeMap> computeFileBasedQVS(Query qOri, IRDFDataset d1) {
		List<VarTypeMap> signature = new ArrayList<>();
		try {
			OntModel inf = ModelFactory.createOntologyModel();
			InputStream in = new FileInputStream((String) d1.getEndPointURL());
			if (in == null) {
				throw new IllegalArgumentException("File: " + (String) d1.getEndPointURL() + " not found");
			} // ...import the content of the owl file in the Jena model.
			inf.read(in, "");
			// ...querying ...
			// Query q = QueryFactory.create(qString);
			QueryExecution qexec = QueryExecutionFactory.create(qOri, inf);
			if (qOri.isSelectType()) {
				ResultSet results = qexec.execSelect();
				List<QuerySolution> resList = ResultSetFormatter.toList(results);// .out(, results, q);
				if (resList.size() > 0) {
					QuerySolution firstSol = resList.get(0);
					Iterator<String> varIter = firstSol.varNames();
					while (varIter.hasNext()) {
						final String varName = varIter.next();
						final String varType;
						RDFNode varValue = firstSol.get(varName);
						if (varValue.isURIResource()) {
							varType = TYPE_OF_URI;
						} else if (varValue.isLiteral()) {
							RDFDatatype literalValue = varValue.asLiteral().getDatatype();
							varType = literalValue.getURI();
						} else {
							varType = "";
						}
						VarTypeMap vtm = new VarTypeMap(varName, varType);
						signature.add(vtm);
					}
				}
				qexec.close();
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(QueryResultTypeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
		}
		return signature;
	}

	private List<VarTypeMap> computeSPARQLEndPointBasedQVS(Query qOri, IRDFDataset d1) {
		List<VarTypeMap> signature = new ArrayList<>();

		String endpoint = (String) d1.getEndPointURL();

		// QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri);
		// QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri,
		// (String)d1.getGraph());
		if (qOri.isSelectType()) {
			Query newqOri = QueryFactory.create(qOri.toString(), Syntax.syntaxSPARQL_11);
			newqOri.setLimit(1);
			// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS]
			// qTMP.setLimit(1)" +qTMP);
			try (QueryExecution qexec = new QueryEngineHTTP(endpoint, newqOri)) {
				// //log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] qOri"
				// +qOri);
				// try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,
				// qOri, (String)d1.getGraph());) {
				ResultSet results = qexec.execSelect();

				List<QuerySolution> resList = ResultSetFormatter.toList(results);// .out(, results, q);

				if (resList.size() > 0) {
					QuerySolution firstSol = resList.get(0);

					Iterator<String> varIter = firstSol.varNames();
					while (varIter.hasNext()) {
						final String varName = varIter.next();
						// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] varName
						// " +varName);

						final String varType;
						RDFNode varValue = firstSol.get(varName);

						// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] varValue
						// " +varValue);
						if (varValue.isURIResource()) {
							varType = TYPE_OF_URI;
						} else if (varValue.isLiteral()) {
							RDFDatatype literalValue = varValue.asLiteral().getDatatype();
							varType = literalValue.getURI();
							// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] varType
							// " +varType);
						} else {
							varType = "";
						}

						VarTypeMap vtm = new VarTypeMap(varName, varType);
						signature.add(vtm);
					}
					// // log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS]
					// signature " +signature.size());

					return signature;
				}
			} catch (RuntimeException ex) {
				return signature;
				// System.out.println(ex.getMessage());

			}
		}
		return signature;
	}

	private List<VarTypeMap> computeSPARQLEndPointBasedQVS1(Query qOri, IRDFDataset d1) {
		List<VarTypeMap> signature = new ArrayList<>();

		String endpoint = (String) d1.getEndPointURL();

		// QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri);
		// QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri,
		// (String)d1.getGraph());
		if (qOri.isSelectType()) {
			Query qTMP = QueryFactory.create(qOri.toString(), Syntax.syntaxSPARQL_11);
			qTMP.setLimit(1);
			ArrayList<QuerySolution> resList = null;
			try {
				String encodedQuery = URLEncoder.encode(qTMP.toString(), "UTF-8");
				String GET_URL = d1.getEndPointURL() + "?query=" + encodedQuery;
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet getRequest = new HttpGet(GET_URL);
				// getRequest.addHeader("accept", "application/sparql-results+json");
				getRequest.addHeader("accept", "application/sparql-results+json");

				HttpResponse response = httpClient.execute(getRequest);
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException(
							"Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output;
				String result = "";
				while ((output = br.readLine()) != null) {
					result = result + output;
				}
				httpClient.getConnectionManager().shutdown();
				log.info("solution space result " + result);

				resList = writeQueryResultsAsJenaQuerySolution(result, qTMP.getProjectVars());
				// log.info("This is the solution space query: " + q.toString());
				log.info("solution space result size " + resList.size());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (resList != null) {
				if (resList.size() > 0) {
					QuerySolution firstSol = resList.get(0);

					Iterator<String> varIter = firstSol.varNames();
					while (varIter.hasNext()) {
						final String varName = varIter.next();
						// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] varName
						// " +varName);

						final String varType;
						RDFNode varValue = firstSol.get(varName);

						// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] varValue
						// " +varValue);
						if (varValue.isURIResource()) {
							varType = TYPE_OF_URI;
						} else if (varValue.isLiteral()) {
							RDFDatatype literalValue = varValue.asLiteral().getDatatype();
							varType = literalValue.getURI();
							// log.info("[QueryResultTypeSimilarity::computeSPARQLEndPointBasedQVS] varType
							// " +varType);
						} else {
							varType = "";
						}
						VarTypeMap vtm = new VarTypeMap(varName, varType);
						signature.add(vtm);
					}
					return signature;
				}
			}
		}
		return signature;
	}

	// private ArrayList<QuerySolution> writeQueryResultsAsJenaQuerySolution(String
	// result, List<Var> projectVars) {
	// ArrayList<QuerySolution> output = new ArrayList<>();
	// String[] resBindings = result.split("bindings");
	//
	// String[] solutionAsStrings = resBindings[1]
	// .substring(0, resBindings[1].indexOf("]") - 1)
	// .split("}\\s*}\\s*\\,?");
	// for (int i = 0; i <= solutionAsStrings.length - 1; i++) {
	// String[] solValueArray = solutionAsStrings[i].split("}\\s*\\,?");
	//
	// ArrayList<VarNameVarValuePair> varNameVarValuePairList = new ArrayList<>();
	//
	// for (String solValueArray1 : solValueArray) {
	//
	// String[] varNameAndvarValueParts =
	// solValueArray1.split("\"type\"\\s*:\\s*\"uri\"\\s*,\\s*\"value\\s*\"\\s*:");
	//
	// if (varNameAndvarValueParts.length < 2) {
	// log.info("this one 1:: " + solValueArray1);
	//
	// varNameAndvarValueParts =
	// solValueArray1.split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"value\\s*\"\\s*:");
	//
	// }
	// if (varNameAndvarValueParts.length < 2) {
	// log.info("this one 2:: " + solValueArray1);
	// varNameAndvarValueParts = solValueArray1
	// .split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"\\w*:\\s*\\w*\"\\s*:\\s*\"\\w*\"\\s*,\\s*\"value\\s*\"\\s*:");
	//
	// }
	// if (varNameAndvarValueParts.length < 2) {
	//
	// log.info("this one 3:: " + solValueArray1);
	// varNameAndvarValueParts = solValueArray1
	// .split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"\\w*\":\\s*\"(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\"\\s*,\\s*\"value\\s*\"\\s*:");
	//
	// }
	// if (varNameAndvarValueParts.length < 2) {
	// log.info("this one 4:: " + solValueArray1);
	//
	// varNameAndvarValueParts =
	// solValueArray1.split("\"type\"\\s*:\\s*\"bnode\"\\s*,\\s*\"value\\s*\"\\s*:");
	//
	// }
	// if (varNameAndvarValueParts.length < 2) {
	// log.info("this one 5:: " + solValueArray1);
	// varNameAndvarValueParts = solValueArray1
	// .split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"\\w*:\\s*\\w*\"\\s*:\\s*\"\\w*-\\w*\"\\s*,\\s*\"value\\s*\"\\s*:");
	//
	// }
	//
	//// if(varNameAndvarValueParts.length==2){
	//// log.info("varNameAndvarValueParts ::[0] " +varNameAndvarValueParts[0] );
	//// log.info("varNameAndvarValueParts ::[1] " +varNameAndvarValueParts[1] );
	//// }
	//// if(varNameAndvarValueParts.length==1){
	//// log.info("varNameAndvarValueParts ::[0] " +varNameAndvarValueParts[0] );
	////
	//// }
	// //TODO: (DONE) i need to add the code to extract each part of the solution...
	// String varNamePart = varNameAndvarValueParts[0];//.split(":")[0];
	// String varValuePart = varNameAndvarValueParts[1];
	// //log.info("varNamePart " +varNamePart.substring(varNamePart.indexOf("\"")+1,
	// varNamePart.lastIndexOf("\"")));
	// String extractedVarName = "";
	// if (varNamePart.contains("[")) {
	// String[] varNameNew = varNamePart.split("\\[");
	// extractedVarName = varNameNew[1];
	// } else {
	// extractedVarName = varNamePart;
	// }
	// String cleanedVarName = StringUtils.substringBetween(extractedVarName, "\"",
	// "\"");
	// String cleanedVarValue = StringUtils.substringBetween(varValuePart, "\"",
	// "\"");
	//
	// VarNameVarValuePair newPairItem = new VarNameVarValuePair(cleanedVarName,
	// cleanedVarValue);
	// varNameVarValuePairList.add(newPairItem);
	// }
	// try {
	// QuerySolutionMap qs = new QuerySolutionMap();
	// for (VarNameVarValuePair v : varNameVarValuePairList) {
	// String cleanedVarName = v.getVarName();
	// String cleanedVarValue = v.getVarValue();
	//
	// // if(isValidateURI(cleanedVarValue)){
	//// final URI uri = URI.create(cleanedVarValue);
	// RDFNode rdfNode = new ResourceImpl(cleanedVarValue);
	// qs.add(cleanedVarName, rdfNode);
	//// }
	//
	// }
	// if (varNameVarValuePairList.size() == projectVars.size()) {
	// output.add(qs);
	// }
	//
	// } catch (Exception e1) {
	// log.info(e1.getMessage());
	// }
	// }
	// return output;
	// }

	private ArrayList<QuerySolution> writeQueryResultsAsJenaQuerySolution(String result, List<Var> projectVars) {

		ArrayList<QuerySolution> output = new ArrayList<>();
		String[] resBindings = result.split("bindings");

		// { "bindings
		// 1: ": [ ] }}
		String[] solutionAsStrings = new String[0];

		String regEx = "\\[\\s*(.*)\\s*\\]";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(resBindings[1]);
		if (matcher.find()) {
			log.info(matcher.group(1));
			solutionAsStrings = resBindings[1].substring(0, resBindings[1].indexOf("]") - 1).split("}\\s*}\\s*\\,?");
		} else {
			return output;
		}

		for (int i = 0; i <= solutionAsStrings.length - 1; i++) {
			String[] solValueArray = solutionAsStrings[i].split("}\\s*\\,?");

			ArrayList<VarNameVarValuePair> varNameVarValuePairList = new ArrayList<>();

			for (String solValueArray1 : solValueArray) {
				log.info("solution:: " + solValueArray1);
				// String[] varNameAndvarValueParts =
				// solValueArray1.split("\"type\"\\s*:\\s*\"uri\"\\s*,\\s*\"value\\s*\"\\s*:");

				String cleanedVarName = "", cleanedVarValue = "";
				log.info("this one 6:: " + solValueArray1);

				String regEx1 = "\"(\\w+)\"\\s*:\\s*\\{.*\"value\"\\s*:\\s*\"(.*)\"";
				Pattern pattern1 = Pattern.compile(regEx1);
				Matcher matcher1 = pattern1.matcher(solValueArray1);

				while (matcher1.find()) {
					log.info("Variable name : {}", matcher1.group(1));
					cleanedVarName = matcher1.group(1);
					log.info("Variable value : {}", matcher1.group(2));
					cleanedVarValue = matcher1.group(2);
				}
				log.info("cleanedVarName : " + cleanedVarName);
				log.info("cleanedVarValue : " + cleanedVarValue);
				if (!cleanedVarName.isEmpty() && !cleanedVarValue.isEmpty()) {
					VarNameVarValuePair newPairItem = new VarNameVarValuePair(cleanedVarName, cleanedVarValue);
					varNameVarValuePairList.add(newPairItem);

				}
			}
			try {
				QuerySolutionMap qs = new QuerySolutionMap();
				for (VarNameVarValuePair v : varNameVarValuePairList) {
					String cleanedVarName = v.getVarName();
					String cleanedVarValue = v.getVarValue();
					RDFNode rdfNode = new ResourceImpl(cleanedVarValue);
					qs.add(cleanedVarName, rdfNode);
				}
				if (varNameVarValuePairList.size() == projectVars.size()) {
					output.add(qs);
				}

			} catch (Exception e1) {
				log.info(e1.getMessage());
			}
		}
		return output;
	}

	private void print(List<VarTypeMap> qOri_d1_signature) {
		for (VarTypeMap map : qOri_d1_signature) {
			System.out.println("[ResultTypeQuerySimilarity::print] varName " + map.getVarName());
			System.out.println("[ResultTypeQuerySimilarity::print] varType " + map.getVarType());
		}
	}

	private boolean contains(List<VarTypeMap> qRec_d2_signature, VarTypeMap map) {
		// boolean found=false;
		String varName = map.getVarName();
		String varType = map.getVarType();
		for (VarTypeMap map1 : qRec_d2_signature) {
			String varName1 = map1.getVarName();
			String varType1 = map1.getVarType();
			// if (!varType.equals("") && !varType1.equals("")) {
			if (varName.equals(varName1) && varType.equals(varType1)) {
				return true;
			}
			// }
		}
		return false;
	}

	private int computeUnionCardinality(List<VarTypeMap> qOri_d1_signature, List<VarTypeMap> qRec_d2_signature,
			int intersection) {
		return (qOri_d1_signature.size() + qRec_d2_signature.size()) - intersection;
	}

}
