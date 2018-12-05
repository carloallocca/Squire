package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core2.QueryCtxNode;
import uk.ac.open.kmi.squire.core2.QueryTempVarSolutionSpace;
import uk.ac.open.kmi.squire.evaluation.Measures;
import uk.ac.open.kmi.squire.evaluation.Measures.Metrics;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeDistance;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.fca.Concept;
import uk.ac.open.kmi.squire.fca.InfMap;
import uk.ac.open.kmi.squire.fca.Lattice;
import uk.ac.open.kmi.squire.fca.QueryTemplateLattice;
import uk.ac.open.kmi.squire.operation.TooGeneralException;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * 
 * @author alessandro
 *
 */
public class BestFirstSpecializer extends AbstractMappedQueryTransform {

	private final IRDFDataset dFrom, dTo;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final boolean strict;

	private Query qG, qO;

	private List<QueryCtxNode> recommendations;

	public BestFirstSpecializer(Query originalQuery, Query generalQuery, IRDFDataset dFrom, IRDFDataset dTo,
			boolean strict) {
		super();
		recommendations = new ArrayList<>();
		this.qO = originalQuery;
		this.qG = generalQuery;
		this.dFrom = dFrom;
		this.dTo = dTo;
		this.strict = strict;
	}

	public List<QueryCtxNode> getSpecializations() {
		return recommendations;
	}

	public float score(Measures weights, Query qTransformed) {
		// 1)...QueryRootDistance ... we start with both value set to 0
		float queryRootDist = 0;
		float queryRootDistSim = 0;
		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, qTransformed);
		float qSpecDistTP = qSpecDist.computeQSDwrtQueryTP(this.qO, qTransformed);
		float qSpecificitySim = 1 - (qSpecDistVar + qSpecDistTP);
		// 3)...QueryResultTypeSimilarity
		QueryResultTypeDistance qRTS = new QueryResultTypeDistance();
		// float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO,
		// this.dFrom, qTransformed, this.dTo);
		// float resultTypeSim = 1 - resulTtypeDist;
		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;

		float score = (weights.queryRootDistanceCoefficient * queryRootDistSim)
				// + (weights.resultTypeSimilarityCoefficient * resultTypeSim)
				+ (weights.querySpecificityDistanceCoefficient * qSpecificitySim)
		// Formula does not consider result size similarity right now
		;
		return score;
	}

	public void specialize() {

		Measures base = new Measures();
		base.setOriginalQuery(this.qO);
		base.setSourceDataset(dFrom);
		base.setTargetDataset(dTo);
		List<QuerySolution> qTsol;
		try {
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			qTsol = temVarValueSpace.computeTempVarSolutionSpace(this.qG, this.dTo, this.strict);
			// Not doing this optimization now (didn't seem to work anyway)
			// // e.g. ( ?opt2 = dc:title ) ( ?opt1 = dc:title )
			log.debug("Solution space size BEFORE = {}", qTsol.size());
			qTsol = eliminateSolutionsBoundToSameValue(qTsol);
			log.debug("Solution space size AFTER = {}", qTsol.size());

			log.debug("General query:\r\n{}", this.qG);

			Lattice<GeneralizedQuery, Var> lattice = QueryTemplateLattice.buildLattice(this.qG, qTsol);
			// Navigate the lattice - we'll see later if we can do so without building it.

			Concept<GeneralizedQuery, Var> top = lattice.getTop();
			log.debug("Top Concept {}", top);
			log.debug(" - extension size = {}", top.getExtension().size());
			for (GeneralizedQuery gq : top.getExtension()) {
				log.debug(" -- {}", gq.getQuery());
			}

			float min_bind_coll = 128.0f; // minimize
			float min_qdist_tp = 128.0f; // minimize
			float min_qdist_var = 128.0f; // minimize
			float max_rt_sim = 0.0f; // maximize
			InfMap bestMap = new InfMap();

			Map<Query, Set<Concept<GeneralizedQuery, Var>>> queriesToConcepts = new HashMap<>();

			for (Concept<GeneralizedQuery, Var> inf : top.getInferiors()) {
				log.debug("Concept {}", inf.getIntension());
				log.debug(" - extension size = {}", inf.getExtension().size());
				for (GeneralizedQuery gq : inf.getExtension()) {

					if (!queriesToConcepts.containsKey(gq.getQuery()))
						queriesToConcepts.put(gq.getQuery(), new HashSet<>());
					queriesToConcepts.get(gq.getQuery()).add(inf);
					log.debug(" -- {}", gq.getQuery());
					base.setTransformedQuery(gq.getQuery());

					float qdist_tp = base.compute(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE);
					float qdist_var = base.compute(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN);
					float rt_sim = base.compute(Metrics.RESULT_TYPE_SIMILARITY);
					base.setOriginalQuery(this.qG); // FIXME should I set the original query to be the generalised one
													// for all?
					float bind_coll = base.compute(Metrics.QUERY_BINDING_COLLAPSE_RATE);

					log.debug(" - Binding collapse rate = {}", bind_coll);
					log.debug(" - Query spec. dist. (Var) = {}", qdist_tp);
					log.debug(" - Query spec. dist. (TP) = {}", qdist_var);
					log.debug(" - Res. type sim. = {}", rt_sim);

					QueryCtxNode qctx = new QueryCtxNode(gq.getQuery());
					qctx.setOriginalQuery(qO);
					qctx.setBindingCollapseRate(bind_coll);
					qctx.setQuerySpecificityDistanceTP(qdist_tp);
					qctx.setQuerySpecificityDistanceVar(qdist_var);
					qctx.setResultTypeSimilarity(rt_sim);
					if (bind_coll <= min_bind_coll) {
						if (bind_coll != min_bind_coll && bestMap.containsKey(Metrics.QUERY_BINDING_COLLAPSE_RATE))
							bestMap.get(Metrics.QUERY_BINDING_COLLAPSE_RATE).clear();
						bestMap.addOptimalNode(Metrics.QUERY_BINDING_COLLAPSE_RATE, qctx);
						min_bind_coll = bind_coll;
						log.debug("NEW candidate for LOWEST Binding collapse rate.");
					}
					if (qdist_tp <= min_qdist_tp) {
						if (qdist_tp != min_qdist_tp
								&& bestMap.containsKey(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN))
							bestMap.get(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN).clear();
						bestMap.addOptimalNode(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN, qctx);
						min_qdist_tp = qdist_tp;
						log.debug("NEW candidate for LOWEST Query spec. dist TP.");
					}
					if (qdist_var <= min_qdist_var) {
						if (qdist_var != min_qdist_var
								&& bestMap.containsKey(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE))
							bestMap.get(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE).clear();
						bestMap.addOptimalNode(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE, qctx);
						min_qdist_var = qdist_var;
						log.debug("NEW candidate for LOWEST Query spec. dist Var.");
					}
					if (rt_sim >= max_rt_sim) {
						if (rt_sim != max_rt_sim && bestMap.containsKey(Metrics.RESULT_TYPE_SIMILARITY))
							bestMap.get(Metrics.RESULT_TYPE_SIMILARITY).clear();
						bestMap.addOptimalNode(Metrics.RESULT_TYPE_SIMILARITY, qctx);
						max_rt_sim = rt_sim;
						log.debug("NEW candidate for HIGHEST Res. type Sim");
					}
				}
			}

			Set<QueryCtxNode> optimals = new HashSet<>();

			log.debug("Inspecting best candidates for each metric:");
			for (Metrics metric : bestMap.keySet()) {
				log.debug("Metric {} ({} queries)", metric, bestMap.getOptimalNodes(metric).size());
				for (QueryCtxNode qctx : bestMap.getOptimalNodes(metric)) {
					log.debug("Query:\r\n{}", qctx.getTransformedQuery());
					log.debug(" - Binding collapse rate = {}", qctx.getBindingCollapseRate());
					log.debug(" - Query spec. dist. (Var) = {}", qctx.getQuerySpecificityDistanceVar());
					log.debug(" - Query spec. dist. (TP) = {}", qctx.getQuerySpecificityDistanceTP());
					log.debug(" - Res. type sim. = {}", qctx.getResultTypeSimilarity());
					log.debug(" - part of concepts : {}", queriesToConcepts.get(qctx.getTransformedQuery()));
					if (max_rt_sim == qctx.getResultTypeSimilarity() && min_bind_coll == qctx.getBindingCollapseRate()
							&& min_qdist_tp == qctx.getQuerySpecificityDistanceTP()
							&& min_qdist_var == qctx.getQuerySpecificityDistanceVar())
						optimals.add(qctx);
				}
			}
			log.debug("Recall the original query: {}", qO);
			if (optimals.isEmpty())
				log.warn("No ideal branch was found, need to find a strategy for the visit plan!");
			else {
				log.debug("Best-scored candidates follow:");
				for (QueryCtxNode qctx : optimals) {
					log.debug("query: {}", qctx.getTransformedQuery());
				}

			}

		} catch (TooGeneralException gex) {
			log.warn("Query is too general to execute safely. Assuming solution exists.");
			log.warn(" * Query : '{}'", gex.getQuery());
			qTsol = new ArrayList<>();
			qTsol.add(new QuerySolutionMap());
		}

	}

	// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	// <http://purl.org/dc/terms/title> )
	private List<QuerySolution> eliminateSolutionsBoundToSameValue(List<QuerySolution> qSolList) {

		Set<Map<String, RDFNode>> kept = new HashSet<>();
		List<QuerySolution> output = new ArrayList<>();
		if (qSolList.isEmpty())
			return qSolList;

		for (QuerySolution qs : qSolList) {
			if (qs instanceof QuerySolutionMap && kept.contains(((QuerySolutionMap) qs).asMap()))
				continue;
			List<String> valuesList = new ArrayList<>();
			Iterator<String> varIter = qs.varNames();
			while (varIter.hasNext()) {
				String varName = varIter.next();
				valuesList.add(qs.get(varName).toString());
			}
			if (valuesList.size() == 1) {
				output.add(qs);
				if (qs instanceof QuerySolutionMap)
					kept.add(((QuerySolutionMap) qs).asMap());
			} else {
				String firstValue = valuesList.get(0);
				valuesList.remove(0);
				boolean isAllDifferent = true;
				Iterator<String> varValueIter = valuesList.iterator();
				while (varValueIter.hasNext() && isAllDifferent) {
					String varValue = varValueIter.next();
					if (varValue.equals(firstValue))
						isAllDifferent = false;
				}
				if (isAllDifferent) {
					output.add(qs);
					if (qs instanceof QuerySolutionMap)
						kept.add(((QuerySolutionMap) qs).asMap());
				}
			}
		}
		return output;
	}

}
