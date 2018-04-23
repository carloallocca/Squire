package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

/**
 *
 * @author carloallocca
 */
public class SQRemoveTripleVisitor extends ElementVisitorBase {

	private Triple tp; // This is the triple pattern that we need to remove from the given query.

	public SQRemoveTripleVisitor(Triple triplePattern) {
		this.tp = triplePattern;
		// triplePattern.getObject().isVariable()
	}

	@Override
	public void visit(ElementOptional el) {
		Element optionalQP = el.getOptionalElement();
		ElementWalker.walk(optionalQP, this);
	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) throw new IllegalArgumentException("The ElementPathBlock is null");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp1 = it.next();
			if (this.tp != null && this.tp.matches(tp1.asTriple())) it.remove();
			// queryGPE.add(tp.toString());
			// System.out.println(tp.toString());
		}
	}

	@Override
	public void visit(ElementFilter el) {
		// ...get the variables of the FILTER expression
		Expr filterExp = el.getExpr();// .getVarsMentioned().contains(el);
		Set<Var> expVars = filterExp.getVarsMentioned();

		// ...get the variables of the triple pattern that we want to delete
		Set<Var> tpVars = new HashSet<>();
		Node subj = this.tp.getSubject();
		if (subj.isVariable()) tpVars.add((Var) subj);
		Node pred = this.tp.getPredicate();
		if (pred.isVariable()) tpVars.add((Var) pred);
		Node obj = this.tp.getObject();
		if (obj.isVariable()) tpVars.add((Var) obj);

		// ...check whether the FILTER expression contains any of the triple pattern
		// variable
		for (Var var : expVars) {
			// ..if it does then we have to delete the entire FILTER expression
			if (tpVars.contains(var)) {
				// UpdateRequest updates = UpdateFactory.create();
				//
				// // set ?username to "test"
				// HashMap<Var, Node> varNodeHashMap = new HashMap<Var, Node>();
				// varNodeHashMap.put(var, NodeFactory.createLiteral("test"));
				//
				// UpdateRequest transform = UpdateTransformOps.transform(updates,
				// varNodeHashMap);
				// System.out.println("--- TRANSFORMED ---\n"+transform.toString());
			}
		}

	}

}
