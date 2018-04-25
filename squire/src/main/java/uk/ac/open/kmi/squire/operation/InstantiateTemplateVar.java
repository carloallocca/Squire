package uk.ac.open.kmi.squire.operation;

import java.util.ListIterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

/**
 * 
 * TODO make this an {@link Operator}.
 *
 * @author callocca
 */
public class InstantiateTemplateVar {

	private class SQInstantiationVisitor extends ElementVisitorBase {

		private Node node;
		private Var varTemplate;

		public SQInstantiationVisitor(Var varTemplate, Node node) {
			// System.out.println("The triple ==> " + tp.toString());
			if (node == null || varTemplate == null) {
				throw new IllegalStateException("[SQInstantiationVisitor]The Node or the varTemplate is null!!");
			}
			this.node = node;
			this.varTemplate = varTemplate;
		}

		@Override
		public void visit(ElementPathBlock el) {
			// System.out.println("[SQInstantiationVisitor::visit(ElementPathBlock el)] ");
			if (el == null) {
				throw new IllegalStateException(
						"[SQInstantiationVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
			}
			ListIterator<TriplePath> it = el.getPattern().iterator();
			while (it.hasNext()) {
				final TriplePath tp = it.next();
				// System.out.println("The triple ==> " + tp.toString());
				Node oldSubject = tp.getSubject();
				final Node newSubject;

				// SUBJECT
				if (oldSubject.isVariable()) {
					if (oldSubject.getName().equals(varTemplate.getName())) {
						if (node.isURI()) {
							newSubject = NodeFactory.createURI(node.getURI());
						} else if (node.isLiteral()) {
							newSubject = NodeFactory.createLiteral(node.getLiteral());
						} else {
							newSubject = oldSubject;
						}
					} else {
						newSubject = oldSubject;
					}
				} else {
					newSubject = oldSubject;
				}

				// PREDICATE
				Node oldPredicate = tp.getPredicate();
				final Node newPredicate;
				if (oldPredicate.isVariable()) {
					if (oldPredicate.getName().equals(varTemplate.getName())) {
						if (node.isURI()) {
							newPredicate = NodeFactory.createURI(node.getURI());
						} else {
							newPredicate = oldPredicate;
						}
					} else {
						newPredicate = oldPredicate;
					}
				} else {
					newPredicate = oldPredicate;
				}

				// OBJECT
				Node oldObject = tp.getObject();
				final Node newObject;

				if (oldObject.isVariable()) {
					if (oldObject.getName().equals(varTemplate.getName())) {
						if (node.isURI()) {
							newObject = NodeFactory.createURI(node.getURI());
						} else if (node.isLiteral()) {
							newObject = NodeFactory.createLiteral(node.getLiteral());
						} else {
							newObject = oldObject;
						}
					} else {
						newObject = oldObject;
					}
				} else {
					newObject = oldObject;
				}
				TriplePath newTriplePattern = new TriplePath(new Triple(newSubject, newPredicate, newObject));
				it.set(newTriplePattern);
			}
		}

	}

	public Query instantiateVarTemplate(Query q, Var varTemplate, Node n) {
		if (q == null || n == null || varTemplate == null) {
			throw new IllegalArgumentException(
					"[SPARQLQueryGeneralization::generalize()]The Query or the Node or the Var is null!!");
		}
		// Query newQuery=QueryFactory.create(q.toString());
		SQInstantiationVisitor instVisitor = new SQInstantiationVisitor(varTemplate, n);
		ElementWalker.walk(q.getQueryPattern(), instVisitor);
		// this.generalizedQuery=this.originalQuery;
		return q;
	}

}
