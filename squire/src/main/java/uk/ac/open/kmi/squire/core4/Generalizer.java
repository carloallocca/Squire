package uk.ac.open.kmi.squire.core4;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.vocabulary.RDF;

import uk.ac.open.kmi.squire.operation.SparqlQueryGeneralization;

/**
 * The {@link BasicGeneralizer} is in charge of deciding which nodes in a query
 * pattern are turned into template variables and which are not, and then
 * applies a {@link SparqlQueryGeneralization} to the designated nodes.
 * 
 * @author carloallocca, Alessandro Adamou<alexdma@apache.org>
 */
public interface Generalizer {

	/**
	 * Internal wrapper for {@link Query} which also keeps an index of the
	 * properties and types in the triple patterns of each root subject (i.e. not an
	 * intermediate subject in a chain of triple patterns).
	 */
	class MappedQuery {

		private Query embedded;

		private Map<Node, Set<Node>> pathOriginsBySubject = new HashMap<>();

		private Map<Node, Set<Node>> typesBySubject = new HashMap<>();

		public MappedQuery(Query query) {
			if (query == null)
				throw new IllegalArgumentException("Cannot create a generalized query from a null query.");
			this.embedded = query;

			/*
			 * Inspect the query
			 */
			ElementWalker.walk(embedded.getQueryPattern(), new ElementVisitorBase() {
				@Override
				public void visit(ElementPathBlock el) {
					Iterator<TriplePath> triples = el.patternElts();
					while (triples.hasNext()) {
						TriplePath tp = triples.next();
						Node p = tp.getPredicate(), s = tp.getSubject();
						if (p.isURI()) {
							if (RDF.type.getURI().equals(p.getURI()) && tp.getObject().isConcrete()) {
								if (!typesBySubject.containsKey(s)) typesBySubject.put(s, new HashSet<>());
								typesBySubject.get(s).add(tp.getObject());
							} else {
								if (!pathOriginsBySubject.containsKey(s)) pathOriginsBySubject.put(s, new HashSet<>());
								pathOriginsBySubject.get(s).add(p);
							}
						}
					}
				}
			});
		}

		public Set<Node> getPathOrigins(Node subject) {
			if (!pathOriginsBySubject.containsKey(subject)) return Collections.emptySet();
			return Collections.unmodifiableSet(pathOriginsBySubject.get(subject));
		}

		public Map<Node, Set<Node>> getPathOriginsPerSubject() {
			return Collections.unmodifiableMap(pathOriginsBySubject);
		}

		/**
		 * 
		 * @return the embedded query
		 */
		public Query getQuery() {
			return embedded;
		}

		public Set<Node> getRootSubjects() {
			Set<Node> set = new HashSet<>(typesBySubject.keySet());
			set.addAll(pathOriginsBySubject.keySet());
			return Collections.unmodifiableSet(set);
		}

		public Set<Node> getTypes(Node subject) {
			if (!typesBySubject.containsKey(subject)) return Collections.emptySet();
			return Collections.unmodifiableSet(typesBySubject.get(subject));
		}

		public Map<Node, Set<Node>> getTypesPerSubject() {
			return Collections.unmodifiableMap(typesBySubject);
		}

	}

	/**
	 * Computes the <em>least general generalization</em> of the given query, i.e.
	 * the queries that are satisfiable with a target dataset and contain as many
	 * query patterns from the original query as possible.
	 * 
	 * If the method returns a singleton, i.e. a single general query, this should
	 * not be the same object as the given query, that is, the method should not
	 * alter the original query but clone it instead.
	 * 
	 * @param q
	 *            the query to be generalized
	 * 
	 * @return the generalized queries.
	 */
	public Set<Query> generalize(Query q);

}
