/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author carloallocca
 */
public interface QueryStringScorePairResource {

    // Example: Returning more than one Item
    @Produces({MediaType.APPLICATION_JSON})
    JobStatement getRecommendedQueryList(String qo,
                                         String source_endpoint,
                                         String target_endpoint,
                                         float resultTypeSimilarityDegree,
                                         float queryRootDistanceDegree,
                                         float resultSizeSimilarityDegree,
                                         float querySpecificityDistanceDegree);

    // Tell me you are alive
    @Produces({MediaType.TEXT_PLAIN})
    String getSignOfLife();

}
