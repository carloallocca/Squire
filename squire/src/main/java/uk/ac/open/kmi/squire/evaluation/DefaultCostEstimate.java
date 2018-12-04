package uk.ac.open.kmi.squire.evaluation;

import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_CLASS;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_DT;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_OBJ;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_PLAIN;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementWalker;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.AbstractSQAggregator;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.TemplateVariableScanner;

/**
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 * 
 *         TODO test that it returns zero if we have reached a recommended query
 *
 */
public class DefaultCostEstimate {

	private Query qStep;

	private Set<Var> tplVarAllProps, tplVarCls, tplVarObj, tplVarDt;

	public DefaultCostEstimate(Query qStep) {
		this.qStep = qStep;
		TemplateVariableScanner s = new TemplateVariableScanner();
		ElementWalker.walk(qStep.getQueryPattern(), s);
		tplVarCls = s.getTemplateVariables(new String[] { TEMPLATE_VAR_CLASS });
		tplVarAllProps = s.getTemplateVariables(
				new String[] { TEMPLATE_VAR_PROP_DT, TEMPLATE_VAR_PROP_OBJ, TEMPLATE_VAR_PROP_PLAIN });
		tplVarObj = s.getTemplateVariables(new String[] { TEMPLATE_VAR_PROP_OBJ });
		tplVarDt = s.getTemplateVariables(new String[] { TEMPLATE_VAR_PROP_DT });
	}

	public float compute(IRDFDataset d2) {
		return estimate_binding_collapse(qStep, d2) + estimate_specificity_distance_Var()
				+ estimate_specificity_distance_TP(qStep) + estimate_result_type_distance(qStep, d2);
	}

	private float estimate_binding_collapse(Query qStep, IRDFDataset d2) {
		// Probability of one or more existing properties to appear in multiple TPs
		float propRatio;
		if (d2.getDatatypePropertySet().isEmpty() && d2.getObjectPropertySet().isEmpty()
				&& !d2.getPropertySet().isEmpty())
			propRatio = (float) tplVarAllProps.size() / d2.getPropertySet().size();
		else
			propRatio = (float) tplVarDt.size() / d2.getDatatypePropertySet().size()
					+ (float) tplVarObj.size() / d2.getObjectPropertySet().size();

		return propRatio + (float) tplVarCls.size() / d2.getClassSet().size();
	}

	private float estimate_result_type_distance(Query qStep, IRDFDataset d2) {
		// likelihood of variables losing their type bindings
		int noq = tplVarObj.size();
		int ndq = tplVarDt.size();
		return (float) (noq * d2.getDatatypePropertySet().size() + ndq * d2.getObjectPropertySet().size())
				/ d2.getPropertySet().size();
	}

	private float estimate_specificity_distance_TP(Query qStep) {
		// number of TPs still with template variables

		final Set<TriplePath> templatedTPs = new HashSet<>();

		ElementWalker.walk(this.qStep.getQueryPattern(), new AbstractSQAggregator<TriplePath>() {
			private boolean isTemplateVar(Node node) {
				if (node.isConcrete())
					return false;
				String name = ((Var) node).getName();
				return name.startsWith(TEMPLATE_VAR_CLASS) || name.startsWith(TEMPLATE_VAR_PROP_OBJ)
						|| name.startsWith(TEMPLATE_VAR_PROP_DT);
			}

			@Override
			public void visit(ElementPathBlock el) {
				ListIterator<TriplePath> it = el.getPattern().iterator();
				while (it.hasNext()) {
					TriplePath tp = it.next();
					if (isTemplateVar(tp.getSubject()) || isTemplateVar(tp.getPredicate())
							|| isTemplateVar(tp.getObject()))
						templatedTPs.add(tp);
				}
			}
		});

		return templatedTPs.size();
	}

	private float estimate_specificity_distance_Var() {
		// number of non-instantiated template variables
		return tplVarAllProps.size() + tplVarCls.size();
	}

}
