package uk.ac.open.kmi.squire.evaluation;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * A collection of coefficients to be used as weights when scoring a
 * recommendation.
 * 
 * @author alessandro
 *
 */
public class Measures {

	public enum Metrics {
		QUERY_BINDING_COLLAPSE_RATE, QUERY_ROOT_DISTANCE, QUERY_SPECIFICITY_DISTANCE, QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN, QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE, RESULT_SIZE_SIMILARITY, RESULT_TYPE_SIMILARITY
	}

	public float queryBindingCollapseRate = 1.0f;
	
	public float queryRootDistanceCoefficient = 1.0f;

	public float querySpecificityDistanceCoefficient = 1.0f;

	public float resultSizeSimilarityCoefficient = 1.0f;

	public float resultTypeSimilarityCoefficient = 1.0f;

	private Logger log = LoggerFactory.getLogger(getClass());

	private Query q0, qS;

	private QueryRootDistance qRootDist;

	private QueryResultTypeDistance qRTS;

	private QuerySpecificityDistance qSpecDist;
	
	private QueryBindingCollapse qColl;

	private IRDFDataset srcDs, tgtDs;

	public Measures() {
		qSpecDist = new QuerySpecificityDistance();
		qRootDist = new QueryRootDistance();
		qRTS = new QueryResultTypeDistance();
		qColl = new QueryBindingCollapse();
	}

	public Measures(float resultTypeSimilarityCoefficient, float queryRootDistanceCoefficient,
			float resultSizeSimilarityCoefficient, float querySpecificityDistanceCoefficient) {
		this.resultTypeSimilarityCoefficient = resultTypeSimilarityCoefficient;
		this.queryRootDistanceCoefficient = queryRootDistanceCoefficient;
		this.resultSizeSimilarityCoefficient = resultSizeSimilarityCoefficient;
		this.querySpecificityDistanceCoefficient = querySpecificityDistanceCoefficient;
	}

	public float compute(Metrics metric) {
		float score = 0.0f;
		switch (metric) {
		case QUERY_BINDING_COLLAPSE_RATE:
			if (getOriginalQuery() == null || getTransformedQuery() == null) throw new IllegalArgumentException(
					"Query specificity measures require that both the original and the transformed query be set."
							+ " Please do so by calling setOriginalQuery() and setTransformedQuery().");
			score = qColl.compute(getOriginalQuery(), getTransformedQuery());
			break;
		case QUERY_ROOT_DISTANCE:
			log.warn("Query Root distance implementation is empty! returning default score", score);
			break;
		case QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE:
			if (getOriginalQuery() == null || getTransformedQuery() == null) throw new IllegalArgumentException(
					"Query specificity measures require that both the original and the specialised query be set."
							+ " Please do so by calling setOriginalQuery() and setSpecializedQuery().");
			score = qSpecDist.computeQSDwrtQueryVariable(getOriginalQuery(), getTransformedQuery());
			break;
		case QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN:
			if (getOriginalQuery() == null || getTransformedQuery() == null) throw new IllegalArgumentException(
					"Query specificity measures require that both the original and the transformed query be set."
							+ " Please do so by calling setOriginalQuery() and setTransformedQuery().");
			score = qSpecDist.computeQSDwrtQueryTP(getOriginalQuery(), getTransformedQuery());
			break;
		case QUERY_SPECIFICITY_DISTANCE:
			score = 1 - (compute(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_VARIABLE)
					+ compute(Metrics.QUERY_SPECIFICITY_DISTANCE_WRT_TRIPLEPATTERN));
			break;
		case RESULT_SIZE_SIMILARITY:
			throw new NotImplementedException(
					"Result size similarity is deprecated and must not be used for the time being.");
		case RESULT_TYPE_SIMILARITY:
			if (getOriginalQuery() == null || getTransformedQuery() == null) throw new IllegalArgumentException(
					"Result type measures require that both the original and the transformed query be set."
							+ " Please do so by calling setOriginalQuery() and setTransformedQuery().");
			if (getSourceDataset() == null || getTargetDataset() == null) throw new IllegalArgumentException(
					"Result type measures require that both the source and the target dataset be set."
							+ " Please do so by calling setSourceDataset() and setTargetDataset().");
			float resulTtypeDist = qRTS.computeQueryResultTypeDistance(getOriginalQuery(), getSourceDataset(),
					getTransformedQuery(), getTargetDataset());
			score = 1 - resulTtypeDist;
			break;
		default:
			break;
		}
		return score;
	}

	public Query getOriginalQuery() {
		return this.q0;
	}

	public IRDFDataset getSourceDataset() {
		return this.srcDs;
	}

	public IRDFDataset getTargetDataset() {
		return this.tgtDs;
	}

	public Query getTransformedQuery() {
		return this.qS;
	}

	public void setOriginalQuery(Query q) {
		this.q0 = q;
	}

	public void setSourceDataset(IRDFDataset d) {
		this.srcDs = d;
	}

	public void setTargetDataset(IRDFDataset d) {
		this.tgtDs = d;
	}

	public void setTransformedQuery(Query q) {
		this.qS = q;
	}

}
