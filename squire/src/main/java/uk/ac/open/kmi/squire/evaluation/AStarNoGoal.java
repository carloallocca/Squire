package uk.ac.open.kmi.squire.evaluation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A variant of the A* algorithm where the goal is not a target node, but simply
 * relies on the heuristic function estimating zero.
 * 
 * @author alessandro
 *
 * @param <N>
 *            the nodes to be traversed
 */
public abstract class AStarNoGoal<N> extends AStar<N> {

	public abstract float heuristicCostEstimate(N current);

	/**
	 * In this variant we don't know where we want to go, we just want to reach a
	 * heuristic estimate of zero.
	 * 
	 * @param start
	 * @param goal
	 */
	public List<N> traverse(N start) {

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
		fScore.put(start, heuristicCostEstimate(start));

		while (!openSet.isEmpty()) {

			N current = Collections.min(fScore.entrySet(), Comparator.comparing(Entry::getValue)).getKey();

			if (heuristicCostEstimate(current) == 0)
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
				fScore.put(neighbor, gScore.get(neighbor) + heuristicCostEstimate(neighbor));

			}

		}
		return Collections.emptyList();
	}

}
