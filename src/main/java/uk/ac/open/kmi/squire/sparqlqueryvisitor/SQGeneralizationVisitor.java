/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.ListIterator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
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

/**
 *
 * @author callocca
 */
public class SQGeneralizationVisitor extends ElementVisitorBase {

    private Node node;
    private Var varTemplate;

    public SQGeneralizationVisitor(Node n, Var varTemplate) {
        //            System.out.println("The triple ==> " + tp.toString());
        if (n == null || varTemplate == null) {
            throw new IllegalStateException("[SQGeneralizationVisitor]The Node or the varTemplate is null!!");
        }
        this.node = n;
        this.varTemplate = varTemplate;
    }

    @Override
    public void visit(ElementPathBlock el) {
//        System.out.println("[SQGeneralizationVisitor::visit(ElementPathBlock el)] ");
        if (el == null) {
            throw new IllegalStateException("[SQGeneralizationVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
        }
        ListIterator<TriplePath> it = el.getPattern().iterator();
        while (it.hasNext()) {
            final TriplePath tp = it.next();
            //System.out.println("The triple ==> " + tp.toString());

            Node oldSubject = tp.getSubject();
            final Node newSubject;
            // SUBJECT
            if (!oldSubject.isVariable()) {
                if (oldSubject.isURI() && node.isURI()) {
                    if (oldSubject.getURI().equals(node.getURI())) {
                        newSubject = Var.alloc(varTemplate);
                    } else {
                        newSubject = oldSubject;
                    }
                } else {
                    if (oldSubject.isLiteral() && node.isLiteral()) {
                        if (oldSubject.getLiteral().toString().equals(node.getLiteral().toString())) {
                            newSubject = Var.alloc(varTemplate);
                        } else {
                            newSubject = oldSubject;
                        }
                    } else {
                        newSubject = oldSubject;
                    }
                }
            } else {
                newSubject = oldSubject;
            }

            Node oldPredicate = tp.getPredicate();
            final Node newPredicate;
            // PREDICATE
            if (!oldPredicate.isVariable()) {
                if (oldPredicate.isURI() && node.isURI()) {
                    if (oldPredicate.getURI().equals(node.getURI())) {
                        newPredicate = Var.alloc(varTemplate);
                    } else {
                        newPredicate = oldPredicate;
                    }
                } else {
                    newPredicate = oldPredicate;
                }

            } else {
                newPredicate = oldPredicate;
            }

            // OBJECT
            Node oldObject = tp.getObject();
            final Node newObject;
            if (!oldObject.isVariable()) {
                if (oldObject.isURI() && node.isURI()) {
                    if (oldObject.getURI().equals(node.getURI())) {
                        newObject = Var.alloc(varTemplate);
                    } else {
                        newObject = oldObject;
                    }
                } else {
                    if (oldObject.isLiteral() && node.isLiteral()) {
                        if (oldObject.getLiteral().toString().equals(node.getLiteral().toString())) {
                            newObject = Var.alloc(varTemplate);
                        } else {
                            newObject = oldObject;
                        }
                    } else {
                        newObject = oldObject;
                    }
                }
            } else {
                newObject = oldObject;
            }
            TriplePath newTriplePattern = new TriplePath(new Triple(newSubject, newPredicate, newObject));
            it.set(newTriplePattern);
        }
    }

    @Override
    public void visit(ElementAssign el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementAssign el))] ");

    }

    @Override
    public void visit(ElementBind el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementBind el)] ");

    }

    @Override
    public void visit(ElementSubQuery el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementSubQuery el)] ");
    }

    @Override
    public void visit(ElementService el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementService el)] ");
    }

    @Override
    public void visit(ElementMinus el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementMinus el)] ");
    }

    @Override
    public void visit(ElementNotExists el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementNotExists el)] ");
    }

    @Override
    public void visit(ElementExists el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementExists el)] ");
    }

    @Override
    public void visit(ElementNamedGraph el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementNamedGraph el)] ");
    }

    @Override
    public void visit(ElementGroup el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementGroup el)] ");
    }

    @Override
    public void visit(ElementOptional el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementOptional el)] ");
    }

    @Override
    public void visit(ElementDataset el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementDataset el)] ");
    }

    @Override
    public void visit(ElementUnion el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementUnion el)] ");
    }

    @Override
    public void visit(ElementData el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementData el)] ");
    }

    @Override
    public void visit(ElementFilter el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementFilter el)] ");
    }

    @Override
    public void visit(ElementTriplesBlock el) {
    //    System.out.println("[SQGeneralizationVisitor::visit(ElementTriplesBlock el)] ");
    }

}
