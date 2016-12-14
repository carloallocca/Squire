/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQInstantiationVisitor;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

/**
 *
 * @author callocca
 */
public class SPARQLQueryInstantiation {

    public SPARQLQueryInstantiation() {
        super();
    }

    public Query instantiateFromVarTemplateToNode(Query q, Var varTemplate,Node n) {
        if (q == null || n == null || varTemplate == null) {
            throw new IllegalArgumentException("[SPARQLQueryGeneralization::generalize()]The Query or the Node or the Var is null!!");
        }
//        Query newQuery=QueryFactory.create(q.toString());
        SQInstantiationVisitor instVisitor = new SQInstantiationVisitor(varTemplate,n);
        ElementWalker.walk(q.getQueryPattern(), instVisitor);
        //this.generalizedQuery=this.originalQuery;
        return q;
    }

}
