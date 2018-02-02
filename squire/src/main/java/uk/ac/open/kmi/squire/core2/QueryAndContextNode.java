/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryAndContextNode {

	public static class QRScoreComparator implements Comparator<QueryAndContextNode> {
		// @Override
		public int compare(QueryAndContextNode p1, QueryAndContextNode p2) {
			float score1 = p1.getqRScore();
			float score2 = p2.getqRScore();
			// ...For ascending order
			return Float.compare(score2, score1);
			// return (-1)*Float.compare(score1, score2); // For discending order
		}
	}

	public static Comparator<QueryAndContextNode> queryScoreComp = new Comparator<QueryAndContextNode>() {

		@Override
		public int compare(QueryAndContextNode o1, QueryAndContextNode o2) {
			float score1 = o1.getqRScore();
			float score2 = o2.getqRScore();
			// ...For ascending order
			return Float.compare(score2, score1);
			// return (-1)*Float.compare(score1, score2); // For discending order
		}
	};

	private IRDFDataset rdfD1;
	private IRDFDataset rdfD2;

	// private Set<Var> qRTemplateVariableSet;
	// private Set<TriplePath> qRTriplePathSet;

	private Query qO;
	private Query qR;

	private String entityqO;
	private String entityqR;

	private List<String> operationList;
	private String op; // It can be either R (for Removal) or I (Instanciation).
	private float queryResultTypeSimilarity;

	private float queryRootDistance;
	private float queryRootDistanceSim;

	private float queryResultSizeSimilarity;

	// private ArrayList<VarTemplateAndEntityQoQr> tvEntityQoQrInstanciatedList;

	private float querySpecificityDistance;

	private float qRScore;
	private List<String> indSetD2, dpSetD2, cSetD2, opSetD2, lSetD2, rdfVD2, pSetD2;

	private List<QuerySolution> queryTempVarSolutionSpace;

	private Map<Var, Set<RDFNode>> queryTempVarValueMap;

	public QueryAndContextNode() {
		super();
	}

	public QueryAndContextNode cloneMe(QueryAndContextNode qRScoreMaxNode) {

		QueryAndContextNode clonedNode = new QueryAndContextNode();

		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(qRScoreMaxNode.getqO().toString());
		clonedNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(qRScoreMaxNode.getqR().toString());
		clonedNode.setqR(clonedqR);

		clonedNode.setRdfD1(qRScoreMaxNode.getRdfD1());
		clonedNode.setRdfD2(qRScoreMaxNode.getRdfD2());

		String clonedEntityqO = qRScoreMaxNode.getEntityqO();
		clonedNode.setEntityqO(clonedEntityqO);

		String clonedEntityqR = qRScoreMaxNode.getEntityqR();
		clonedNode.setEntityqR(clonedEntityqR);

		// ...set the set of classes, object property, datatype property,...;
		List<String> clonedcSetD2 = new ArrayList<>();
		clonedcSetD2.addAll(qRScoreMaxNode.getRdfD2().getClassSet());
		clonedNode.setcSetD2(clonedcSetD2);

		List<String> clonedDpSetD2 = new ArrayList<>();
		clonedDpSetD2.addAll(qRScoreMaxNode.getRdfD2().getDatatypePropertySet());
		clonedNode.setDpSetD2(clonedDpSetD2);

		List<String> clonedOpSetD2 = new ArrayList<>();
		clonedOpSetD2.addAll(qRScoreMaxNode.getRdfD2().getObjectPropertySet());
		clonedNode.setOpSetD2(clonedOpSetD2);

		List<String> clonedlSetD2 = new ArrayList<>();
		clonedlSetD2.addAll(qRScoreMaxNode.getRdfD2().getLiteralSet());
		clonedNode.setlSetD2(clonedlSetD2);

		List<String> clonedIndSetD2 = new ArrayList<>();
		clonedIndSetD2.addAll(qRScoreMaxNode.getRdfD2().getIndividualSet());
		clonedNode.setIndSetD2(clonedIndSetD2);

		List<String> clonedpSetD2 = new ArrayList<>();
		clonedpSetD2.addAll(qRScoreMaxNode.getRdfD2().getPropertySet());
		clonedNode.setpSetD2(clonedpSetD2);

		List<String> clonedRdfVSetD2 = new ArrayList<>();
		clonedRdfVSetD2.addAll(qRScoreMaxNode.getRdfD2().getRDFVocabulary());
		clonedNode.setRdfVD2(clonedRdfVSetD2);

		// ...set the score measurements

		float clonedQueryRootDistance = qRScoreMaxNode.getQueryRootDistance();
		clonedNode.setQueryRootDistanceSimilarity(clonedQueryRootDistance);

		float clonedQueryResultTypeSimilarity = qRScoreMaxNode.getQueryResultTypeSimilarity();
		clonedNode.setQueryResultTypeSimilarity(clonedQueryResultTypeSimilarity);

		float clonedQuerySpecificityDistance = qRScoreMaxNode.getQuerySpecificityDistance();
		clonedNode.setQuerySpecificityDistanceSimilarity(clonedQuerySpecificityDistance);

		float clonedQueryResultSizeSimilarity = qRScoreMaxNode.getQueryResultSizeSimilarity();
		clonedNode.setQueryResultSizeSimilarity(clonedQueryResultSizeSimilarity);

		float clonedqRScore = qRScoreMaxNode.getqRScore();
		clonedNode.setqRScore(clonedqRScore);

		String clonedOp = qRScoreMaxNode.getOp();
		clonedNode.setOp(clonedOp);

		List<String> clonedOperationList = new ArrayList<>();
		clonedOperationList.addAll(qRScoreMaxNode.getOperationList());
		clonedNode.setOperationList(clonedOperationList);

		// Set<Var> clonedqRTemplateVariableSet = new HashSet();
		// clonedqRTemplateVariableSet.addAll(qRScoreMaxNode.getqRTemplateVariableSet());
		// clonedNode.setqRTemplateVariableSet(clonedqRTemplateVariableSet);
		//
		// Set<TriplePath> clonedqRTriplePathSet = new HashSet();
		// clonedqRTriplePathSet.addAll(qRScoreMaxNode.getqRTriplePathSet());
		// clonedNode.setqRTriplePathSet(clonedqRTriplePathSet);

		// ...set the QueryTempVarSolutionSpace
		List<QuerySolution> clonedQueryTempVarSolutionSpace = new ArrayList<>();
		clonedQueryTempVarSolutionSpace.addAll(qRScoreMaxNode.getQueryTempVarSolutionSpace());
		clonedNode.setSolutionSpace(clonedQueryTempVarSolutionSpace);

		return clonedNode;

	}

	public List<String> getcSetD2() {
		return cSetD2;
	}

	public List<String> getDpSetD2() {
		return dpSetD2;
	}

	public String getEntityqO() {
		return entityqO;
	}

	public String getEntityqR() {
		return entityqR;
	}

	// public Set<TriplePath> getqRTriplePathSet() {
	// return qRTriplePathSet;
	// }
	//
	// public void setqRTriplePathSet(Set<TriplePath> qRTriplePathSet) {
	// this.qRTriplePathSet = qRTriplePathSet;
	// }

	public List<String> getIndSetD2() {
		return indSetD2;
	}

	public List<String> getlSetD2() {
		return lSetD2;
	}

	public String getOp() {
		return op;
	}

	public List<String> getOperationList() {
		return operationList;
	}

	// public void setqRTemplateVariableSet(Set<Var> qRTemplateVariableSet) {
	// this.qRTemplateVariableSet = qRTemplateVariableSet;
	// }

	public List<String> getOpSetD2() {
		return opSetD2;
	}

	public List<String> getpSetD2() {
		return pSetD2;
	}

	public Query getqO() {
		return qO;
	}

	public Query getqR() {
		return qR;
	}

	public float getqRScore() {
		return qRScore;
	}

	public float getQueryResultSizeSimilarity() {
		return queryResultSizeSimilarity;
	}

	public float getQueryResultTypeSimilarity() {
		return queryResultTypeSimilarity;
	}

	public float getQueryRootDistance() {
		return queryRootDistance;
	}

	public float getQueryRootDistanceSimilarity() {
		return this.queryRootDistanceSim;
	}

	public float getQuerySpecificityDistance() {
		return querySpecificityDistance;
	}

	public List<QuerySolution> getQueryTempVarSolutionSpace() {
		return queryTempVarSolutionSpace;
	}

	public Map<Var, Set<RDFNode>> getQueryTempVarValueMap() {
		return queryTempVarValueMap;
	}

	public IRDFDataset getRdfD1() {
		return rdfD1;
	}

	public IRDFDataset getRdfD2() {
		return rdfD2;
	}

	public List<String> getRdfVD2() {
		return rdfVD2;
	}

	public void setcSetD2(List<String> cSetD2) {
		this.cSetD2 = cSetD2;
	}

	public void setDpSetD2(List<String> dpSetD2) {
		this.dpSetD2 = dpSetD2;
	}

	public void setEntityqO(String entityqO) {
		this.entityqO = entityqO;
	}

	public void setEntityqR(String entityqR) {
		this.entityqR = entityqR;
	}

	public void setIndSetD2(List<String> indSetD2) {
		this.indSetD2 = indSetD2;
	}

	public void setlSetD2(List<String> lSetD2) {
		this.lSetD2 = lSetD2;
	}

	// public Set<Var> getqRTemplateVariableSet() {
	// return qRTemplateVariableSet;
	// }

	public void setOp(String op) {
		this.op = op;
	}

	public void setOperationList(List<String> clonedOperationList) {
		this.operationList = clonedOperationList;
	}

	public void setOpSetD2(List<String> opSetD2) {
		this.opSetD2 = opSetD2;
	}

	public void setpSetD2(List<String> pSetD2) {
		this.pSetD2 = pSetD2;
	}

	public void setqO(Query qO) {
		this.qO = qO;
	}

	public void setqR(Query qR) {
		this.qR = qR;
	}

	public void setqRScore(float qRScore) {
		this.qRScore = qRScore;
	}

	public void setQueryResultSizeSimilarity(float queryResultSizeSimilarity) {
		this.queryResultSizeSimilarity = queryResultSizeSimilarity;
	}

	public void setQueryResultTypeSimilarity(float queryResultTypeSimilarity) {
		this.queryResultTypeSimilarity = queryResultTypeSimilarity;
	}

	public void setQueryRootDistance(float queryRootDistance) {
		this.queryRootDistance = queryRootDistance;
	}

	public void setQueryRootDistanceSimilarity(float queryRootDistance) {
		this.queryRootDistanceSim = queryRootDistance;
	}

	public void setQuerySpecificityDistanceSimilarity(float querySpecificityDistance) {
		this.querySpecificityDistance = querySpecificityDistance;
	}

	public void setQueryTempVarValueMap(Map<Var, Set<RDFNode>> queryTempVarValueMap) {
		this.queryTempVarValueMap = queryTempVarValueMap;
	}

	public void setRdfD1(IRDFDataset rdfD1) {
		this.rdfD1 = rdfD1;
	}

	public void setRdfD2(IRDFDataset rdfD2) {
		this.rdfD2 = rdfD2;
	}

	public void setRdfVD2(List<String> rdfVD2) {
		this.rdfVD2 = rdfVD2;
	}

	public void setSolutionSpace(List<QuerySolution> queryTempVarSolutionSpace) {
		if (queryTempVarSolutionSpace == null) {
			this.queryTempVarSolutionSpace = new ArrayList<>();
		} else {
			this.queryTempVarSolutionSpace = queryTempVarSolutionSpace;
		}

	}

}
