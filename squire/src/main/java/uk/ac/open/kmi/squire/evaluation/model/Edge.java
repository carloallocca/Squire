package uk.ac.open.kmi.squire.evaluation.model;

import java.util.Arrays;

public class Edge<V> {
	private final V source, destination;
	private final int weight;

	public Edge(V source, V destination, int weight) {
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Edge))
			return false;
		@SuppressWarnings("unchecked")
		Edge<V> e = (Edge<V>) obj;
		return getSource().equals(e.getSource()) && getDestination().equals(e.getDestination())
				&& getWeight() == e.getWeight();
	}

	public V getDestination() {
		return destination;
	}

	public V getSource() {
		return source;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] { getSource(), getDestination(), getWeight() });
	}

	@Override
	public String toString() {
		return source + " " + destination;
	}

}