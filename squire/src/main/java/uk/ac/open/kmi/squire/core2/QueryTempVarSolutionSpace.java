/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
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
import uk.ac.open.kmi.squire.sparqlqueryvisitor.TemplateVariableScanner;
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

	private class NotTemplatedException extends Exception {
		private static final long serialVersionUID = 5830516022506521166L;
		private Query q;

		public NotTemplatedException(Query q) {
			this.q = q;
		}

		public Query getQuery() {
			return this.q;
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Maps the "kept" variable to the reduced ones.
	 */
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
	public List<QuerySolution> computeTempVarSolutionSpace(Query qChild, IRDFDataset rdfd2, Var... projectToThese)
			throws TooGeneralException {
		List<QuerySolution> qTsol;
		// Defer the check for template variables to the templatizeAndReduce() function.
		try {
			// 1. Transform the query qChild so that only template variables are projected.
			Query qT = templatizeAndReduce(qChild, projectToThese);
			// 1a. Check if the resulting query is not too general to be executed.
			checkGenerality(qT);
			// 2. Compute the solution space for the templated (and possibly reduced) query.
			log.debug("Computing solution space for subquery:");
			log.debug("{}", qT);
			String res = SparqlUtils.doRawQuery(qT.toString(), rdfd2.getEndPointURL().toString());
			qTsol = SparqlUtils.extractProjectedValues(res, qT.getProjectVars());
			// 2a. Re-expand the solution space to include the variables that were reduced
			// earlier.
			Map<Var, Set<Var>> diocane = filter(reductions, qT.getProjectVars().toArray(new Var[0]));
			qTsol = SparqlUtils.inflateResultSet(qTsol, diocane);
			log.debug(" ... Solution space size = {} ", qTsol.size());
		} catch (SparqlException ex) {
			log.error("Connection failed while checking solution space.", ex);
			log.error("Assuming empty solution space.");
			qTsol = new ArrayList<>();
		} catch (NotTemplatedException ex) {
			log.error("Apparently the query has no template variables.");
			log.error("Assuming empty solution space.");
			qTsol = new ArrayList<>();
		}
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

	private Map<Var, Set<Var>> filter(Map<Var, Set<Var>> reductions, Var... variables) {
		Set<Var> projv = new HashSet<>(Arrays.asList(variables));
		Map<Var, Set<Var>> filtered = new HashMap<>();
		for (Var v : reductions.keySet()) {
			// If the key is filtered in, copy the whole value
			if (projv.contains(v)) filtered.put(v, new HashSet<>(reductions.get(v)));
			else {
				for (Var r : reductions.get(v))
					if (projv.contains(r)) {
						if (!filtered.containsKey(v)) filtered.put(v, new HashSet<>());
						filtered.get(v).add(r);
					}
			}

		}
		return filtered;
	}

	private Set<Var> getQueryTemplateVariableSet(Query qR) {
		TemplateVariableScanner v = new TemplateVariableScanner();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getTemplateVariables();
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
	private Query templatizeAndReduce(Query queryOrig, Var... projectToThese) throws NotTemplatedException {
		log.debug("Original query: {}", queryOrig);
		Set<Var> templateVars = getQueryTemplateVariableSet(queryOrig);
		if (projectToThese.length > 0) {
			log.debug("Projection forced to the following variables: {}", (Object[]) projectToThese);
			templateVars.retainAll(new HashSet<>(Arrays.asList(projectToThese)));
		}
		if (templateVars.isEmpty()) throw new NotTemplatedException(queryOrig);
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
				final ElementPathBlock pathBlock = new ElementPathBlock();
				// Here we decide what to copy into qpNu and what not to

				Set<Var> projected = new HashSet<>(Arrays.asList(projectToThese));
				// Do a first scan to decide which TPs to keep
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					if (projected.contains(tp.getSubject()) || projected.contains(tp.getPredicate())
							|| projected.contains(tp.getObject()))
						try {
						if (tp.getPredicate().isVariable() && tp.getObject().isVariable()) keep(tp, pathBlock);
					} catch (NotTemplatedException ex) {
						throw new RuntimeException(ex);
					}
				}
				// Do another scan for reductions
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					// For each subject, only keep the first trivial TP (i.e. where both predicate
					// and object are variables) that we haven't already kept.
					Node s = tp.getSubject();
					if (tp.getPredicate().isVariable() && tp.getObject().isVariable()) {
						Var p = (Var) tp.getPredicate(), o = (Var) tp.getObject();
						if (!subjectsWithGenericTPs.containsKey(s)) {
							try {
								keep(tp, pathBlock);
							} catch (NotTemplatedException ex) {
								throw new RuntimeException(ex);
							}
						} else {
							// memorize the variables for the other trivial TPs.
							TriplePath kept = subjectsWithGenericTPs.get(s);
							reductions.get((Var) kept.getPredicate()).add(p);
							reductions.get((Var) kept.getObject()).add(o);
						}
					} else {
						pathBlock.addTriple(tp); // Always add nontrivial TPs
						if (s.isVariable()) usedVars.add((Var) s);
						if (tp.getPredicate().isVariable()) usedVars.add((Var) tp.getPredicate());
						if (tp.getObject().isVariable()) usedVars.add((Var) tp.getObject());
					}
				}
				qpNu.addElement(pathBlock);
			}

			private void keep(TriplePath tp, ElementPathBlock pathBlock) throws NotTemplatedException {
				if (tp.getPredicate().isConcrete() || tp.getObject().isConcrete())
					throw new NotTemplatedException(queryOrig);
				Node s = tp.getSubject();
				Var p = (Var) tp.getPredicate(), o = (Var) tp.getObject();
				subjectsWithGenericTPs.put(s, tp);
				pathBlock.addTriple(tp);
				usedVars.add(p);
				if (!reductions.containsKey(p)) reductions.put(p, new HashSet<>());
				usedVars.add(o);
				if (!reductions.containsKey(o)) reductions.put(o, new HashSet<>());
				if (s.isVariable()) usedVars.add((Var) s);
			}

		});

		Query qT = QueryFactory.create();
		qT.setDistinct(true);
		qT.setQueryPattern(qpNu);
		qT.setQuerySelectType();
		for (Var tv : templateVars)
			if (usedVars.contains(tv)) qT.addResultVar(tv.getName());
		log.debug("Reduced query: {}", qT);
		return qT;
	}

}
