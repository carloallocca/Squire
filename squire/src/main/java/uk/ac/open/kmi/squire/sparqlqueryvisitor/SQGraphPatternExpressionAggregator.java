/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.ListIterator;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;

/**
 *
 * @author carloallocca
 */
public class SQGraphPatternExpressionAggregator extends AbstractSQAggregator<TriplePath> {

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) throw new IllegalArgumentException("The element path block must not be null.");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext())
			queryEntitySet.add(it.next());
	}

}
