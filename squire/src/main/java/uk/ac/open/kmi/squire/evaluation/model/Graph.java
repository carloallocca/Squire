package uk.ac.open.kmi.squire.evaluation.model;

import java.util.Collection;

/**
 * A generic combinatorial structure with a set of vertices and edges connecting
 * them.
 * 
 * @author alessandro
 *
 * @param <V>
 *            the type of vertices in this graph
 */
public abstract class Graph<V> {

	protected final Collection<Edge<V>> edges;

	protected final Collection<V> vertices;

	public Graph(Collection<V> vertices, Collection<Edge<V>> edges) {
		this.vertices = vertices;
		this.edges = edges;
	}

	public Collection<Edge<V>> getEdges() {
		return edges;
	}

	public Collection<V> getVertices() {
		return vertices;
	}

}