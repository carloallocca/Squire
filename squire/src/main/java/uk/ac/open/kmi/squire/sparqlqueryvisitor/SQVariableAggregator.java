package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.ListIterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;

/**
 *
 * @author carloallocca
 */
public class SQVariableAggregator extends AbstractSQAggregator<String> {

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) throw new IllegalArgumentException("The ElementPathBlock is null");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp = it.next();
			handle(tp.getSubject());
			handle(tp.getPredicate());
			handle(tp.getObject());

		}
	}

	private void handle(Node n) {
		if (n.isVariable() && !this.queryEntitySet.contains(n.getName())) this.queryEntitySet.add(n.getName());
	}

}
