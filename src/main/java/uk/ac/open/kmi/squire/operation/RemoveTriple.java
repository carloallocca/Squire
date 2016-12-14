/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import java.util.Iterator;
import java.util.Set;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.RemoveOpTransform;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionVisitor;

/**
 *
 * @author carloallocca
 */
public class RemoveTriple {
    
    public RemoveTriple(){
        super();
    }
    
    public Query removeTP(Query q, Triple tp) {
        
        
        SQGraphPatternExpressionVisitor gpeVisitorO = new SQGraphPatternExpressionVisitor();
        //...get the GPE of  qOri
        ElementWalker.walk(q.getQueryPattern(), gpeVisitorO);
        Set qGPE = gpeVisitorO.getQueryGPE();
        
        if(qGPE.size()>1){
        
        RemoveOpTransform rOpTransform= new RemoveOpTransform(q,tp);
        Query queryWithoutTriplePattern = QueryTransformOps.transform(q, rOpTransform) ;         
//        if(queryWithoutTriplePattern.getGroupBy()!=null){
//            //Iterator orderBy=queryWithoutTriplePattern.getGroupBy().getVars().iterator();
//            //            queryWithoutTriplePattern
//        }
        //queryWithoutTriplePattern.getAggregators()
        
        return queryWithoutTriplePattern;

            
        }
        
        return q;
        
    }

    
}
