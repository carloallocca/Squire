/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.AbstractQueryRecommendationObservable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.SparqlUtils.SparqlException;

/**
 *
 * @author callocca
 */
public class SPARQLQuerySatisfiable extends AbstractQueryRecommendationObservable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public SPARQLQuerySatisfiable() {
	}

	public SPARQLQuerySatisfiable(String token) {
		this();
		this.token = token;
	}

	public boolean isSatisfiable(Query q, IRDFDataset d) {
		String datasetPath = (String) d.getEndPointURL();
		Query qTMP = QueryFactory.create(q.toString());
		qTMP.setLimit(2);

		if (datasetPath == null || datasetPath.isEmpty()) return false;

		// TO ADD: check if it is an instance of FileBasedRDFDataset or SPARQLEndPoint
		if (d instanceof SparqlIndexedDataset) {
			List<QuerySolution> resList;
			// QueryExecution qexec = new QueryEngineHTTP((String) d.getEndPointURL(), q);
			// ResultSet results = qexec.execSelect();
			// resList = ResultSetFormatter.toList(results); //.out(, results, q);
			// return resList.size() >= 1;

			try {
				QueryExecution qexec = QueryExecutionFactory.sparqlService(datasetPath, qTMP, (String) d.getGraph());
				ResultSet results = qexec.execSelect();

				resList = ResultSetFormatter.toList(results); // .out(, results, q);
				return resList.size() >= 1;
			} catch (Exception ex) {
				log.error("", ex);
				return false;
			}

			// return resList.size() >= 1;
		} else // What is d then?

			try {

			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
			// ...import the content of the owl file in the Jena model.
			m.read(new FileInputStream(datasetPath), "");

			// ...querying ...
			QueryExecution qexec = QueryExecutionFactory.create(qTMP, m);
			if (qTMP.isSelectType()) {
			ResultSet results = qexec.execSelect();

			// Output query results
			List<QuerySolution> resList = ResultSetFormatter.toList(results);// .out(, results, q);
			qexec.close();
			return resList.size() >= 1;
			}
			return false;
			} catch (FileNotFoundException ex) {
			log.error("", ex);
			} finally {
			// try {
			// in.close();
			// } catch (IOException ex) {
			// Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE,
			// null, ex);
			// }
			}
		return false;
	}

	public boolean isSatisfiableWRTProjectVar(Query qRec) {

		List<String> qOvarList = computeQueryVariableSet(qRec);
		// System.out.println("[SPARQLQuerySatisfiable::isSatisfiableWRTProjectVar] 1 "
		// + qOvarList.toString());

		List<Var> projectVarList = qRec.getProjectVars();
		List<String> projectVarListString = new ArrayList<>();

		for (Var varProj : projectVarList) {
			projectVarListString.add(varProj.getVarName());
		}
		// System.out.println("[SPARQLQuerySatisfiable::isSatisfiableWRTProjectVar] 2 "
		// + projectVarListString.toString());
		return qOvarList.containsAll(projectVarListString);
	}

	public boolean isSatisfiableWrtResults(Query q, IRDFDataset rdfd2) throws java.net.ConnectException {
		String datasetPath = (String) rdfd2.getEndPointURL();
		if (datasetPath == null || datasetPath.isEmpty()) {
			this.notifyQuerySatisfiableValue(q, false);
			return false;
		}
		// Clone as another query with LIMIT 1
		Query qTMP = QueryFactory.create(q.toString());
		qTMP.setLimit(1); // XXX perhaps we could turn it into an ASK
		try {
			String raw = SparqlUtils.doRawQuery(qTMP.toString(), datasetPath);
			List<QuerySolution> resultList = SparqlUtils.extractProjectedValues(raw, q.getProjectVars());
			boolean cond = resultList.size() > 0;
			log.info("Satisfiable? {}", cond ? "YES" : "NO");
			notifyQuerySatisfiableValue(q, cond);
			return cond;
		} catch (SparqlException e) {
			log.warn("Satisfiability query failed. Reason follows.", e);
			return false;
		}
	}

	private List<String> computeQueryVariableSet(Query qO) {
		SQVariableVisitor v = new SQVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qO.getQueryPattern(), v);
		// System.out.println("[QuerySpecificityDistance::computeQueryVariableSet]
		// v.getQueryClassSet() " + v.getQueryClassSet().toString());
		return v.getQueryVariableSet();
	}

}
