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
import uk.ac.open.kmi.squire.entityvariablemapping.ClassVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.DatatypePropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.IndividualVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.LiteralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.ObjectPropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.RDFVocVarMapping;
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
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;
import uk.ac.open.kmi.squire.utils.PowerSetFactory;

/**
 *
 * @author carloallocca
 */
public class Specializer extends QueryOperator {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static String OPID_INSTANTIATE = "I";
	private static String OPID_TP_REMOVE = "R";

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

	public Specializer(Query qo, Query qr, IRDFDataset d1, IRDFDataset d2, ClassVarMapping cVM,
			ObjectPropertyVarMapping opVM, DatatypePropertyVarMapping dpVM, IndividualVarMapping indVM,
			LiteralVarMapping lVM, RDFVocVarMapping rdfVM, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree,
			String token) {

		super();

		this.qO = QueryFactory.create(qo.toString());
		this.qR = QueryFactory.create(qr.toString());

		this.rdfd1 = d1;
		this.rdfd2 = d2;

		this.classVarTable = cVM;
		this.individualVarTable = indVM;
		this.literalVarTable = lVM;
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
		List<String> operationList = new ArrayList<>();

		// B. Compute the qRTemplateVariableSet and qRTriplePatternSet
		Set<Var> qRTemplateVariableSet = getQueryTemplateVariableSet(this.qR);
		Set<TriplePath> qRTriplePatternSet = getQueryTriplePathSet(this.qR);

		// C. Compute the QueryTempVarSolutionSpace
		List<QuerySolution> qTsol;
		try {
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			qTsol = temVarValueSpace.computeTempVarSolutionSpace(qr, this.rdfd2);

			// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
			// <http://purl.org/dc/terms/title> ),
			qTsol = eliminateSolutionBindedToTheSameValue(qTsol);
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
		qAndcNode.setqO(qo);
		qAndcNode.setqR(qr);

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
		qAndcNode.setQuerySpecificityDistanceSimilarity(qSpecificitySim);

		qAndcNode.setQueryResultTypeSimilarity(resultTypeSim);
		qAndcNode.setQueryResultSizeSimilarity(queryResultSizeSimilarity);
		qAndcNode.setqRScore(recommentedQueryScore);

		qAndcNode.setOp(op);
		qAndcNode.setOperationList(operationList);

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
			log.debug("   - qO = {}", qctx.getOriginalQuery());
			log.debug("   - qR = {}", qctx.getTransformedQuery());
		}

		StatefulSPARQLQuerySatisfiable satisfiability = new StatefulSPARQLQuerySatisfiable(this.rdfd2);

		if ((this.specializables.size() == 1) && (this.specializables.get(0).getQueryTempVarSolutionSpace().isEmpty())
				&& (isIProcessable(this.specializables.get(0)))) {

			// log.info("WE WILL START THE SUB PROCESS SPECIALIZATION...");
			// 1. Get and Remove the QueryAndContextNode with qRScore max
			QueryAndContextNode parentQctx = popTopScoredQueryCtx();
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
				log.info("triplePathSubSet ::" + triplePathSubSet.toString());
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
					log.info("subQuery Remove operation::: " + subQuery.toString());
					if (!(isQueryIndexed(subQuery))) {
						// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...

						boolean b = false;

						try {
							b = satisfiability.isSatisfiableWrtResults(subQuery);
							log.info("isSatisfiableWRTResultsWithToken :: " + b);
						} catch (Exception ex) {
							log.info(ex.getMessage());
						}

						if (b) {
							log.info("subQuery Remove operation::: " + subQuery);
							QueryAndContextNode childNode = createNewQueryAndContextNodeForRemovalOp(subQuery,
									parentQctx);
							log.info("childNode Solution List... " + childNode.getQueryTempVarSolutionSpace().size());

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
				QueryAndContextNode childNode = createEmptyQueryAndContextNode(spec.getOriginalQuery());
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
			QueryAndContextNode parentNode = popTopScoredQueryCtx();
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
					log.info("queryChild Instanciation step: " + queryChild.toString());
					log.info("qSolList size: " + qSolList.size());

					for (QuerySolution sol : qSolList) {

						// for (int i = 0; i < 10; i++) {
						// // calling repeatedly to increase chances of a clean-up
						// System.gc();
						// }
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
									QueryAndContextNode childNode = createNewQueryAndContextNodeForInstanciateOp(
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

	private float computeInstanciateOperationCost(String entityqO, String entityqR) {
		if (entityqO == null || entityqR == null) {
			return (float) 0.0;
		}
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

	private QueryAndContextNode createEmptyQueryAndContextNode(Query qo) throws Exception {
		QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();

		Query clonedqO = QueryFactory.create(qo);
		childQueryAndContextNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(qo);
		childQueryAndContextNode.setqR(clonedqR);

		childQueryAndContextNode.setqRScore(1);

		return childQueryAndContextNode;

	}

	private QueryAndContextNode createNewQueryAndContextNodeForInstanciateOp(Query childQueryCopyInstanciated,
			QueryAndContextNode parentQueryAndContextNode,
			List<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList) {
		QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();
		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentQueryAndContextNode.getOriginalQuery());
		childQueryAndContextNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(childQueryCopyInstanciated.toString());
		childQueryAndContextNode.setqR(clonedqR);

		// XXX cloning the dataset object for the child nodes, why?
		// ..set the RDF dataset 1
		IRDFDataset rdfD1 = parentQueryAndContextNode.getRdfD1();
		if (rdfD1 instanceof SparqlIndexedDataset) {
			childQueryAndContextNode
					.setRdfD1(new SparqlIndexedDataset((String) parentQueryAndContextNode.getRdfD1().getEndPointURL(),
							(String) parentQueryAndContextNode.getRdfD1().getGraph()));
		} else { // TO ADD the case of FILEBASED dataset
		}
		// ..set the RDF dataset 2
		IRDFDataset rdfD2 = parentQueryAndContextNode.getRdfD2();
		if (rdfD2 instanceof SparqlIndexedDataset) {
			childQueryAndContextNode
					.setRdfD2(new SparqlIndexedDataset(((String) parentQueryAndContextNode.getRdfD2().getEndPointURL()),
							(String) parentQueryAndContextNode.getRdfD2().getGraph()));
			// //C. Compute the QueryTempVarSolutionSpace
			// QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			// // [REPLACED] List<QuerySolution> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2);
			// //Map<Var, Set<RDFNode>> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2, null);
			// childQueryAndContextNode.setQueryTempVarValueMap(qTsolMap);
			childQueryAndContextNode
					.setSolutionSpace(new ArrayList<>(parentQueryAndContextNode.getQueryTempVarSolutionSpace()));
		} else { // TO ADD the case of FILEBASED dataset
		}

		// //...SET THE CLASS, OBJECT AND DATATYPE PROPERTIES SETs...;
		// ArrayList<String> clonedcSetD2 = new ArrayList();
		// clonedcSetD2.addAll(parentQueryAndContextNode.getRdfD2().getClassSet());
		// childQueryAndContextNode.setcSetD2(clonedcSetD2);
		//
		// ArrayList<String> clonedDpSetD2 = new ArrayList();
		// clonedDpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getDatatypePropertySet());
		// childQueryAndContextNode.setDpSetD2(clonedDpSetD2);
		//
		// ArrayList<String> clonedOpSetD2 = new ArrayList();
		// clonedOpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getObjectPropertySet());
		// childQueryAndContextNode.setOpSetD2(clonedOpSetD2);
		//
		// ArrayList<String> clonedlSetD2 = new ArrayList();
		// clonedlSetD2.addAll(parentQueryAndContextNode.getRdfD2().getLiteralSet());
		// childQueryAndContextNode.setlSetD2(clonedlSetD2);
		//
		// ArrayList<String> clonedIndSetD2 = new ArrayList();
		// clonedIndSetD2.addAll(parentQueryAndContextNode.getRdfD2().getIndividualSet());
		// childQueryAndContextNode.setIndSetD2(clonedIndSetD2);
		//
		// ArrayList<String> clonedpSetD2 = new ArrayList();
		// clonedpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getPropertySet());
		// childQueryAndContextNode.setpSetD2(clonedpSetD2);
		//
		// ArrayList<String> clonedRdfVSetD2 = new ArrayList();
		// clonedRdfVSetD2.addAll(parentQueryAndContextNode.getRdfD2().getRDFVocabulary());
		// childQueryAndContextNode.setRdfVD2(clonedRdfVSetD2);
		// ...set the openration list
		ArrayList<String> clonedOperationList = new ArrayList();
		clonedOperationList.addAll(parentQueryAndContextNode.getOperationList());
		clonedOperationList.add(OPID_INSTANTIATE);
		childQueryAndContextNode.setOperationList(clonedOperationList);

		childQueryAndContextNode.setOp(OPID_INSTANTIATE);
		// ...set the score measurements

		// A. Compute the query recommentedQueryScore:
		// 1)...QueryRootDistance
		float newQueryRootDist = parentQueryAndContextNode.getQueryRootDistance()
				+ computeInstanciationOperationCost(templVarEntityQoQrInstanciatedList);
		childQueryAndContextNode.setQueryRootDistance(newQueryRootDist);

		float queryRootDistSim = 1 - newQueryRootDist;
		childQueryAndContextNode.setQueryRootDistanceSimilarity(queryRootDistSim);

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, this.qR);
		float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(this.qO, this.qR);
		float qSpecificitySim = 1 - (qSpecDistSimVar + qSpecDistSimTriplePattern);
		childQueryAndContextNode.setQuerySpecificityDistanceSimilarity(qSpecificitySim);

		// log.info("newQueryRootDistI " +newQueryRootDist);
		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float newResulttypeSim = qRTS.computeQueryResultTypeDistance(childQueryAndContextNode.getOriginalQuery(),
				this.rdfd1, childQueryAndContextNode.getTransformedQuery(), this.rdfd2);
		// log.info("newQueryRootDistI " +newQueryRootDist);
		log.info("newResulttypeSimI " + newResulttypeSim);
		childQueryAndContextNode.setQueryResultTypeSimilarity(newResulttypeSim);

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

		float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * newResulttypeSim)
				+ (querySpecificityDistanceDegree * (qSpecificitySim)));

		// float recommentedQueryScore = (newResulttypeSim +
		// (qSpecDistSimVar+qSpecDistSimTriplePattern));
		//
		// log.info("qSpecDistSimTriplePatternI " +qSpecDistSimTriplePattern);
		// log.info("qSpecDistSimVar/qSpecDistSimTriplePatternI "
		// +(qSpecDistSimVar+qSpecDistSimTriplePattern));
		// log.info("recommentedQueryScoreI " +recommentedQueryScore);
		//// float justSUM= 1* newQueryRootDist +newResulttypeSim +
		// (qSpecDistVar/qSpecDistSimTriplePattern);
		//// log.info("recommentedQueryScoreI2 "+justSUM );
		log.info("clonedqR I " + clonedqR);

		childQueryAndContextNode.setqRScore(recommentedQueryScore);

		return childQueryAndContextNode;

	}

	private QueryAndContextNode createNewQueryAndContextNodeForRemovalOp(Query qWithoutTriple,
			QueryAndContextNode parentQueryAndContextNode) throws Exception {
		QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();
		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentQueryAndContextNode.getOriginalQuery());
		childQueryAndContextNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(qWithoutTriple.toString());
		childQueryAndContextNode.setqR(clonedqR);

		// ...set the entities: EntityqO and EntityqR
		childQueryAndContextNode.setEntityqO("");
		childQueryAndContextNode.setEntityqR("");
		// ..set the RDF dataset 1
		IRDFDataset rdfD1 = parentQueryAndContextNode.getRdfD1();
		if (rdfD1 instanceof SparqlIndexedDataset) {
			IRDFDataset newRdfD1 = new SparqlIndexedDataset(
					((String) parentQueryAndContextNode.getRdfD1().getEndPointURL()),
					(String) parentQueryAndContextNode.getRdfD1().getGraph());
			childQueryAndContextNode.setRdfD1(newRdfD1);
		} else { // TO ADD the case of FILEBASED dataset
		}
		// ..set the RDF dataset 2
		IRDFDataset rdfD2 = parentQueryAndContextNode.getRdfD2();
		if (rdfD2 instanceof SparqlIndexedDataset) {
			IRDFDataset newRdfD2 = new SparqlIndexedDataset(
					((String) parentQueryAndContextNode.getRdfD2().getEndPointURL()),
					(String) parentQueryAndContextNode.getRdfD2().getGraph());
			childQueryAndContextNode.setRdfD2(newRdfD2);
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
			List<QuerySolution> qTsolTMP = temVarValueSpace.computeTempVarSolutionSpace(qWithoutTriple, this.rdfd2);

			// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
			// <http://purl.org/dc/terms/title> ),
			List<QuerySolution> qTsolCleaned = eliminateSolutionBindedToTheSameValue(qTsolTMP);

			qTsolChild.addAll(qTsolCleaned);
			childQueryAndContextNode.setSolutionSpace(qTsolCleaned);
		} else { // TO ADD the case of FILEBASED dataset
		}

		// ...set the set of classes, object property, datatype property,...;
		// ArrayList<String> clonedcSetD2 = new ArrayList();
		// clonedcSetD2.addAll(parentQueryAndContextNode.getRdfD2().getClassSet());
		// childQueryAndContextNode.setcSetD2(clonedcSetD2);
		//
		// ArrayList<String> clonedDpSetD2 = new ArrayList();
		// clonedDpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getDatatypePropertySet());
		// childQueryAndContextNode.setDpSetD2(clonedDpSetD2);
		//
		// ArrayList<String> clonedOpSetD2 = new ArrayList();
		// clonedOpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getObjectPropertySet());
		// childQueryAndContextNode.setOpSetD2(clonedOpSetD2);
		//
		// ArrayList<String> clonedlSetD2 = new ArrayList();
		// clonedlSetD2.addAll(parentQueryAndContextNode.getRdfD2().getLiteralSet());
		// childQueryAndContextNode.setlSetD2(clonedlSetD2);
		//
		// ArrayList<String> clonedIndSetD2 = new ArrayList();
		// clonedIndSetD2.addAll(parentQueryAndContextNode.getRdfD2().getIndividualSet());
		// childQueryAndContextNode.setIndSetD2(clonedIndSetD2);
		//
		// ArrayList<String> clonedpSetD2 = new ArrayList();
		// clonedpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getPropertySet());
		// childQueryAndContextNode.setpSetD2(clonedpSetD2);
		//
		// ArrayList<String> clonedRdfVSetD2 = new ArrayList();
		// clonedRdfVSetD2.addAll(parentQueryAndContextNode.getRdfD2().getRDFVocabulary());
		// childQueryAndContextNode.setRdfVD2(clonedRdfVSetD2);
		// ...set the openration list
		ArrayList<String> clonedOperationList = new ArrayList();
		clonedOperationList.addAll(parentQueryAndContextNode.getOperationList());
		clonedOperationList.add(OPID_TP_REMOVE);
		childQueryAndContextNode.setOperationList(clonedOperationList);

		childQueryAndContextNode.setOp(OPID_TP_REMOVE);
		// ...set the score measurements

		/*
		 * Compute the query recommentedQueryScore: 1)QueryRootDistance
		 */
		float newQueryRootDist = parentQueryAndContextNode.getQueryRootDistance() + computeRemoveOperationCost(
				childQueryAndContextNode.getOriginalQuery(), childQueryAndContextNode.getTransformedQuery());
		childQueryAndContextNode.setQueryRootDistance(newQueryRootDist);
		float queryRootDistSim = 1 - newQueryRootDist;
		childQueryAndContextNode.setQueryRootDistanceSimilarity(queryRootDistSim);

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, this.qR);
		float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(this.qO, this.qR);
		float qSpecificitySim = 1 - (qSpecDistSimVar + qSpecDistSimTriplePattern);
		childQueryAndContextNode.setQuerySpecificityDistanceSimilarity(qSpecificitySim);

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;
		childQueryAndContextNode.setQueryResultTypeSimilarity(resultTypeSim);

		log.debug("newResulttypeSimR = {}", resultTypeSim);

		// log.info("qSpecDistSimVarR " + Float.toString(qSpecDistVar));
		// log.info("qSpecDistSimTriplePatternR " + Float.toString(qSpecDistTP));
		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;
		// float recommentedQueryScore = ( (queryRootDistanceDegree * newQueryRootDist)
		// +
		// (resultTypeSimilarityDegree * newResulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar+qSpecDistTP)));

		// float recommentedQueryScore = ( ( newQueryRootDist) +
		// ( newResulttypeSim) +
		// ( (qSpecDistVar/qSpecDistSimTriplePattern)));
		// log.info("qSpecDistSimVar/qSpecDistSimTriplePattern " +
		// (qSpecDistSimVar+qSpecDistSimTriplePattern));
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree * resultTypeSim);

		float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * resultTypeSim) + (querySpecificityDistanceDegree * (qSpecificitySim)));

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
		// + (resultTypeSimilarityDegree * resultTypeSim)
		// + (querySpecificityDistanceDegree * (qSpecificitySim)));
		// float recommentedQueryScore = (
		// ( newResulttypeSim) +
		// ( (qSpecDistSimVar+qSpecDistSimTriplePattern)));
		log.info("recommentedQueryScoreR " + recommentedQueryScore);
		childQueryAndContextNode.setqRScore(recommentedQueryScore);

		log.info("qR R " + clonedqR);

		return childQueryAndContextNode;
	}

	// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	// <http://purl.org/dc/terms/title> ),
	private List<QuerySolution> eliminateSolutionBindedToTheSameValue(List<QuerySolution> qSolList) {
		List<QuerySolution> output = new ArrayList<>();

		if (!qSolList.isEmpty()) {
			for (QuerySolution qs : qSolList) {
				ArrayList<String> valuesList = new ArrayList<>();
				Iterator<String> varIter = qs.varNames();
				while (varIter.hasNext()) {
					String varName = varIter.next();
					valuesList.add(qs.get(varName).toString());
				}

				if (valuesList.size() == 1) {
					output.add(qs);
				} else {
					String firstValue = valuesList.get(0);
					// log.info("firstValue " +firstValue);
					valuesList.remove(0);
					boolean isAllDifferent = true;
					Iterator<String> varValueIter = valuesList.iterator();
					while ((varValueIter.hasNext()) && (isAllDifferent)) {
						String varValue = varValueIter.next();
						// log.info("varValue" +varValue);
						if (varValue.equals(firstValue)) {
							isAllDifferent = false;
						}
					}
					if (isAllDifferent) {
						output.add(qs);
					}
				}
			}
			return output;
		} else {
			return qSolList;
		}

	}

	private String getEntityQo(Var tv) {
		String entityQo = "";
		String varName = tv.getVarName();

		if (varName.startsWith(CLASS_TEMPLATE_VAR)) {
			entityQo = this.classVarTable.getClassFromVar(varName);
		} else if (varName.startsWith(OBJ_PROP_TEMPLATE_VAR)) {
			entityQo = this.objectProperyVarTable.getObjectProperyFromVar(varName);
		} else if (varName.startsWith(DT_PROP_TEMPLATE_VAR)) {
			entityQo = this.datatypePropertyVarTable.getDatatypeProperyFromVar(varName);
		}
		return entityQo;
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
		} else {
			return entityqO;
		}
	}

	private QueryAndContextNode popTopScoredQueryCtx() {

		// Collections.sort(this.specializableQueryAndContextNodeList, new
		// QueryAndContextNode.QRScoreComparator());
		if (this.specializables.size() > 0) {
			QueryAndContextNode maxNode = this.specializables.get(0);
			this.specializables.remove(maxNode);
			return maxNode;
		}
		return null;
	}

	private Set<Var> getQueryTemplateVariableSet(Query qR) {
		SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getQueryTemplateVariableSet();

	}

	private Set<TriplePath> getQueryTriplePathSet(Query q) {
		if (q == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::getTriplePathSet(Query originalQuery)]The query is null!!");
		}
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
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}
		Collections.sort(s);
		return queryIndex.containsKey(s.toString());
	}

}
