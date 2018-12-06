package uk.ac.open.kmi.squire.core4;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core2.QueryCtxNode;
import uk.ac.open.kmi.squire.core2.QueryCtxNodeFactory;
import uk.ac.open.kmi.squire.core2.QueryTempVarSolutionSpace;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeDistance;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.operation.InstantiateTemplateVar;
import uk.ac.open.kmi.squire.operation.IsSparqlQuerySatisfiableStateful;
import uk.ac.open.kmi.squire.operation.RemoveTriple;
import uk.ac.open.kmi.squire.operation.TooGeneralException;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.TemplateVariableScanner;
import uk.ac.open.kmi.squire.utils.PowerSetFactory;

/**
 * 
 * XXX for a {@link AbstractMappedQueryTransform} in itself, this class has too
 * much control over other operations.
 *
 * @author carloallocca
 */
public class Specializer extends AbstractMappedQueryTransform {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Query> queryIndex = new HashMap<>();

	private final List<QueryCtxNode> recommendations = new ArrayList<>();

	/**
	 * How many specializations before printing the log.
	 */
	protected int logFreq = 20;

	protected final QueryCtxNodeFactory nodeFactory;

	protected final Query qO, qR;

	protected IRDFDataset rdfd1, rdfd2;

	/**
	 * List of query and context nodes that are still _left_ to specialize: it is
	 * emptied as the process goes on.
	 */
	protected final List<QueryCtxNode> specializables = new ArrayList<>();

	public Specializer(Query originalQuery, Query generalQuery, IRDFDataset d1, IRDFDataset d2,
			MappedQueryTransform previousOp, float resultTypeSimilarityDegree, float queryRootDistanceDegree,
			float resultSizeSimilarityDegree, float querySpecificityDistanceDegree, boolean strict, String token) {
		super();

		this.qO = QueryFactory.create(originalQuery.toString());
		this.qR = QueryFactory.create(generalQuery.toString());

		this.rdfd1 = d1;
		this.rdfd2 = d2;

		this.nodeFactory = new QueryCtxNodeFactory(originalQuery, generalQuery, d1, d2, resultTypeSimilarityDegree,
				queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree, strict);

		this.classVarTable = previousOp.getClassVarTable();
		this.individualVarTable = previousOp.getIndividualVarTable();
		this.literalVarTable = previousOp.getLiteralVarTable();
		this.objectProperyVarTable = previousOp.getObjectProperyVarTable();
		this.datatypePropertyVarTable = previousOp.getDatatypePropertyVarTable();
		this.rdfVocVarTable = previousOp.getRdfVocVarTable();

		this.token = token;

		log.info("Computing scores for generalized query wrt. original...");
		// A. Compute the query recommentedQueryScore:
		// 1)...QueryRootDistance ... we start with both value set to 0
		float queryRootDist = 0;
		float queryRootDistSim = 0;

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, this.qR);
		float qSpecDistTP = qSpecDist.computeQSDwrtQueryTP(this.qO, this.qR);
		float qSpecificitySim = 1 - (qSpecDistVar + qSpecDistTP);

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeDistance qRTS = new QueryResultTypeDistance();
		float resulTtypeDist = qRTS.compute(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;

		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree * resultTypeSim);
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDist) +
		// (resultTypeSimilarityDegree * resulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar + qSpecDistTP)));
		// float recommentedQueryScore = resulttypeSim + qSpecDistVar+qSpecDistTP;

		/*
		 * This is working as it should but it does not consider the similarity distance
		 * between the replaced entities
		 */
		float recommendedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * resultTypeSim) + (querySpecificityDistanceDegree * (qSpecificitySim)));

		log.info(" - query specificity distance wrt variables = {}", qSpecDistVar);
		log.info(" - query specificity distance wrt triple patterns = {}", qSpecDistTP);
		log.info(" - query specificity similarity = {}", resultTypeSim);
		log.info(" - result type similarity = {}", resultTypeSim);
		log.info(" - overall score = {}", recommendedQueryScore);

		// C. Compute the QueryTempVarSolutionSpace
		List<QuerySolution> qTsol;
		try {
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			qTsol = temVarValueSpace.computeTempVarSolutionSpace(generalQuery, this.rdfd2, strict);
			qTsol = nodeFactory.eliminateDuplicateBindings(qTsol);
		} catch (TooGeneralException gex) {
			log.warn("Query is too general to execute safely. Assuming solution exists.");
			log.warn(" * Query : '{}'", gex.getQuery());
			qTsol = new ArrayList<>();
			qTsol.add(new QuerySolutionMap()); // Add an empty solution, just not to make it fail.
		}

		// D. Build the QueryAndContextNode from the query
		QueryCtxNode qAndcNode = new QueryCtxNode(generalQuery);
		qAndcNode.setOriginalQuery(originalQuery);
		// ...set the score measurements
		qAndcNode.setQueryRootDistance(queryRootDist);
		// qAndcNode.setQuerySpecificityDistanceSimilarity(qSpecDistVar + qSpecDistTP);
		qAndcNode.setQuerySpecificityDistanceTP(qSpecDistTP);
		qAndcNode.setQuerySpecificityDistanceVar(qSpecDistVar);
		qAndcNode.setqRScore(recommendedQueryScore);
		// qAndcNode.setLastOperation(op);
		// qAndcNode.setqRTemplateVariableSet(qRTemplateVariableSet);
		// qAndcNode.setqRTriplePathSet(qRTriplePatternSet);
		// ...set the QueryTempVarSolutionSpace
		qAndcNode.setTplVarSolutionSpace(qTsol);
		// qAndcNode.setQueryTempVarValueMap(qTsolMap);

		// E. Create a sorted list of "specializable" queries
		// NOTE: This implementation only produces singletons.
		this.specializables.add(qAndcNode);
		Collections.sort(this.specializables, new QueryCtxNode.QRScoreComparator());
	}

	public List<QueryCtxNode> getSpecializations() {
		return recommendations;
	}

	/**
	 * This is how I understood it works:
	 * <ol>
	 * <li>By this time the query has already been generalized and the solution
	 * space for the general variables has been computed.
	 * <li>We have a set of one or more general queries.
	 * <li>If the general query is not satisfiable in the target dataset (possible
	 * if e.g. there are several properties in common between the two datasets but
	 * they never appear together), then expand the general query to the power set
	 * of queries with every combination of its triple patterns. This seems to be
	 * replacing the removal operation.
	 * <li>For every general query, perform an operation that instantiates ALL the
	 * template variables together atomically. Compute the satisfiability, solution
	 * space, score etc. for the resulting query.
	 * </ol>
	 * 
	 * I see the following problems with this:
	 * <ol>
	 * <li>Instantiating all the template variables together does not allow a greedy
	 * best-first (pure heuristic) approach, which could for example work alongside
	 * this one.
	 * <li>It does not account for the case where the general query has a solution
	 * in the target dataset, but the solution does not include the query we are
	 * looking for (see e.g. the third query of the egov_1 gold standard: the
	 * properties match, but never for the type School): in the current
	 * implementation, no removal/powerset operation is performed and the desired
	 * query will not be recommended at all.
	 * <li>Because the powerset/removal operations might be needed in such
	 * non-extreme cases, the computational impact of their brute-force nature can
	 * no longer be neglected.
	 * </ol>
	 */
	public List<QueryCtxNode> specialize() {

		log.debug(" - {} specializable query templates", this.specializables.size());
		for (QueryCtxNode qctx : this.specializables) {
			log.debug("   - original query:\r\n{}", qctx.getOriginalQuery());
			log.debug("   - generalized query:\r\n{}", qctx.getTransformedQuery());
		}

		IsSparqlQuerySatisfiableStateful satisfiability = new IsSparqlQuerySatisfiableStateful(this.rdfd2);

		if (this.specializables.size() == 1) {
			QueryCtxNode qctx = this.specializables.get(0);
			List<QuerySolution> spc = qctx.getTplVarSolutionSpace();
			boolean isTpl = isQueryTemplated(qctx);
			log.debug("Single specializable node:");
			log.debug(" - solution space size = {}", spc.size());
			log.debug(" - has template variables that can be instantiated: {}", isTpl ? "YES" : "NO");

			/*
			 * Worst case: the generalized query has no solution but it does have template
			 * variables. This may happen for example because the assumptions made in the
			 * generalization, such as the co-occurrence of two or more properties, were
			 * incorrect. This case expands on all the possible combinations of templated
			 * triple patterns. It also seems to be the only one where removal takes place.
			 * 
			 * TODO an avoidance strategy is probably better than dealing with the case.
			 */
			if (spc.isEmpty() && isTpl) {
				log.warn("Specializable query has no solution!"
						+ " This is usually a consequence of bad assumptions from the generalization process.");
				log.warn("Fallback is to expand the specializable query as the power set of its query patterns.");
				log.warn("This has a high computational load.");

				QueryCtxNode parentQctx = popTopScoredQueryCtx(this.specializables);
				// ADD the code for generating the subqueries with removed triple patterns

				// The power set of the triple pattern set.
				// P.S. Look at the code down in the section 3.
				Query parentqRCopy = QueryFactory.create(parentQctx.getTransformedQuery());
				Set<TriplePath> triplePaths = getQueryTriplePathSet(parentqRCopy);
				List<String> qRTemplateVariableSet = parentqRCopy.getResultVars();
				log.debug("Initial size of triple path set in query: {}", triplePaths.size());
				List<List<TriplePath>> tpPowerSet = PowerSetFactory.powerset(triplePaths);
				tpPowerSet = PowerSetFactory.order(tpPowerSet);
				log.debug("Size of triple path power set: {}", tpPowerSet.size());

				for (List<TriplePath> triplePathSubSet : tpPowerSet) {
					log.debug("triplePathSubSet :: {}", triplePathSubSet);
					if (!triplePathSubSet.isEmpty()) {
						SelectBuilder sb = new SelectBuilder();
						// adding the triple patterns
						for (TriplePath tp : triplePathSubSet)
							sb.addWhere(tp.asTriple()); // apply the removal operation
						// adding the output variable
						for (String var : qRTemplateVariableSet)
							sb.addVar(var);
						sb.setDistinct(true);
						Query subQuery = sb.build();

						// add here the rest of the code, including the fact that satisfiable and create
						// a node child from the node parent
						// Check if it is alredy indexed and therefore generated
						log.debug("subQuery Remove operation::: " + subQuery.toString());
						if (!isCached(subQuery)) {
							// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...
							boolean sat = false;
							try {
								sat = satisfiability.isSatisfiableWrtResults(subQuery);
								log.debug("isSatisfiableWRTResultsWithToken :: " + sat);
							} catch (Exception ex) {
								log.error("{}", ex);
							}
							if (sat) {
								log.debug("subQuery Remove operation::: " + subQuery);
								QueryCtxNode childNode = nodeFactory.createQctxForRemoval(subQuery, parentQctx);
								log.debug("childNode Solution List... " + childNode.getTplVarSolutionSpace().size());
								this.specializables.add(childNode);
							}
							// add qWithoutTriple to the index
							addQueryToIndexIFAbsent(subQuery);
						}
					}
				}
				Collections.sort(this.specializables, new QueryCtxNode.QRScoreComparator());
			}
		}

		if (this.specializables.size() == 1) {
			QueryCtxNode spec = this.specializables.get(0);
			Query q0 = spec.getOriginalQuery();
			if (spec.getTplVarSolutionSpace().isEmpty() && satisfiability.isSatisfiableWrtResults(q0)) {
				// Create a node for no operation
				QueryCtxNode childNode = nodeFactory.createNoOpQctx(q0);
				this.specializables.add(childNode);
				Collections.sort(this.specializables, new QueryCtxNode.QRScoreComparator());
				this.recommendations.add(childNode);
				notifyQueryRecommendation(q0, 1);
				notifyQueryRecommendationCompletion(true);
				return this.recommendations;
			}
		}

		// XXX Scary conditioned loop : specializables is reduced in another method...
		while (!this.specializables.isEmpty()) {

			// 1. Get and Remove the QueryAndContextNode with qRScore max
			// XXX ... below, precisely.
			QueryCtxNode parentNode = popTopScoredQueryCtx(this.specializables);
			if (parentNode != null) {
				// 2. Store the QueryAndContextNode with qRScore max into the
				// recommandedQueryList as it could be one of the recommended query
				this.recommendations.add(parentNode);
				// this.notifyQueryRecommendation(parentQueryAndContextNode.getqR(),
				// parentQueryAndContextNode.getqRScore());

				// 4. check if we can apply an instantiation operation;
				if (isQueryTemplated(parentNode)) {
					Query queryChild = QueryFactory.create(parentNode.getTransformedQuery());
					log.debug("Child query: {}", queryChild);
					List<QuerySolution> qSolList = parentNode.getTplVarSolutionSpace();
					log.debug("queryChild Instantiation step: {}", queryChild.toString());
					log.debug("qSolList size = {} ", qSolList.size());
					int c = 0, csat = 0;
					DecimalFormat format = new DecimalFormat("##.##%");

					for (QuerySolution sol : qSolList) {
						log.trace("Solution {}: {}", c++, sol);
						Query childQueryCopy = QueryFactory.create(queryChild.toString());
						Set<Var> qTplVars = getQueryTemplateVariableSet(childQueryCopy);
						Query qInst = null;

						// this contains all the pairs <entityQo, entityQr> for each varName
						// that is going to be instantiated
						List<NodeTransformation> instantiationList = new ArrayList<>();
						for (Var tv : qTplVars) {
							RDFNode node = sol.get(tv.getName());
							if (node != null && node.asNode().isURI()) {
								// XXX The operator is stateful so we have to re-instantiate it...
								InstantiateTemplateVar op_inst = new InstantiateTemplateVar();
								qInst = op_inst.instantiateVarTemplate(childQueryCopy, tv, node.asNode());
								Node entityQo = getOriginalEntity(tv);
								Node entityQr = node.asNode(); // Expected to be concrete and named
								NodeTransformation item = new NodeTransformation(entityQo, entityQr);
								instantiationList.add(item);
							} else
								log.error("Unexpected state of node {} for template variable '{}'", node, tv);
						}
						if (qInst != null) {
							// 4.1.2. Check if it is already indexed and therefore generated
							if (!isCached(qInst)) {
								// add qWithoutTriple to the index
								addQueryToIndexIFAbsent(qInst);

								// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...

								if (satisfiability.isSatisfiableWrtResults(qInst)) {
									csat++;
									QueryCtxNode childNode = nodeFactory.createQctxForInstantiation(qInst, parentNode,
											instantiationList);

									// ======
									// Ho commentato questa riga perche non ha un senso logico. Non c'e' motivo
									// di aggiungere la query istanziata nella lista di query da specializzare.
									// did it 13/05/2017
									// addSpecializableQueryList(childNode);
									// invece, ho aggiunto questa in quanto la query e' pronta per essere
									// raccomandata e non avra piu :
									this.recommendations.add(childNode);

									// =====

									log.debug("qR score ======" + childNode.getqRScore());
									log.debug("qR " + childNode.getTransformedQuery());

									notifyQueryRecommendation(childNode.getTransformedQuery(), childNode.getqRScore());

									// add qWithoutTriple to the index
									addQueryToIndexIFAbsent(qInst);
								} else {
									addQueryToIndexIFAbsent(qInst);
								}
							}
						}
						double ratio = (double) c / qSolList.size();
						if (0 == c % logFreq)
							log.info(" ... {} done ({} of {}) ", format.format(ratio), c, qSolList.size());
					} // end for
					log.info("{} done (processed={}, satisfiable={}) ", format.format(1), c, csat);
				}
			}

		} // end while
		this.notifyQueryRecommendationCompletion(true);
		return this.recommendations;
	}

	private Query applyInstanciationOP(Query queryChild, QuerySolution sol) {

		Set<Var> qTempVarSet = getQueryTemplateVariableSet(queryChild);
		for (Var tv : qTempVarSet) {
			RDFNode node = sol.get(tv.getName());
			InstantiateTemplateVar instOP = new InstantiateTemplateVar();
			queryChild = instOP.instantiateVarTemplate(queryChild, tv, node.asNode());

			// String entityqO =
			// this.classVarTable.getClassFromVar(templateVar.getVarName());
			// String entityqR = clas;
			//
			// ArrayList<String> childOperationList = new ArrayList();
			// childOperationList.addAll(pNode.getOperationList());
			// childOperationList.add(INSTANCE_OP);
			// String op = INSTANCE_OP;
		}
		return queryChild;
	}

	private void applyRemovalOp(QueryCtxNode qRScoreMaxNode) {

		Query qRCopy = qRScoreMaxNode.getTransformedQuery();
		Set<TriplePath> triplePathSet = getQueryTriplePathSet(qRCopy);

		for (TriplePath tp : triplePathSet) {

			// 1. Remove the TriplePath tp from the qRCopy
			Query qWithoutTriple = new RemoveTriple(qRCopy, tp.asTriple()).apply();

			// 2. Check if it is already indexed and therefore generated
			if (!isCached(qWithoutTriple)) {

				// // BUILD A NEW QueryAndContextNode
				// // 2.1. Clone the QueryAndContextNode with qRScore max so it can be processed
				// for applying operations
				// QueryAndContextNode qRScoreMaxNodeCloned =
				// qRScoreMaxNode.cloneMe(qRScoreMaxNode);
				//
				// // 2.2 Update the triplePathSet of the clonedNode
				// qRScoreMaxNodeCloned.getqRTriplePathSet().remove(tp);
				//
				// // 2.3. Update the recommended query of the clonedNode
				// qRScoreMaxNodeCloned.setqR(qWithoutTriple);
				//
				// // 2.4. devo fare tutti gli aggiornameti: operation list, le quattro misure,
				// etc...
				// ArrayList<String> clonedOperationList = new ArrayList();
				// clonedOperationList.addAll(qRScoreMaxNode.getOperationList());
				// clonedOperationList.add(REMOVE_TP_OP);
				// qRScoreMaxNodeCloned.setOperationList(clonedOperationList);
				//
				// qRScoreMaxNodeCloned.setOp(REMOVE_TP_OP);
				//
				// //In generale guarda dall'altra procedura per capire cosa manca
				//
				// // 7. devo aggiungere to specializableQueryAndContextNodeList in order to be
				// further specialized
				// addSpecializableQueryList(qRScoreMaxNodeCloned);
				//
				// this.recommandedQueryList.add(qRScoreMaxNodeCloned);
				//
				// // 8. Add node to the index
				// addQueryToIndexIFAbsent(qWithoutTriple);
			}

		}

	}

	protected void addQueryToIndexIFAbsent(Query qWithoutTriple) {
		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
		ArrayList<String> s = new ArrayList<>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection)
			s.add(tp.toString());
		queryIndex.putIfAbsent(s.toString(), qWithoutTriple);
	}

	/**
	 * Returns the node corresponding to this template variable before it was
	 * transformed into one.
	 * 
	 * @param tplVar
	 * @return
	 */
	protected Node getOriginalEntity(Var tplVar) {
		String varName = tplVar.getVarName();
		VarMapping<Var, Node> map;
		if (varName.startsWith(TEMPLATE_VAR_CLASS))
			map = this.classVarTable;
		else if (varName.startsWith(TEMPLATE_VAR_PROP_OBJ))
			map = this.objectProperyVarTable;
		else if (varName.startsWith(TEMPLATE_VAR_PROP_DT))
			map = this.datatypePropertyVarTable;
		else
			return null;
		return map.getValueFromVar(tplVar);
	}

	protected Set<Var> getQueryTemplateVariableSet(Query qR) {
		TemplateVariableScanner v = new TemplateVariableScanner();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getTemplateVariables();
	}

	protected Set<TriplePath> getQueryTriplePathSet(Query q) {
		if (q == null)
			throw new IllegalArgumentException("Query cannot be null");
		// Remember distinct objects in this
		final Set<TriplePath> tpSet = new HashSet<>();
		// This will walk through all parts of the query
		ElementWalker.walk(q.getQueryPattern(),
				// For each element
				new ElementVisitorBase() { // ...when it's a block of triples...
					public void visit(ElementPathBlock el) {
						// ...go through all the triples...
						Iterator<TriplePath> triples = el.patternElts();
						while (triples.hasNext())
							tpSet.add(triples.next());
					}
				});
		return tpSet;
	}

	protected boolean isCached(Query qWithoutTriple) {
		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
		ArrayList<String> s = new ArrayList<>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection)
			s.add(tp.toString());
		Collections.sort(s);
		return queryIndex.containsKey(s.toString());
	}

	/**
	 * A query can be instantiated if and only if it is "templated", i.e. there is
	 * at least one template variable in the query pattern, even if that variable is
	 * not in the projection.
	 * 
	 * @param node
	 * @return true iff there is at least one template variable in the transformed
	 *         query.
	 */
	protected boolean isQueryTemplated(QueryCtxNode queryNode) {
		Set<Var> tempVarSet = getQueryTemplateVariableSet(queryNode.getTransformedQuery());
		return tempVarSet.size() > 0;
	}

	/**
	 * Takes the highest-scored query from a list of specializable queries and
	 * removes it.
	 * 
	 * @return
	 */
	protected QueryCtxNode popTopScoredQueryCtx(List<QueryCtxNode> rankedList) {
		if (rankedList.size() > 0) {
			QueryCtxNode maxNode = rankedList.get(0);
			rankedList.remove(maxNode);
			return maxNode;
		}
		return null;
	}

}
