/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.jobs;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.query.Query;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;

import uk.ac.open.kmi.squire.core4.IQueryRecommendationObserver;
import uk.ac.open.kmi.squire.core4.QueryRecommendatorForm4;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class JobManager implements IQueryRecommendationObserver {

    private static JobManager me = null;

    private static int tokenCounter = 0;

    private final Map<String,RecommendationJobStatus> map = new ConcurrentHashMap<>();
    private final Map<String,Future<?>> taskMap = new ConcurrentHashMap<>();

    private ExecutorService threadPool;

    private JobManager() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    public static JobManager getInstance() {
        if (me == null) me = new JobManager();
        return me;
    }

    public RecommendationJobStatus getJobStatus(String token) {
        synchronized (map) {
            return map.get(token);
        }
    }

    // It returns a token as it is the method that is going to be called for the Web Interface, and the token
    // is used later on to do ping...
    public String doQueryRecommendation(String qString,
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
        Future<?> promise = this.threadPool.submit(R1);
        taskMap.put(strToken, promise);

        // Thread recExec = new Thread(R1);

        // recExec.start();
        // if (!recExec.isAlive()) {
        // recExec.stop();
        // }

        return Integer.toString(tokenCounter);

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

    @Override
    public void updateQueryRecommendationCompletion(Boolean finished, String token) {
        if (map.containsKey(token)) {
            getJobStatus(token).setIsFinished(finished);
        }
    }

}
