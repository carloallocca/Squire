package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Arrays;
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

	private float bindingCollapseRate, queryRootDistance, querySpecDist_Tp, querySpecDist_Var, resultTypeSimilarity;

	private IRDFDataset ds1, ds2;

	private Query qO, qR;

	private float qRScore;

	private List<QuerySolution> queryTempVarSolutionSpace;

	public QueryAndContextNode(Query transformedQuery) {
		this.qR = transformedQuery;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof QueryAndContextNode)) return false;
		QueryAndContextNode qctx = (QueryAndContextNode) obj;
		return getOriginalQuery().equals(qctx.getOriginalQuery())
				&& getTransformedQuery().equals(qctx.getTransformedQuery())
				&& getSourceDataset().equals(qctx.getSourceDataset())
				&& getTargetDataset().equals(qctx.getTargetDataset()) && getqRScore() == qctx.getqRScore();
		// TODO consider the detailed measures rather than the score
	}

	public float getBindingCollapseRate() {
		return bindingCollapseRate;
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

	public float getQuerySpecificityDistanceTP() {
		return querySpecDist_Tp;
	}

	public float getQuerySpecificityDistanceVar() {
		return querySpecDist_Var;
	}

	public List<QuerySolution> getQueryTempVarSolutionSpace() {
		return queryTempVarSolutionSpace;
	}

	public float getResultTypeSimilarity() {
		return resultTypeSimilarity;
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

	@Override
	public int hashCode() {
		// TODO consider the detailed measures rather than the score
		return Arrays.hashCode(new Object[] { qO, qR, ds1, ds2, qRScore });
	}

	public void setBindingCollapseRate(float bindingCollapseRate) {
		this.bindingCollapseRate = bindingCollapseRate;
	}

	public void setDataset1(IRDFDataset rdfD1) {
		this.ds1 = rdfD1;
	}

	public void setDataset2(IRDFDataset rdfD2) {
		this.ds2 = rdfD2;
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

	public void setQuerySpecificityDistanceTP(float value) {
		this.querySpecDist_Tp = value;
	}

	public void setQuerySpecificityDistanceVar(float value) {
		this.querySpecDist_Var = value;
	}

	public void setResultTypeSimilarity(float resultTypeSimilarity) {
		this.resultTypeSimilarity = resultTypeSimilarity;
	}

	public void setSolutionSpace(List<QuerySolution> queryTempVarSolutionSpace) {
		if (queryTempVarSolutionSpace == null) this.queryTempVarSolutionSpace = new ArrayList<>();
		else this.queryTempVarSolutionSpace = queryTempVarSolutionSpace;
	}

	public void setTransformedQuery(Query qR) {
		this.qR = qR;
	}

}
