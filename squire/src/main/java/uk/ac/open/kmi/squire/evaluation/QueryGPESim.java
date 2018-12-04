/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_CLASS;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_DT;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_OBJ;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementWalker;

import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionAggregator;

/**
 *
 * @author carloallocca
 *
 *         This class compute the similarity in the context of Remove operation
 *         between two SPARQL queries.
 *
 */
public class QueryGPESim {

	/**
	 * A measure of the number of triple patterns that are lost from one query to
	 * another.
	 * 
	 * If the transformed query were to GAIN triple patterns, the similarity would
	 * be negative. If it neither gained nor lost triple patterns, it would be zero.
	 * 
	 * @param qO
	 * @param qR
	 * @return
	 */
	public float computeQueryPatternLoss(Query qO, Query qR) {
		// ...get the GPE of qOri
		SQGraphPatternExpressionAggregator visitor = new SQGraphPatternExpressionAggregator();
		ElementWalker.walk(qO.getQueryPattern(), visitor);
		Set<TriplePath> qOGPE = visitor.getMembersInQuery();
		// log.info("qOGPE : " +qOGPE.toString());

		// ...get the GPE of qRec
		visitor = new SQGraphPatternExpressionAggregator();
		ElementWalker.walk(qR.getQueryPattern(), visitor);
		Set<TriplePath> qRGPE = visitor.getMembersInQuery();
		// log.info("qRGPE : " +qRGPE.toString());

		// this is as it was before 13-04-2017
		if (qRGPE.size() > 0 && qOGPE.size() > 0) {
			return 1 - ((float) ((1.0 * qRGPE.size()) / (1.0 * qOGPE.size())));
		}
		return 0f;

		// this is the new one, after 13-04-2017
		// float r = 1 - computeSim(qOGPE, qRGPE);
		// return r;
	}

	public float computeQueryPatternsSimWithWeighedNonCommonTriplePattern(Query qO, Query qR) {
		// ...get the GPE of qOri
		SQGraphPatternExpressionAggregator gpeVisitorO = new SQGraphPatternExpressionAggregator();
		ElementWalker.walk(qO.getQueryPattern(), gpeVisitorO);
		Set<TriplePath> qOGPE = gpeVisitorO.getMembersInQuery();
		// log.info("qOGPE : " +qOGPE.toString());

		// ...get the GPE of qRec
		SQGraphPatternExpressionAggregator gpeVisitorR = new SQGraphPatternExpressionAggregator();
		ElementWalker.walk(qR.getQueryPattern(), gpeVisitorR);
		Set<TriplePath> qRGPE = gpeVisitorR.getMembersInQuery();
		// log.info("qRGPE : " +qRGPE.toString());

		// //this is as it was before 13-04-2017
		// if (qRGPE.size() > 0 && qOGPE.size() > 0) {
		// return 1 - ((float) (((1.0) * qRGPE.size()) / ((1.0) * qOGPE.size())));
		// }
		// return (float) 0.0;

		// this is the new one, after 13-04-2017
		float r = 1 - computeSim(qOGPE, qRGPE);
		return r;
	}

	private float computeCommonTriplePattern(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {

		float sim = (float) 0;
		int cardSignatureQo = qOGPE.size();
		int cardSignatureQr = qRGPE.size();

		if (cardSignatureQo != 0 && cardSignatureQr != 0) {
			int intersection = 0;
			for (TriplePath tp : qOGPE)
				if (qRGPE.contains(tp))
					intersection = intersection + 1;
			// log.info("computeCommonTriplePattern::intersection : " + intersection);
			// sim =(float) (1.0*(((1.0*qOvarList.size())/(1.0*qRvarList.size()))));

			sim = (float) ((1.0 * intersection) / (1.0 * cardSignatureQo));
			// log.info("computeCommonTriplePattern::sim : " + sim);
			return sim;
		}
		return sim;
	}

	/**
	 * URIs weigh 0.5, template variables weigh 0.1, other variables weigh 0.4,
	 * everything else weight 0.
	 * 
	 * @param n
	 * @return
	 */
	private float computeNodeWeight(Node n) {
		if (n.isURI())
			return 0.5f;
		else if (n.isVariable()) {
			String na = n.getName();
			if (na.startsWith(TEMPLATE_VAR_CLASS) || na.startsWith(TEMPLATE_VAR_PROP_OBJ)
					|| na.startsWith(TEMPLATE_VAR_PROP_DT))
				return 0.1f;
			else
				return 0.4f;
		}
		return 0;
	}

	private float computeSim(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {
		if (!(qOGPE.isEmpty() && !(qRGPE.isEmpty()))) {
			float commonTriplePattern = computeCommonTriplePattern(qOGPE, qRGPE);
			float weighedNoNCommonTriplePattern = computeWeighedNonCommonTriplePattern(qOGPE, qRGPE);
			return commonTriplePattern + weighedNoNCommonTriplePattern;
		}
		return 0;
	}

	/**
	 * The weight of a triple pattern is the average weight of its elements.
	 * 
	 * @param tp
	 * @return
	 */
	private float computeTpWeight(TriplePath tp) {
		float weight = computeNodeWeight(tp.getSubject()) + computeNodeWeight(tp.getPredicate())
				+ computeNodeWeight(tp.getObject());
		return weight / 3;

	}

	private float computeWeighedNonCommonTriplePattern(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {
		float sim = (float) 0;
		int cardSignatureQo = qOGPE.size();
		int cardSignatureQr = qRGPE.size();

		if (!(cardSignatureQo == 0) && !(cardSignatureQr == 0)) {
			// compute qOGPEComplementaryqRGPE
			Set<WeighedTriplePath> weighedTriplePathSetqoqr = new HashSet<>();
			for (TriplePath tp : qOGPE)
				if (!qRGPE.contains(tp)) {
					// qOGPEComplqRGPECardinality = qOGPEComplqRGPECardinality + 1;
					float tpWeigh = computeTpWeight(tp);
					weighedTriplePathSetqoqr.add(new WeighedTriplePath(tp, tpWeigh));
				}
			// compute qRGPEComplementaryqOGPE
			Set<WeighedTriplePath> weighedTriplePathSetqrqo = new HashSet<>();
			for (TriplePath tp : qRGPE)
				if (!qOGPE.contains(tp)) {
					float tpWeigh = computeTpWeight(tp);
					weighedTriplePathSetqrqo.add(new WeighedTriplePath(tp, tpWeigh));
				}
			float qoqr = sumWeighedTriplePathSet(weighedTriplePathSetqoqr);
			float qrqo = sumWeighedTriplePathSet(weighedTriplePathSetqrqo);
			sim = (float) ((1.0 * qrqo) / (1.0 * qoqr));
			// log.info("computeWeighedNonCommonTriplePattern::sim : " + sim);
			return sim;
		}
		return sim;
	}

	private float sumWeighedTriplePathSet(Set<WeighedTriplePath> weighedTriplePathSetqoqr) {
		float sum = 0f;
		for (WeighedTriplePath wtp : weighedTriplePathSetqoqr)
			sum += wtp.getWeigh();
		return sum;
	}

}
