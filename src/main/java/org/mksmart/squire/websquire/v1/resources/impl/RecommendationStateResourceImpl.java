/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources.impl;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mksmart.squire.websquire.v1.resources.RecommendationStateResource;

import uk.ac.open.kmi.squire.jobs.JobManager;
import uk.ac.open.kmi.squire.jobs.RecommendationJobStatus;

/**
 *
 * @author carloallocca
 */
@Path("/v1/job")
public class RecommendationStateResourceImpl implements RecommendationStateResource {

    @Override
    @GET
    @Path("/{token}")
    public RecommendationJobStatus getStatus(@PathParam("token") String token) {
        return JobManager.getInstance().getJobStatus(token);
    }

    @Override
    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{token}")
    public RecommendationJobStatus stop(@PathParam("token") String token) {
        JobManager.getInstance().stopJob(token);
        return JobManager.getInstance().getJobStatus(token);
    }

}
