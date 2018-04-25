/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;

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
			// return -1*Float.compare(score1, score2); // For descending order
		}

	}

	private IRDFDataset ds1, ds2;

	private String op;

	private Query qO, qR;

	private float qRScore;
	private float queryRootDistance;
	private float querySpecificityDistance;

	private List<QuerySolution> queryTempVarSolutionSpace;

	/**
	 * It can be either R (for Removal) or I (Instantiation).
	 *
	 * @return
	 */
	public String getLastOperation() {
		return op;
	}

	public Query getOriginalQuery() {
		return qO;
	}

	public float getqRScore() {
		return qRScore;
	}

	public float getQueryRootDistance() {
		return queryRootDistance;
	}

	public float getQuerySpecificityDistance() {
		return querySpecificityDistance;
	}

	public List<QuerySolution> getQueryTempVarSolutionSpace() {
		return queryTempVarSolutionSpace;
	}

	public IRDFDataset getSourceDataset() {
		return ds1;
	}

	public IRDFDataset getTargetDataset() {
		return ds2;
	}

	public Query getTransformedQuery() {
		return qR;
	}

	public void setDataset1(IRDFDataset rdfD1) {
		this.ds1 = rdfD1;
	}

	public void setDataset2(IRDFDataset rdfD2) {
		this.ds2 = rdfD2;
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

	public void setQueryRootDistance(float queryRootDistance) {
		this.queryRootDistance = queryRootDistance;
	}

	public void setQuerySpecificityDistance(float querySpecificityDistance) {
		this.querySpecificityDistance = querySpecificityDistance;
	}

	public void setSolutionSpace(List<QuerySolution> queryTempVarSolutionSpace) {
		if (queryTempVarSolutionSpace == null) this.queryTempVarSolutionSpace = new ArrayList<>();
		else this.queryTempVarSolutionSpace = queryTempVarSolutionSpace;
	}

	public void setTransformedQuery(Query qR) {
		this.qR = qR;
	}

}
