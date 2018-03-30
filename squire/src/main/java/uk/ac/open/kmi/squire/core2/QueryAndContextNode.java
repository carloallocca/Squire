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
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * AA: Apparently an all-knowing POJO for the process of generating
 * recommendations. It also includes the solution space of that query
 * 
 * TODO make this a real treenode, with pointers to children and all that.
 *
 * @author carloallocca
 */
public class QueryAndContextNode {

	public static class QRScoreComparator implements Comparator<QueryAndContextNode> {

		@Override
		public int compare(QueryAndContextNode p1, QueryAndContextNode p2) {
			float score1 = p1.getqRScore();
			float score2 = p2.getqRScore();
			// ...For ascending order
			return Float.compare(score2, score1);
			// return (-1)*Float.compare(score1, score2); // For discending order
		}

	}

	private String entityqO, entityqR;

	// private Set<Var> qRTemplateVariableSet;
	// private Set<TriplePath> qRTriplePathSet;

	private String op; // It can be either R (for Removal) or I (Instantiation).

	private Query qO, qR;

	private float qRScore;

	private float queryResultSizeSimilarity;
	private float queryResultTypeSimilarity;
	private float queryRootDistance;
	private float queryRootDistanceSim;
	private float querySpecificityDistance;

	private List<QuerySolution> queryTempVarSolutionSpace;

	private Map<Var, Set<RDFNode>> queryTempVarValueMap;

	private IRDFDataset rdfD1, rdfD2;

	public String getEntityqO() {
		return entityqO;
	}

	public String getEntityqR() {
		return entityqR;
	}

	public String getOp() {
		return op;
	}

	public Query getOriginalQuery() {
		return qO;
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
		return queryRootDistanceSim;
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

	public Query getTransformedQuery() {
		return qR;
	}

	public void setEntityqO(String entityqO) {
		this.entityqO = entityqO;
	}

	public void setEntityqR(String entityqR) {
		this.entityqR = entityqR;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public void setOriginalQuery(Query qO) {
		this.qO = qO;
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

	public void setQuerySpecificityDistance(float querySpecificityDistance) {
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

	public void setSolutionSpace(List<QuerySolution> queryTempVarSolutionSpace) {
		if (queryTempVarSolutionSpace == null) this.queryTempVarSolutionSpace = new ArrayList<>();
		else this.queryTempVarSolutionSpace = queryTempVarSolutionSpace;
	}

	public void setTransformedQuery(Query qR) {
		this.qR = qR;
	}

}
