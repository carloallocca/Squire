/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.util.Collection;

import org.apache.jena.query.Query;

/**
 *
 * @author carloallocca
 */
public interface IQueryRecommendationObserver {

	public void updateDatasetSimilarity(float simScore, String token);

	public void updateGeneralized(Collection<Query> lgg);

	public void updateQueryRecommendated(Query qR, float score, String token);

	public void updateQueryRecommendationCompletion(Boolean finished, String token);

	public void updateSatisfiableValue(Query query, boolean value, String token);

}
