/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.operation.SPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.RDFDatasetSimilarity;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendatorForm4 extends AbstractQueryRecommendationObservable implements
        IQueryRecommendationObserver, Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final float queryRootDistanceDegree;
    private final float querySpecificityDistanceDegree;

    private final String queryString;
    private final IRDFDataset rdfd1;
    private final IRDFDataset rdfd2;
    private final List<QueryStringScorePair> recommendedQueryList;

    private final float resultSizeSimilarityDegree;

    private final float resultTypeSimilarityDegree;

    public QueryRecommendatorForm4(String qString,
                                   IRDFDataset d1,
                                   IRDFDataset d2,
                                   float resultTypeSimilarityDegree,
                                   float queryRootDistanceDegree,
                                   float resultSizeSimilarityDegree,
                                   float querySpecificityDistanceDegree,
                                   String token) {

        this.token = token;

        this.queryString = qString;
        this.rdfd1 = d1;
        this.rdfd2 = d2;

        this.recommendedQueryList = new ArrayList();

        this.queryRootDistanceDegree = queryRootDistanceDegree;
        this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
        this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
        this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;

    }

    @Override
    public void run() {
        try {
            // recommend();
            recommendWithToken(this.token);
        } catch (Exception ex) {
            if (ex instanceof ClosedByInterruptException) {
                log.warn(" A task with token " + token + " was interrupted."
                         + " This may have been requested by a client.");
            } else log.error("Caught exception of type " + ex.getClass().getName() + " : " + ex.getMessage()
                             + " - doing nothing with it.", ex);
        }
    }

    @Override
    public void updateDatasetSimilarity(float simScore, String token) {
        this.notifyDatatsetSimilarity(simScore);
    }

    @Override
    public void updateQueryRecommendated(Query qR, float score, String token) {
        this.notifyQueryRecommendation(qR, score);
    }

    @Override
    public void updateQueryRecommendationCompletion(Boolean finished, String token) {
        this.notifyQueryRecommendationCompletion(finished);
    }

    @Override
    public void updateSatisfiableMessage(String msg, String token) {
        this.notifyQuerySatisfiableMessage(msg);
    }

    @Override
    public void updateSatisfiableValue(Boolean value, String token) {
        if (value) {
            // System.out.println("[QueryRecommendatorForm4::updateSatisfiableValue] value is " + value);
            this.notifyQuerySatisfiableMessage("[QueryRecommendatorForm4::updateSatisfiableValue] The input query is satisfiable ");
        } else {
            // System.out.println("[QueryRecommendatorForm4::updateSatisfiableValue] value is " + value);
            this.notifyQuerySatisfiableMessage("[QueryRecommendatorForm4::updateSatisfiableValue] The input query is NOT satisfiable ");
        }
    }

    private List<QueryStringScorePair> recommendWithToken(String token) {
        Query query;
        try {
            query = QueryFactory.create(queryString);
            System.out.println("");
            System.out.println("[QueryRecommendatorForm4::recommend] THE SOURCE QUERY ");
            System.out.println("");
            System.out.println(query.toString());
        } catch (QueryParseException ex) { // QueryParseException
            throw new QueryParseException(
                    "[QueryRecommendatorForm4::recommendWithToken] THE SOURCE QUERY is not parsable!!!", -1,
                    -1);
        }

        // Phase 1 : check query satisfiability
        SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable(this.token);
        qs.register(this);
        boolean querySat = false;
        try {
            log.debug("Checking satisfiability against source dataset <{}>", rdfd1.getEndPointURL());
            querySat = qs.isSatisfiableWRTResultsWithToken(query, rdfd1);
            log.debug(" ... is satisfiable? {}", querySat);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // Phase 2 : check dataset similarity
        if (querySat) {
            try {
                this.notifyQuerySatisfiableValue(true);
                RDFDatasetSimilarity querySim = new RDFDatasetSimilarity(this.token);
                querySim.register(this);
                float score = querySim.computeSim(rdfd1, rdfd2);

                // Phase 3 : recommend
                QueryRecommendator4 qR = new QueryRecommendator4(query, rdfd1, rdfd2,
                        resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
                        querySpecificityDistanceDegree);
                qR.register(this);
                qR.buildRecommendation();

            } catch (Exception ex) {
                log.error("", ex);
            }
        } else {
            this.notifyQuerySatisfiableValue(false);
            this.notifyQuerySatisfiableMessage("[QueryRecommendatorForm4::recommendWithToken]The input query is not satisfiable w.r.t the input dataset... ");
        }
        return Collections.emptyList();
    }

}
