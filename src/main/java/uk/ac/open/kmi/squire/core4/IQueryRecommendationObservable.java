/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import org.apache.jena.query.Query;

/**
 *
 * @author carloallocca
 */
public interface IQueryRecommendationObservable  {
    
    public void register(IQueryRecommendationObserver o);
    public void unregister(IQueryRecommendationObserver o);
    public void notifyQueryRecommendation(Query qR, float score);
    public void notifyQuerySatisfiableValue(boolean value);
    
    public void notifyDatatsetSimilarity(float score);
    public void notifyQuerySatisfiableMessage(String msg);
    
    public void notifyQueryRecommendationCompletion(Boolean finished);
    
   
    
}
