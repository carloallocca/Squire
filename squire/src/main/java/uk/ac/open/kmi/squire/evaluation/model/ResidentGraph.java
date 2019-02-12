package uk.ac.open.kmi.squire.evaluation.model;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link Graph} whose vertices and edges are all pre-generated.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 * @param <V>
 *            the type of vertices in this graph
 */
public class ResidentGraph<V> extends Graph<V> {

	public ResidentGraph(Collection<V> vertices, Collection<Edge<V>> edges) {
		super(Collections.unmodifiableCollection(vertices), Collections.unmodifiableCollection(edges));
	}

}
