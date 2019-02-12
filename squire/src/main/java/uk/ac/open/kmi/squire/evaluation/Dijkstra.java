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
import uk.ac.open.kmi.squire.evaluation.model.Graph;

public class Dijkstra<V> {

	protected final Map<V, Set<Edge<V>>> edges;
	private Set<V> settledNodes, unSettledNodes;
	private Map<V, V> predecessors;
	private Map<V, Float> distance;

	public Dijkstra(Graph<V> graph) {
		// create a copy of the array so that we can operate on this array
		// this.nodes = new HashSet<>(graph.getVertices());
		this.edges = new HashMap<>();
		for (Edge<V> ed : graph.getEdges())
			addEdge(ed);
	}

	public void addEdge(Edge<V> edge) {
		V from = edge.getSource();
		if (!edges.containsKey(from))
			edges.put(from, new HashSet<>());
		edges.get(from).add(edge);
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

	protected List<V> findMinimalDistances(V node) {
		List<V> adjacentNodes = getNeighbors(node);
		for (V target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}
		return adjacentNodes;
	}

	protected float getDistance(V node, V target) {
		if (!edges.containsKey(node) && node != target)
			throw new RuntimeException("Target is unreachable from node.");
		for (Edge<V> edge : edges.get(node)) {
			if (edge.getSource().equals(node) && edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("The algorithm can only calculate the distance between neighboring nodes.");
	}

	protected List<V> getNeighbors(V node) {
		if (!edges.containsKey(node))
			return Collections.emptyList();
		List<V> neighbors = new ArrayList<>();
		for (Edge<V> edge : edges.get(node)) {
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

	protected boolean isSettled(V vertex) {
		return settledNodes.contains(vertex);
	}

	public float getShortestDistance(V destination) {
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