package uk.ac.open.kmi.squire.evaluation;

import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionAggregator;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableAggregator;

/**
 *
 * @author carloallocca
 */
public class QuerySpecificityDistance {

	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * The more triple patterns are altered or removed in the transformation from
	 * one query to another, the longer this distance.
	 * 
	 * @param originalQuery
	 * @param qR
	 * @return
	 */
	public float computeQSDwrtQueryTP(Query originalQuery, Query recommendedQuery) {
		float dist;

		// ...get the GPE of originalQuery
		SQGraphPatternExpressionAggregator gpeVisitorO = new SQGraphPatternExpressionAggregator();
		ElementWalker.walk(originalQuery.getQueryPattern(), gpeVisitorO);
		Set<TriplePath> qOGPE = gpeVisitorO.getMembersInQuery();
		// ...get the GPE of recommendedQuery
		SQGraphPatternExpressionAggregator gpeVisitorR = new SQGraphPatternExpressionAggregator();
		ElementWalker.walk(recommendedQuery.getQueryPattern(), gpeVisitorR);
		Set<TriplePath> qRGPE = gpeVisitorR.getMembersInQuery();

		dist = 1 - tpOverlapRate(qOGPE, qRGPE);
		return dist;

	}

	/**
	 * The more variables are instantiated in the transformation from one query to
	 * another, the longer this distance.
	 * 
	 * XXX variables or occurrences of variables?
	 * 
	 * @param qO
	 * @param qR
	 * @return
	 */
	public float computeQSDwrtQueryVariable(Query qO, Query qR) {
		return 1 - varOverlapRate(computeQueryVariableSet(qO), computeQueryVariableSet(qR));
	}

	private float computeQSsim(List<String> qOvarList, List<String> qRvarList) {
		float sim = 0;
		if (!qOvarList.isEmpty() && !qRvarList.isEmpty()) {
			sim = (float) (1.0 * (((1.0 * qRvarList.size()) / (1.0 * qOvarList.size()))));
			return sim;
		}
		return sim;
	}

	/**
	 * Extracts the set of all variables, both template and non-template, regardless
	 * of where they appear in the triple patterns.
	 * 
	 * @param qO
	 * @return
	 */
	private Set<String> computeQueryVariableSet(Query qO) {
		SQVariableAggregator v = new SQVariableAggregator();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qO.getQueryPattern(), v);
		return v.getMembersInQuery();
	}

	/**
	 * @deprecated Why is this here since {@link TriplePath} implements
	 *             {@link Object#hashCode()} ?
	 * 
	 * @param qRGPE
	 * @param tp
	 * @return
	 */
	private boolean contains(Set<TriplePath> qRGPE, TriplePath tp) {
		String tpAsString = tp.toString();
		for (TriplePath tp1 : qRGPE)
			if (tp1.toString().compareTo(tpAsString) == 0)
				return true;
		return false;
	}

	/**
	 * The ratio between the number of common triple patterns and that of all the
	 * triple patterns from both queries combined. The more triple patterns are
	 * altered or removed in the transformation from one query to another, the lower
	 * the value.
	 * 
	 * @param qOvars
	 * @param qRvars
	 * @return
	 */
	private float tpOverlapRate(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {
		if (qOGPE.isEmpty() || qRGPE.isEmpty()) {
			log.warn("Cannot compute overlap rate with an empty TriplePath set.");
			return 0f;
		}
		int intersectionTPCardinality = 0; // # TPs from original query also present in transformed query.
		for (TriplePath tp : qOGPE)
			if (qRGPE.contains(tp))
				intersectionTPCardinality++;
		// Cardinality of the union of both sets
		int unionTPCardinality = qOGPE.size() + qRGPE.size() - intersectionTPCardinality;
		return (float) ((1.0 * intersectionTPCardinality) / unionTPCardinality);
	}

	/**
	 * The ratio between the number of common variables and that of all the
	 * variables from both queries combined. The more variables are instantiated in
	 * the transformation from one query to another, the lower the value.
	 * 
	 * @param qOvars
	 * @param qRvars
	 * @return
	 */
	private float varOverlapRate(Set<String> qOvars, Set<String> qRvars) {
		if (qOvars.isEmpty() || qRvars.isEmpty()) {
			log.warn("Cannot compute overlap rate with an empty variable set.");
			return 0f;
		}
		int intersectionVarCardinality = 0; // # Variables from original query also present in transformed query.
		for (String st : qOvars)
			if (qRvars.contains(st))
				intersectionVarCardinality++;
		// Cardinality of the union of both sets
		float unionVarCardinality = qOvars.size() + qRvars.size() - intersectionVarCardinality;
		return (float) ((1.0 * intersectionVarCardinality) / unionVarCardinality);
	}

}
