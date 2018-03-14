/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.operation.TooGeneralException;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.SparqlUtils.SparqlException;

/**
 * This class transforms a given query q with template variable into another
 * query q' whose project variables are the template variables. Ex: q = SELECT
 * DISTINCT ?mod ?title ?code WHERE { ?mod rdf:type ?ct1 ;
 * <http://purl.org/dc/terms/title> ?title ; ?dpt1 ?code } into
 *
 * q' = SELECT DISTINCT ?ct1 ?dpt1 WHERE { ?mod rdf:type ?ct1 ;
 * <http://purl.org/dc/terms/title> ?title ; ?dpt1 ?code }
 * 
 * @author carloallocca
 */
public class QueryTempVarSolutionSpace {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<Var, Set<Var>> reductions = new HashMap<>();

	/**
	 * 
	 * @param qChild
	 * @param rdfd2
	 * @return
	 * @throws TooGeneralException
	 *             if the query is too general (e.g. too many variables and few
	 *             URIs) to be safely executed.
	 */
	public List<QuerySolution> computeTempVarSolutionSpace(Query qChild, IRDFDataset rdfd2) throws TooGeneralException {
		List<QuerySolution> qTsol;

		// 0. Only proceed if the input query has some template variables.
		Set<Var> templateVarSet = getQueryTemplateVariableSet(qChild);
		if (templateVarSet.size() > 0) try {

			// 1. Transform the query qChild so that only template variables are projected.
			Query qT = templatizeAndReduce(qChild);

			// 1a. Check if the resulting query is not too general to be executed.
			checkGenerality(qT);

			// 2. Compute the solution space for the templated (and possibly reduced) query.
			log.debug("Computing solution space for subquery:");
			log.debug("{}", qT);
			String res = SparqlUtils.doRawQuery(qT.toString(), rdfd2.getEndPointURL().toString());
			qTsol = SparqlUtils.extractProjectedValues(res, qT.getProjectVars());

			// 2a. Re-expand the solution space to include the variables that were reduced
			// earlier.
			qTsol = reinstateReducedBindings(qTsol);
			log.debug(" ... Solution space size = {} ", qTsol.size());
		} catch (SparqlException ex) {
			log.error("Connection failed while checking solution space.", ex);
			log.error("Assuming empty solution space.");
			qTsol = new ArrayList<>();
		}
		else qTsol = new ArrayList<>();

		return qTsol;
	}

	private void checkGenerality(Query q) throws TooGeneralException {
		final boolean[] throwIt = new boolean[] { true };
		ElementWalker.walk(q.getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					if (!tp.getSubject().isVariable() || !tp.getPredicate().isVariable()
							|| !tp.getObject().isVariable()) {
						throwIt[0] = false;
						break;
					}
				}
			}
		});
		if (throwIt[0]) throw new TooGeneralException(q);
	}

	private Set<Var> getQueryTemplateVariableSet(Query qR) {
		SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getQueryTemplateVariableSet();
	}

	/**
	 * If the query was reduced by a call to {@link #templatizeAndReduce(Query)}
	 * before sending it to the endpoint, this function will attempt to rebuild the
	 * solution space as the cross product between the reduced variables and the
	 * surviving ones.
	 * 
	 * @param solution
	 * @return
	 */
	private List<QuerySolution> reinstateReducedBindings(List<QuerySolution> solution) {

		// Cannot directly use QuerySolution because equivalence does not seem to be
		// implemented for that class.
		Set<Map<String, RDFNode>> expanded = new HashSet<>();

		for (QuerySolution sol : solution) {
			// For every reduced solution generate the cross-product solutions.
			Iterator<QuerySolution> it2 = solution.iterator();
			while (it2.hasNext()) {
				QuerySolution tmpSol = it2.next();
				QuerySolutionMap solNu = new QuerySolutionMap();
				// Iterate over all the variables in the solution and check if other variables
				// were reduced into them. If so, reconstruct their values.
				for (Iterator<String> it = sol.varNames(); it.hasNext();) {
					String v = it.next();
					solNu.add(v, sol.get(v));
					Var vv = Var.alloc(v);
					if (reductions.containsKey(vv)) for (Var reduced : reductions.get(vv))
						solNu.add(reduced.getName(), tmpSol.get(v));
				}
				expanded.add(solNu.asMap());
			}

		}

		// Now re-copy everything into a list of the desired return type.
		List<QuerySolution> result = new ArrayList<>();
		for (Map<String, RDFNode> exp : expanded) {
			QuerySolutionMap sol = new QuerySolutionMap();
			for (Entry<String, RDFNode> entry : exp.entrySet())
				sol.add(entry.getKey(), entry.getValue());
			result.add(sol);
		}

		return result;
	}

	/**
	 * Rewrites a given query using its template variables and also tries to
	 * eliminate computational hogs that could cause endpoints to fail.
	 * 
	 * For example, the query pattern { ?x a ?t ; ?p1 ?y1 ; ?p2 ?y2 } is reduced to
	 * { ?x a ?t ; ?p1 ?y1 } .
	 * 
	 * The values for p2 and y2 can be reconstructed later by calling
	 * {@link #reinstateReducedBindings(List)}.
	 * 
	 * @param queryOrig
	 * @return
	 */
	private Query templatizeAndReduce(Query queryOrig) {
		log.debug("Original query: {}", queryOrig);
		Set<Var> templateVarSet = getQueryTemplateVariableSet(queryOrig);
		Set<Var> usedVars = new HashSet<>();
		/*
		 * The subject node and the corresponding TP that we choose to keep.
		 */
		final Map<Node, TriplePath> subjectsWithGenericTPs = new HashMap<>();
		Element qp = queryOrig.getQueryPattern();
		final ElementGroup qpNu = new ElementGroup();

		ElementWalker.walk(qp, new ElementVisitorBase() {

			@Override
			public void visit(ElementPathBlock el) {
				ElementPathBlock elNu = new ElementPathBlock();
				// Here we decide what to copy into qpNu and what not to
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					// Only add the first trivial TP (i.e. predicate and object are variables) for
					// each subject
					if (tp.getPredicate().isVariable() && tp.getObject().isVariable()) {
						Var p = (Var) tp.getPredicate(), o = (Var) tp.getObject();
						if (!subjectsWithGenericTPs.containsKey(tp.getSubject())) {
							subjectsWithGenericTPs.put(tp.getSubject(), tp);
							elNu.addTriple(tp);
							usedVars.add(p);
							reductions.put(p, new HashSet<>());
							usedVars.add(o);
							reductions.put(o, new HashSet<>());
							if (tp.getSubject().isVariable()) usedVars.add((Var) tp.getSubject());
						} else {
							// memorize the variables for the other trivial TPs.
							TriplePath kept = subjectsWithGenericTPs.get(tp.getSubject());
							reductions.get((Var) kept.getPredicate()).add(p);
							reductions.get((Var) kept.getObject()).add(o);
						}
					} else {
						elNu.addTriple(tp); // Always add nontrivial TPs
						if (tp.getSubject().isVariable()) usedVars.add((Var) tp.getSubject());
						if (tp.getPredicate().isVariable()) usedVars.add((Var) tp.getPredicate());
						if (tp.getObject().isVariable()) usedVars.add((Var) tp.getObject());
					}
				}
				qpNu.addElement(elNu);
			}

		});

		Query qT = QueryFactory.create();
		qT.setDistinct(true);
		qT.setQueryPattern(qpNu);
		qT.setQuerySelectType();
		for (Var tv : templateVarSet)
			if (usedVars.contains(tv)) qT.addResultVar(tv.getName());
		log.debug("Reduced query: {}", qT);
		return qT;
	}

}
