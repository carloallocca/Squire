package uk.ac.open.kmi.squire.evaluation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import uk.ac.open.kmi.squire.evaluation.model.Edge;
import uk.ac.open.kmi.squire.evaluation.model.Graph;
import uk.ac.open.kmi.squire.evaluation.model.ResidentGraph;

public class TestDijkstra {

	private List<Edge<Vertex>> edges;
	private List<Vertex> nodes;

	@Test
	public void testExcute() {
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge<Vertex>>();
		for (int i = 0; i < 11; i++) {
			Vertex location = new Vertex("Node_" + i, "Node_" + i);
			nodes.add(location);
		}

		addLane("Edge_0", 0, 1, 85);
		addLane("Edge_1", 0, 2, 217);
		addLane("Edge_2", 0, 4, 173);
		addLane("Edge_3", 2, 6, 186);
		addLane("Edge_4", 2, 7, 103);
		addLane("Edge_5", 3, 7, 183);
		addLane("Edge_6", 5, 8, 250);
		addLane("Edge_7", 8, 9, 84);
		addLane("Edge_8", 7, 9, 167);
		addLane("Edge_9", 4, 9, 502);
		addLane("Edge_10", 9, 10, 40);
		addLane("Edge_11", 1, 10, 600);

		// Lets check from location Loc_1 to Loc_10
		Graph<Vertex> graph = new ResidentGraph<>(nodes, edges);
		Dijkstra<Vertex> dijkstra = new Dijkstra<>(graph);
		dijkstra.execute(nodes.get(0));
		LinkedList<Vertex> path = dijkstra.getPath(nodes.get(10));

		assertNotNull(path);
		assertTrue(path.size() > 0);

		for (Vertex vertex : path)
			System.out.println(vertex);

	}

	private void addLane(String laneId, int sourceLocNo, int destLocNo, int duration) {
		Edge<Vertex> lane = new Edge<>(nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
		edges.add(lane);
	}
}