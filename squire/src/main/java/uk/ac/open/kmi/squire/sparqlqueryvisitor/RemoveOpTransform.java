/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
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
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

/**
 *
 * @author carloallocca
 *
 *         This has been used.
 */
public class RemoveOpTransform implements ElementTransform {

	private Query query;
	private Triple triple;

	private ElementPathBlock epb;
	// private HashMap<Element, ElementGroup> queryElemMap = new HasMap<Element,
	// ElementGroup>();
	private HashMap<Element, ElementGroup> queryElemMap = new HashMap<Element, ElementGroup>();

	public RemoveOpTransform(Query q, Triple tp) {
		this.query = q;
		this.triple = tp;
		// this.queryElemMap = buildQueryElementMap(q);
	}

	@Override
	public Element transform(ElementTriplesBlock arg0) {
		// System.out.println("[RemoveOpTransform::transform(ElementTriplesBlock arg0)]
		// " + arg0.toString());
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementGroup arg0, List<Element> arg1) {
		// ElementGroup arg0New = new ElementGroup();
		// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
		// List<Element> arg0) arg0 " + arg0.toString());
		// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
		// List<Element> arg1) arg1 " + arg1.toString());

		List<Element> elemList = arg0.getElements();
		Iterator<Element> itr = elemList.iterator();
		// Iterator<Element> itr = arg1.iterator();
		while (itr.hasNext()) {
			Element elem = itr.next();
			// I should go one by one the and examinate all the possible cases. For example:

			// GRAPH ElementNamedGraph
			if (elem instanceof ElementNamedGraph) {
				boolean isElementNamedGraphEmpty = isElementNamedGraphEmpty1((ElementNamedGraph) elem);
				if (isElementNamedGraphEmpty) {
					itr.remove();
				}
			}

			// OPTION
			if (elem instanceof ElementOptional) {
				boolean isElementOptionalEmpty = isElementOptionalEmpty1((ElementOptional) elem);
				if (isElementOptionalEmpty) {
					itr.remove();
					// ElementGroup el2 = new ElementGroup();
					// el2.getElements().addAll(arg1);
					// return el2;
				}

				// return (ElementOptional) elem;
			}

			// ELEMENTGROUP
			if (elem instanceof ElementGroup) {
				// boolean isElementGroupEmpty = isElementGroupEmpty((ElementGroup) elem);
				boolean isElementGroupEmpty = isElementGroupEmpty1((ElementGroup) elem);
				if (isElementGroupEmpty) {
					System.out.println("");
					// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
					// List<Element> arg1)] ElementGroup " + ((ElementGroup) elem).toString() + "IS
					// GOING TO BE REMOVED!!!!");
					itr.remove();
					// ElementGroup el2 = new ElementGroup();
					// el2.getElements().addAll(arg1);
					// return el2;
				}
			}

			// FILTER
			if (elem instanceof ElementFilter) {
				// ... check if this filter is the one that we should remove
				// ...get the variables of the triple pattern that we want to delete
				Set<Var> tpVars = new HashSet();
				Node subj = this.triple.getSubject();
				if (subj.isVariable()) {
					tpVars.add((Var) subj);
				}
				Node pred = this.triple.getPredicate();
				if (pred.isVariable()) {
					tpVars.add((Var) pred);
				}
				Node obj = this.triple.getObject();
				if (obj.isVariable()) {
					tpVars.add((Var) obj);
				}
				// ...get the variables of the FILTER expression
				Set<Var> expVars = ((ElementFilter) elem).getExpr().getVarsMentioned();
				// ...check whether the FILTER expression contains any of the triple pattern
				// variable
				for (Var var : expVars) {
					// ...if it does then we have to delete the entire FILTER expression
					if (tpVars.contains(var)) {
						// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
						// List<Element> arg1)] THE " + ((ElementFilter) elem).toString() + "IS GOING TO
						// BE REMOVED!!!");
						// Expr e = new NodeValueBoolean(true);
						// ElementFilter newFilter = new ElementFilter(e);
						// return newFilter;
						// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
						// List<Element> arg1)] arg1 BEFORE REMOVING " + arg1.toString());
						// itr.remove();
						if (!existAnotherTripleWithThatVariable(var, arg1)) {
							itr.remove();
							// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)] arg1 AFTER
							// REMOVING " + arg1.toString());
							break;
						}

					}
				}
			}

			// UNNION
			if (elem instanceof ElementUnion) {
				// boolean isUnionBothSidesEmpty = isUnionBothSidesEmpty1((ElementUnion) elem);
				boolean isUnionBothSidesEmpty = isUnionBothSidesEmpty2((ElementUnion) elem);
				// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)]
				// ElementUnion isUnionBothSidesEmpty " + isUnionBothSidesEmpty);
				if (isUnionBothSidesEmpty) {
					itr.remove();
					// ElementGroup el2 = new ElementGroup();
					// el2.getElements().addAll(arg1);
					// return el2;
					// return this.transform(arg0, arg1);
				}

				// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
				// List<Element> arg1)] ElementUnion ");
				// return (ElementUnion) elem;
			}
		} // End of While

		// With this code I am saying the following: make effective all the
		// modifications that
		// have been made so far, if any.
		// System.out.println("arg0 " + arg0.getElements());
		// System.out.println("arg1" + arg1);
		return arg0;
		// if (arg0.getElements() == arg1) {
		// if (arg0.getElements().equals(arg1)) {
		// return arg0;
		// } else {
		// ElementGroup el2 = new ElementGroup();
		// el2.getElements().addAll(arg1);
		// System.out.println("el2 "+el2);
		// return el2;
		// }

		// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0)] arg1 "
		// + arg1.toString());
		// System.out.println("");
		// return arg0New;
		// return arg0;
	}

	// @Override
	// public Element transform(ElementGroup arg0, List<Element> arg1) {
	// //ElementGroup arg0New = new ElementGroup();
	// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
	// List<Element> arg0) arg0 " + arg0.toString());
	// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0,
	// List<Element> arg1) arg1 " + arg1.toString());
	//
	// //List<Element> elemList = arg0.getElements();
	// //Iterator<Element> itr = elemList.iterator();
	// Iterator<Element> itr = arg1.iterator();
	// while (itr.hasNext()) {
	// Element elem = itr.next();
	// if (elem instanceof ElementOptional) {
	// boolean isElementOptionalEmpty = isElementOptionalEmpty((ElementOptional)
	// elem);
	// if (isElementOptionalEmpty) {
	// System.out.println("");
	// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)]
	// ElementOptional 777777777 " + ((ElementOptional) elem).toString() + " IS
	// GOING TO BE REMOVED AS IT IS EMPTY!!!!");
	// itr.remove();
	// }
	// } else if (elem instanceof ElementGroup) {
	// boolean isElementGroupEmpty = isElementGroupEmpty((ElementGroup) elem);
	// if (isElementGroupEmpty) {
	// System.out.println("");
	// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)]
	// ElementGroup " + ((ElementGroup) elem).toString() + "IS GOING TO BE
	// REMOVED!!!!");
	// itr.remove();
	// ElementGroup el2 = new ElementGroup();
	// el2.getElements().addAll(arg1);
	// return el2;
	// }
	// } else if (elem instanceof ElementFilter) {
	// //... check if this filter is the one that we should remove
	// //...get the variables of the triple pattern that we want to delete
	// Set<Var> tpVars = new HashSet();
	// Node subj = this.triple.getSubject();
	// if (subj.isVariable()) {
	// tpVars.add((Var) subj);
	// }
	// Node pred = this.triple.getPredicate();
	// if (pred.isVariable()) {
	// tpVars.add((Var) pred);
	// }
	// Node obj = this.triple.getObject();
	// if (obj.isVariable()) {
	// tpVars.add((Var) obj);
	// }
	// //...get the variables of the FILTER expression
	// Set<Var> expVars = ((ElementFilter) elem).getExpr().getVarsMentioned();
	// //...check whether the FILTER expression contains any of the triple pattern
	// variable
	// for (Var var : expVars) {
	// //...if it does then we have to delete the entire FILTER expression
	// if (tpVars.contains(var)) {
	// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)] THE " +
	// ((ElementFilter) elem).toString() + "IS GOING TO BE REMOVED!!!");
	// //Expr e = new NodeValueBoolean(true);
	// //ElementFilter newFilter = new ElementFilter(e);
	// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)] arg1 BEFORE
	// REMOVING " + arg1.toString());
	//
	// itr.remove();
	//
	// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)] arg1 AFTER
	// REMOVING " + arg1.toString());
	//
	// ElementGroup el2 = new ElementGroup();
	// el2.getElements().addAll(arg1);
	// return el2;
	//
	// //return newFilter;
	//// return this.transform(arg0, arg1);
	// }
	// }
	// } else if (elem instanceof ElementUnion) {
	//// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)]
	// ElementUnion 2222222 " + ((ElementUnion) elem).toString());
	// boolean isUnionBothSidesEmpty = isUnionBothSidesEmpty1((ElementUnion) elem);
	// System.out.println("[RemoveOpTransform::visit(ElementGroup arg0)]
	// ElementUnion isUnionBothSidesEmpty " + isUnionBothSidesEmpty);
	// if (isUnionBothSidesEmpty) {
	// itr.remove();
	// ElementGroup el2 = new ElementGroup();
	// el2.getElements().addAll(arg1);
	// return el2;
	// //return this.transform(arg0, arg1);
	// } else {
	// if (arg0.getElements() == arg1) {
	// return arg0;
	// }
	// ElementGroup el2 = new ElementGroup();
	// el2.getElements().addAll(arg1);
	// return el2;
	//
	// }
	//
	// }
	// }// End of While
	//
	// if (arg0.getElements() == arg1) {
	// return arg0;
	// } else {
	// ElementGroup el2 = new ElementGroup();
	// el2.getElements().addAll(arg1);
	// return el2;
	// }
	//
	//// System.out.println("[RemoveOpTransform::transform(ElementGroup arg0)] arg1
	// " + arg1.toString());
	//// System.out.println("");
	// //return arg0New;
	// //return arg0;
	// }
	@Override
	public Element transform(ElementPathBlock eltPB) {
		if (eltPB.isEmpty()) {
			// System.out.println("[RemoveOpTransform::transform(ElementPathBlock arg0)]
			// ElementPathBlock IS EMPTY:: " + eltPB.toString());
			return eltPB;
		}
		// System.out.println("[RemoveOpTransform::transform(ElementPathBlock arg0)]
		// ElementPathBlock:: " + eltPB.toString());
		Iterator<TriplePath> l = eltPB.patternElts();
		while (l.hasNext()) {
			TriplePath tp = l.next();
			if (tp.asTriple().matches(this.triple)) {
				l.remove();
				// System.out.println("[RemoveOpTransform::transform(ElementPathBlock arg0)]
				// ElementPathBlock:: " + tp.toString() + " TRIPLE JUST REMOVED!!!");
				// //System.out.println("[RemoveOpTransform::transform(ElementPathBlock arg0)]
				// TRIPLE JUST REMOVED!!! ");
				// System.out.println("");
				return this.transform(eltPB);// eltPB;
			}
		}
		return eltPB;
	}

	@Override
	public Element transform(ElementFilter arg0, Expr arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementFilter arg0)] arg0 "
		// + arg0.toString());
		// System.out.println("[RemoveOpTransform::transform(ElementFilter arg1)] arg1 "
		// + arg1.toString());
		//
		// //...get the variables of the triple pattern that we want to delete
		// Set<Var> tpVars = new HashSet();
		// Node subj = this.triple.getSubject();
		// if (subj.isVariable()) {
		// tpVars.add((Var) subj);
		// }
		// Node pred = this.triple.getPredicate();
		// if (pred.isVariable()) {
		// tpVars.add((Var) pred);
		// }
		// Node obj = this.triple.getObject();
		// if (obj.isVariable()) {
		// tpVars.add((Var) obj);
		// }
		//
		// //...get the variables of the FILTER expression
		// Set<Var> expVars = arg1.getVarsMentioned();
		//
		// //...check whether the FILTER expression contains any of the triple pattern
		// variable
		// for (Var var : expVars) {
		// //..if it does then we have to delete the entire FILTER expression
		// if (tpVars.contains(var)) {
		// System.out.println("[SQRemoveTripleVisitor::visit(ElementFilter arg0)] FILTER
		// TO BE REMOVED!!!HOW? ");
		// Expr e = new NodeValueBoolean(true);
		// ElementFilter newFilter = new ElementFilter(e);
		// return newFilter;
		// }
		// }c
		//
		return arg0;
	}

	@Override
	public Element transform(ElementAssign arg0, Var arg1, Expr arg2) {
		// System.out.println("[RemoveOpTransform::transform(ElementAssign arg0)] " +
		// arg0.toString());
		// System.out.println("");

		return arg0;
	}

	@Override
	public Element transform(ElementBind arg0, Var arg1, Expr arg2) {
		// System.out.println("[RemoveOpTransform::transform(ElementBind arg0)] " +
		// arg0.toString());
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementData arg0) {

		// System.out.println("[RemoveOpTransform::transform(ElementData arg0)] " +
		// arg0.toString());
		// System.out.println("");
		return arg0;

	}

	@Override
	public Element transform(ElementDataset arg0, Element arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementDataset arg0)] " +
		// arg0.toString());
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementUnion arg0, List<Element> arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementUnion arg0)] arg0
		// yyyyyy " + arg0.toString());
		// System.out.println("[RemoveOpTransform::transform(ElementUnion arg0)] arg1
		// yyyyyy " + arg1.toString());
		// System.out.println("");
		for (Element e : arg1) {
			if (e instanceof ElementGroup) {
				this.transform((ElementGroup) e, ((ElementGroup) e).getElements());
			}
		}

		// With this code I am saying the following: make effective all the
		// modifications that
		// have been made so far, if any.
		// if (arg0.getElements() == arg1) {
		// return arg0;
		// }
		// ElementGroup el2 = new ElementGroup();
		// el2.getElements().addAll(arg1);
		// return el2;
		//// List<Element> elemList = arg1.getElements();
		// Iterator<Element> itr = arg1.iterator();
		// while (itr.hasNext()) {
		// Element elem = itr.next();
		//// if (elem instanceof ElementGroup) {
		//// List<Element> elemList = ((ElementGroup) elem).getElements();
		////
		//// // System.out.println("[RemoveOpTransform::visit(ElementUnion arg0,
		// List<Element> arg1)] ONE SIDE OF THE UNION "+((ElementGroup)
		// elem).getElements().toString());
		//// System.out.println("[RemoveOpTransform::visit(ElementUnion arg0,
		// List<Element> arg1)] ONE SIDE OF THE UNION " + elemList.get(0).toString());
		//// if (((ElementGroup) elem).getElements().isEmpty()) {
		//// System.out.println("[RemoveOpTransform::visit(ElementUnion arg0,
		// List<Element> arg1)] ONE SIDE OF THE UNION IS EMPTY !!!");
		//// }
		//// }
		// }
		return arg0;
	}

	@Override
	public Element transform(ElementOptional arg0, Element arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementOptional arg0)]
		// 55555555555555555 " + arg0.toString());
		// System.out.println("");
		// if (arg1 instanceof ElementGroup) {
		// this.transform((ElementGroup) arg1, ((ElementGroup) arg1).getElements());
		// }
		return arg0;
	}

	@Override
	public Element transform(ElementNamedGraph arg0, Node arg1, Element arg2) {
		// System.out.println("[RemoveOpTransform::transform(ElementNamedGraph arg0)]
		// ElementNamedGraph arg0 " + arg0.toString());
		// System.out.println("[RemoveOpTransform::transform(ElementNamedGraph arg0)]
		// Node arg1 " + arg1.toString());
		// System.out.println("[RemoveOpTransform::transform(ElementNamedGraph
		// arg0)]Element arg2 " + arg2.toString());
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementExists arg0, Element arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementExists arg0)] ");
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementNotExists arg0, Element arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementNotExists arg0)] ");
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementMinus arg0, Element arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementMinus arg0)] ");
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementService arg0, Node arg1, Element arg2) {
		// System.out.println("[RemoveOpTransform::transform(ElementService arg0)] ");
		// System.out.println("");
		return arg0;
	}

	@Override
	public Element transform(ElementSubQuery arg0, Query arg1) {
		// System.out.println("[RemoveOpTransform::transform(ElementSubQuery arg0)] ");
		// System.out.println("");
		return arg0;
	}

	// private boolean isUnionBothSidesEmpty(ElementUnion elementUnion) {
	//
	// List<Element> elemListUnion = elementUnion.getElements();//.getElements();
	// boolean leftSide = false;
	// boolean rightSide = false;
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty]elemListUnion.size()
	// " + elemListUnion.size());
	//
	// if (elemListUnion.size() == 2) {
	// for (int i = 0; i < elemListUnion.size(); ++i) {
	// if (i == 0) {
	// if ((((ElementGroup) elemListUnion.get(i)).getElements().size() == 1))
	// {//getElements().size() == 1)) {
	// //System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " +i);
	// List<Element> elList = ((ElementGroup) elemListUnion.get(i)).getElements();
	// if (elList.size() == 1) {
	// Element el = (Element) elList.get(0);
	// if (el.toString().contains("# Empty BGP")) {
	// leftSide = true;
	// }
	// }
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " + i);
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " + leftSide);
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " +
	// elList.toString());
	// }
	// }
	// if (i == 1) {
	// if ((((ElementGroup) elemListUnion.get(i)).getElements().size() == 1))
	// {//getElements().size() == 1)) {
	// List<Element> elList = ((ElementGroup) elemListUnion.get(i)).getElements();
	// if (elList.size() == 1) {
	// Element el = (Element) elList.get(0);
	// if (el.toString().contains("# Empty BGP")) {
	// rightSide = true;
	// }
	// }
	//
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " + i);
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " + leftSide);
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] " +
	// elList.toString());
	//
	// }
	// }
	// }
	// }
	//
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] BEFORE RETURN
	// leftSide " + leftSide);
	// System.out.println("[RemoveOpTransform::isUnionBothSidesEmpty] BEFORE RETURN
	// rightSide " + rightSide);
	//
	// return leftSide && rightSide;
	// }
	// private boolean isUnionBothSidesEmpty1(ElementUnion elementUnion) {
	// List<Element> elemListUnion = elementUnion.getElements();//.getElements();
	// if (elemListUnion.size() == 2) {
	// Element left = elemListUnion.get(0);
	// Element right = elemListUnion.get(1);
	// if (left instanceof ElementGroup && right instanceof ElementGroup) {
	// if ((((ElementGroup) left).getElements().size() == 1) && (((ElementGroup)
	// right).getElements().size() == 1)) {
	// Element elLeft = ((ElementGroup) left).getElements().get(0);
	// Element elRight = ((ElementGroup) right).getElements().get(0);
	// if ((elLeft instanceof ElementPathBlock) && (elRight instanceof
	// ElementPathBlock)) {
	// ElementPathBlock epbLeft = (ElementPathBlock) elLeft;
	// ElementPathBlock epbRight = (ElementPathBlock) elRight;
	// if (epbLeft.isEmpty() && epbRight.isEmpty()) {
	// return true;
	// }
	// }
	// }
	// }
	// }
	// return false;
	// }
	private boolean isUnionBothSidesEmpty2(ElementUnion elementUnion) {
		List<Element> elemListUnion = elementUnion.getElements();// .getElements();
		boolean leftEmptyGroup = false;
		boolean rightEmptyGroup = false;

		if (elemListUnion.size() == 2) {
			Element left = elemListUnion.get(0);
			Element right = elemListUnion.get(1);
			if (left instanceof ElementGroup && right instanceof ElementGroup) {
				// ...LEFT SIDE
				Iterator<Element> itr = ((ElementGroup) left).getElements().iterator();// g.iterator();
				// I need to check only if it is a triple map.
				while (itr.hasNext()) {
					Element elLeft = itr.next();
					if ((elLeft instanceof ElementPathBlock)) {
						ElementPathBlock tmp = (ElementPathBlock) elLeft;
						leftEmptyGroup = tmp.isEmpty();
					}
				}
				// ...RIGHT SIDE
				Iterator<Element> itr1 = ((ElementGroup) right).getElements().iterator();// g.iterator();
				// I need to check only if it is a triple map.
				while (itr1.hasNext()) {
					Element elRight = itr1.next();
					if ((elRight instanceof ElementPathBlock)) {
						ElementPathBlock tmp = (ElementPathBlock) elRight;
						rightEmptyGroup = tmp.isEmpty();
					}
				}
				return leftEmptyGroup && rightEmptyGroup;
			}
		}
		return leftEmptyGroup && rightEmptyGroup;
	}

	private boolean isElementGroupEmpty1(ElementGroup elementGroup) {

		boolean isElementGroupEmpty = false;
		// ...RIGHT SIDE
		Iterator<Element> itr1 = elementGroup.getElements().iterator();// g.iterator();
		// I need to check only if it is a triple map.
		while (itr1.hasNext()) {
			Element elRight = itr1.next();
			if ((elRight instanceof ElementPathBlock)) {
				ElementPathBlock tmp = (ElementPathBlock) elRight;
				isElementGroupEmpty = tmp.isEmpty();
			}
		}
		return isElementGroupEmpty;
	}

	private boolean isElementGroupEmpty(ElementGroup elementGroup) {
		List<Element> elList = ((ElementGroup) elementGroup).getElements();
		if (elList.size() == 1) {
			Element el = (Element) elList.get(0);
			////////
			if (el instanceof ElementPathBlock) {
				ElementPathBlock tmp = (ElementPathBlock) el;
				if (tmp.isEmpty()) {
					return true;
				}
			}
			////
			// if (el.toString().contains("# Empty BGP")) {
			// isEmpty = true;
			// }
		}
		return false;
	}

	private Element fixupGroupsOfOne(ElementGroup eg) {
		ElementTransform transform = new ElementTransformCleanGroupsOfOne();
		Element el2 = ElementTransformer.transform(eg, transform);
		return el2;
	}

	// private boolean isElementOptionalEmpty(ElementOptional elementOptional) {
	// Element elm = ((ElementOptional)
	// elementOptional).getOptionalElement();//.getElements();
	// if (elm instanceof ElementGroup) {
	// List<Element> elList = ((ElementGroup) elm).getElements();
	// if (elList.size() == 1) {
	// Element el = (Element) elList.get(0);
	// if (el instanceof ElementPathBlock) {
	// ElementPathBlock tmp = (ElementPathBlock) el;
	// if (tmp.isEmpty()) {
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }
	private boolean isElementOptionalEmpty1(ElementOptional elementOptional) {
		Element elm = ((ElementOptional) elementOptional).getOptionalElement();// .getElements();
		if (elm instanceof ElementGroup) {
			return isElementGroupEmpty1((ElementGroup) elm);
		}
		return false;
	}

	// private boolean isElementNamedGraphEmpty(ElementNamedGraph elementNamedGraph)
	// {
	// Element elm = (Element) elementNamedGraph.getElement();
	// if (elm instanceof ElementGroup) {
	// List<Element> elList = ((ElementGroup) elm).getElements();
	// if (elList.size() == 1) {
	// Element el = (Element) elList.get(0);
	// if (el instanceof ElementPathBlock) {
	// ElementPathBlock tmp = (ElementPathBlock) el;
	// if (tmp.isEmpty()) {
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }
	private boolean isElementNamedGraphEmpty1(ElementNamedGraph elementNamedGraph) {
		Element elm = (Element) elementNamedGraph.getElement();
		if (elm instanceof ElementGroup) {
			return isElementGroupEmpty1((ElementGroup) elm);
		}
		return false;
	}

	// private HashMap<Element, ElementGroup> buildQueryElementMap(Query q) {
	// HashMap<Element, ElementGroup> queryElemMapResult = new HashMap<Element,
	// ElementGroup>();
	//
	// SQGraphPatternExpressionVisitor gpeVisitorO = new
	// SQGraphPatternExpressionVisitor();
	// //...get the GPE of qOri
	// ElementWalker.walk(q.getQueryPattern(), gpeVisitorO);
	// Set qOGPE = gpeVisitorO.getQueryGPE();
	// System.out.println("[RemoveOpTransform::buildQueryElementMap]
	// gpeVisitor.getQueryGPE() " + qOGPE.toString());
	// System.out.println("[RemoveOpTransform::buildQueryElementMap] qOGPE.size() "
	// + qOGPE.size());
	//
	// //List<Element> elemList = arg0.getElements();
	// //q.setValuesDataBlock(variables, values);
	//// System.out.println(q.);
	// return queryElemMapResult;
	// }
	private boolean existAnotherTripleWithThatVariable(Var var, List<Element> arg1) {
		boolean result = false;

		Iterator<Element> itr = arg1.iterator();
		// I need to check only if it is a triple map.
		while (itr.hasNext()) {
			Element elem = itr.next();

			// ElementPathBlock
			if (elem instanceof ElementPathBlock) {
				ElementPathBlock triplePath = ((ElementPathBlock) elem);
				// System.out.println("[RemoveOpTransform::existAnotherTripleWithThatVariable]
				// 1111 " + triplePath.toString());
				Iterator<TriplePath> l = triplePath.patternElts();
				while (l.hasNext()) {
					TriplePath tp = l.next();
					if (!tp.asTriple().matches(this.triple)) {
						Node sub = tp.asTriple().getSubject();
						Node pred = tp.asTriple().getPredicate();
						Node obj = tp.asTriple().getObject();
						if (sub.isVariable()) {
							if (sub.getName().equals(var.getName())) {
								return true;
							}
						}
						if (pred.isVariable()) {
							if (pred.getName().equals(var.getName())) {
								return true;
							}
						}
						if (obj.isVariable()) {
							if (obj.getName().equals(var.getName())) {
								return true;
							}
						}
					}
				}
			}
		}
		return result;
	}

}
