/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.jobs;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.jena.query.Query;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.IQueryRecommendationObserver;
import uk.ac.open.kmi.squire.core4.QueryRecommendatorForm4;
import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;

/**
 *
 * @author carloallocca
 */
public class JobManager implements IQueryRecommendationObserver {

    private static JobManager me = null;

    private Logger log = LoggerFactory.getLogger(getClass());

    private static int tokenCounter = 0;

    public static JobManager getInstance() {
        if (me == null) me = new JobManager();
        return me;
    }

    private final Map<String,RecommendationJobStatus> map = new ConcurrentHashMap<>();

    private final Map<String,Future<?>> taskMap = new ConcurrentHashMap<>();

    private ThreadPoolExecutor threadPool;

    private JobManager() {
        this.threadPool = new ScheduledThreadPoolExecutor(3);
    }

    /*
     * It returns a token as it is the method that is going to be called for the Web Interface, and the token
     * is used later on to do ping...
     */
    public synchronized String doQueryRecommendation(String qString,
                                                     IRDFDataset d1,
                                                     IRDFDataset d2,
                                                     float resultTypeSimilarityDegree,
                                                     float queryRootDistanceDegree,
                                                     float resultSizeSimilarityDegree,
                                                     float querySpecificityDistanceDegree) {

        // As a new request has just arrived, a new token is generated.

        tokenCounter = tokenCounter + 1;
        String strToken = Integer.toString(tokenCounter);

        // IQueryRecommendationObservable qSatisfiableObservable = new SPARQLQuerySatisfiable(strToken);
        // qSatisfiableObservable.register(this);
        // IQueryRecommendationObservable datasetsSimObservable = new RDFDatasetSimilarity(strToken);
        // datasetsSimObservable.register(this);
        // IQueryRecommendationObservable querySpecializerObservable= new

        // A new thread per QueryRecFORM is created too.

        synchronized (map) {
            map.put(strToken, new RecommendationJobStatus());
        }
        QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(qString, d1, d2, resultTypeSimilarityDegree,
                queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
                Integer.toString(tokenCounter));
        R1.register(this);
        log.debug("Active threads before submission: {} (completed: {})", this.threadPool.getActiveCount(),
            this.threadPool.getCompletedTaskCount());
        Future<?> promise = this.threadPool.submit(R1);
        log.debug("Active threads after submission (may have terminated): {} (completed: {})",
            this.threadPool.getActiveCount(), this.threadPool.getCompletedTaskCount());

        taskMap.put(strToken, promise);

        // Thread recExec = new Thread(R1);

        // recExec.start();
        // if (!recExec.isAlive()) {
        // recExec.stop();
        // }

        return Integer.toString(tokenCounter);

    }

    public synchronized String doRDFDatasetIndexing(String endPointURI) {
        return this.doRDFDatasetIndexing(endPointURI, "");
    }

    public synchronized String doRDFDatasetIndexing(String endPointURI, String graphName) {

        // implement the logic of indexing the endpoint if it does not exist already...
        // As a new request has just arrived, a new token is generated.

        tokenCounter = tokenCounter + 1;
        String strToken = Integer.toString(tokenCounter);

        // IQueryRecommendationObservable qSatisfiableObservable = new SPARQLQuerySatisfiable(strToken);
        // qSatisfiableObservable.register(this);
        // IQueryRecommendationObservable datasetsSimObservable = new RDFDatasetSimilarity(strToken);
        // datasetsSimObservable.register(this);
        // IQueryRecommendationObservable querySpecializerObservable= new

        // A new thread per QueryRecFORM is created too.

        synchronized (map) {
            map.put(strToken, new RecommendationJobStatus());

        }
        try {
            IRDFDataset d1 = new SparqlIndexedDataset(endPointURI, graphName);
            if (!d1.isIndexed()) {
//                RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();

                // System.out.println("[SPARQLEndPoint::SPARQLEndPoint, constructor] SPARQL endpoint " +
                // urlAddress + " is not yet indexed ");
//                d1.computeClassSet();
//                d1.computeObjectPropertySet();
//                d1.computeDataTypePropertySet();
//                d1.computeRDFVocabularySet();
//                instance.indexSignature(endPointURI, graphName, d1.getClassSet(), d1.getObjectPropertySet(),
//                    d1.getDatatypePropertySet(), d1.getIndividualSet(), d1.getLiteralSet(),
//                    d1.getRDFVocabulary(), d1.getPropertySet());
  
                      // R1.register(this);


            
            }
            
            Future<?> promise = this.threadPool.submit(d1);
            taskMap.put(strToken, promise);
  
        } catch (IOException ex) {
            log.error("Exception logged.", ex);
        }

        // IRDFDataset d2 = new SPARQLEndPoint(target_endpoint, "");
        //
        //
        //
        // QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(qString, d1, d2,
        // resultTypeSimilarityDegree,
        // queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
        // Integer.toString(tokenCounter));

        // return Integer.toString(tokenCounter);
        return strToken;

    }

    public RecommendationJobStatus getJobStatus(String token) {
        synchronized (map) {
            return map.get(token);
        }
    }

    public Future<?> getPromise(String token) {
        return taskMap.get(token);
    }

    public synchronized boolean stopJob(String token) {
        Future<?> promise = taskMap.get(token);
        boolean existed = promise != null;
        if (existed) promise.cancel(true);
        taskMap.remove(token);
        return existed;
    }

    @Override
    public void updateDatasetSimilarity(float simScore, String token) {
        if (map.containsKey(token)) {
            getJobStatus(token).setDatasetsSim(simScore);
        }
    }

    @Override
    public void updateQueryRecommendated(Query qR, float score, String token) {
        if (map.containsKey(token)) {
            QueryStringScorePair elem = new QueryStringScorePair();
            elem.setQuery(qR.toString());
            elem.setScore(score);

            // getJobStatus(token).getRecommandedQueryList().add(elem);

            SortedSet<QueryStringScorePair> tmpList = getJobStatus(token).getRecommandedQueryList();
            tmpList.add(elem);
            getJobStatus(token).setRecommandedQueryList(tmpList);
            System.out.println("JobManager::updateQueryRecommendated1111");
            for (QueryStringScorePair queryPair : getJobStatus(token).getRecommandedQueryList()) {
                System.out.println("RECOMMENDED QUERY SCORE ==== " + queryPair.getScore());
                System.out.println(queryPair.getQuery());
            }
        }
        // ADD CODE to send the list of SPARQL query and their score to the WebClient
    }

    @Override
    public void updateQueryRecommendationCompletion(Boolean finished, String token) {
        if (map.containsKey(token)) {
            getJobStatus(token).setIsFinished(finished);
        }
    }

    @Override
    public void updateSatisfiableMessage(String msg, String token) {
        System.out.println("JobManager::updateSatisfiableMessage");
        if (map.containsKey(token)) {
            System.out.println("[JobManager::updateSatisfiableMessage] msg " + msg);
            getJobStatus(token).setMessage(msg);
        }
    }

    @Override
    public void updateSatisfiableValue(Boolean value, String token) {
        System.out.println("JobManager::updateSatisfiableValue");
        if (map.containsKey(token)) {
            getJobStatus(token).setSatisfiable(value);
        }
    }

}
