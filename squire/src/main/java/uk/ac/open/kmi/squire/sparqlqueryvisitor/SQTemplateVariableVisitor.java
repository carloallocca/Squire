/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

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
public class SQTemplateVariableVisitor extends ElementVisitorBase {

	private final Set<Var> queryTemplateVariableSet = new HashSet<>();

	private static final String CLASS_TEMPLATE_VAR = "ct";
	private static final String OBJ_PROP_TEMPLATE_VAR = "opt";
	private static final String DT_PROP_TEMPLATE_VAR = "dpt";
	private static final String INDIVIDUAL_TEMPLATE_VAR = "it";
	private static final String LITERAL_TEMPLATE_VAR = "lt";

	public SQTemplateVariableVisitor() {
		super();
	}

	public Set<Var> getQueryTemplateVariableSet() {
		return queryTemplateVariableSet;
	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) {
			throw new IllegalStateException(
					"[SQVariableVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
		}
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp = it.next();
			// System.out.println("The triple ==> " + tp.toString());
			Node subject = tp.getSubject();
			// SUBJECT
			if (subject.isVariable()) {
				if (isTemplateVar(subject)) {
					if (!this.queryTemplateVariableSet.contains((Var) subject)) {
						this.queryTemplateVariableSet.add((Var) subject);
					}
				}
			}
			// PREDICATE
			Node predicate = tp.getPredicate();
			if (predicate.isVariable()) {
				if (isTemplateVar(predicate)) {
					if (!this.queryTemplateVariableSet.contains((Var) predicate)) {
						this.queryTemplateVariableSet.add((Var) predicate);
					}
				}
			}
			// OBJECT
			Node object = tp.getObject();
			if (object.isVariable()) {
				if (isTemplateVar(object)) {
					if (!this.queryTemplateVariableSet.contains((Var) object)) {
						this.queryTemplateVariableSet.add((Var) object);
					}
				}
			}
		}
	}

	private boolean isTemplateVar(Node subject) {
		return ((Var) subject).getName().startsWith(CLASS_TEMPLATE_VAR)
				|| ((Var) subject).getName().startsWith(OBJ_PROP_TEMPLATE_VAR)
				|| ((Var) subject).getName().startsWith(DT_PROP_TEMPLATE_VAR);
	}

}
