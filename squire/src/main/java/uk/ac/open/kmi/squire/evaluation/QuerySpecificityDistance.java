/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementWalker;

import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionVisitor;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;

/**
 *
 * @author carloallocca
 */
public class QuerySpecificityDistance {

	public float computeQSDwrtQueryVariable(Query qO, Query qR) {
		float dist;

		List<String> qOvarList = computeQueryVariableSet(qO);
		// log.info("qOvarList " +qOvarList.toString());
		List<String> qRvarList = computeQueryVariableSet(qR);
		// log.info("qRvarList " +qRvarList.toString());
		// dist = computeQSsim(qOvarList, qRvarList);
		dist = 1 - varOverlapRate(qOvarList, qRvarList);
		return dist;
	}

	private List<String> computeQueryVariableSet(Query qO) {
		SQVariableVisitor v = new SQVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qO.getQueryPattern(), v);
		return v.getQueryVariableSet();
	}

	private float varOverlapRate(List<String> qOvarList, List<String> qRvarList) {
		float overlapRate = 0;
		if (qOvarList.size() > 0 && qRvarList.size() > 0) {

			// compute the intersectionVarCardinality
			int intersectionVarCardinality = 0;
			for (String st : qOvarList) {
				if (qRvarList.contains(st)) {
					intersectionVarCardinality = intersectionVarCardinality + 1;
				}
			}

			float unionVarCardinality = computeUnionCardinalityVar(qOvarList, qRvarList, intersectionVarCardinality);
			overlapRate = (float) ((1.0 * intersectionVarCardinality) / unionVarCardinality);
			return overlapRate;
		}
		return overlapRate;
	}

	private float computeQSsim(List<String> qOvarList, List<String> qRvarList) {
		float sim = 0;
		if (qOvarList.size() > 0 && qRvarList.size() > 0) {
			sim = (float) (1.0 * (((1.0 * qRvarList.size()) / (1.0 * qOvarList.size()))));
			// log.info("computeQSsim " +dist);
			return sim;
		}
		return sim;
	}

	public float computeQSDwrtQueryTP(Query originalQuery, Query qR) {
		float dist = 0;

		// QueryGPESim simQuery= new QueryGPESim();
		// sim = simQuery.computeQueryPatternsSim(originalQuery, qR);
		//
		//// sim=simQuery.computeQueryPatternsSimWithWeighedNonCommonTriplePattern(qR,
		// qR);
		// return sim;

		// ...get the GPE of qOri
		SQGraphPatternExpressionVisitor gpeVisitorO = new SQGraphPatternExpressionVisitor();
		ElementWalker.walk(originalQuery.getQueryPattern(), gpeVisitorO);
		Set<TriplePath> qOGPE = gpeVisitorO.getQueryGPE();

		// ...get the GPE of qRec
		SQGraphPatternExpressionVisitor gpeVisitorR = new SQGraphPatternExpressionVisitor();
		ElementWalker.walk(qR.getQueryPattern(), gpeVisitorR);
		Set<TriplePath> qRGPE = gpeVisitorR.getQueryGPE();

		dist = 1 - tpOverlapRate(qOGPE, qRGPE);

		return dist;

	}

	private float tpOverlapRate(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {

		float overlapRate = (float) 0;
		int cardSignatureQo = qOGPE.size();
		int cardSignatureQr = qRGPE.size();

		if (!(qOGPE.isEmpty()) && !(qRGPE.isEmpty())) {
			int intersectionTPCardinality = 0;
			for (TriplePath tp : qOGPE) {
				if (contains(qRGPE, tp)) {
					intersectionTPCardinality = intersectionTPCardinality + 1;
				}
			}

			int unionTPCardinality = computeUnionCardinalityTP(qOGPE, qRGPE, intersectionTPCardinality);
			overlapRate = (float) ((1.0 * intersectionTPCardinality) / unionTPCardinality);
			return overlapRate;
		}
		return overlapRate;
	}

	private int computeUnionCardinalityTP(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE, int intersectionTPCardinality) {
		return (qOGPE.size() + qRGPE.size()) - intersectionTPCardinality;

	}

	private boolean contains(Set<TriplePath> qRGPE, TriplePath tp) {
		String tpAsString = tp.toString();
		for (TriplePath tp1 : qRGPE) {
			if (tp1.toString().compareTo(tpAsString) == 0) {
				return true;
			}
		}
		return false;
	}

	private float computeUnionCardinalityVar(List<String> qOvarList, List<String> qRvarList,
			int intersectionVarCardinality) {
		return (qOvarList.size() + qRvarList.size()) - intersectionVarCardinality;

	}

}
