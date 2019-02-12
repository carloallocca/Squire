/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;

/**
 *
 * @author carloallocca
 */
public abstract class AbstractQueryRecommendationObservable implements IQueryRecommendationObservable {

	private final Set<IQueryRecommendationObserver> observers = new HashSet<>();

	protected String token;

	@Override
	public void notifyDatatsetSimilarity(float score) {
		for (IQueryRecommendationObserver obs : observers)
			obs.updateDatasetSimilarity(score, token);
	}

	@Override
	public void notifyQueryGeneralized(Collection<Query> lgg) {
		for (IQueryRecommendationObserver obs : observers)
			obs.updateGeneralized(lgg);
	}

	@Override
	public void notifyQueryRecommendation(Query qR, float score) {
		for (IQueryRecommendationObserver obs : observers)
			obs.updateQueryRecommendated(qR, score, token);
	}

	@Override
	public void notifyQueryRecommendationCompletion(Boolean finished) {
		for (IQueryRecommendationObserver obs : observers)
			obs.updateQueryRecommendationCompletion(finished, token);
	}

	@Override
	public void notifyQuerySatisfiableValue(Query query, boolean value) {
		for (IQueryRecommendationObserver observer : observers)
			observer.updateSatisfiableValue(query, value, token);
	}

	@Override
	public void register(IQueryRecommendationObserver o) {
		observers.add(o);
	}

	@Override
	public void unregister(IQueryRecommendationObserver o) {
		observers.remove(o);
	}

}
