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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQClassVisitor;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;

/**
 *
 * @author carloallocca
 */
public class QuerySpecificityDistance {
    
    
        private final Logger log = LoggerFactory.getLogger(getClass());

    public QuerySpecificityDistance() {
        super();
    }

    public float computeQuerySpecificityDistanceWRTQueryVariable(Query qO, Query qR) {
        float sim;

        List<String> qOvarList = computeQueryVariableSet(qO);
//        log.info("qOvarList " +qOvarList.toString());
        List<String> qRvarList = computeQueryVariableSet(qR);
//        log.info("qRvarList " +qRvarList.toString());
        sim = computeQSsim(qOvarList, qRvarList);
        return sim;
    }

    private List<String> computeQueryVariableSet(Query qO) {
        SQVariableVisitor v = new SQVariableVisitor();
        //... This will walk through all parts of the query
        ElementWalker.walk(qO.getQueryPattern(), v);
        return v.getQueryVariableSet();
    }

    private float computeQSsim(List<String> qOvarList, List<String> qRvarList) {
        float sim = 0;
        if (qOvarList.size() > 0 && qRvarList.size() > 0) {
            sim = (float) (1.0 * (((1.0 * qRvarList.size()) / (1.0 * qOvarList.size()))));
            //log.info("computeQSsim " +sim);
            return sim;
        }
        return sim;
    }

    public float computeQuerySpecificityDistanceWRTQueryTriplePatter(Query originalQuery, Query qR) {
        float sim = 0;

        QueryGPESim simQuery= new QueryGPESim();
        sim=simQuery.computeQueryPatternsSim(originalQuery, qR);
        return sim;

    }

}
