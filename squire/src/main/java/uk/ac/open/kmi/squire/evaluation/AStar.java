package uk.ac.open.kmi.squire.evaluation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author alessandro
 *
 */
public abstract class AStar<N> {

	public float distBetween(N current, N neighbor) {
		return 1f;
	}

	public abstract Set<N> getNeighbors(N current);

	/**
	 * This should be monotonic if possible
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
	public abstract float heuristicCostEstimate(N current, N goal);

	protected List<N> reconstructPath(Map<N, N> cameFrom, N current) {
		List<N> totalPath = new LinkedList<>();
		totalPath.add(current);
		while (cameFrom.keySet().contains(current)) {
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		return totalPath;
	}

	/**
	 * Assumes we are minimizing the cost
	 * 
	 * @param start
	 * @param goal
	 */
	public List<N> traverse(N start, N goal) {

		// The set of nodes already evaluated
		Set<N> closedSet = new HashSet<>();
		// The set of currently discovered nodes that are not evaluated yet.
		Set<N> openSet = new HashSet<>();
		openSet.add(start);

		// For each node, which node it can most efficiently be reached from.
		// If a node can be reached from many nodes, cameFrom will eventually contain
		// the most efficient previous step.
		Map<N, N> cameFrom = new HashMap<>();

		// For each node, the cost of getting from the start node to that node.
		Map<N, Float> gScore = new HashMap<>();
		// If the key is not found, consider the value as Infinity (or greater than any
		// expected cost)

		// The cost of going from start to start is zero.
		gScore.put(start, 0f);

		// For each node, the total cost of getting from the start node to the goal
		// by passing by that node. That value is partly known, partly heuristic.
		Map<N, Float> fScore = new HashMap<>();
		// If the key is not found, consider the value as Infinity (or greater than any
		// expected cost)

		// For the first node, that value is completely heuristic.
		gScore.put(start, heuristicCostEstimate(start, goal));

		while (!openSet.isEmpty()) {

			N current = Collections.min(fScore.entrySet(), Comparator.comparing(Entry::getValue)).getKey();

			if (current.equals(goal))
				return reconstructPath(cameFrom, current);

			openSet.remove(current);
			closedSet.add(current);

			for (N neighbor : getNeighbors(current)) {

				if (closedSet.contains(neighbor))
					continue; // Ignore the neighbor which is already evaluated.
				if (!openSet.contains(neighbor))
					openSet.add(neighbor); // Discover a new node

				// The distance from start to a neighbor.
				// The "dist_between" function may vary as per the solution requirements.
				float tentative_gScore = gScore.get(current) + distBetween(current, neighbor);
				if (tentative_gScore >= gScore.get(neighbor))
					continue; // This is not a better path.

				// This path is the best until now. Record it!
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentative_gScore);
				fScore.put(neighbor, gScore.get(neighbor) + heuristicCostEstimate(neighbor, goal));

			}

		}
		return Collections.emptyList();
	}

}
