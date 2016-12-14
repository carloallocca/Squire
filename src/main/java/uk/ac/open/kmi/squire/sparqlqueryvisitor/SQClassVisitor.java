/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
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
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class SQClassVisitor extends ElementVisitorBase {

    private IRDFDataset d;
    private ArrayList<String> datasetClassSet;

    private Set<String> queryClassSet = new HashSet<String>();

    public SQClassVisitor(IRDFDataset d1) {
        if (d1 == null) {
            throw new IllegalStateException("[SQClassVisitor]The IRDFDataset d1 is null!!");
        }
        this.d = d1;
        this.datasetClassSet = d1.getClassSet();
    }

    @Override
    public void visit(ElementPathBlock el) {
//        System.out.println("[SQInstantiationVisitor::visit(ElementPathBlock el)] ");
        if (el == null) {
            throw new IllegalStateException("[SQClassVisitor::visit(ElementPathBlock el)] The ElementPathBlock is null!!");
        }
        ListIterator<TriplePath> it = el.getPattern().iterator();
        while (it.hasNext()) {
            final TriplePath tp = it.next();
            //System.out.println("The triple ==> " + tp.toString());
            Node subject = tp.getSubject();
            // SUBJECT            
            if (subject.isURI()) {
                if (this.datasetClassSet.contains(subject.getURI())) {
                    this.queryClassSet.add(subject.getURI());
                }
            }
            // OBJECT
            Node object = tp.getObject();
            if (object.isURI()) {
                if (this.datasetClassSet.contains(object.getURI())) {
                    this.queryClassSet.add(object.getURI());
                }
            }
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

    public Set<String> getQueryClassSet() {
        return queryClassSet;
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
