/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.operation.SPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.treequerypatterns.QueryRecommendation;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendatorForm {

    private final String queryString;
    private final IRDFDataset rdfd1;
    private final IRDFDataset rdfd2;
    
    private final float resultTypeSimilarityDegree;
    private final float queryRootDistanceDegree;
    private final float resultSizeSimilarityDegree;
    private final float querySpecificityDistanceDegree;
    

    public QueryRecommendatorForm(String qString, IRDFDataset d1, IRDFDataset d2, 
            float resultTypeSimilarityDegree, 
            float queryRootDistanceDegree, 
            float resultSizeSimilarityDegree,
            float querySpecificityDistanceDegree) {
        
        this.queryString = qString;
        this.rdfd1 = d1;
        this.rdfd2 = d2;
        this.queryRootDistanceDegree = queryRootDistanceDegree;
        this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
        this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
        this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
    }

    public List<QueryStringScorePair> recommend() {
        
        List<QueryStringScorePair> newResult=new ArrayList<QueryStringScorePair>();

        List<QueryScorePair> sortedRecomQueryList=new ArrayList();
        
        Query query;
        //...checking if the input query is parsable....
        try {
            query = QueryFactory.create(queryString);
            System.out.println("");
            System.out.println("[QueryRecommendatorForm::recommend] THE SOURCE QUERY ");
            System.out.println("");
            System.out.println(query.toString());            
        } catch (org.apache.jena.query.QueryParseException ex) { //QueryParseException 
            throw new QueryParseException("[QueryRecommendatorForm::recommend] THE SOURCE QUERY is not parsable!!!", -1, -1);
        }
        //...checking if the input query is satisfiable w.r.t. D1 ...
        SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
        if (qs.isSatisfiable(query, rdfd1)) {
            
            try {
                QueryRecommendator qR = new QueryRecommendator(query, rdfd1, rdfd2, resultTypeSimilarityDegree,
                        queryRootDistanceDegree,
                        resultSizeSimilarityDegree,
                        querySpecificityDistanceDegree);
                qR.buildRecommendation();
                
                sortedRecomQueryList=qR.getSortedRecomQueryList();
                
                // reconvert old list by brute force
                for( QueryScorePair entry : sortedRecomQueryList )
                    newResult.add(new QueryStringScorePair(entry.getQuery().toString(),entry.getScore()));
                
                return newResult;
                //return sortedRecomQueryList;
            } catch (Exception ex) {
                Logger.getLogger(QueryRecommendatorForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("[SQueryRecommendationWorker::queryRecommendation5]The input query is not satisfiable w.r.t the input dataset... ");     
        }

        return Collections.emptyList();
    }
    
    //this is the old verison that returns QueryScorePair
    public List<QueryScorePair> recommendTest() {

        List<QueryScorePair> sortedRecomQueryList=new ArrayList();
        
        Query query;
        //...checking if the input query is parsable....
        try {
            query = QueryFactory.create(queryString);
            System.out.println("");
            System.out.println("[QueryRecommendatorForm::recommend] THE SOURCE QUERY ");
            System.out.println("");
            System.out.println(query.toString());            
        } catch (org.apache.jena.query.QueryParseException ex) { //QueryParseException 
            throw new QueryParseException("[QueryRecommendatorForm::recommend] THE SOURCE QUERY is not parsable!!!", -1, -1);
        }
        //...checking if the input query is satisfiable w.r.t. D1 ...
        SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
        if (qs.isSatisfiable(query, rdfd1)) {
            
            try {
                QueryRecommendator qR = new QueryRecommendator(query, rdfd1, rdfd2, resultTypeSimilarityDegree,
                        queryRootDistanceDegree,
                        resultSizeSimilarityDegree,
                        querySpecificityDistanceDegree);
                qR.buildRecommendation();
                
                sortedRecomQueryList=qR.getSortedRecomQueryList();
                
                
                
                
//            //... generalizing the input query into a SPARLQ Template Query ....
//            qr.generalizeToQueryTemplate();
//            
//            System.out.println(" ");
//            System.out.println("[SQueryRecommendationWorker::queryRecommendation5] We are specializing the input query ... ");
//            qr.specializeToQueryInstance1();
//                        System.out.println(" ");
//                        System.out.println(" ");
//                        System.out.println(" ");
//                        System.out.println(" ");
//                        System.out.println(" ");
//                        System.out.println(" ");
//                        System.out.println(" ");
//                        System.out.println(" ");
//                         
//            qr.computeRecommendateQueryScore(qr.getRootTemplate(), 0);
//            // sort the result list
//            Collections.sort(qr.getQueryRecommendatedList(), QueryScorePair.queryScoreComp);    

//            return qr.getQueryRecommendatedList();
return sortedRecomQueryList;
            } catch (Exception ex) {
                Logger.getLogger(QueryRecommendatorForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("[SQueryRecommendationWorker::queryRecommendation5]The input query is not satisfiable w.r.t the input dataset... ");     
        }

        return sortedRecomQueryList;
    }


}
