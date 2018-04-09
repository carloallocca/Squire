package uk.ac.open.kmi.squire.evaluation;

/**
 * A collection of coefficients to be used as weights when scoring a
 * recommendation.
 * 
 * @author alessandro
 *
 */
public class Metrics {

	public float queryRootDistanceCoefficient = 1.0f;

	public float querySpecificityDistanceCoefficient = 1.0f;

	public float resultSizeSimilarityCoefficient = 1.0f;

	public float resultTypeSimilarityCoefficient = 1.0f;

	public Metrics() {
	}

	public Metrics(float resultTypeSimilarityCoefficient, float queryRootDistanceCoefficient,
			float resultSizeSimilarityCoefficient, float querySpecificityDistanceCoefficient) {
		this.resultTypeSimilarityCoefficient = resultTypeSimilarityCoefficient;
		this.queryRootDistanceCoefficient = queryRootDistanceCoefficient;
		this.resultSizeSimilarityCoefficient = resultSizeSimilarityCoefficient;
		this.querySpecificityDistanceCoefficient = querySpecificityDistanceCoefficient;
	}

}
