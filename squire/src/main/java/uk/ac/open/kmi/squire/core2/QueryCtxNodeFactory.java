package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.NodeTransformation;
import uk.ac.open.kmi.squire.evaluation.QueryBindingCollapse;
import uk.ac.open.kmi.squire.evaluation.QueryGPESim;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeDistance;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.ontologymatching.JaroWinklerSimilarity;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.utils.StringUtils;

public class QueryCtxNodeFactory {

	private final IRDFDataset ds1, ds2;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Query qOriginal, qGeneral;

	private float resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
			querySpecificityDistanceDegree;

	private boolean strict = false;

	public QueryCtxNodeFactory(Query originalQuery, Query generalQuery, IRDFDataset ds1, IRDFDataset ds2,
			float resultTypeSimilarityDegree, float queryRootDistanceDegree, float resultSizeSimilarityDegree,
			float querySpecificityDistanceDegree, boolean strict) {
		this.qOriginal = originalQuery;
		this.qGeneral = generalQuery;
		this.ds1 = ds1;
		this.ds2 = ds2;
		this.strict = strict;

		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
	}

	/**
	 * Implemented as the complement of the average Jaro-Winkler similarity...
	 * 
	 * @param tplVarInstantiations
	 * @return
	 */
	public float computeInstantiationCost(List<NodeTransformation> tplVarInstantiations) {
		int size = tplVarInstantiations.size();
		float cumulatedCost = 0;
		if (size > 0) {
			for (NodeTransformation item : tplVarInstantiations) {
				String entityqO_TMP = StringUtils.getLocalName(item.getFrom().toString());
				String entityqR_TMP = StringUtils.getLocalName(item.getTo().toString());
				cumulatedCost += computeInstantiationCost(entityqO_TMP, entityqR_TMP);
			}
			// divide by size as we want the value to be between 0 and 1 (XXX why?)
			return 1f - (cumulatedCost / size);
		}
		return 0;
	}

	/**
	 * The cost of a single instantiation step
	 * 
	 * Instantiating an entity into another costs the Jaro-Winkler similarity of
	 * their respective names...
	 * 
	 * @param entityqO
	 *            the entity in the original query
	 * @param entityqR
	 *            the entity in the reformulated query
	 * @return
	 */
	public float computeInstantiationCost(String entityqO, String entityqR) {
		if (entityqO == null || entityqR == null || entityqO.equals(entityqR)) {
			log.warn("Instantiation to or from a null entity, or between equal entities, has no cost"
					+ " (from: {} , to: {}).", entityqO, entityqR);
			return 0f;
		}
		JaroWinklerSimilarity jwSim = new JaroWinklerSimilarity();
		float cost = jwSim.computeMatchingScore(entityqO, entityqR);
cost = 1f/cost;
		// log.debug("Instantiating from {} to {} costs {}.", entityqO, entityqR, sim);
		return cost;
	}

	public float computeInstantiationCost(String entityqO, String entityqR, Query qAfter) {
		float cost = computeInstantiationCost(entityqO, entityqR);
		cost += new QueryBindingCollapse().compute(qOriginal, qAfter);
		return cost;
	}

	public float computeRemoveOperationCost(Query originalQuery, Query childQuery) {
		QueryGPESim queryGPEsim = new QueryGPESim();
		float sim = queryGPEsim.computeQueryPatternLoss(originalQuery, childQuery);
		return 1f - sim;
	}

	public QueryCtxNode createNoOpQctx(Query qo) {
		QueryCtxNode qCtx = new QueryCtxNode(QueryFactory.create(qo));
		qCtx.setOriginalQuery(QueryFactory.create(qo));
		qCtx.setqRScore(1);
		return qCtx;
	}

	public QueryCtxNode createQctxForInstantiation(Query queryPostOp, QueryCtxNode parentNode,
			List<NodeTransformation> entityTransformations) {

		QueryCtxNode node = new QueryCtxNode(queryPostOp.cloneQuery());

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
		float newResulttypeSim = qRTS.compute(node.getOriginalQuery(), this.ds1, node.getTransformedQuery(), this.ds2);

		float recommendedQueryScore = (queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * newResulttypeSim) + (querySpecificityDistanceDegree
						* (1 - (node.getQuerySpecificityDistanceTP() + node.getQuerySpecificityDistanceVar())));

		node.setqRScore(recommendedQueryScore);

		return node;
	}

	public QueryCtxNode createQctxForRemoval(Query queryPostOp, QueryCtxNode parentNode) {

		QueryCtxNode node = new QueryCtxNode(queryPostOp.cloneQuery());

		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentNode.getOriginalQuery());
		node.setOriginalQuery(clonedqO);

		// Compute the QueryTempVarSolutionSpace

		List<QuerySolution> qTsolChild = new ArrayList<>();
		QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
		List<QuerySolution> qTsolTMP = temVarValueSpace.computeTempVarSolutionSpace(queryPostOp, this.ds2, strict);

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
		float resulTtypeDist = qRTS.compute(this.qOriginal, this.ds1, this.qGeneral, this.ds2);
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

	/**
	 * e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	 * <http://purl.org/dc/terms/title> )
	 */
	public List<QuerySolution> eliminateDuplicateBindings(List<QuerySolution> qSolList) {
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

	/**
	 * 
	 * @param node
	 * @param parentNode
	 * @param tplVarEntityQoQrInstantiated
	 * @return the modified input node
	 */
	private QueryCtxNode computeCommonScores(QueryCtxNode node, QueryCtxNode parentNode) {

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		Query orig = node.getOriginalQuery(), transf = node.getTransformedQuery();
		float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(orig, transf);
		float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(orig, transf);
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

}
