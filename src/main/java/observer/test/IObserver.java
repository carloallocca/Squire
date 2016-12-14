/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package observer.test;

import java.util.List;

/**
 *
 * @author carloallocca
 */
public interface IObserver {
    
    
    public void updateQueryRecommendedList(List<Integer> elemList);
    
    
}
