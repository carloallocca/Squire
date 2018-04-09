package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.evaluation.Metrics;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendator4 extends AbstractQueryRecommender {

	protected Query qTemplate;

	/*
	 * This is for storing the output of the QueryRecommendator
	 */
	private List<QueryScorePair> sortedRecomQueryList = new ArrayList<>();

	public QueryRecommendator4(Query query, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree) {
		super(query, d1, d2, new Metrics(resultTypeSimilarityDegree, queryRootDistanceDegree,
				resultSizeSimilarityDegree, querySpecificityDistanceDegree));
	}

	public void buildRecommendation() {

		// GENERALIZE...
		Generalizer qG = new Generalizer(getQuery(), getSourceDataset(), getTargetDataset());
		this.qTemplate = qG.generalize();

		// SPECIALIZE...
		Specializer qS = new Specializer(getQuery(), this.qTemplate, getSourceDataset(), getTargetDataset(), qG,
				getMetrics().resultTypeSimilarityCoefficient, getMetrics().queryRootDistanceCoefficient,
				getMetrics().resultSizeSimilarityCoefficient, getMetrics().querySpecificityDistanceCoefficient,
				this.token);
		qS.register(this);
		qS.specialize();

		// RANK...
		rankRecommendations(qS.getRecommendations());
	}

	protected void rankRecommendations(List<QueryAndContextNode> qRList) {
		for (QueryAndContextNode qrRecom : qRList) {
			QueryScorePair pair = new QueryScorePair(qrRecom.getTransformedQuery(), qrRecom.getqRScore());
			this.sortedRecomQueryList.add(pair);
		}
		Collections.sort(this.sortedRecomQueryList, QueryScorePair.queryScoreComp);
	}

}
