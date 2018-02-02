/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package observer.test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author carloallocca
 */
public class QuerySpecializerSubject implements ISubject{

    private ArrayList<IObserver> observers; 
    
    private boolean isFinished=false;
    
    private List<Integer> recommendedQueryList;
    
    private int value;
    
    public QuerySpecializerSubject(){
        observers= new ArrayList();
        recommendedQueryList= new ArrayList();
        value=0;
    }
    
    @Override
    public void register(IObserver newObserver) {
        observers.add(newObserver);
    }

    @Override
    public void unregister(IObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyQueryRecommendationsToObserver() {
        
        int i=0;
        
        while(i<101){
            
            if ((this.recommendedQueryList.size() % 10 == 0) || i==100) {
                for (IObserver o : observers) {
                    o.updateQueryRecommendedList(this.recommendedQueryList);
                }
            }
            i++;
            this.recommendedQueryList.add(this.value++);
        }
        isFinished=true;
        
    }
    
}
