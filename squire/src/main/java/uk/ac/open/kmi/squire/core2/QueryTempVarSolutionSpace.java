/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.SparqlUtils.SparqlException;

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

	private final Logger log = LoggerFactory.getLogger(getClass());

	public List<QuerySolution> computeTempVarSolutionSpace(Query qChild, IRDFDataset rdfd2) {
		// 0. Check if the input query has any template variable, otherwise qTsol is
		// empty
		Set<Var> templateVarSet = getQueryTemplateVariableSet(qChild);
		if (templateVarSet.size() > 0) {
			try {
				// 1. Transform the the query qChild into a query containing the template
				// variable only.
				Query qT = rewriteQueryWithTemplateVar(qChild);
				// 2. Compute the QuerySolution for qT;
				log.debug("Computing solution space for subquery:");
				log.debug("{}", qT);
				String res = SparqlUtils.doRawQuery(qT.toString(), rdfd2.getEndPointURL().toString());
				List<QuerySolution> qTsol = SparqlUtils.extractProjectedValues(res, qT.getProjectVars());
				log.debug(" ... Solution space size = {} ", qTsol.size());
				return qTsol;
			} catch (SparqlException ex) {
				log.error("Connection failed while checking solution space.", ex);
				log.error("Assuming empty solution space.");
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
	}

	private Set<Var> getQueryTemplateVariableSet(Query qR) {
		SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getQueryTemplateVariableSet();

	}

	private Query rewriteQueryWithTemplateVar(Query qR) {
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

}
