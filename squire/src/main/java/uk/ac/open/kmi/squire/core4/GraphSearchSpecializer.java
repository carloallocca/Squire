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
import uk.ac.open.kmi.squire.evaluation.model.SpecializationGraph;
import uk.ac.open.kmi.squire.operation.InstantiateTemplateVar;
import uk.ac.open.kmi.squire.operation.IsSparqlQuerySatisfiableStateful;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.utils.StringUtils;

public class GraphSearchSpecializer extends Specializer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public GraphSearchSpecializer(Query qo, Query qr, IRDFDataset d1, IRDFDataset d2, MappedQueryTransform previousOp,
			float resultTypeSimilarityDegree, float queryRootDistanceDegree, float resultSizeSimilarityDegree,
			float querySpecificityDistanceDegree, boolean strict, String token) {
		super(qo, qr, d1, d2, previousOp, resultTypeSimilarityDegree, queryRootDistanceDegree,
				resultSizeSimilarityDegree, querySpecificityDistanceDegree, strict, token);
	}

	private final List<QueryCtxNode> recommendations = new ArrayList<>();

	public List<QueryCtxNode> getSpecializations() {
		return recommendations;
	}

	private SpecializationGraph<QueryCtxNode> buildGraph(QueryCtxNode root) {

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

		SpecializationGraph<QueryCtxNode> g = new SpecializationGraph<>(ops2Nodes.values(), edges);
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
			// Construct the graph
			SpecializationGraph<QueryCtxNode> g = buildGraph(parentNode);

			Dijkstra<QueryCtxNode> dijkstra = new Dijkstra<>(g);
			dijkstra.execute(parentNode);

			// Inspect for final vertices
			Set<QueryCtxNode> finalVertices = new HashSet<>(g.getVertices());
			for (Edge<QueryCtxNode> edge : g.getEdges())
				finalVertices.remove(edge.getSource());

			for (QueryCtxNode end : finalVertices) {
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
			notifyQueryRecommendation(e.getKey().getTransformedQuery(), score);
		}

		this.notifyQueryRecommendationCompletion(true);
		return this.recommendations;
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

		@Override
		public Iterator<Set<E>> iterator() {
			return this;
		}

	}

}
