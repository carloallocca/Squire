package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core2.QueryCtxNode;
import uk.ac.open.kmi.squire.entityvariablemapping.RdfVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.evaluation.Dijkstra;
import uk.ac.open.kmi.squire.evaluation.PropertyTypePreservationDistance;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.evaluation.model.Edge;
import uk.ac.open.kmi.squire.evaluation.model.Graph;
import uk.ac.open.kmi.squire.evaluation.model.ProceduralGraph;
import uk.ac.open.kmi.squire.evaluation.model.ResidentGraph;
import uk.ac.open.kmi.squire.operation.InstantiateTemplateVar;
import uk.ac.open.kmi.squire.operation.IsSparqlQuerySatisfiableStateful;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.utils.StringUtils;

public class GraphSearchSpecializer extends Specializer {

	/**
	 * variant that does not require a wholly generated graph, but only the starting
	 * node.
	 * 
	 * @author alessandro
	 *
	 * @param <V>
	 */
	public class DijkstraDynamic extends Dijkstra<QueryCtxNode> {

		private Map<VarMapping<Var, Node>, QueryCtxNode> vertexMap = new HashMap<>();

		Set<QueryCtxNode> fringe = new HashSet<>();

		/**
		 * 
		 * @param graph
		 */
		public DijkstraDynamic(Graph<QueryCtxNode> partialGraph) {
			super(partialGraph);
			this.edges.clear();
		}

		@Override
		public void execute(QueryCtxNode source) {
			vertexMap.put(new RdfVarMapping(), source);
			super.execute(source);
		}

		@Override
		protected float getDistance(QueryCtxNode node, QueryCtxNode neighbor) {
			log.debug("Measuring cost of {} to {}", node.getBindings(), neighbor.getBindings());
			if (edges.containsKey(node))
				for (Edge<QueryCtxNode> edge : edges.get(node))
					if (edge.getSource().equals(node) && edge.getDestination().equals(neighbor)) {
						float cost = super.getDistance(node, neighbor);
						log.debug(" ... edge is already stored. Cost = {}", cost);
						return cost;
					}
			// Find the operation that goes from one node to another
			VarMapping<Var, Node> fromOps = node.getBindings(), toOps = neighbor.getBindings();
			if (toOps.getVarToValueTable().size() != fromOps.getVarToValueTable().size() + 1)
				throw new RuntimeException(
						"Nodes requiring more than one operation to reach one another cannot be neighbors.");
			Map<Var, Node> toTable = toOps.getVarToValueTable();
			for (Entry<Var, Node> e : fromOps.getVarToValueEntries()) {
				Var k = e.getKey();
				if (!toTable.containsKey(k) || !toTable.get(k).equals(e.getValue()))
					throw new RuntimeException("It seems these nodes are not neighbors");
			}
			for (Entry<Var, Node> e : toOps.getVarToValueEntries()) {
				log.debug("Inspecting operation {} <- {}", e.getKey(), e.getValue());
				Var k = e.getKey();
				if (!fromOps.getVarToValueTable().containsKey(k)) {
					log.debug(" ... Apparently still needs to be computed.");
					Node nodeFrom = getOriginalEntity(k);
					Node nodeTo = e.getValue();
					String entityqO_TMP = StringUtils.getLocalName(nodeFrom.getURI());
					String entityqR_TMP = StringUtils.getLocalName(nodeTo.getURI());
					float convergence = nodeFactory.computeInstantiationCost(entityqO_TMP, entityqR_TMP,
							neighbor.getTransformedQuery());
					log.debug("Convergence of {} to {} is {}", entityqO_TMP, entityqR_TMP, convergence);
					float qspecdist = new QuerySpecificityDistance().computeQSDwrtQueryTP(qO,
							neighbor.getTransformedQuery());
					// log.debug("Specificity distance (TP) from {} to {} is {}", qO,
					// neighbor.getTransformedQuery(), qspecdist);
					float preservdist = new PropertyTypePreservationDistance().compute(neighbor.getBindings(), rdfd2);
					log.debug("Preservation distance of properties of {} wrt {} is {}", neighbor.getBindings(), rdfd2,
							preservdist);

					float cost = convergence + qspecdist + preservdist;

					addEdge(new Edge<QueryCtxNode>(node, neighbor, cost));
					return cost;
				} else
					log.debug(" ... already performed, not checking again. Is that correct?");
			}
			throw new RuntimeException("Should not happen");
		}

		@Override
		protected List<QueryCtxNode> findMinimalDistances(QueryCtxNode node) {
			List<QueryCtxNode> lastAdjacent = super.findMinimalDistances(node);
			if(lastAdjacent.isEmpty())
				notifyQueryRecommendation(node.getTransformedQuery(), 1.0f/getShortestDistance(node));
			return lastAdjacent;

		}

		@Override
		protected List<QueryCtxNode> getNeighbors(QueryCtxNode node) {
			List<QueryCtxNode> neighbors = new ArrayList<>();
			// log.debug("Finding neighbours of {}", node.getBindings());
			vertexMap.put(new RdfVarMapping(node.getBindings()), node);
			List<QuerySolution> sols = node.getTplVarSolutionSpace();
			for (QuerySolution sol : sols) {
				Set<String> vars = new HashSet<>();
				for (Iterator<String> it = sol.varNames(); it.hasNext();)
					vars.add(it.next());
				// Check all combinations of variable bindings in this solution:
				// Those that _properly_ contain the bindings of this node are neighbors
				PowerSet<String> pset = new PowerSet<String>(vars);
				for (Set<String> set : pset) {
					// log.debug("Checking if {} is a candidate neighbor of {}", set,
					// node.getBindings());
					Map<Var, Node> bindings = node.getBindings().getVarToValueTable();
					// A neighbor of this node must be larger than this node by exactly one.
					if (set.size() != bindings.size() + 1)
						continue;
					boolean isNeighbor = true;
					NodeTransformation op = null;
					for (Entry<Var, Node> e : bindings.entrySet()) {
						String vn = e.getKey().getName();
						if (!set.contains(vn) || !e.getValue().equals(sol.get(vn).asNode())) {
							isNeighbor = false;
							break;
						}
					}
					// log.debug("Is {} a neighbor of {} ? {}", set, node.getBindings(),
					// isNeighbor);
					if (isNeighbor) {
						// Find the "new" variable in the new node
						for (String s : set) {
							Var v = Var.alloc(s);
							// log.debug("Is {} a key in {} ? {}", v, bindings, bindings.containsKey(v));
							if (!bindings.containsKey(v)) {
								if (op != null) {
									log.error("Assigned transformation {} to {}", op.getFrom(), op.getTo());
									throw new RuntimeException("Node transformation was already assigned.");
								}
								// log.debug("Assigning transformation {} to {}", v, sol.get(s).asNode());
								op = new NodeTransformation(v, sol.get(s).asNode());
								break;
							}
						}
						VarMapping<Var, Node> key = makeVarMapping(set.toArray(new String[0]), sol);
						if (!vertexMap.containsKey(key)) {
							Query qOp = node.getTransformedQuery().cloneQuery();
							InstantiateTemplateVar op_inst = new InstantiateTemplateVar();
							qOp = op_inst.instantiateVarTemplate(qOp, (Var) op.getFrom(), op.getTo());
							QueryCtxNode newNode = new QueryCtxNode(qOp, key);
							newNode.setTplVarSolutionSpace(node.getTplVarSolutionSpace());
							vertexMap.put(key, newNode);
						}
						QueryCtxNode newNode = vertexMap.get(key);
						if (!isSettled(newNode))
							neighbors.add(newNode);
					}
				}
			}

			if (neighbors.isEmpty())
				fringe.add(node);
			return neighbors;
		}

	}

	/**
	 * Generator version
	 * 
	 * @author alessandro
	 *
	 * @param <E>
	 */
	private class PowerSet<E> implements Iterator<Set<E>>, Iterable<Set<E>> {
		private E[] arr = null;
		private BitSet bset = null;

		@SuppressWarnings("unchecked")
		public PowerSet(Set<E> set) {
			arr = (E[]) set.toArray();
			bset = new BitSet(arr.length + 1);
		}

		@Override
		public boolean hasNext() {
			return !bset.get(arr.length);
		}

		@Override
		public Iterator<Set<E>> iterator() {
			return this;
		}

		@Override
		public Set<E> next() {
			Set<E> returnSet = new TreeSet<E>();
			for (int i = 0; i < arr.length; i++) {
				if (bset.get(i))
					returnSet.add(arr[i]);
			}
			// increment bset
			for (int i = 0; i < bset.size(); i++) {
				if (!bset.get(i)) {
					bset.set(i);
					break;
				} else
					bset.clear(i);
			}

			return returnSet;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not Supported!");
		}

	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final List<QueryCtxNode> recommendations = new ArrayList<>();

	public GraphSearchSpecializer(Query qo, Query qr, IRDFDataset d1, IRDFDataset d2, MappedQueryTransform previousOp,
			float resultTypeSimilarityDegree, float queryRootDistanceDegree, float resultSizeSimilarityDegree,
			float querySpecificityDistanceDegree, boolean strict, String token) {
		super(qo, qr, d1, d2, previousOp, resultTypeSimilarityDegree, queryRootDistanceDegree,
				resultSizeSimilarityDegree, querySpecificityDistanceDegree, strict, token);
	}

	public List<QueryCtxNode> getSpecializations() {
		return recommendations;
	}

	public List<QueryCtxNode> specialize() {

		log.debug(" - {} specializable query templates", this.specializables.size());
		for (QueryCtxNode qctx : this.specializables) {
			log.debug("   - original query:\r\n{}", qctx.getOriginalQuery());
			log.debug("   - generalized query:\r\n{}", qctx.getTransformedQuery());
		}

		IsSparqlQuerySatisfiableStateful satisfiability = new IsSparqlQuerySatisfiableStateful(this.rdfd2);

		Map<QueryCtxNode, Float> scoreMap = new HashMap<>();

		// XXX Scary conditioned loop : specializables is reduced in another method...
		while (!this.specializables.isEmpty()) {
			QueryCtxNode parentNode = popTopScoredQueryCtx(this.specializables);

			// Dijkstra will go ballistic if there are paths to self,
			// Therefore a non-specializable query must break immediately.
			boolean isTpl = isQueryTemplated(parentNode);
			log.debug("Single specializable node:");
			log.debug(" - has template variables that can be instantiated: {}", isTpl ? "YES" : "NO");
			if (!isTpl) {
				this.recommendations.add(parentNode);
				//notifyQueryRecommendation(parentNode.getTransformedQuery(), parentNode.getqRScore());
				continue;
			}

			// Construct the graph
			Graph<QueryCtxNode> g
			// = buildGraph(parentNode);
					= buildDynamicGraph(parentNode);
			DijkstraDynamic dijkstra = new DijkstraDynamic(g);
			log.debug("Building minimum spanning tree (nodes[initial]={}, edges=NOT_GENERATED)",
					g.getVertices().size());
			dijkstra.execute(parentNode);

			// Inspect for final vertices
			// Set<QueryCtxNode> finalVertices = new HashSet<>(g.getVertices());
			// for (Edge<QueryCtxNode> edge : dijkstra.getEdges().values())
			// if (!dijkstra.getEdges().containsKey(edge.getDestination()))
			// finalVertices.add(edge.getSource());

			for (QueryCtxNode end : dijkstra.fringe) {
				log.debug("Checking for paths from {} to {}", parentNode.getBindings(), end.getBindings());
				List<QueryCtxNode> path = dijkstra.getPath(end);
				if (path == null) {
					log.warn("No path to {} !", end.getBindings());
					continue;
				}
				for (QueryCtxNode vertex : path) {
					System.out.println(vertex.getBindings());
				}

				float cost = dijkstra.getShortestDistance(end);
				System.out.println("cost = " + cost);

				scoreMap.put(end, cost);
			}

		} // end while

		Map<QueryCtxNode, Float> sortedByCount = scoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		for (Entry<QueryCtxNode, Float> e : sortedByCount.entrySet()) {
			log.info("{} : {}", e.getValue(), e.getKey().getTransformedQuery());
			float score = 1f / e.getValue();
			e.getKey().setqRScore(score);
			this.recommendations.add(e.getKey());
			// notifyQueryRecommendation(e.getKey().getTransformedQuery(), score);
		}

		this.notifyQueryRecommendationCompletion(true);
		return this.recommendations;
	}

	private Graph<QueryCtxNode> buildDynamicGraph(QueryCtxNode root) {
		Set<QueryCtxNode> nodes = new HashSet<>();
		nodes.add(root); // just use the root node
		Graph<QueryCtxNode> g = new ProceduralGraph<>(nodes);
		return g;
	}

	private Graph<QueryCtxNode> buildGraph(QueryCtxNode root) {

		Map<VarMapping<Var, Node>, QueryCtxNode> ops2Nodes = new HashMap<>();
		Set<Edge<QueryCtxNode>> edges = new HashSet<>();
		VarMapping<Var, Node> blank = new RdfVarMapping();
		ops2Nodes.put(blank, root);
		List<QuerySolution> sols = root.getTplVarSolutionSpace();

		Set<String> vars = new HashSet<>();
		for (QuerySolution sol : sols) {
			for (Iterator<String> it = sol.varNames(); it.hasNext();) {
				String varName = it.next();
				vars.add(varName);
				NodeTransformation op = new NodeTransformation(Var.alloc(varName), sol.get(varName).asNode());
				buildNext(blank, op, ops2Nodes, edges);
			}

			PowerSet<String> pset = new PowerSet<String>(vars);

			for (Set<String> set : pset) {

				// System.out.println(set);

				// Compute the rest
				Set<String> rest = new HashSet<>();
				for (Iterator<String> it = sol.varNames(); it.hasNext();) {
					String v = it.next();
					if (!set.contains(v))
						rest.add(v);
				}

				// for every set create a node if it doesn't exist.
				// Combine this and the rest into another node.

				// Something like the above, but taking into account the set and the rest
				// and keeping track of the parent node.

				// One for itself....
				for (String v : rest) {
					VarMapping<Var, Node> rvm = makeVarMapping(set.toArray(new String[0]), sol);
					NodeTransformation op = new NodeTransformation(Var.alloc(v), sol.get(v).asNode());
					buildNext(rvm, op, ops2Nodes, edges);
				}
			}
		}

		Graph<QueryCtxNode> g = new ResidentGraph<>(ops2Nodes.values(), edges);
		return g;
	}

	private QueryCtxNode buildNext(final VarMapping<Var, Node> current, NodeTransformation op,
			Map<VarMapping<Var, Node>, QueryCtxNode> vertexMap, Set<Edge<QueryCtxNode>> edges) {
		if (!vertexMap.containsKey(current))
			throw new IllegalArgumentException("Vertex map must contain current var mapping as key.");

		VarMapping<Var, Node> applied = new RdfVarMapping(current);
		Node from = op.getFrom();
		if (!from.isVariable())
			throw new IllegalStateException(
					"Original node of transformation was supposed to be a variable, but was instead a "
							+ from.getClass().getName());
		applied.put((Var) from, op.getTo());
		QueryCtxNode parent = vertexMap.get(current), child;
		Query qOp = parent.getTransformedQuery().cloneQuery();
		InstantiateTemplateVar op_inst = new InstantiateTemplateVar();

		qOp = op_inst.instantiateVarTemplate(qOp, (Var) from, op.getTo());
		Node nodeFrom = getOriginalEntity((Var) from);
		Node nodeTo = op.getTo();
		String entityqO_TMP = StringUtils.getLocalName(nodeFrom.getURI());
		String entityqR_TMP = StringUtils.getLocalName(nodeTo.getURI());
		float cost = nodeFactory.computeInstantiationCost(entityqO_TMP, entityqR_TMP, qOp);
		cost += new QuerySpecificityDistance().computeQSDwrtQueryTP(this.qO, qOp);
		cost += new PropertyTypePreservationDistance().compute(applied, this.rdfd2);
		if (vertexMap.containsKey(applied)) {
			// log.warn("Applied child already exists: "+applied);

			// But try to add an edge
			edges.add(new Edge<QueryCtxNode>(parent, vertexMap.get(applied), cost));
			return vertexMap.get(applied);
		}
		log.debug("Creating node for {} + {}", current, op);

		child = new QueryCtxNode(qOp, applied);
		child.setOriginalQuery(this.qO);
		vertexMap.put(applied, child);

		edges.add(new Edge<QueryCtxNode>(parent, child, cost));
		return child;
	}

	private VarMapping<Var, Node> makeVarMapping(String[] vars, QuerySolution sol) {
		VarMapping<Var, Node> rvm = new RdfVarMapping();
		for (String s : vars)
			rvm.put(Var.alloc(s), sol.get(s).asNode());
		return rvm;
	}

}
