package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.core2.QueryCtxNode;
import uk.ac.open.kmi.squire.evaluation.Measures;
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

	protected boolean strict = false;

	public QueryRecommendator4(Query query, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree) {
		this(query, d1, d2, resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
				querySpecificityDistanceDegree, false);
	}

	public QueryRecommendator4(Query query, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree,
			boolean strict) {
		super(query, d1, d2, new Measures(resultTypeSimilarityDegree, queryRootDistanceDegree,
				resultSizeSimilarityDegree, querySpecificityDistanceDegree));
		this.qTemplate = new HashSet<>();
		this.strict = strict;
	}

	public void buildRecommendation() {
		this.qTemplate.clear();
		IRDFDataset d1 = getSourceDataset(), d2 = getTargetDataset();

		// GENERALIZE...
		// TODO allow generalizer pipelines
		BasicGeneralizer genOp = new ClassSignatureGeneralizer(d1, d2); // = new BasicGeneralizer(d1, d2);
		this.qTemplate.addAll(genOp.generalize(getQuery()));

		// SPECIALIZE...
		List<QueryCtxNode> recoms = new LinkedList<>();
		for (Query q : this.qTemplate) {
			Specializer spec = new 
					// Specializer
				GraphSearchSpecializer
					( getQuery(), q, d1, d2, genOp,
					getMetrics().resultTypeSimilarityCoefficient, getMetrics().queryRootDistanceCoefficient,
					getMetrics().resultSizeSimilarityCoefficient, getMetrics().querySpecificityDistanceCoefficient,
					strict, this.token);
			spec.register(this);
			spec.specialize();
			recoms.addAll(spec.getSpecializations());
		}

		// RANK...
		rankRecommendations(recoms);
	}

	@Override
	public List<QueryScorePair> getRecommendations() {
		return orderedRecommendations;
	}

	protected void rankRecommendations(List<QueryCtxNode> qRList) {
		for (QueryCtxNode qrRecom : qRList) {
			QueryScorePair pair = new QueryScorePair(qrRecom.getTransformedQuery(), qrRecom.getqRScore());
			this.orderedRecommendations.add(pair);
		}
		Collections.sort(this.orderedRecommendations, QueryScorePair.queryScoreComp);
	}

}
