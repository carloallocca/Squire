/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

/**
 *
 * @author carloallocca
 */
public class SQGraphPatternExpressionVisitor extends ElementVisitorBase {

	private Set<TriplePath> queryGPE = new HashSet<>();

	public Set<TriplePath> getQueryGPE() {
		return queryGPE;
	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) throw new IllegalArgumentException("The element path block must not be null.");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext())
			queryGPE.add(it.next());
	}

}
