package uk.ac.open.kmi.squire.fca;

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
public class AStarLatticeNavigator {

	/**
	 * Assumes we are minimizing the cost
	 * 
	 * @param start
	 * @param goal
	 */
	public List<GQVConcept> traverse(GQVConcept start, GQVConcept goal) {

		// The set of nodes already evaluated
		Set<GQVConcept> closedSet = new HashSet<>();
		// The set of currently discovered nodes that are not evaluated yet.
		Set<GQVConcept> openSet = new HashSet<>();
		openSet.add(start);

		// For each node, which node it can most efficiently be reached from.
		// If a node can be reached from many nodes, cameFrom will eventually contain
		// the most efficient previous step.
		Map<GQVConcept, GQVConcept> cameFrom = new HashMap<>();

		// For each node, the cost of getting from the start node to that node.
		Map<GQVConcept, Float> gScore = new HashMap<>();
		// If the key is not found, consider the value as Infinity (or greater than any
		// expected cost)

		// The cost of going from start to start is zero.
		gScore.put(start, 0f);

		// For each node, the total cost of getting from the start node to the goal
		// by passing by that node. That value is partly known, partly heuristic.
		Map<GQVConcept, Float> fScore = new HashMap<>();
		// If the key is not found, consider the value as Infinity (or greater than any
		// expected cost)

		// For the first node, that value is completely heuristic.
		gScore.put(start, heuristicCostEstimate(start, goal));

		while (!openSet.isEmpty()) {

			GQVConcept current = Collections.min(fScore.entrySet(), Comparator.comparing(Entry::getValue)).getKey();

			if (current.equals(goal)) return reconstructPath(cameFrom, current);

			openSet.remove(current);
			closedSet.add(current);

			for (Concept<?, ?> neighborg : current.getInferiors()) {
				if (!(neighborg instanceof GQVConcept)) {
					continue;
				}
				GQVConcept neighbor = (GQVConcept) neighborg;

				if (closedSet.contains(neighbor)) continue; // Ignore the neighbor which is already evaluated.
				if (!openSet.contains(neighbor)) openSet.add(neighbor); // Discover a new node

				// The distance from start to a neighbor.
				// The "dist_between" function may vary as per the solution requirements.
				float tentative_gScore = gScore.get(current) + distBetween(current, neighbor);
				if (tentative_gScore >= gScore.get(neighbor)) continue; // This is not a better path.

				// This path is the best until now. Record it!
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentative_gScore);
				fScore.put(neighbor, gScore.get(neighbor) + heuristicCostEstimate(neighbor, goal));

			}

		}
		return Collections.emptyList();
	}

	private float distBetween(GQVConcept current, GQVConcept neighbor) {
		return 1f;
	}

	/**
	 * This should be monotonic if possible
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
	private float heuristicCostEstimate(GQVConcept current, GQVConcept goal) {
		return 1f;
	}

	private List<GQVConcept> reconstructPath(Map<GQVConcept, GQVConcept> cameFrom, GQVConcept current) {
		List<GQVConcept> totalPath = new LinkedList<>();
		totalPath.add(current);
		while (cameFrom.keySet().contains(current)) {
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		return totalPath;
	}

}
