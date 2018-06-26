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

		public SQInstantiationVisitor(Var tplVar, Node node) {
			if (node == null || tplVar == null) throw new IllegalArgumentException(
					"Neither the template variable to be instantiated nor the instance node can be null.");
			this.node = node;
			this.varTemplate = tplVar;
		}

		@Override
		public void visit(ElementPathBlock el) {
			if (el == null)
				throw new IllegalStateException("Called a visit on a null path block. This shouldn't happen.");
			ListIterator<TriplePath> it = el.getPattern().iterator();
			while (it.hasNext()) {
				final TriplePath tp = it.next();

				// SUBJECT
				Node old = tp.getSubject();
				final Node newSubject;
				if (old.isVariable() && old.getName().equals(varTemplate.getName())) {
					if (node.isURI()) newSubject = NodeFactory.createURI(node.getURI());
					// XXX can it really ever be so?
					else if (node.isLiteral()) newSubject = NodeFactory.createLiteral(node.getLiteral());
					else newSubject = old;
				} else newSubject = old;

				// PREDICATE
				old = tp.getPredicate();
				final Node newPredicate;
				if (old.isVariable() && old.getName().equals(varTemplate.getName()) && node.isURI())
					newPredicate = NodeFactory.createURI(node.getURI());
				else newPredicate = old;

				// OBJECT
				old = tp.getObject();
				final Node newObject;
				if (old.isVariable() && old.getName().equals(varTemplate.getName())) {
					if (node.isURI()) newObject = NodeFactory.createURI(node.getURI());
					else if (node.isLiteral()) newObject = NodeFactory.createLiteral(node.getLiteral());
					else newObject = old;
				} else newObject = old;

				TriplePath newTriplePattern = new TriplePath(new Triple(newSubject, newPredicate, newObject));
				it.set(newTriplePattern);
			}
		}

	}

	public Query instantiateVarTemplate(Query q, Var tplVar, Node n) {
		if (q == null || n == null || tplVar == null) throw new IllegalArgumentException(
				"Neither the query, nor the template variable to be instantiated, nor the instance node can be null.");
		SQInstantiationVisitor instVisitor = new SQInstantiationVisitor(tplVar, n);
		ElementWalker.walk(q.getQueryPattern(), instVisitor);
		return q;
	}

}
