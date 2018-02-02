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
import uk.ac.open.kmi.squire.entityvariablemapping.ClassVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.DatatypePropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.IndividualVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.LiteralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.ObjectPropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.RDFVocVarMapping;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendator4<T> extends AbstractQueryRecommendationObservable
		implements IQueryRecommendationObserver {

	private final IRDFDataset rdfD1;
	private final Query q0;
	private final Query q0Copy;
	private Query qTemplate;

	private final float resultTypeSimilarityDegree;
	private final float queryRootDistanceDegree;
	private final float resultSizeSimilarityDegree;
	private final float querySpecificityDistanceDegree;

	private final IRDFDataset rdfD2;

	/*
	 * This is for storing the output of the specializer
	 */
	private List<QueryAndContextNode> qRList = new ArrayList<>();

	/*
	 * This is for storing the output of the QueryRecommendator
	 */
	private List<QueryScorePair> sortedRecomQueryList = new ArrayList<>();

	private LiteralVarMapping literalVarTable;
	private ClassVarMapping classVarTable;
	private DatatypePropertyVarMapping datatypePropertyVarTable;
	private IndividualVarMapping individualVarTable;
	private ObjectPropertyVarMapping objectProperyVarTable;
	private RDFVocVarMapping rdfVocVarTable;

	private static final String CLASS_TEMPLATE_VAR = "ct";
	private static final String OBJ_PROP_TEMPLATE_VAR = "opt";
	private static final String DT_PROP_TEMPLATE_VAR = "dpt";
	private static final String INDIVIDUAL_TEMPLATE_VAR = "it";
	private static final String LITERAL_TEMPLATE_VAR = "lt";

	private static final String INSTANCE_OP = "I";
	private static final String REMOVE_TP_OP = "R";

	public QueryRecommendator4(Query query, IRDFDataset d1, IRDFDataset d2, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree) {
		q0 = QueryFactory.create(query.toString());
		q0Copy = QueryFactory.create(query.toString());
		rdfD1 = d1;
		classVarTable = new ClassVarMapping();
		individualVarTable = new IndividualVarMapping();
		literalVarTable = new LiteralVarMapping();
		objectProperyVarTable = new ObjectPropertyVarMapping();
		datatypePropertyVarTable = new DatatypePropertyVarMapping();
		rdfVocVarTable = new RDFVocVarMapping();
		rdfD2 = d2;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
	}

	public void buildRecommendation() throws Exception {

		// GENERALIZE...
		QueryGeneralizer4 qG = new QueryGeneralizer4(this.q0Copy, this.rdfD1, this.rdfD2);
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
		QuerySpecializer4 qS = new QuerySpecializer4(this.q0Copy, this.qTemplate, this.rdfD1, this.rdfD2,

				this.classVarTable, this.objectProperyVarTable, this.datatypePropertyVarTable, this.individualVarTable,
				this.literalVarTable, this.rdfVocVarTable,

				this.resultTypeSimilarityDegree, this.queryRootDistanceDegree, this.resultSizeSimilarityDegree,
				this.querySpecificityDistanceDegree, this.token);
		qS.register(this);
		qS.specialize();

		// RANKING...
		this.qRList = qS.getRecommandedQueryList();
		applyRankingToRecommandedQueryList(qRList);

		// for(QueryAndContextNode n:this.qRList){
		// log.info("[queryR] " +n.getqR());
		// log.info("[query Score] " +n.getqRScore());
		// }

	}

	public List<QueryScorePair> getSortedRecomQueryList() {
		return sortedRecomQueryList;
	}

	private void applyRankingToRecommandedQueryList(List<QueryAndContextNode> qRList) {
		for (QueryAndContextNode qrRecom : qRList) {
			QueryScorePair pair = new QueryScorePair(qrRecom.getqR(), qrRecom.getqRScore());
			this.sortedRecomQueryList.add(pair);
		}
		Collections.sort(this.sortedRecomQueryList, QueryScorePair.queryScoreComp);
	}

	@Override
	public void updateDatasetSimilarity(float simScore, String token) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	@Override
	public void updateQueryRecommendated(Query qR, float score, String token) {
		this.notifyQueryRecommendation(qR, score);
	}

	@Override
	public void updateSatisfiableMessage(String msg, String token) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	@Override
	public void updateSatisfiableValue(Boolean value, String token) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	@Override
	public void updateQueryRecommendationCompletion(Boolean finished, String token) {
		this.notifyQueryRecommendationCompletion(finished);
	}

}
