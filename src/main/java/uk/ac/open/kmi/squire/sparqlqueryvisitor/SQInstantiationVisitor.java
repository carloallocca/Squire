/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.Iterator;
import java.util.ListIterator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
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
 * @author callocca
 */
public class SQInstantiationVisitor extends ElementVisitorBase {

    private Node node;
    private Var varTemplate;

    public SQInstantiationVisitor(Var varTemplate, Node n) {
        //            System.out.println("The triple ==> " + tp.toString());
        if (n == null || varTemplate == null) {
            throw new IllegalStateException("[SQInstantiationVisitor]The Node or the varTemplate is null!!");
        }
        this.node = n;
        this.varTemplate = varTemplate;
    }

    @Override
    public void visit(ElementPathBlock el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementPathBlock el)] ");
        if (el == null) {
            throw new IllegalStateException("[SQInstantiationVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
        }
        ListIterator<TriplePath> it = el.getPattern().iterator();
        while (it.hasNext()) {
            final TriplePath tp = it.next();
            //System.out.println("The triple ==> " + tp.toString());
            Node oldSubject = tp.getSubject();
            final Node newSubject;

            // SUBJECT            
            if (oldSubject.isVariable()) {
                if (oldSubject.getName().equals(varTemplate.getName())) {
                    if (node.isURI()) {
                        newSubject = NodeFactory.createURI(node.getURI());
                    } else if (node.isLiteral()) {
                        newSubject = NodeFactory.createLiteral(node.getLiteral());
                    }else{
                        newSubject = oldSubject;
                    }
                } else {
                    newSubject = oldSubject;
                }
            } else {
                newSubject = oldSubject;
            }

            // PREDICATE
            Node oldPredicate = tp.getPredicate();
            final Node newPredicate;
            if (oldPredicate.isVariable()) {
                if (oldPredicate.getName().equals(varTemplate.getName())) {
                    if (node.isURI()) {
                        newPredicate = NodeFactory.createURI(node.getURI());
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

            if (oldObject.isVariable()) {
                if (oldObject.getName().equals(varTemplate.getName())) {
                    if (node.isURI()) {
                        newObject = NodeFactory.createURI(node.getURI());
                    } else if (node.isLiteral()) {
                        newObject = NodeFactory.createLiteral(node.getLiteral());
                    }else {
                        newObject = oldObject;
                    }
                } else {
                    newObject = oldObject;
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
//        System.out.println("[SQInstantiationVisitor::visit(ElementAssign el))] ");

    }

    @Override
    public void visit(ElementBind el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementBind el)] ");

    }

    @Override
    public void visit(ElementSubQuery el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementSubQuery el)] ");
    }

    @Override
    public void visit(ElementService el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementService el)] ");
    }

    @Override
    public void visit(ElementMinus el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementMinus el)] ");
    }

    @Override
    public void visit(ElementNotExists el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementNotExists el)] ");
    }

    @Override
    public void visit(ElementExists el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementExists el)] ");
    }

    @Override
    public void visit(ElementNamedGraph el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementNamedGraph el)] ");
    }

    @Override
    public void visit(ElementGroup el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementGroup el)] ");
    }

    @Override
    public void visit(ElementOptional el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementOptional el)] ");
    }

    @Override
    public void visit(ElementDataset el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementDataset el)] ");
    }

    @Override
    public void visit(ElementUnion el) {
    }

    @Override
    public void visit(ElementData el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementData el)] ");
    }

    @Override
    public void visit(ElementFilter el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementFilter el)] ");
    }

    @Override
    public void visit(ElementTriplesBlock el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementTriplesBlock el)] ");
    }

}
