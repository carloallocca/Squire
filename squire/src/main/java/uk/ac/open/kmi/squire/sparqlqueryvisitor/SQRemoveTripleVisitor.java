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
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

/**
 *
 * @author carloallocca
 */
public class SQRemoveTripleVisitor extends ElementVisitorBase {

	private Triple tp; // This is the triple pattern that we need to remove from the given query.

	// private subjVar
	public SQRemoveTripleVisitor() {
		super();
	}

	public SQRemoveTripleVisitor(Triple triplePattern) {
		this.tp = triplePattern;
		// triplePattern.getObject().isVariable()
	}

	@Override
	public void visit(ElementAssign el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementAssign el))] ");

	}

	@Override
	public void visit(ElementBind el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementBind el)] ");

	}

	@Override
	public void visit(ElementSubQuery el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementSubQuery el)] ");
	}

	@Override
	public void visit(ElementService el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementService el)] ");
	}

	@Override
	public void visit(ElementMinus el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementMinus el)] ");
	}

	@Override
	public void visit(ElementNotExists el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementNotExists el)] ");
	}

	@Override
	public void visit(ElementExists el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementExists el)] ");
	}

	@Override
	public void visit(ElementNamedGraph el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementNamedGraph el)] ");
	}

	@Override
	public void visit(ElementGroup el) {

		System.out.println("[SQRemoveTripleVisitor::visit(ElementGroup el)] " + el.toString());
		System.out.println("");
	}

	@Override
	public void visit(ElementOptional el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementOptional el)] ");
		// get optional elements and walk them
		Element optionalQP = el.getOptionalElement();
		ElementWalker.walk(optionalQP, this);

	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) {
			throw new IllegalStateException(
					"[SQRemoveTripleVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
		}

		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp1 = it.next();
			System.out
					.println("[SQRemoveTripleVisitor::11111111111111111] TriplePath tp1 " + tp1.asTriple().toString());

			if (this.tp != null) {
				if (this.tp.matches(tp1.asTriple())) {
					System.out
							.println("[SQRemoveTripleVisitor::3333333333333] this.tp.toString() " + this.tp.toString());

					it.remove();
				} else {
					System.out.println(
							"[SQRemoveTripleVisitor::222222222222222222] this.tp.toString() " + this.tp.toString());

				}
			}
			// queryGPE.add(tp.toString());
			// System.out.println(tp.toString());
		}
		System.out.println("[SQRemoveTripleVisitor::visit(ElementPathBlock el)] " + el.toString());
		System.out.println("");

	}

	@Override
	public void visit(ElementDataset el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementDataset el)] ");
	}

	@Override
	public void visit(ElementUnion el) {
		System.out.println("[SQRemoveTripleVisitor::visit(ElementUnion el)] " + el.toString());
		System.out.println("");

	}

	@Override
	public void visit(ElementData el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementData el)] ");
	}

	@Override
	public void visit(ElementFilter el) {
		System.out.println("[SQRemoveTripleVisitor::visit(ElementFilter el)] " + el.toString());
		System.out.println("");
		// el.getExpr()

		// ...get the variables of the FILTER expression
		Expr filterExp = el.getExpr();// .getVarsMentioned().contains(el);
		Set<Var> expVars = filterExp.getVarsMentioned();

		// ...get the variables of the triple pattern that we want to delete
		Set<Var> tpVars = new HashSet();
		Node subj = this.tp.getSubject();
		if (subj.isVariable()) {
			tpVars.add((Var) subj);
		}
		Node pred = this.tp.getPredicate();
		if (pred.isVariable()) {
			tpVars.add((Var) pred);
		}
		Node obj = this.tp.getObject();
		if (obj.isVariable()) {
			tpVars.add((Var) obj);
		}

		// ...check whether the FILTER expression contains any of the triple pattern
		// variable
		for (Var var : expVars) {
			// ..if it does then we have to delete the entire FILTER expression
			if (tpVars.contains(var)) {
				System.out.println("[SQRemoveTripleVisitor::visit(ElementFilter el)] YESssssssssssssssss ");
				// filterExp.

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

	@Override
	public void visit(ElementTriplesBlock el) {
		// System.out.println("[SQInstantiationVisitor::visit(ElementTriplesBlock el)]
		// ");
	}

}
