/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources.impl;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.mksmart.squire.websquire.v1.resources.JobStatement;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePairResource;

import uk.ac.open.kmi.squire.jobs.JobManager;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPointBasedRDFDataset;

/**
 *
 * @author carloallocca
 */
@Path("/v1/recommend")
public class QueryStringScorePairResourceImpl implements QueryStringScorePairResource {

    @Override
    @POST
    public JobStatement getRecommendedQueryList(@FormParam("qo") String qo,
                                                @FormParam("source_endpoint") String source_endpoint,
                                                @FormParam("target_endpoint") String target_endpoint,
                                                @FormParam("weight_rts") float resultTypeSimilarityDegree,
                                                @FormParam("weight_qrd") float queryRootDistanceDegree,
                                                @FormParam("weight_rss") float resultSizeSimilarityDegree,
                                                @FormParam("weight_qsd") float querySpecificityDistanceDegree) {
        IRDFDataset d1 = new SPARQLEndPointBasedRDFDataset(source_endpoint, "");
        IRDFDataset d2 = new SPARQLEndPointBasedRDFDataset(target_endpoint, "");

        JobManager jobMan = JobManager.getInstance();
        String token = jobMan.doQueryRecommendation(qo, d1, d2, resultTypeSimilarityDegree,
            queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree);

        return new JobStatement(token);

        // QueryRecommendatorForm instance = new QueryRecommendatorForm(qo, d1, d2,
        // resultTypeSimilarityDegree,
        // queryRootDistanceDegree,
        // resultSizeSimilarityDegree,
        // querySpecificityDistanceDegree);

        // return instance.recommend();
    }

    @Override
    @GET
    public String getSignOfLife() {
        return "Hi. This is the SQUIRE query recommendation service.";
    }

}
