/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources.impl;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.mksmart.squire.websquire.v1.resources.JobStatement;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePairResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.jobs.JobManager;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;

/**
 *
 * @author carloallocca
 */
@Path("/v1/recommend")
public class QueryStringScorePairResourceImpl implements QueryStringScorePairResource {

    private Logger log = LoggerFactory.getLogger(getClass());

    protected JobManager jobMan = JobManager.getInstance();

    @Override
    @POST
    public JobStatement getRecommendedQueryList(@FormParam("qo") String qo,
            @FormParam("source_endpoint") String source_endpoint,
            @FormParam("target_endpoint") String target_endpoint,
            @FormParam("weight_rts") float resultTypeSimilarityDegree,
            @FormParam("weight_qrd") float queryRootDistanceDegree,
            @FormParam("weight_rss") float resultSizeSimilarityDegree,
            @FormParam("weight_qsd") float querySpecificityDistanceDegree) {
        log.debug("Received request");
        log.debug(" ... FROM endpoint <{}>", source_endpoint);
        log.debug(" ... TO endpoint <{}>", target_endpoint);
        log.trace(" ... for query : \"{}\"", qo);

        IRDFDataset d1, d2;
        try {
            d1 = new SPARQLEndPoint(source_endpoint);
            d2 = new SPARQLEndPoint(target_endpoint);
        } catch (IOException e) {
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }

        String depToken1 = jobMan.doRDFDatasetIndexing(source_endpoint);
        log.debug("Check-and-index job {} submitted for <{}>", depToken1, source_endpoint);
        String depToken2 = jobMan.doRDFDatasetIndexing(target_endpoint);
        log.debug("Check-and-index job {} submitted for <{}>", depToken2, target_endpoint);
        String[] deps = new String[]{depToken1, depToken2};

        log.debug("Awaiting promises for indexing jobs...");
        Object jobIndex1 = handlePromise(depToken1);
        log.debug("Promise for source indexing job:");
        if (jobIndex1 == null) {
            log.debug(" - promised object is null (this may be expected).");
        } else {
            log.debug(" - promised object is of type {}", jobIndex1.getClass());
        }
        Object jobIndex2 = handlePromise(depToken2);
        log.debug("Promise for target indexing job:");
        if (jobIndex2 == null) {
            log.debug(" - promised object is null (this may be expected).");
        } else {
            log.debug(" - promised object is of type {}", jobIndex2.getClass());
        }

        // if((jobIndex1 instanceof SPARQLEndPoint && jobIndex1==d1) && (jobIndex2 instanceof
        // SPARQLEndPoint && jobIndex2==d2) ){
        if (!(jobIndex1 == null && jobIndex2 == null)) {
            log.warn("Not all dependency jobs are complete! [source={};target={}]", jobIndex1 == null,
                    jobIndex2 == null);
            return null;
        }
        log.info("Indices complete, should now proceed with recommendation.");
        JobStatement jb;
        String token = jobMan.doQueryRecommendation(qo, d1, d2, resultTypeSimilarityDegree,
                queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree);
        jb = new JobStatement(token);
        jb.setDependencyTokens(Arrays.asList(deps));
        return jb;
    }

    @Override
    @GET
    public String getSignOfLife() {
        return "Hi. This is the SQUIRE query recommendation service.";
    }

    protected Object handlePromise(String token) throws WebApplicationException {
        log.debug("Checking token {}", token);
        Future<?> promise = jobMan.getPromise(token);
        if (promise == null) {
            log.error(" ... No promise present in taskmap!");
            throw new WebApplicationException(Response.serverError().entity("Promise for token "
                    + token + " could not be found. Has the indexing job been submitted?").build());
        }
        Object jobIndex;
        try {
            jobIndex = promise.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new WebApplicationException(e);
        }
        if (promise.isDone()) {
            log.debug("Source endpoint indexing (job {}) reportedly complete.", token);
        } else {
            log.warn("Source endpoint indexing (job {}) not complete despite wait-out!", token);
        }
        return jobIndex;
    }

}
