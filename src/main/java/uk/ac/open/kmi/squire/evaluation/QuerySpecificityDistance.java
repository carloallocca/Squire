/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementWalker;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQClassVisitor;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;

/**
 *
 * @author carloallocca
 */
public class QuerySpecificityDistance {

    public QuerySpecificityDistance() {
        super();
    }

    public float computeQuerySpecificityDistanceWRTQueryVariable(Query qO, Query qR) {
        float sim;

//        System.out.println("[QuerySpecificityDistance::computeQuerySpecificityDistanceWRTQueryVariable] Query qO "+qO.toString());
        List<String> qOvarList = computeQueryVariableSet(qO);
//        System.out.println("[QuerySpecificityDistance::computeQuerySpecificityDistanceWRTQueryVariable] qOvarList size "+qOvarList.size());

//        System.out.println("[QuerySpecificityDistance::computeQuerySpecificityDistanceWRTQueryVariable] Query qR "+qR.toString());
        List<String> qRvarList = computeQueryVariableSet(qR);
//        System.out.println("[QuerySpecificityDistance::computeQuerySpecificityDistanceWRTQueryVariable] qRvarList size "+qRvarList.size());

        sim = computeQSsim(qOvarList, qRvarList);
        return sim;
    }

    private List<String> computeQueryVariableSet(Query qO) {
        SQVariableVisitor v = new SQVariableVisitor();
        //... This will walk through all parts of the query
        ElementWalker.walk(qO.getQueryPattern(), v);
//        System.out.println("[QuerySpecificityDistance::computeQueryVariableSet] v.getQueryClassSet()  " + v.getQueryClassSet().toString());
        return v.getQueryVariableSet();
    }

    private float computeQSsim(List<String> qOvarList, List<String> qRvarList) {
        float sim = 0;//;
        if (qOvarList.size() > 0 && qRvarList.size() > 0) {
            sim = (float) (1.0 * (((1.0 * qOvarList.size()) / (1.0 * qRvarList.size()))));
//            System.out.println("[QuerySpecificityDistance::computeQSsim] sim "+sim);
            return sim;
        }
        return sim;
    }

    public float computeQuerySpecificityDistanceWRTQueryTriplePatter(Query originalQuery, Query qR) {
        float sim = 0;//;
        
        QueryGPESim simQuery= new QueryGPESim();
        sim=simQuery.computeQueryPatternsSim(originalQuery, qR);
        return sim;

    }

}
