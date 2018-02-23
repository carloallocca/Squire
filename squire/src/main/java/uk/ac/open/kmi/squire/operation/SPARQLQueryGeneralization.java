/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author callocca
 */
public class SPARQLQueryGeneralization {

	private class SQGeneralizationVisitor extends ElementVisitorBase {

		private Node node;
		private Var varTemplate;

		public SQGeneralizationVisitor(Node n, Var varTemplate) {
			// System.out.println("The triple ==> " + tp.toString());
			if (n == null || varTemplate == null) {
				throw new IllegalStateException("[SQGeneralizationVisitor]The Node or the varTemplate is null!!");
			}
			this.node = n;
			this.varTemplate = varTemplate;
		}

		@Override
		public void visit(ElementPathBlock el) {
			// System.out.println("[SQGeneralizationVisitor::visit(ElementPathBlock el)] ");
			if (el == null) {
				throw new IllegalStateException(
						"[SQGeneralizationVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
			}
			ListIterator<TriplePath> it = el.getPattern().iterator();
			while (it.hasNext()) {
				final TriplePath tp = it.next();
				// System.out.println("The triple ==> " + tp.toString());

				Node oldSubject = tp.getSubject();
				final Node newSubject;
				// SUBJECT
				if (!oldSubject.isVariable()) {
					if (oldSubject.isURI() && node.isURI()) {
						if (oldSubject.getURI().equals(node.getURI())) {
							newSubject = Var.alloc(varTemplate);
						} else {
							newSubject = oldSubject;
						}
					} else {
						if (oldSubject.isLiteral() && node.isLiteral()) {
							if (oldSubject.getLiteral().toString().equals(node.getLiteral().toString())) {
								newSubject = Var.alloc(varTemplate);
							} else {
								newSubject = oldSubject;
							}
						} else {
							newSubject = oldSubject;
						}
					}
				} else {
					newSubject = oldSubject;
				}

				Node oldPredicate = tp.getPredicate();
				final Node newPredicate;
				// PREDICATE
				if (!oldPredicate.isVariable()) {
					if (oldPredicate.isURI() && node.isURI()) {
						if (oldPredicate.getURI().equals(node.getURI())) {
							newPredicate = Var.alloc(varTemplate);
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
				if (!oldObject.isVariable()) {
					if (oldObject.isURI() && node.isURI()) {
						if (oldObject.getURI().equals(node.getURI())) {
							newObject = Var.alloc(varTemplate);
						} else {
							newObject = oldObject;
						}
					} else {
						if (oldObject.isLiteral() && node.isLiteral()) {
							if (oldObject.getLiteral().toString().equals(node.getLiteral().toString())) {
								newObject = Var.alloc(varTemplate);
							} else {
								newObject = oldObject;
							}
						} else {
							newObject = oldObject;
						}
					}
				} else {
					newObject = oldObject;
				}
				TriplePath newTriplePattern = new TriplePath(new Triple(newSubject, newPredicate, newObject));
				it.set(newTriplePattern);
			}
		}

	}

	public Query perform(Query q, Node n, Var varTemplate) {
		if (q == null || n == null || varTemplate == null) {
			throw new IllegalArgumentException(
					"[SPARQLQueryGeneralization::generalize()]The Query or the Node or the Var is null!!");
		}
		// Query newQuery=QueryFactory.create(q.toString());
		SQGeneralizationVisitor genVisitor = new SQGeneralizationVisitor(n, varTemplate);
		ElementWalker.walk(q.getQueryPattern(), genVisitor);
		// this.generalizedQuery=this.originalQuery;
		return q;
	}

}
