package uk.ac.open.kmi.squire.operation;

import java.util.ListIterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a single generalization step replacing a node with a template
 * variable in a query. All three objects are taken as arguments by the
 * {@link #perform(Query, Node, Var)} method.
 *
 * @author callocca
 */
public class SparqlQueryGeneralization {

	private class SQGeneralizationVisitor extends ElementVisitorBase {

		private Node node;
		private Var varTemplate;

		public SQGeneralizationVisitor(Node n, Var varTemplate) {
			if (n == null || varTemplate == null)
				throw new IllegalArgumentException("The RDF node and template variable cannot be null.");
			this.node = n;
			this.varTemplate = varTemplate;
		}

		@Override
		public void visit(ElementPathBlock el) {
			if (el == null) throw new IllegalStateException("The ElementPathBlock is null");
			ListIterator<TriplePath> it = el.getPattern().iterator();
			while (it.hasNext()) {
				final TriplePath tp = it.next();
				log.trace("Visiting triple: {}", tp);

				// SUBJECT
				Node oldSubject = tp.getSubject();
				final Node newSubject;
				if (!oldSubject.isVariable()) {
					if (oldSubject.isURI() && node.isURI()) {
						if (oldSubject.getURI().equals(node.getURI())) newSubject = Var.alloc(varTemplate);
						else newSubject = oldSubject;
					} else newSubject = oldSubject;
				} else newSubject = oldSubject;

				// PREDICATE
				Node oldPredicate = tp.getPredicate();
				final Node newPredicate;
				if (!oldPredicate.isVariable()) {
					if (oldPredicate.isURI() && node.isURI()) {
						if (oldPredicate.getURI().equals(node.getURI())) newPredicate = Var.alloc(varTemplate);
						else newPredicate = oldPredicate;
					} else newPredicate = oldPredicate;
				} else newPredicate = oldPredicate;

				// OBJECT
				Node oldObject = tp.getObject();
				final Node newObject;
				if (!oldObject.isVariable()) {
					if (oldObject.isURI() && node.isURI()) {
						if (oldObject.getURI().equals(node.getURI())) newObject = Var.alloc(varTemplate);
						else newObject = oldObject;
					} else {
						if (oldObject.isLiteral() && node.isLiteral()) {
							if (oldObject.getLiteral().toString().equals(node.getLiteral().toString()))
								newObject = Var.alloc(varTemplate);
							else newObject = oldObject;
						} else newObject = oldObject;
					}
				} else newObject = oldObject;
				TriplePath newTriplePattern = new TriplePath(new Triple(newSubject, newPredicate, newObject));
				it.set(newTriplePattern);
			}
		}

	}

	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * FIXME the operation alters the given query! is it safe?
	 * 
	 * @param q
	 * @param n
	 * @param varTemplate
	 * @return the altered (not cloned!) q
	 */
	public Query perform(Query q, Node n, Var varTemplate) {
		if (q == null) throw new IllegalArgumentException("The query cannot be null.");
		if (n == null) throw new IllegalArgumentException("The node cannot be null.");
		if (varTemplate == null) throw new IllegalArgumentException("The template variable cannot be null.");
		log.trace(" * Generalizing over node {}", n);
		SQGeneralizationVisitor genVisitor = new SQGeneralizationVisitor(n, varTemplate);
		ElementWalker.walk(q.getQueryPattern(), genVisitor);
		log.trace("Generalization step returned query:\r\n{}", q);
		return q;
	}

}
