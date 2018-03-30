/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.arq.querybuilder.SelectBuilder;
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
import uk.ac.open.kmi.squire.core2.VarTemplateAndEntityQoQr;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.evaluation.QueryGPESim;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeSimilarity;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.ontologymatching.JaroWinklerSimilarity;
import uk.ac.open.kmi.squire.operation.RemoveTriple;
import uk.ac.open.kmi.squire.operation.SPARQLQueryInstantiation;
import uk.ac.open.kmi.squire.operation.StatefulSPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.operation.TooGeneralException;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.TemplateVariableScanner;
import uk.ac.open.kmi.squire.utils.PowerSetFactory;

/**
 *
 * @author carloallocca
 */
public class Specializer extends QueryOperator {

	private static String OPID_INSTANTIATE = "I";

	private static String OPID_TP_REMOVE = "R";
	private final Logger log = LoggerFactory.getLogger(getClass());

	private Query qO, qR;
	private Map<String, Query> queryIndex = new HashMap<>();
	private IRDFDataset rdfd1, rdfd2;
	private final List<QueryAndContextNode> recommendations = new ArrayList<>();
	private float resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
			querySpecificityDistanceDegree;

	/**
	 * List of specializable query and context nodes
	 */
	private final List<QueryAndContextNode> specializables = new ArrayList<>();

	public Specializer(Query qo, Query qr, IRDFDataset d1, IRDFDataset d2, VarMapping cVM, VarMapping opVM,
			VarMapping dpVM, VarMapping indVM, VarMapping literalVM, VarMapping rdfVM, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree,
			String token) {
		super();

		this.qO = QueryFactory.create(qo.toString());
		this.qR = QueryFactory.create(qr.toString());

		this.rdfd1 = d1;
		this.rdfd2 = d2;

		this.classVarTable = cVM;
		this.individualVarTable = indVM;
		this.literalVarTable = literalVM;
		this.objectProperyVarTable = opVM;
		this.datatypePropertyVarTable = dpVM;
		this.rdfVocVarTable = rdfVM;

		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;

		this.token = token;

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
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;

		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree * resultTypeSim);
		float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * resultTypeSim) + (querySpecificityDistanceDegree * (qSpecificitySim)));
		//
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDist) +
		// (resultTypeSimilarityDegree * resulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar + qSpecDistTP)));
		// float recommentedQueryScore = resulttypeSim + qSpecDistVar+qSpecDistTP;
		/*
		 * This is working as it should but it does not consider the similarity distance
		 * between the replaced entities
		 */
		// log.debug("[QueryGeneralizer4::QuerySpecializer4] recommentedQueryScore " +
		// recommentedQueryScore);

		String op = ""; // It can be either R (for Removal) or I (Instanciation).

		// B. Compute the qRTemplateVariableSet and qRTriplePatternSet
		Set<Var> qRTemplateVariableSet = getQueryTemplateVariableSet(this.qR);
		Set<TriplePath> qRTriplePatternSet = getQueryTriplePathSet(this.qR);

		// C. Compute the QueryTempVarSolutionSpace
		List<QuerySolution> qTsol;
		try {
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			qTsol = temVarValueSpace.computeTempVarSolutionSpace(qr, this.rdfd2);
			qTsol = eliminateSolutionsBoundToSameValue(qTsol);
		} catch (TooGeneralException gex) {
			log.warn("Query is too general to execute safely. Assuming solution exists.");
			log.warn(" * Query : '{}'", gex.getQuery());
			qTsol = new ArrayList<>();
			qTsol.add(new QuerySolutionMap());
		}

		// Map<Var, Set<RDFNode>>
		// qTsolMap=temVarValueSpace.computeTempVarSolutionSpace(qr, this.rdfd2, null);
		// D. Build the QueryAndContextNode from the query
		QueryAndContextNode qAndcNode = new QueryAndContextNode();

		// ...set the original query and the recommended query;
		qAndcNode.setOriginalQuery(qo);
		qAndcNode.setTransformedQuery(qr);

		qAndcNode.setRdfD1(d1);
		qAndcNode.setRdfD2(d2);

		qAndcNode.setEntityqO("");
		qAndcNode.setEntityqR("");

		//// ...SET THE CLASS, OBJECT AND DATATYPE PROPERTIES SETs...;
		// qAndcNode.setcSetD2(d2.getClassSet());
		// qAndcNode.setDpSetD2(d2.getDatatypePropertySet());
		// qAndcNode.setOpSetD2(d2.getObjectPropertySet());
		// qAndcNode.setlSetD2(d2.getLiteralSet());
		// qAndcNode.setIndSetD2(d2.getIndividualSet());
		//// // As we have the issue of indexing long String when merging dpPropertySet
		//// and opPropertySet, I do not index and I do their merging here
		// ArrayList<String> propertySet = new ArrayList();
		// propertySet.addAll(d2.getDatatypePropertySet());
		// propertySet.addAll(d2.getObjectPropertySet());
		// d2.setPropertySet(propertySet);
		// qAndcNode.setpSetD2(propertySet);
		// qAndcNode.setRdfVD2(d2.getRDFVocabulary());
		// ...set the score measurements
		qAndcNode.setQueryRootDistance(queryRootDist);
		qAndcNode.setQueryRootDistanceSimilarity(queryRootDistSim); // do also for the other measurements,
																	// computeTempVarSolutionSpace them...

		// qAndcNode.setQuerySpecificityDistanceSimilarity(qSpecDistVar + qSpecDistTP);
		qAndcNode.setQuerySpecificityDistance(qSpecificitySim);

		qAndcNode.setQueryResultTypeSimilarity(resultTypeSim);
		qAndcNode.setQueryResultSizeSimilarity(queryResultSizeSimilarity);
		qAndcNode.setqRScore(recommentedQueryScore);

		qAndcNode.setOp(op);

		// qAndcNode.setqRTemplateVariableSet(qRTemplateVariableSet);
		// qAndcNode.setqRTriplePathSet(qRTriplePatternSet);
		// ...set the QueryTempVarSolutionSpace
		qAndcNode.setSolutionSpace(qTsol);
		// qAndcNode.setQueryTempVarValueMap(qTsolMap);

		// log.info("[QueryGeneralizer4::QuerySpecializer4] qTsol size = " +
		// qTsol.size());
		// E. Sorted Insert of the QueryAndContextNode into the
		// specializableQueryAndContextNodeList
		// if(qTsol.size()>=1){

		this.specializables.add(qAndcNode);
		Collections.sort(this.specializables, new QueryAndContextNode.QRScoreComparator());

		// this.recommandedQueryList.add(qAndcNode);
	}

	public List<QueryAndContextNode> getRecommendations() {
		return recommendations;
	}

	public List<QueryAndContextNode> specialize() throws Exception {
		log.debug(" - {} specializable query templates", this.specializables.size());
		for (QueryAndContextNode qctx : this.specializables) {
			log.debug("   - original query:\r\n{}", qctx.getOriginalQuery());
			log.debug("   - generalized query:\r\n{}", qctx.getTransformedQuery());
		}

		StatefulSPARQLQuerySatisfiable satisfiability = new StatefulSPARQLQuerySatisfiable(this.rdfd2);

		if ((this.specializables.size() == 1) && (this.specializables.get(0).getQueryTempVarSolutionSpace().isEmpty())
				&& (isIProcessable(this.specializables.get(0)))) {

			// log.info("WE WILL START THE SUB PROCESS SPECIALIZATION...");
			// 1. Get and Remove the QueryAndContextNode with qRScore max
			QueryAndContextNode parentQctx = popTopScoredQueryCtx(this.specializables);
			// log.info("getqO "+parentQctx.getqO());
			// log.info("getqR "+parentQctx.getqR());
			// ADD the code for generating the subqueries with removed triple patterns
			// (QueryAndContextNode)
			// The power set of the triple pattern set.
			// P.S. Look at the code down in the section 3.
			Query parentqRCopy = QueryFactory.create(parentQctx.getTransformedQuery());
			Set<TriplePath> triplePathSet = getQueryTriplePathSet(parentqRCopy);
			// List<Var> qRTemplateVariableSet=parentqRCopy.getProjectVars();
			List<String> qRTemplateVariableSet = parentqRCopy.getResultVars();

			List<List<TriplePath>> triplePathPowerSet = PowerSetFactory.powerset(triplePathSet);
			List<List<TriplePath>> triplePathPowerSetOrdered = PowerSetFactory.order(triplePathPowerSet);

			for (List<TriplePath> triplePathSubSet : triplePathPowerSetOrdered) {
				// for (int i=0; i<15; i++) {
				// List<TriplePath> triplePathSubSet = triplePathPowerSetOrdered.get(i);
				log.debug("triplePathSubSet ::" + triplePathSubSet.toString());
				if (!triplePathSubSet.isEmpty()) {
					SelectBuilder sb = new SelectBuilder();
					// adding the triple patters
					for (TriplePath tp : triplePathSubSet)
						sb.addWhere(tp.asTriple()); // apply the removal operation
					// adding the output variable
					for (String var : qRTemplateVariableSet)
						sb.addVar(var);
					sb.setDistinct(true);
					Query subQuery = sb.build();

					// add here the rest of the code,
					// including the fact that satisfiacibile and crete
					// a node child from the node parent
					// Check if it is alredy indexed and therefore generated
					log.debug("subQuery Remove operation::: " + subQuery.toString());
					if (!(isQueryIndexed(subQuery))) {
						// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...

						boolean b = false;

						try {
							b = satisfiability.isSatisfiableWrtResults(subQuery);
							log.debug("isSatisfiableWRTResultsWithToken :: " + b);
						} catch (Exception ex) {
							log.error("{}", ex);
						}

						if (b) {
							log.debug("subQuery Remove operation::: " + subQuery);
							QueryAndContextNode childNode = createQctxForRemoval(subQuery, parentQctx);
							log.debug("childNode Solution List... " + childNode.getQueryTempVarSolutionSpace().size());

							this.specializables.add(childNode);

							// this.specializableQueryAndContextNodeList.add(childNode);

							// add qWithoutTriple to the index
							addQueryToIndexIFAbsent(subQuery);
							// printQuerySolutionSpaceMap(parentQueryAndContextNode);

						} else addQueryToIndexIFAbsent(subQuery);
					}
				}
			}
			Collections.sort(this.specializables, new QueryAndContextNode.QRScoreComparator());
		}

		// The trivial case: there is only one specializable node and its query is
		// satisfiable wrt. the target dataset.
		if (this.specializables.size() == 1) {
			QueryAndContextNode spec = this.specializables.get(0);
			if (spec.getQueryTempVarSolutionSpace().isEmpty()
					&& satisfiability.isSatisfiableWrtResults(spec.getOriginalQuery())) {
				QueryAndContextNode childNode = createNoOpQctx(spec.getOriginalQuery());
				this.specializables.add(childNode);
				Collections.sort(this.specializables, new QueryAndContextNode.QRScoreComparator());
				this.recommendations.add(childNode);
				notifyQueryRecommendation(spec.getOriginalQuery(), 1);
				notifyQueryRecommendationCompletion(true);
				return this.recommendations;
			}
		}

		// log.info("WE WILL START THE SPECIALIZATION PROCESS...");

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

				// 4. check if we can apply a instanciation operation;
				if (isIProcessable(parentNode)) {
					Query queryChild = QueryFactory.create(parentNode.getTransformedQuery());
					log.debug("Child query: {}", queryChild);
					List<QuerySolution> qSolList = parentNode.getQueryTempVarSolutionSpace();
					log.debug("queryChild Instantiation step: {}", queryChild.toString());
					log.debug("qSolList size = {} ", qSolList.size());

					int c = 0;
					for (QuerySolution sol : qSolList) {
						log.trace("Solution {}: {}", c++, sol);
						Query childQueryCopy = QueryFactory.create(queryChild.toString());

						// [ REPLACED ] Query childQueryCopyInstanciated=
						// applyInstanciationOP(childQueryCopy, sol);
						Set<Var> qTempVarSet = getQueryTemplateVariableSet(childQueryCopy);
						Query childQueryCopyInstanciated = null;

						// this contains all the tuples <varName, entityQo, entityQr> for each varName
						// that is going to be instantiated
						List<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList = new ArrayList<>();
						for (Var tv : qTempVarSet) {
							// log.info("Var tv: " +tv.getVarName());
							// log.info("Var tv: " +tv.getName());
							RDFNode node = sol.get(tv.getName());
							// log.info("RDFNode node: " +node.toString());

							SPARQLQueryInstantiation instOP = new SPARQLQueryInstantiation();
							childQueryCopyInstanciated = instOP.instantiateVarTemplate(childQueryCopy, tv,
									node.asNode());

							String entityQo = getEntityQo(tv);
							String entityQr = node.asNode().getURI(); // as it is the name of a concrete node and not of
																		// a variable;
							VarTemplateAndEntityQoQr item = new VarTemplateAndEntityQoQr(tv, entityQo, entityQr);
							templVarEntityQoQrInstanciatedList.add(item);
						}
						if (childQueryCopyInstanciated != null) {
							// 4.1.2. Check if it is alredy indexed and therefore generated
							if (!(isQueryIndexed(childQueryCopyInstanciated))) {
								// add qWithoutTriple to the index
								addQueryToIndexIFAbsent(childQueryCopyInstanciated);

								// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...

								if (satisfiability.isSatisfiableWrtResults(childQueryCopyInstanciated)) {
									QueryAndContextNode childNode = createQctxForInstantiation(
											childQueryCopyInstanciated, parentNode, templVarEntityQoQrInstanciatedList);

									// ======
									// Ho commentato questa riga perche non ha un senso logico. Non c'e' motivo
									// di aggiungere la query istanziata nella lista di query da specializzare.
									// did it 13/05/2017
									// addSpecializableQueryList(childNode);
									// invece, ho aggiunto questa in quanto la query e' pronta per essere
									// raccomandata e non avra piu :
									this.recommendations.add(childNode);

									// =====

									log.info("qR score ======" + childNode.getqRScore());
									log.info("qR " + childNode.getTransformedQuery());

									notifyQueryRecommendation(childNode.getTransformedQuery(), childNode.getqRScore());

									// add qWithoutTriple to the index
									addQueryToIndexIFAbsent(childQueryCopyInstanciated);
								} else {
									addQueryToIndexIFAbsent(childQueryCopyInstanciated);
								}
							}
						}
					} // end for
				}
			}

		} // end while
		this.notifyQueryRecommendationCompletion(true);
		return this.recommendations;
	}

	private void addQueryToIndexIFAbsent(Query qWithoutTriple) {
		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
		ArrayList<String> s = new ArrayList<>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection)
			s.add(tp.toString());
		queryIndex.putIfAbsent(s.toString(), qWithoutTriple);
	}

	private Query applyInstanciationOP(Query queryChild, QuerySolution sol) {

		Set<Var> qTempVarSet = getQueryTemplateVariableSet(queryChild);
		for (Var tv : qTempVarSet) {
			RDFNode node = sol.get(tv.getName());
			SPARQLQueryInstantiation instOP = new SPARQLQueryInstantiation();
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
			RemoveTriple instance = new RemoveTriple();
			Query qWithoutTriple = instance.removeTP(qRCopy, tp.asTriple());

			// 2. Check if it is alredy indexed and therefore generated
			if (!(isQueryIndexed(qWithoutTriple))) {

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
		float qSpecificitySim = 1 - (qSpecDistSimVar + qSpecDistSimTriplePattern);
		node.setQuerySpecificityDistance(qSpecificitySim);

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

	private float computeInstanciateOperationCost(String entityqO, String entityqR) {
		if (entityqO == null || entityqR == null) return (float) 0.0;
		JaroWinklerSimilarity jwSim = new JaroWinklerSimilarity();
		float sim = jwSim.computeMatchingScore(entityqO, entityqR);
		// return (float) (1.0 - sim);
		return sim;
	}

	private float computeInstanciationOperationCost(List<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList) {
		int size = templVarEntityQoQrInstanciatedList.size();
		float nodeCost = 0;
		if (size > 0) {
			for (VarTemplateAndEntityQoQr item : templVarEntityQoQrInstanciatedList) {
				String entityqO_TMP = getLocalName(item.getEntityQo());
				String entityqR_TMP = getLocalName(item.getEntityQr());
				nodeCost = nodeCost + computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
			}
			return (float) 1 - (nodeCost / size); // we divide by size as we want the value to be between 0 and 1.
		}
		return 0;
	}

	private float computeRemoveOperationCost(Query originalQuery, Query childQuery) {
		QueryGPESim queryGPEsim = new QueryGPESim();
		float sim = queryGPEsim.computeQueryPatternsSim(originalQuery, childQuery);
		return (float) 1.0 - sim;
		// return sim;
		// return 0;
	}

	private QueryAndContextNode createQctxForInstantiation(Query queryPostOp, QueryAndContextNode parentNode,
			List<VarTemplateAndEntityQoQr> tplVarEntityQoQrInstantiated) {
		QueryAndContextNode node = new QueryAndContextNode();

		// (a) Set the queries on the node
		node.setOriginalQuery(QueryFactory.create(parentNode.getOriginalQuery()));
		node.setTransformedQuery(QueryFactory.create(queryPostOp.toString()));

		// (b) Set the (cloned) datasets on the node
		// XXX cloning the dataset object for the child nodes, why?
		IRDFDataset rdfD1 = parentNode.getRdfD1();
		if (rdfD1 instanceof SparqlIndexedDataset) {
			IRDFDataset clone = new SparqlIndexedDataset((String) parentNode.getRdfD1().getEndPointURL(),
					(String) parentNode.getRdfD1().getGraph());
			node.setRdfD1(clone);
		} else { // TO ADD the case of FILEBASED dataset
		}
		IRDFDataset rdfD2 = parentNode.getRdfD2();
		if (rdfD2 instanceof SparqlIndexedDataset) {
			IRDFDataset clone = new SparqlIndexedDataset(((String) parentNode.getRdfD2().getEndPointURL()),
					(String) parentNode.getRdfD2().getGraph());
			node.setRdfD2(clone);
			// Clone the solution space (XXX why?)
			node.setSolutionSpace(new ArrayList<>(parentNode.getQueryTempVarSolutionSpace()));
		} else { // TO ADD the case of FILEBASED dataset
		}

		// The following is no longer being done:
		// - set class, object/datatype property sets etc. on the node
		// - keep track of the operations list on each node (was not being used)
		node.setOp(OPID_INSTANTIATE);

		// ...set the score measurements
		computeCommonScores(node, parentNode);

		// 1)...QueryRootDistance
		float newQueryRootDist = parentNode.getQueryRootDistance()
				+ computeInstanciationOperationCost(tplVarEntityQoQrInstantiated);
		node.setQueryRootDistance(newQueryRootDist);
		float queryRootDistSim = 1 - newQueryRootDist;
		node.setQueryRootDistanceSimilarity(queryRootDistSim);

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float newResulttypeSim = qRTS.computeQueryResultTypeDistance(node.getOriginalQuery(), this.rdfd1,
				node.getTransformedQuery(), this.rdfd2);
		node.setQueryResultTypeSimilarity(newResulttypeSim);

		float recommendedQueryScore = (queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * newResulttypeSim)
				+ (querySpecificityDistanceDegree * node.getQuerySpecificityDistance());

		// float recommentedQueryScore = (newResulttypeSim +
		// (qSpecDistSimVar+qSpecDistSimTriplePattern));
		// log.info("recommentedQueryScoreI " +recommentedQueryScore);
		//// float justSUM= 1* newQueryRootDist +newResulttypeSim +
		// (qSpecDistVar/qSpecDistSimTriplePattern);
		//// log.info("recommentedQueryScoreI2 "+justSUM );

		node.setqRScore(recommendedQueryScore);

		return node;
	}

	private QueryAndContextNode createQctxForRemoval(Query queryPostOp, QueryAndContextNode parentNode)
			throws TooGeneralException {
		QueryAndContextNode node = new QueryAndContextNode();
		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentNode.getOriginalQuery());
		node.setOriginalQuery(clonedqO);

		Query clonedqR = QueryFactory.create(queryPostOp.toString());
		node.setTransformedQuery(clonedqR);

		// ...set the entities: EntityqO and EntityqR
		node.setEntityqO("");
		node.setEntityqR("");
		// ..set the RDF dataset 1
		IRDFDataset rdfD1 = parentNode.getRdfD1();
		if (rdfD1 instanceof SparqlIndexedDataset) {
			IRDFDataset newRdfD1 = new SparqlIndexedDataset(((String) parentNode.getRdfD1().getEndPointURL()),
					(String) parentNode.getRdfD1().getGraph());
			node.setRdfD1(newRdfD1);
		} else { // TO ADD the case of FILEBASED dataset
		}
		// ..set the RDF dataset 2
		IRDFDataset rdfD2 = parentNode.getRdfD2();
		if (rdfD2 instanceof SparqlIndexedDataset) {
			IRDFDataset newRdfD2 = new SparqlIndexedDataset(((String) parentNode.getRdfD2().getEndPointURL()),
					(String) parentNode.getRdfD2().getGraph());
			node.setRdfD2(newRdfD2);
			// //C. Compute the QueryTempVarSolutionSpace
			// QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			// // [REPLACED] List<QuerySolution> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2);
			// //Map<Var, Set<RDFNode>> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2, null);
			// childQueryAndContextNode.setQueryTempVarValueMap(qTsolMap);
			// List<QuerySolution> qTsol =
			// parentQueryAndContextNode.getQueryTempVarSolutionSpace();
			List<QuerySolution> qTsolChild = new ArrayList();

			// Compute the QueryTempVarSolutionSpace
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			List<QuerySolution> qTsolTMP = temVarValueSpace.computeTempVarSolutionSpace(queryPostOp, this.rdfd2);

			// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
			// <http://purl.org/dc/terms/title> ),
			List<QuerySolution> qTsolCleaned = eliminateSolutionsBoundToSameValue(qTsolTMP);

			qTsolChild.addAll(qTsolCleaned);
			node.setSolutionSpace(qTsolCleaned);
		} else { // TO ADD the case of FILEBASED dataset
		}

		// The following is no longer being done:
		// - set class, object/datatype property sets etc. on the node
		// - keep track of the operations list on each node (was not being used)
		node.setOp(OPID_TP_REMOVE);

		// ...set the score measurements
		computeCommonScores(node, parentNode);
		/*
		 * Compute the query recommentedQueryScore: 1)QueryRootDistance
		 */
		float newQueryRootDist = parentNode.getQueryRootDistance()
				+ computeRemoveOperationCost(node.getOriginalQuery(), node.getTransformedQuery());
		node.setQueryRootDistance(newQueryRootDist);
		float queryRootDistSim = 1 - newQueryRootDist;
		node.setQueryRootDistanceSimilarity(queryRootDistSim);

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;
		node.setQueryResultTypeSimilarity(resultTypeSim);

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
				+ (resultTypeSimilarityDegree * resultTypeSim)
				+ (querySpecificityDistanceDegree * node.getQuerySpecificityDistance());
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
		// + (resultTypeSimilarityDegree * resultTypeSim)
		// + (querySpecificityDistanceDegree * (qSpecificitySim)));
		// float recommentedQueryScore = (
		// ( newResulttypeSim) +
		// ( (qSpecDistSimVar+qSpecDistSimTriplePattern)));
		node.setqRScore(recommendedQueryScore);

		return node;
	}

	private QueryAndContextNode createNoOpQctx(Query qo) throws Exception {
		QueryAndContextNode qCtx = new QueryAndContextNode();
		qCtx.setOriginalQuery(QueryFactory.create(qo));
		qCtx.setTransformedQuery(QueryFactory.create(qo));
		qCtx.setqRScore(1);
		return qCtx;
	}

	// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	// <http://purl.org/dc/terms/title> )
	private List<QuerySolution> eliminateSolutionsBoundToSameValue(List<QuerySolution> qSolList) {
		List<QuerySolution> output = new ArrayList<>();
		if (qSolList.isEmpty()) return qSolList;
		for (QuerySolution qs : qSolList) {
			List<String> valuesList = new ArrayList<>();
			Iterator<String> varIter = qs.varNames();
			while (varIter.hasNext()) {
				String varName = varIter.next();
				valuesList.add(qs.get(varName).toString());
			}
			if (valuesList.size() == 1) output.add(qs);
			else {
				String firstValue = valuesList.get(0);
				valuesList.remove(0);
				boolean isAllDifferent = true;
				Iterator<String> varValueIter = valuesList.iterator();
				while (varValueIter.hasNext() && isAllDifferent) {
					String varValue = varValueIter.next();
					if (varValue.equals(firstValue)) isAllDifferent = false;
				}
				if (isAllDifferent) output.add(qs);
			}
		}
		return output;
	}

	private String getEntityQo(Var tv) {
		String varName = tv.getVarName();
		VarMapping map;
		if (varName.startsWith(TEMPLATE_VAR_CLASS)) map = this.classVarTable;
		else if (varName.startsWith(TEMPLATE_VAR_PROP_OBJ)) map = this.objectProperyVarTable;
		else if (varName.startsWith(TEMPLATE_VAR_PROP_DT)) map = this.datatypePropertyVarTable;
		else return "";
		return map.getValueFromVar(varName);
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
		} else return entityqO;
	}

	private Set<Var> getQueryTemplateVariableSet(Query qR) {
		TemplateVariableScanner v = new TemplateVariableScanner();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getTemplateVariables();
	}

	private Set<TriplePath> getQueryTriplePathSet(Query q) {
		if (q == null) throw new IllegalArgumentException("Query cannot be null");
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

	private boolean isIProcessable(QueryAndContextNode qRScoreMaxNodeCloned) {
		Set<Var> tempVarSet = getQueryTemplateVariableSet(qRScoreMaxNodeCloned.getTransformedQuery());
		return tempVarSet.size() > 0;
	}

	private boolean isQueryIndexed(Query qWithoutTriple) {
		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
		ArrayList<String> s = new ArrayList<>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection)
			s.add(tp.toString());
		Collections.sort(s);
		return queryIndex.containsKey(s.toString());
	}

	/**
	 * Takes the highest-scored query from a list of specializable queries and
	 * removes it.
	 * 
	 * @return
	 */
	private QueryAndContextNode popTopScoredQueryCtx(List<QueryAndContextNode> rankedList) {
		if (rankedList.size() > 0) {
			QueryAndContextNode maxNode = rankedList.get(0);
			rankedList.remove(maxNode);
			return maxNode;
		}
		return null;
	}

}
