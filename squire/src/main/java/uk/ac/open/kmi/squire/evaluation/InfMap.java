package uk.ac.open.kmi.squire.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.evaluation.Measures.Metrics;

/**
 * For every sibling concept and every metric being considered, it provides the
 * set of queries that maximize or minimize them.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public class InfMap extends HashMap<Metrics, Set<QueryAndContextNode>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 534293999520492941L;

	public boolean addOptimalNode(Metrics metric, QueryAndContextNode node) {
		if (!containsKey(metric)) put(metric, new HashSet<>());
		return get(metric).add(node);
	}

	public Set<QueryAndContextNode> getOptimalNodes(Metrics metric) {
		return get(metric);
	}

}
