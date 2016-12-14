/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.processor;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.sparql.core.Var;

/**
 *
 * @author carloallocca
 */
public class TripleRemovalOp implements IOperation {

    private final Query q; 
    private final TriplePattern tp;    
    
    public TripleRemovalOp(Query q, TriplePattern tp){
        this.q=q;
        this.tp=tp;
    }
   

    @Override
    public  Object apply() {
        System.out.println(" Removing the TriplePattern " +this.tp+  " from the query Q = " +this.q.toString());
        return null; // return this.q modified
    }

}
    

