package uk.ac.open.kmi.squire.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measures the effect of binding two or more property variables to the same
 * value for the same subject, causing those variables to "collapse" into a
 * smaller amount of values. This can be used as a penalty to inflict to the
 * scores of recommended queries.
 * <p>
 * For example if we have the following query pattern:
 * 
 * <pre>
 * ?x a foaf:Document
 *  ; ?dpt1 ?title
 *  ; ?dpt2 ?text
 * </pre>
 * 
 * then this specialized query should have a value of zero for binding collapse:
 * 
 * <pre>
 * ?x a foaf:Document
 *  ;  dc:title ?title
 *  ; rdf:value ?text
 * </pre>
 * 
 * whereas the following should have a value greater than zero:
 * 
 * <pre>
 * ?x a foaf:Document
 *  ; dc:title ?title
 *  ; dc:title ?text
 * </pre>
 * 
 * unless the properties came from instantiating the same variable, which isn't
 * the case here.
 * </p>
 * 
 * This measure does not take into account whether data property values become
 * object property values and vice versa: for that metric, see
 * {@link QueryResultTypeDistance}.
 * 
 * The measure has the following characteristics:
 * <ol>
 * <li>It increases greatly with the frequency of the value the variables are
 * collapsed in
 * <li>It increases with the number of variables that are collapsed
 * <li>It decreases with the frequency of the variables that are collapsed
 * <li>It decreases with the overall number of variables with the same role as
 * the collapsed ones
 * </ol>
 * 
 * TODO: better to compute it from an operation record than by re-scanning the
 * query.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public class QueryBindingCollapse {

	private Logger log = LoggerFactory.getLogger(getClass());

	public float compute(Query qOriginal, Query qTransformed) {
		if (qOriginal == null || qTransformed == null)
			throw new IllegalArgumentException("Both queries need to be non-null.");

		// Inspect the original query for the frequency of property variables
		// Inspect the variables and values of the transformed query
		// Detect when a variable has collapsed into an existing value

		final Map<Node, Integer> origOcc = new HashMap<>(), transfOcc = new HashMap<>();
		ElementWalker.walk(qOriginal.getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					Node p = tp.getPredicate();
					origOcc.put(p, origOcc.containsKey(p) ? 1 + origOcc.get(p) : 1);
				}
			}
		});
		ElementWalker.walk(qTransformed.getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					Node p = tp.getPredicate();
					transfOcc.put(p, transfOcc.containsKey(p) ? 1 + transfOcc.get(p) : 1);
				}
			}
		});
		int num = 0, denom = 0;
		// Scan the original predicate map for any URI that has increased and any
		// variable that has disappeared in the transformed one.
		final Set<Node> increased = new HashSet<>();
		for (Node p : origOcc.keySet()) {
			if (p.isURI() && transfOcc.containsKey(p) && origOcc.get(p) < transfOcc.get(p)) {
				log.warn("Value {} has increased from {} to {} !", p, origOcc.get(p), transfOcc.get(p));
				increased.add(p);
				num += origOcc.get(p);
			} else if (p.isVariable()) denom += origOcc.get(p);
		}
		if (!increased.isEmpty()) for (Node p : origOcc.keySet()) {
			if (p.isVariable() && !transfOcc.containsKey(p)) {
				log.warn("Variable {} has been collapsed.", p);
				num += origOcc.get(p);
			}
		}
		float score = 1.0f * num / denom;
		log.debug("Collapse rate is calculated as {}/{} = {}", num, denom, score);
		return score;
	}

}
