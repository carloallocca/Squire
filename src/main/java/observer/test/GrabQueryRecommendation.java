/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package observer.test;

/**
 *
 * @author carloallocca
 */
public class GrabQueryRecommendation {
    
    public static void main(String[] args) throws InterruptedException {
        
        QuerySpecializerSubject qSpecSubj= new QuerySpecializerSubject();
        QueryRecommendatorObserver observer = new QueryRecommendatorObserver(qSpecSubj);
        qSpecSubj.notifyQueryRecommendationsToObserver();
//        observer.updateQueryRecommendedList(elemList);
    }
    
    
}
