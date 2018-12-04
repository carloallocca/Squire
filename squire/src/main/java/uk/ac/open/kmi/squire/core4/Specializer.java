/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.core2.QueryTempVarSolutionSpace;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.evaluation.QueryGPESim;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeDistance;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.ontologymatching.JaroWinklerSimilarity;
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

	/**
	 * A record of transforming every instance of one RDF node into another,
	 * including (and especially) SPARQL variables.
	 * 
	 * @author carloallocca
	 *
	 */
	protected class NodeTransformation {

		private Node entityQo;
		private Node entityQr;

		public NodeTransformation(Node originalNode, Node transformedNode) {
			this.entityQo = originalNode;
			this.entityQr = transformedNode;
		}

		public Node getOriginalNode() {
			return entityQo;
		}

		public Node getTransformedNode() {
			return entityQr;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			s.append("{");
			s.append(getOriginalNode());
			s.append("<-");
			s.append(getTransformedNode());
			s.append("}");
			return s.toString();
		}

	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, Query> queryIndex = new HashMap<>();

	private final List<QueryAndContextNode> recommendations = new ArrayList<>();

	private float resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
			querySpecificityDistanceDegree;

	private final boolean strict;

	/**
	 * How many specializations before printing the log.
	 */
	protected int logFreq = 20;

	protected final Query qO, qR;

	protected IRDFDataset rdfd1, rdfd2;

	/**
	 * List of query and context nodes that are still _left_ to specialize: it is
	 * emptied as the process goes on.
	 */
	protected final List<QueryAndContextNode> specializables = new ArrayList<>();

	public Specializer(Query originalQuery, Query generalQuery, IRDFDataset d1, IRDFDataset d2,
			MappedQueryTransform previousOp, float resultTypeSimilarityDegree, float queryRootDistanceDegree,
			float resultSizeSimilarityDegree, float querySpecificityDistanceDegree, boolean strict, String token) {
		super();

		this.strict = strict;

		this.qO = QueryFactory.create(originalQuery.toString());
		this.qR = QueryFactory.create(generalQuery.toString());

		this.rdfd1 = d1;
		this.rdfd2 = d2;

		this.classVarTable = previousOp.getClassVarTable();
		this.individualVarTable = previousOp.getIndividualVarTable();
		this.literalVarTable = previousOp.getLiteralVarTable();
		this.objectProperyVarTable = previousOp.getObjectProperyVarTable();
		this.datatypePropertyVarTable = previousOp.getDatatypePropertyVarTable();
		this.rdfVocVarTable = previousOp.getRdfVocVarTable();

		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;

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
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
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

		// B. Compute the qRTemplateVariableSet and qRTriplePatternSet
		String op = ""; // It can be either R (for Removal) or I (Instantiation).

		Set<Var> qRTemplateVariableSet = getQueryTemplateVariableSet(this.qR);
		Set<TriplePath> qRTriplePatternSet = getQueryTriplePathSet(this.qR);

		// C. Compute the QueryTempVarSolutionSpace
		List<QuerySolution> qTsol;
		try {
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			qTsol = temVarValueSpace.computeTempVarSolutionSpace(generalQuery, this.rdfd2, strict);
			qTsol = eliminateDuplicateBindings(qTsol);
		} catch (TooGeneralException gex) {
			log.warn("Query is too general to execute safely. Assuming solution exists.");
			log.warn(" * Query : '{}'", gex.getQuery());
			qTsol = new ArrayList<>();
			qTsol.add(new QuerySolutionMap()); // Add an empty solution, just not to make it fail.
		}

		// D. Build the QueryAndContextNode from the query
		QueryAndContextNode qAndcNode = new QueryAndContextNode(generalQuery);
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
		Collections.sort(this.specializables, new QueryAndContextNode.QRScoreComparator());
	}

	public List<QueryAndContextNode> getSpecializations() {
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
	public List<QueryAndContextNode> specialize() {

		log.debug(" - {} specializable query templates", this.specializables.size());
		for (QueryAndContextNode qctx : this.specializables) {
			log.debug("   - original query:\r\n{}", qctx.getOriginalQuery());
			log.debug("   - generalized query:\r\n{}", qctx.getTransformedQuery());
		}

		IsSparqlQuerySatisfiableStateful satisfiability = new IsSparqlQuerySatisfiableStateful(this.rdfd2);

		if (this.specializables.size() == 1) {
			QueryAndContextNode qctx = this.specializables.get(0);
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

				QueryAndContextNode parentQctx = popTopScoredQueryCtx(this.specializables);
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
								QueryAndContextNode childNode = createQctxForRemoval(subQuery, parentQctx);
								log.debug("childNode Solution List... " + childNode.getTplVarSolutionSpace().size());
								this.specializables.add(childNode);
							}
							// add qWithoutTriple to the index
							addQueryToIndexIFAbsent(subQuery);
						}
					}
				}
				Collections.sort(this.specializables, new QueryAndContextNode.QRScoreComparator());
			}
		}

		if (this.specializables.size() == 1) {
			QueryAndContextNode spec = this.specializables.get(0);
			Query q0 = spec.getOriginalQuery();
			if (spec.getTplVarSolutionSpace().isEmpty() && satisfiability.isSatisfiableWrtResults(q0)) {
				// Create a node for no operation
				QueryAndContextNode childNode = createNoOpQctx(q0);
				this.specializables.add(childNode);
				Collections.sort(this.specializables, new QueryAndContextNode.QRScoreComparator());
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
			QueryAndContextNode parentNode = popTopScoredQueryCtx(this.specializables);
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

						// this contains all the tuples <varName, entityQo, entityQr> for each varName
						// that is going to be instantiated
						List<NodeTransformation> instantiationList = new ArrayList<>();
						for (Var tv : qTplVars) {
							RDFNode node = sol.get(tv.getName());
							if (node != null && node.asNode().isURI()) {
								// XXX The operator is stateful so we have to re-instantiate it...
								InstantiateTemplateVar op_inst = new InstantiateTemplateVar();
								qInst = op_inst.instantiateVarTemplate(childQueryCopy, tv, node.asNode());
								Node entityQo = getEntityQo(tv);
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
									QueryAndContextNode childNode = createQctxForInstantiation(qInst, parentNode,
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

	private void applyRemovalOp(QueryAndContextNode qRScoreMaxNode) {

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

	/**
	 * 
	 * @param node
	 * @param parentNode
	 * @param tplVarEntityQoQrInstantiated
	 * @return the modified input node
	 */
	private QueryAndContextNode computeCommonScores(QueryAndContextNode node, QueryAndContextNode parentNode) {

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(node.getOriginalQuery(),
				node.getTransformedQuery());
		float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(node.getOriginalQuery(),
				node.getTransformedQuery());
		node.setQuerySpecificityDistanceTP(qSpecDistSimTriplePattern);
		node.setQuerySpecificityDistanceVar(qSpecDistSimVar);

		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;
		// float recommentedQueryScore = ((queryRootDistanceDegree * newQueryRootDist) +
		// (resultTypeSimilarityDegree * newResulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar+qSpecDistTP)));
		// float recommentedQueryScore = newQueryRootDist + newResulttypeSim +
		// (qSpecDistVar/qSpecDistSimTriplePattern);

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree *
		// newResulttypeSim);

		return node;
	}

	/**
	 * Implemented as the complement of the average Jaro-Winkler similarity...
	 * 
	 * @param templVarEntityQoQrInstanciatedList
	 * @return
	 */
	private float computeInstantiationCost(List<NodeTransformation> templVarEntityQoQrInstanciatedList) {
		int size = templVarEntityQoQrInstanciatedList.size();
		float nodeCost = 0;
		if (size > 0) {
			for (NodeTransformation item : templVarEntityQoQrInstanciatedList) {
				String entityqO_TMP = getLocalName(item.getOriginalNode().toString());
				String entityqR_TMP = getLocalName(item.getTransformedNode().toString());
				nodeCost += computeInstantiationCost(entityqO_TMP, entityqR_TMP);
			}
			return (float) 1 - (nodeCost / size); // we divide by size as we want the value to be between 0 and 1.
		}
		return 0;
	}

	/**
	 * Instantiating an entity into another costs the Jaro-Winkler similarity of
	 * their respective names...
	 * 
	 * @param entityqO
	 * @param entityqR
	 * @return
	 */
	private float computeInstantiationCost(String entityqO, String entityqR) {
		if (entityqO == null || entityqR == null) {
			log.warn("Instantiation to or from a null entity has no cost (from: {} , to: {}).", entityqO, entityqR);
			return 0f;
		}
		JaroWinklerSimilarity jwSim = new JaroWinklerSimilarity();
		float sim = jwSim.computeMatchingScore(entityqO, entityqR);
		// return (float) (1.0 - sim);
		return sim;
	}

	private float computeRemoveOperationCost(Query originalQuery, Query childQuery) {
		QueryGPESim queryGPEsim = new QueryGPESim();
		float sim = queryGPEsim.computeQueryPatternLoss(originalQuery, childQuery);
		return 1f - sim;
	}

	// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	// <http://purl.org/dc/terms/title> )
	private List<QuerySolution> eliminateDuplicateBindings(List<QuerySolution> qSolList) {
		List<QuerySolution> output = new ArrayList<>();
		if (qSolList.isEmpty())
			return qSolList;
		for (QuerySolution qs : qSolList) {
			List<String> valuesList = new ArrayList<>();
			Iterator<String> varIter = qs.varNames();
			while (varIter.hasNext()) {
				String varName = varIter.next();
				valuesList.add(qs.get(varName).toString());
			}
			if (valuesList.size() == 1)
				output.add(qs);
			else {
				String firstValue = valuesList.get(0);
				valuesList.remove(0);
				boolean isAllDifferent = true;
				Iterator<String> varValueIter = valuesList.iterator();
				while (varValueIter.hasNext() && isAllDifferent) {
					String varValue = varValueIter.next();
					if (varValue.equals(firstValue))
						isAllDifferent = false;
				}
				if (isAllDifferent)
					output.add(qs);
			}
		}
		return output;
	}

	private String getLocalName(String entityqO) {
		String localName = "";
		if (entityqO.startsWith("http://") || entityqO.startsWith("https://")) {
			if (entityqO.contains("#")) {
				localName = entityqO.substring(entityqO.indexOf("#") + 1, entityqO.length());
				return localName;
			}
			localName = entityqO.substring(entityqO.lastIndexOf("/") + 1, entityqO.length());
			return localName;
		} else
			return entityqO;
	}

	protected void addQueryToIndexIFAbsent(Query qWithoutTriple) {
		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
		ArrayList<String> s = new ArrayList<>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection)
			s.add(tp.toString());
		queryIndex.putIfAbsent(s.toString(), qWithoutTriple);
	}

	protected QueryAndContextNode createNoOpQctx(Query qo) {
		QueryAndContextNode qCtx = new QueryAndContextNode(QueryFactory.create(qo));
		qCtx.setOriginalQuery(QueryFactory.create(qo));
		qCtx.setqRScore(1);
		return qCtx;
	}

	protected QueryAndContextNode createQctxForInstantiation(Query queryPostOp, QueryAndContextNode parentNode,
			List<NodeTransformation> entityTransformations) {

		QueryAndContextNode node = new QueryAndContextNode(queryPostOp.cloneQuery());

		// (a) Set the queries on the node
		node.setOriginalQuery(QueryFactory.create(parentNode.getOriginalQuery()));

		node.setTplVarSolutionSpace(new ArrayList<>(parentNode.getTplVarSolutionSpace()));

		// The following is no longer being done:
		// - set class, object/datatype property sets etc. on the node
		// - keep track of the operations list on each node (was not being used)
		// node.setLastOperation(OPID_INSTANTIATE);

		// ...set the score measurements
		computeCommonScores(node, parentNode);

		// 1)...QueryRootDistance
		// AA: it seems to maximize the average Jaro-Winkler similarity of all the
		// instantiations.
		float newQueryRootDist = parentNode.getQueryRootDistance() + computeInstantiationCost(entityTransformations);
		node.setQueryRootDistance(newQueryRootDist);
		float queryRootDistSim = 1 - newQueryRootDist;

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeDistance qRTS = new QueryResultTypeDistance();
		float newResulttypeSim = qRTS.computeQueryResultTypeDistance(node.getOriginalQuery(), this.rdfd1,
				node.getTransformedQuery(), this.rdfd2);

		float recommendedQueryScore = (queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * newResulttypeSim) + (querySpecificityDistanceDegree
						* (1 - (node.getQuerySpecificityDistanceTP() + node.getQuerySpecificityDistanceVar())));

		node.setqRScore(recommendedQueryScore);

		return node;
	}

	protected QueryAndContextNode createQctxForRemoval(Query queryPostOp, QueryAndContextNode parentNode) {

		QueryAndContextNode node = new QueryAndContextNode(queryPostOp.cloneQuery());

		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentNode.getOriginalQuery());
		node.setOriginalQuery(clonedqO);

		// Compute the QueryTempVarSolutionSpace

		List<QuerySolution> qTsolChild = new ArrayList<>();
		QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
		List<QuerySolution> qTsolTMP = temVarValueSpace.computeTempVarSolutionSpace(queryPostOp, this.rdfd2, strict);

		// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
		// <http://purl.org/dc/terms/title> ),
		List<QuerySolution> qTsolCleaned = eliminateDuplicateBindings(qTsolTMP);

		qTsolChild.addAll(qTsolCleaned);
		node.setTplVarSolutionSpace(qTsolCleaned);

		// The following is no longer being done:
		// - set class, object/datatype property sets etc. on the node
		// - keep track of the operations list on each node (was not being used)
		// node.setLastOperation(OPID_TP_REMOVE);

		// ...set the score measurements
		computeCommonScores(node, parentNode);
		/*
		 * Compute the query recommentedQueryScore: 1)QueryRootDistance
		 */
		float newQueryRootDist = parentNode.getQueryRootDistance()
				+ computeRemoveOperationCost(node.getOriginalQuery(), node.getTransformedQuery());
		node.setQueryRootDistance(newQueryRootDist);
		float queryRootDistSim = 1 - newQueryRootDist;

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeDistance qRTS = new QueryResultTypeDistance();
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;

		// float recommentedQueryScore = ( ( newQueryRootDist) +
		// ( newResulttypeSim) +
		// ( (qSpecDistVar/qSpecDistSimTriplePattern)));
		// log.info("qSpecDistSimVar/qSpecDistSimTriplePattern " +
		// (qSpecDistSimVar+qSpecDistSimTriplePattern));
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree * resultTypeSim);

		float recommendedQueryScore = (queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * resultTypeSim) + (querySpecificityDistanceDegree
						* (1 - (node.getQuerySpecificityDistanceTP() + node.getQuerySpecificityDistanceVar())));
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
		// + (resultTypeSimilarityDegree * resultTypeSim)
		// + (querySpecificityDistanceDegree * (qSpecificitySim)));
		// float recommentedQueryScore = (
		// ( newResulttypeSim) +
		// ( (qSpecDistSimVar+qSpecDistSimTriplePattern)));
		node.setqRScore(recommendedQueryScore);

		return node;
	}

	protected Node getEntityQo(Var tv) {
		String varName = tv.getVarName();
		VarMapping<Var, Node> map;
		if (varName.startsWith(TEMPLATE_VAR_CLASS))
			map = this.classVarTable;
		else if (varName.startsWith(TEMPLATE_VAR_PROP_OBJ))
			map = this.objectProperyVarTable;
		else if (varName.startsWith(TEMPLATE_VAR_PROP_DT))
			map = this.datatypePropertyVarTable;
		else
			return null;
		return map.getValueFromVar(tv);
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
	protected boolean isQueryTemplated(QueryAndContextNode queryNode) {
		Set<Var> tempVarSet = getQueryTemplateVariableSet(queryNode.getTransformedQuery());
		return tempVarSet.size() > 0;
	}

	/**
	 * Takes the highest-scored query from a list of specializable queries and
	 * removes it.
	 * 
	 * @return
	 */
	protected QueryAndContextNode popTopScoredQueryCtx(List<QueryAndContextNode> rankedList) {
		if (rankedList.size() > 0) {
			QueryAndContextNode maxNode = rankedList.get(0);
			rankedList.remove(maxNode);
			return maxNode;
		}
		return null;
	}

}
