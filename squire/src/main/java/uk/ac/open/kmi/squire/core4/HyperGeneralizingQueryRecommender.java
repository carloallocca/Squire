package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;

import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

public class HyperGeneralizingQueryRecommender extends QueryRecommendator4 {

	public HyperGeneralizingQueryRecommender(Query query, IRDFDataset d1, IRDFDataset d2,
			float resultTypeSimilarityDegree, float queryRootDistanceDegree, float resultSizeSimilarityDegree,
			float querySpecificityDistanceDegree) {
		super(query, d1, d2, resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
				querySpecificityDistanceDegree);
	}

	public void buildRecommendation() {
		ProgrammableGeneralizer gen = new ProgrammableGeneralizer(getQuery(), getSourceDataset(), getTargetDataset());
		List<QueryAndContextNode> recoms = new ArrayList<>();
		for (Query qGen : gen.generalizeMultiple()) {
			Specializer qS = new Specializer(this.q0, qGen, this.rdfD1, this.rdfD2, gen,
					getMetrics().resultTypeSimilarityCoefficient, getMetrics().queryRootDistanceCoefficient,
					getMetrics().resultSizeSimilarityCoefficient, getMetrics().querySpecificityDistanceCoefficient,
					this.token);
			qS.register(this);
			qS.specialize();
			recoms.addAll(qS.getRecommendations());
		}
		rankRecommendations(recoms);
	}

}
