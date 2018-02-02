/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

/**
 *
 * @author carloallocca
 */
public interface IQuerySpecializerObservable {
    
    public void register(IQueryRecommendationObserver o);
    public void unregister(IQueryRecommendationObserver o);
    public void notifyNewQueryRecommendationsToObserver();

    
}
