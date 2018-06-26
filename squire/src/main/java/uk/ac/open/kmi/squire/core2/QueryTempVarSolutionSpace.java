/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_DT;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_OBJ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.atlas.json.JsonParseException;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.util.ExprUtils;
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
	 * @param strict
	 *            consider only the solutions that preserve property types.
	 * @param projectToThese
	 * @return
	 * @throws TooGeneralException
	 *             if the query is too general (e.g. too many variables and few
	 *             concrete nodes) to be safely executed.
	 */
	public List<QuerySolution> computeTempVarSolutionSpace(Query qChild, IRDFDataset rdfd2, boolean strict,
			Var... projectToThese) throws TooGeneralException {
		List<QuerySolution> qTsol = new ArrayList<>();
		// Defer the check for template variables to the templatizeAndReduce() function.
		String res = null;
		Query qT = qChild;
		try {
			// 1. Transform the query qChild so that only template variables are projected.
			qT = templatizeAndReduce(qChild, strict, projectToThese);
			// 1a. Check if the resulting query is not too general to be executed.
			checkGenerality(qT);
			// 2. Compute the solution space for the templated (and possibly reduced) query.
			log.debug("Computing solution space for subquery:");
			log.debug("{}", qT);
			res = SparqlUtils.doRawQuery(qT.toString(), rdfd2.getEndPointURL().toString());
			qTsol = SparqlUtils.extractProjectedValues(res, qT.getProjectVars());
		} catch (SparqlException | JsonParseException ex) {
			if (ex instanceof SparqlException) log.error("Connection failed while checking solution space.", ex);
			else if (ex instanceof JsonParseException) {
				log.error("Solution space result size is not valid JSON. Content follows:");
				log.error("{}", res);
			}
			log.error("Falling back to paginated querying.");
			final Set<Map<String, RDFNode>> fallbackSol = new HashSet<>();
			computeSolutionSpacePaginated(qT, rdfd2, 0, 100, fallbackSol);
			log.debug("Fallback procedure computed {} solutions", fallbackSol.size());
			for (Map<String, RDFNode> sol : fallbackSol) {
				QuerySolutionMap qs = new QuerySolutionMap();
				for (Entry<String, RDFNode> entry : sol.entrySet())
					qs.add(entry.getKey(), entry.getValue());
				qTsol.add(qs);
				log.trace("Added {}", qs);
			}
			log.debug("Templated solution size now = {}", qTsol.size());
		} catch (NotTemplatedException ex) {
			log.error("Apparently the subquery has no template variables.");
			log.error(" ... Subquery was:\r\n{}", qT);
			log.error(" ... Assuming empty solution space.");
			qTsol = Collections.emptyList();
		} finally {
			// 2a. Re-expand the solutions space to include the variables that were reduced
			// earlier.
			Map<Var, Set<Var>> diocane = filter(reductions, qT.getProjectVars().toArray(new Var[0]));
			qTsol = SparqlUtils.inflateResultSet(qTsol, diocane);
			log.debug(" ... Solution space size = {} ", qTsol.size());
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
			else for (Var r : reductions.get(v))
				if (projv.contains(r)) {
					if (!filtered.containsKey(v)) filtered.put(v, new HashSet<>());
					filtered.get(v).add(r);
				}
		}
		return filtered;
	}

	private Set<Var> getTemplateVariables(Query qR) {
		TemplateVariableScanner v = new TemplateVariableScanner();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getTemplateVariables();
	}

	/**
	 * Rewrites a given query using its template variables and also tries to
	 * eliminate potential computational hogs that could cause endpoints to fail.
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
	private Query templatizeAndReduce(Query queryOrig, boolean strict, Var... projectToThese)
			throws NotTemplatedException {

		/**
		 * Utility class used as key for unique (subject,propertyType) pairs.
		 * 
		 * @author alessandro
		 *
		 */
		class NodeAndPropType {

			Node node;
			Class<? extends Property> propertyType;

			NodeAndPropType(Node node, Class<? extends Property> propertyType) {
				this.node = node;
				this.propertyType = propertyType;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) return true;
				if (obj instanceof NodeAndPropType) return node.equals(((NodeAndPropType) obj).node)
						&& propertyType.equals(((NodeAndPropType) obj).propertyType);
				return false;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(new Object[] { node, propertyType });
			}
		}

		log.debug("Query before reduction:\r\n{}", queryOrig);
		Set<Var> templateVars = getTemplateVariables(queryOrig);
		if (projectToThese.length > 0) {
			log.debug("Projection forced to the following variables: {}", (Object[]) projectToThese);
			templateVars.retainAll(new HashSet<>(Arrays.asList(projectToThese)));
		}
		if (templateVars.isEmpty()) throw new NotTemplatedException(queryOrig);
		Set<Var> usedVars = new HashSet<>();
		/*
		 * The subject node and the corresponding TP that we choose to keep.
		 */
		final Map<NodeAndPropType, TriplePath> subjectsWithGenericTPs = new HashMap<>();
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
					// If we are being strict, keep one for each (subject, propertyType)
					Node s = tp.getSubject();
					if (tp.getPredicate().isVariable() && tp.getObject().isVariable()) {
						Var p = (Var) tp.getPredicate(), o = (Var) tp.getObject();
						Class<? extends Property> propType;
						if (strict) {
							if (p.getName().startsWith(TEMPLATE_VAR_PROP_DT)) propType = DatatypeProperty.class;
							else if (p.getName().startsWith(TEMPLATE_VAR_PROP_OBJ)) propType = DatatypeProperty.class;
							else propType = Property.class;
						} else propType = Property.class;
						NodeAndPropType key = new NodeAndPropType(s, propType);
						if (!subjectsWithGenericTPs.containsKey(key)) {
							try {
								keep(tp, pathBlock);
							} catch (NotTemplatedException ex) {
								throw new RuntimeException(ex);
							}
						} else {
							// memorize the variables for the other trivial TPs.
							TriplePath kept = subjectsWithGenericTPs.get(key);
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
				Class<? extends Property> pt;
				if (strict) {
					String nam = p.getName();
					if (nam.startsWith(TEMPLATE_VAR_PROP_DT)) pt = DatatypeProperty.class;
					else if (nam.startsWith(TEMPLATE_VAR_PROP_OBJ)) pt = ObjectProperty.class;
					else pt = Property.class;
				} else pt = Property.class;
				subjectsWithGenericTPs.put(new NodeAndPropType(s, pt), tp);
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
		qT.setQuerySelectType();
		for (Var tv : templateVars)
			if (usedVars.contains(tv)) qT.addResultVar(tv.getName());

		if (strict) {
			String exp = "";
			Set<String> projected = new HashSet<>(qT.getResultVars());
			List<String> conds = new ArrayList<>();
			ElementWalker.walk(qpNu, new ElementVisitorBase() {
				@Override
				public void visit(ElementPathBlock el) {
					for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
						TriplePath tp = it.next();
						if (tp.getPredicate().isVariable() && tp.getObject().isVariable()
								&& projected.contains(tp.getPredicate().getName())) {
							String nam = tp.getPredicate().getName();
							if (nam.startsWith(TEMPLATE_VAR_PROP_DT)) {
								conds.add("isLiteral(?" + tp.getObject().getName() + ")");
								projected.remove(nam);
							} else if (nam.startsWith(TEMPLATE_VAR_PROP_OBJ)) {
								conds.add("isIRI(?" + tp.getObject().getName() + ")");
								projected.remove(nam);
							}
						}
					}
				}
			});
			for (int i = 0; i < conds.size(); i++) {
				if (i > 0) exp += " && ";
				exp += conds.get(i);
			}
			log.debug(exp);
			qpNu.addElementFilter(new ElementFilter(ExprUtils.parse(exp)));
		}
		qT.setQueryPattern(qpNu);

		log.debug("Reduced query: {}", qT);
		return qT;
	}

	protected void computeSolutionSpacePaginated(Query baseQuery, IRDFDataset dataset, int step, int stepLength,
			final Set<Map<String, RDFNode>> solutions) {
		if (stepLength <= 0) throw new IllegalArgumentException("Step length must be a positive integer.");
		long before = System.currentTimeMillis();
		Query paginatedQuery = QueryFactory.create(baseQuery);
		paginatedQuery.setLimit(stepLength);
		paginatedQuery.setOffset(step * stepLength);
		String res = "";
		boolean error = false;
		try {
			res = SparqlUtils.doRawQuery(paginatedQuery.toString(), dataset.getEndPointURL().toString());
			List<QuerySolution> items = SparqlUtils.extractProjectedValues(res, paginatedQuery.getProjectVars());
			boolean doAgain = false;
			if (items.size() > 0) {
				int added = 0;
				// Inspect for new bindings: if at least one is found, do another round (rhyme
				// unintentional)
				for (QuerySolution sol : items) {
					QuerySolutionMap solMap;
					if (sol instanceof QuerySolutionMap) solMap = (QuerySolutionMap) sol;
					else {
						solMap = new QuerySolutionMap();
						solMap.addAll(sol);
					}
					Map<String, RDFNode> mapSol = solMap.asMap();
					if (!solutions.contains(mapSol)) {
						solutions.add(mapSol);
						added++;
						doAgain = true;
					}
				}
				log.debug("Added {} new solutions (time={} ms)", added, System.currentTimeMillis() - before);
			} else log.debug("No new solutions, stopping at size {}", solutions.size());
			doAgain &= items.size() == stepLength;
			if (doAgain) computeSolutionSpacePaginated(baseQuery, dataset, step + 1, stepLength, solutions);
		} catch (SparqlException ex) {
			log.error("Query failed.", ex);
			error = true;
		} catch (JsonParseException ex) {
			log.error("Malformed JSON returned at index ({},{}).", ex.getLine(), ex.getColumn());
			log.error("Content follows:\r\n{}", res);
			log.error("Stopping iteration and keeping previous results.");
			error = true;
		} catch (ResultSetException ex) {
			log.error("Returned JSON does not seem to be a SPARQL result set. Reason: {}", ex.getMessage());
			log.error("Content follows:\r\n{}", res);
			error = true;
		} catch (Exception ex) {
			log.error("An unthought-of error occurred. Check exception trace", ex);
			error = true;
		}
		if (error) {
			log.error("Failing query follows:\r\n{}", paginatedQuery);
			log.error("Stopping iteration and keeping previous results.");
		}
	}

}
