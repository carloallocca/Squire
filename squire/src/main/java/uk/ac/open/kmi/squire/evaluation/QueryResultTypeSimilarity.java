/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.SparqlUtils.SparqlException;

/**
 *
 * @author carloallocca
 */
public class QueryResultTypeSimilarity {

	private static final String TYPE_OF_URI = "uri";
	private final Logger log = LoggerFactory.getLogger(getClass());

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

	private List<VarTypeMap> computeFileBasedQVS(Query qOri, IRDFDataset d1) {
		List<VarTypeMap> signature = new ArrayList<>();
		try {
			OntModel inf = ModelFactory.createOntologyModel();
			inf.read(new FileInputStream((String) d1.getEndPointURL()), "");
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
			log.error("{}", ex);
		}
		return signature;
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
		if (!qOri.isSelectType())
			throw new UnsupportedOperationException("Only SELECT queries are supported at the moment.");
		List<VarTypeMap> signature = new ArrayList<>();
		// Clone as a query with LIMIT 1 (XXX transform to ASK?)
		Query qTMP = QueryFactory.create(qOri.toString(), Syntax.syntaxSPARQL_11);
		qTMP.setLimit(1);
		List<QuerySolution> resList;
		try {
			String raw = SparqlUtils.doRawQuery(qTMP.toString(), d1.getEndPointURL().toString());
			resList = SparqlUtils.extractProjectedValues(raw, qTMP.getProjectVars());
			log.debug(" ... solution space result size = {}", resList.size());
		} catch (SparqlException e) {
			log.error("SPARQL query for solution space failed. Reason follows.", e);
			log.error("Assuming empty solution space.");
			return signature;
		}

		if (resList.size() > 0) {
			QuerySolution firstSol = resList.get(0);
			for (Iterator<String> it = firstSol.varNames(); it.hasNext();) {
				final String varName = it.next();
				final String varType;
				RDFNode varValue = firstSol.get(varName);
				if (varValue.isURIResource()) varType = TYPE_OF_URI;
				else if (varValue.isLiteral()) {
					RDFDatatype literalValue = varValue.asLiteral().getDatatype();
					varType = literalValue.getURI();
				} else varType = "";
				VarTypeMap vtm = new VarTypeMap(varName, varType);
				signature.add(vtm);
			}
		}

		return signature;
	}

	private int computeUnionCardinality(List<VarTypeMap> qOri_d1_signature, List<VarTypeMap> qRec_d2_signature,
			int intersection) {
		return (qOri_d1_signature.size() + qRec_d2_signature.size()) - intersection;
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

}
