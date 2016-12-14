/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.util.Set;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.ElementWalker;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionVisitor;

/**
 *
 * @author carloallocca
 *
 * This class compute the similarity in the context of Remove operation between
 * two SPARQL queries.
 *
 */
public class QueryGPESim {

    public QueryGPESim() {
        super();

    }

    public float computeQueryPatternsSim(Query qO, Query qR) {
        
        SQGraphPatternExpressionVisitor gpeVisitorO = new SQGraphPatternExpressionVisitor();
        //...get the GPE of  qOri
        ElementWalker.walk(qO.getQueryPattern(), gpeVisitorO);
        Set qOGPE = gpeVisitorO.getQueryGPE();
//        System.out.println("[QueryGPESim::computeQueryPatternsSim] gpeVisitor.getQueryGPE()  " + qOGPE.toString());
//        System.out.println("[QueryGPESim::computeQueryPatternsSim] qOGPE.size()  " + qOGPE.size());
        //...get the GPE of  qRec
                
        SQGraphPatternExpressionVisitor gpeVisitorR = new SQGraphPatternExpressionVisitor();
        ElementWalker.walk(qR.getQueryPattern(), gpeVisitorR);
        Set qRGPE = gpeVisitorR.getQueryGPE();
//        System.out.println("[QueryGPESim::computeQueryPatternsSim] gpeVisitor.getQueryGPE()  " + qRGPE.toString());
//        System.out.println("[QueryGPESim::computeQueryPatternsSim] qRGPE.size()  " + qRGPE.size());
        if (qRGPE.size() > 0 && qOGPE.size() > 0) {
            return (float) (((1.0) * qRGPE.size()) / ((1.0) * qOGPE.size()));
        }
        return (float) 0.0;
    }
    
}
