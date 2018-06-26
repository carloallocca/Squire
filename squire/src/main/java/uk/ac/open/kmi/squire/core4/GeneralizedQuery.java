package uk.ac.open.kmi.squire.core4;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

/**
 * A wrapper for a SPARQL {@link Query} that has some nodes replaced by template
 * variables.
 * 
 * Note that a partially generalized query (i.e. one that has at least one node
 * that could still be generalized with respect to a target dataset) is also a
 * {@link GeneralizedQuery}. Therefore, this class can be used to represent
 * intermediate steps of generalization or specialization.
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public class GeneralizedQuery {

	private Query embedded;

	private Map<Var, Node> genMap;

	public GeneralizedQuery(Query query) {
		if (query == null) throw new IllegalArgumentException("Cannot create a generalized query from a null query.");
		this.embedded = query;
		this.genMap = new HashMap<>();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Query) return getQuery().equals((Query) obj);
		if (obj instanceof GeneralizedQuery) return embedded.equals(((GeneralizedQuery) obj).getQuery())
				&& genMap.equals(((GeneralizedQuery) obj).getGenMap());
		return false;
	}

	public Map<Var, Node> getGenMap() {
		return genMap;
	}

	public Node getGenMapping(Var tplVar) {
		return genMap.get(tplVar);
	}

	public Query getQuery() {
		return this.embedded;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] { embedded, genMap });
	}

	public void setGenMapping(Node before, Var after) {
		genMap.put(after, before);
	}

}
