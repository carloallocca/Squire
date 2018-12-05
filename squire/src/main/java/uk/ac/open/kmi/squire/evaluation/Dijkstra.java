package uk.ac.open.kmi.squire.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.open.kmi.squire.evaluation.model.Edge;
import uk.ac.open.kmi.squire.evaluation.model.SpecializationGraph;

public class Dijkstra<V> {

	private final Set<V> nodes;
	private final List<Edge<V>> edges;
	private Set<V> settledNodes;
	private Set<V> unSettledNodes;
	private Map<V, V> predecessors;
	private Map<V, Float> distance;

	public Dijkstra(SpecializationGraph<V> graph) {
		// create a copy of the array so that we can operate on this array
		this.nodes = new HashSet<>(graph.getVertices());
		this.edges = new ArrayList<>(graph.getEdges());
	}

	public void execute(V source) {
		settledNodes = new HashSet<>();
		unSettledNodes = new HashSet<>();
		distance = new HashMap<>();
		predecessors = new HashMap<>();
		distance.put(source, 0f);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			V node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(V node) {
		List<V> adjacentNodes = getNeighbors(node);
		for (V target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private float getDistance(V node, V target) {
		for (Edge<V> edge : edges) {
			if (edge.getSource().equals(node) && edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<V> getNeighbors(V node) {
		List<V> neighbors = new ArrayList<>();
		for (Edge<V> edge : edges) {
			if (edge.getSource().equals(node) && !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}

	private V getMinimum(Set<V> vertexes) {
		V minimum = null;
		for (V vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(V vertex) {
		return settledNodes.contains(vertex);
	}

	private float getShortestDistance(V destination) {
		Float d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and NULL
	 * if no path exists
	 */
	public LinkedList<V> getPath(V target) {
		LinkedList<V> path = new LinkedList<V>();
		V step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

}