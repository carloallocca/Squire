package uk.ac.open.kmi.squire.evaluation.model;

import java.util.Collection;
import java.util.HashSet;

/**
 * A graph that is not fully resident, as its nodes and edges are not generated
 * until required. The information for generating further nodes and edges should
 * be contained in the nodes themselves.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 * @param <V>
 *            the type of vertices in this graph
 */
public class ProceduralGraph<V> extends Graph<V> {

	public ProceduralGraph(Collection<V> startingNodes) {
		super(startingNodes, new HashSet<>());
	}

}
