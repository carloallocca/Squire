/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.entityvariablemapping.GeneralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendator4 extends AbstractQueryRecommendationObservable implements IQueryRecommendationObserver {

	private VarMapping classVarTable;
	private VarMapping datatypePropertyVarTable;
	private VarMapping individualVarTable;
	private VarMapping literalVarTable;
	private VarMapping objectProperyVarTable;
	private VarMapping rdfVocVarTable;

	private final Query q0;

	/*
	 * This is for storing the output of the specializer
	 */
	// private List<QueryAndContextNode> qRList = new ArrayList<>();
	private Query qTemplate;
	private final IRDFDataset rdfD1, rdfD2;

	private final float queryRootDistanceDegree;
	private final float querySpecificityDistanceDegree;
	private final float resultSizeSimilarityDegree;
	private final float resultTypeSimilarityDegree;
	/*
	 * This is for storing the output of the QueryRecommendator
	 */
	private List<QueryScorePair> sortedRecomQueryList = new ArrayList<>();

	public QueryRecommendator4(Query query, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree) {
		q0 = QueryFactory.create(query.toString());
		rdfD1 = d1;
		classVarTable = new GeneralVarMapping();
		individualVarTable = new GeneralVarMapping();
		literalVarTable = new GeneralVarMapping();
		objectProperyVarTable = new GeneralVarMapping();
		datatypePropertyVarTable = new GeneralVarMapping();
		rdfVocVarTable = new GeneralVarMapping();
		rdfD2 = d2;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
	}

	public void buildRecommendation() throws Exception {

		// GENERALIZE...
		Generalizer qG = new Generalizer(this.q0, this.rdfD1, this.rdfD2);
		this.qTemplate = qG.generalize();
		System.out.println(" ");
		System.out.println("[QueryRecommendation, generalizeToQueryTemplate()] THE GENERALIZED QUERY: ");
		System.out.println(this.qTemplate.toString());

		this.classVarTable = qG.getClassVarTable();
		this.individualVarTable = qG.getIndividualVarTable();
		this.literalVarTable = qG.getLiteralVarTable();
		this.objectProperyVarTable = qG.getObjectProperyVarTable();
		this.datatypePropertyVarTable = qG.getDatatypePropertyVarTable();
		this.rdfVocVarTable = qG.getRdfVocVarTable();

		// SPECIALIZE...
		Specializer qS = new Specializer(this.q0, this.qTemplate, this.rdfD1, this.rdfD2, this.classVarTable,
				this.objectProperyVarTable, this.datatypePropertyVarTable, this.individualVarTable,
				this.literalVarTable, this.rdfVocVarTable, this.resultTypeSimilarityDegree,
				this.queryRootDistanceDegree, this.resultSizeSimilarityDegree, this.querySpecificityDistanceDegree,
				this.token);
		qS.register(this);
		qS.specialize();

		// RANKING...
		rankRecommendations(qS.getRecommendations());
	}

	@Override
	public void updateDatasetSimilarity(float simScore, String token) {
		// Nothing to do
	}

	@Override
	public void updateQueryRecommendated(Query qR, float score, String token) {
		this.notifyQueryRecommendation(qR, score); // Just propagate
	}

	@Override
	public void updateQueryRecommendationCompletion(Boolean finished, String token) {
		this.notifyQueryRecommendationCompletion(finished); // Just propagate
	}

	@Override
	public void updateSatisfiableValue(Query query, boolean value, String token) {
		// Nothing to do
	}

	private void rankRecommendations(List<QueryAndContextNode> qRList) {
		for (QueryAndContextNode qrRecom : qRList) {
			QueryScorePair pair = new QueryScorePair(qrRecom.getTransformedQuery(), qrRecom.getqRScore());
			this.sortedRecomQueryList.add(pair);
		}
		Collections.sort(this.sortedRecomQueryList, QueryScorePair.queryScoreComp);
	}

}
