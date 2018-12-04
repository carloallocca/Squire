package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.entityvariablemapping.RdfVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;

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
			return Float.compare(score2, score1); // ...for ascending order
			// return -1*Float.compare(score1, score2); // ...for descending order
		}

	}

	private float bindingCollapseRate, queryRootDistance, querySpecDist_Tp, querySpecDist_Var, resultTypeSimilarity;

	private final VarMapping<Var, Node> bindings;

	private Query qO, qR;

	private float qRScore;

	private List<QuerySolution> tplVarSolutionSpace;

	@Deprecated
	public QueryAndContextNode(Query transformedQuery) {
		this(transformedQuery, new RdfVarMapping());
	}

	public QueryAndContextNode(Query transformedQuery, VarMapping<Var, Node> transformations) {
		this.qR = transformedQuery;
		this.bindings = transformations;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof QueryAndContextNode))
			return false;
		QueryAndContextNode qctx = (QueryAndContextNode) obj;
		return ((getOriginalQuery() == null && qctx.getOriginalQuery() == null)
				|| getOriginalQuery().equals(qctx.getOriginalQuery()))
				&& getTransformedQuery().equals(qctx.getTransformedQuery()) && getqRScore() == qctx.getqRScore();
		// TODO consider the detailed measures rather than the score
	}

	public float getBindingCollapseRate() {
		return bindingCollapseRate;
	}

	public VarMapping<Var, Node> getBindings() {
		if (this.bindings == null)
			throw new IllegalStateException("This instance of " + getClass().getName()
					+ " was created without a binding map."
					+ " If you intended to get the bindings, you should have passed them to the constructor earlier.");
		return this.bindings;
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

	public float getResultTypeSimilarity() {
		return resultTypeSimilarity;
	}

	public List<QuerySolution> getTplVarSolutionSpace() {
		if (tplVarSolutionSpace == null)
			throw new IllegalStateException("No template variable solution space is set yet."
					+ " This may be the case if, for example, the query is an intermediate one (i.e. not fully instantiated).");
		return tplVarSolutionSpace;
	}

	public Query getTransformedQuery() {
		return qR;
	}

	@Override
	public int hashCode() {
		// TODO consider the detailed measures rather than the score
		return Arrays.hashCode(new Object[] { qO, qR, qRScore });
	}

	public void setBindingCollapseRate(float bindingCollapseRate) {
		this.bindingCollapseRate = bindingCollapseRate;
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

	public void setTplVarSolutionSpace(List<QuerySolution> queryTempVarSolutionSpace) {
		if (queryTempVarSolutionSpace == null)
			this.tplVarSolutionSpace = new ArrayList<>();
		else
			this.tplVarSolutionSpace = queryTempVarSolutionSpace;
	}

	public void setTransformedQuery(Query qR) {
		this.qR = qR;
	}

}
