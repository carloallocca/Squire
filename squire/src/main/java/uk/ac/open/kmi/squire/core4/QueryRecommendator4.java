package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	/*
	 * Stores the output of the recommender.
	 */
	private List<QueryScorePair> orderedRecommendations = new ArrayList<>();

	protected final Set<Query> qTemplate;

	public QueryRecommendator4(Query query, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree) {
		super(query, d1, d2, new Metrics(resultTypeSimilarityDegree, queryRootDistanceDegree,
				resultSizeSimilarityDegree, querySpecificityDistanceDegree));
		this.qTemplate = new HashSet<>();
	}

	public void buildRecommendation() {
		this.qTemplate.clear();
		IRDFDataset d1 = getSourceDataset(), d2 = getTargetDataset();

		// GENERALIZE...
		// TODO allow generalizer pipelines
		BasicGeneralizer qG = new ClassSignatureGeneralizer(d1, d2); // = new BasicGeneralizer(d1, d2);
		this.qTemplate.addAll(qG.generalize(getQuery()));

		// SPECIALIZE...
		List<QueryAndContextNode> recoms = new LinkedList<>();
		for (Query q : this.qTemplate) {
			Specializer qS = new Specializer(getQuery(), q, d1, d2, qG, getMetrics().resultTypeSimilarityCoefficient,
					getMetrics().queryRootDistanceCoefficient, getMetrics().resultSizeSimilarityCoefficient,
					getMetrics().querySpecificityDistanceCoefficient, this.token);
			qS.register(this);
			qS.specialize();
			recoms.addAll(qS.getRecommendations());
		}

		// RANK...
		rankRecommendations(recoms);
	}

	@Override
	public List<QueryScorePair> getRecommendations() {
		return orderedRecommendations;
	}

	protected void rankRecommendations(List<QueryAndContextNode> qRList) {
		for (QueryAndContextNode qrRecom : qRList) {
			QueryScorePair pair = new QueryScorePair(qrRecom.getTransformedQuery(), qrRecom.getqRScore());
			this.orderedRecommendations.add(pair);
		}
		Collections.sort(this.orderedRecommendations, QueryScorePair.queryScoreComp);
	}

}
