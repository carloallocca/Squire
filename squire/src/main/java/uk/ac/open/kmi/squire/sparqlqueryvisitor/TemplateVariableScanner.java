/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_CLASS;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_DT;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_OBJ;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

/**
 *
 * @author carloallocca
 */
public class TemplateVariableScanner extends ElementVisitorBase {

	private Set<Var> templateVariables = null;

	public Set<Var> extractTemplateVariables(TriplePath bgp) {
		Set<Var> res = new HashSet<>();
		Node subject = bgp.getSubject();
		if (isTemplateVar(subject))
			res.add((Var) subject);
		Node predicate = bgp.getPredicate();
		if (isTemplateVar(predicate))
			res.add((Var) predicate);
		Node object = bgp.getObject();
		if (isTemplateVar(object))
			res.add((Var) object);
		return res;
	}

	public Set<Var> getTemplateVariables() {
		if (templateVariables == null)
			throw new IllegalStateException("Called getTemplateVariables() before they were extracted.");
		return templateVariables;
	}

	public Set<Var> getTemplateVariables(String[] prefixes) {
		Set<Var> res = new HashSet<>();
		for (Var v : getTemplateVariables())
			for (String prefix : prefixes)
				if (v.getName().startsWith(prefix))
					res.add(v);
		return res;
	}

	public void reset() {
		this.templateVariables = null;
	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null)
			throw new IllegalArgumentException("Element path block cannot be null.");
		if (this.templateVariables == null)
			this.templateVariables = new HashSet<>();
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp = it.next();
			this.templateVariables.addAll(extractTemplateVariables(tp));
		}
	}

	private boolean isTemplateVar(Node node) {
		if (node.isConcrete())
			return false;
		String name = ((Var) node).getName();
		return name.startsWith(TEMPLATE_VAR_CLASS) || name.startsWith(TEMPLATE_VAR_PROP_OBJ)
				|| name.startsWith(TEMPLATE_VAR_PROP_DT);
	}

}
