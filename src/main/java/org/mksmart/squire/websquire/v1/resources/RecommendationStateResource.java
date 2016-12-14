/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uk.ac.open.kmi.squire.jobs.RecommendationJobStatus;

/**
 *
 * @author carloallocca
 */
public interface RecommendationStateResource {

    @Produces({MediaType.APPLICATION_JSON})
    RecommendationJobStatus getStatus(String token);

    RecommendationJobStatus stop(String token);

}
