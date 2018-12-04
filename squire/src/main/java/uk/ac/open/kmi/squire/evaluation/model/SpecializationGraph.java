package uk.ac.open.kmi.squire.evaluation.model;

import java.util.Collection;
import java.util.Collections;

public class SpecializationGraph<V> {
	private final Collection<V> vertices;
	private final Collection<Edge<V>> edges;

	public SpecializationGraph(Collection<V> vertices, Collection<Edge<V>> edges) {
		this.vertices = Collections.unmodifiableCollection(vertices);
		this.edges = Collections.unmodifiableCollection(edges);
	}

	public Collection<V> getVertices() {
		return vertices;
	}

	public Collection<Edge<V>> getEdges() {
		return edges;
	}

}