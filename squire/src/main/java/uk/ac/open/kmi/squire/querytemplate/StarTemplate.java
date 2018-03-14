package uk.ac.open.kmi.squire.querytemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For instance, the BGPs
 * 
 * ?x ?p ?y ; rdf:type ?t
 * 
 * and
 * 
 * ?x rdf:type ?t
 * 
 * should have the same template, because their satisfiability is the same.
 * 
 * @author alessandro
 *
 */
public class StarTemplate {

	public static class NotAStarQuery extends RuntimeException {

		private Query q;

		public NotAStarQuery(Query query) {
			this.q = query;
		}

		public Query getQuery() {
			return this.q;
		}

	}

	private static Logger log = LoggerFactory.getLogger(StarTemplate.class);

	public static StarTemplate fromQuery(Query query) {
		if (!checkStarQuery(query)) throw new NotAStarQuery(query);

		final int[] seq = new int[] { 0 };
		final StarTemplate tpl = new StarTemplate();
		final Map<Var, Var> varMap = new HashMap<>();

		ElementVisitor visitor = new ElementVisitorBase() {

			@Override
			public void visit(ElementGroup el) {
				for (Element ele : el.getElements())
					ele.visit(this);
			}

			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					if (!tp.isTriple()) throw new UnsupportedOperationException(
							"Triple path '" + tp + "' is not a simple triple. Templating from paths is not supported.");
					Triple t = new Triple(handle(tp.getSubject()), handle(tp.getPredicate()), handle(tp.getObject()));
					tpl.tps.add(new TriplePath(t));
				}
				tpl.tps = Collections.unmodifiableSet(tpl.tps);
			};

			private Node handle(Node n) {
				if (n.isVariable()) {
					Var v;
					if (varMap.containsKey((Var) n)) v = varMap.get((Var) n);
					else {
						v = Var.alloc("v" + (++seq[0]));
						varMap.put((Var) n, v);
					}
					return v;
				}
				return n;
			}

		};

		query.getQueryPattern().visit(visitor);
		return tpl;
	}

	private static boolean checkStarQuery(Query q) {
		final Node[] subject = new Node[] { null };
		final boolean[] result = new boolean[] { true };
		q.getQueryPattern().visit(new ElementVisitorBase() {

			@Override
			public void visit(ElementGroup el) {
				for (Element ele : el.getElements())
					ele.visit(this);
			}

			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					if (subject[0] == null) subject[0] = tp.getSubject();
					if (!subject[0].equals(tp.getSubject())) result[0] = false;
				}
			}
		});
		return result[0];
	}

	private Set<TriplePath> tps;

	protected StarTemplate() {
		tps = new HashSet<>();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StarTemplate)) return false;
		StarTemplate st = (StarTemplate) obj;
		return this.getTriplePaths().equals(st.getTriplePaths());
	}

	public Set<TriplePath> getTriplePaths() {
		return this.tps;
	}

}
