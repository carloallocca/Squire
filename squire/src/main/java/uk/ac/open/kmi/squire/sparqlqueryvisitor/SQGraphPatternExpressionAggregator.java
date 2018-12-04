package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.ListIterator;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;

/**
 * Simply aggregates all the triple paths in the query pattern.
 * 
 * Remember this is stateful.
 *
 * @author carloallocca
 */
public class SQGraphPatternExpressionAggregator extends AbstractSQAggregator<TriplePath> {

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null)
			throw new IllegalArgumentException("The element path block must not be null.");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext())
			queryEntitySet.add(it.next());
	}

}
