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
public class QueryRecommendatorObserver implements IObserver{

    private final ISubject querySpecializerSubject;
    
    public QueryRecommendatorObserver(ISubject subject){
        this.querySpecializerSubject=subject;
        subject.register(this);
    }
    
    // potremmo mettere dell interfaccia un metodo che ritorna the List<Integer> elemList to the calling class. 
    @Override
    public void updateQueryRecommendedList(List<Integer> elemList) {
        System.out.println(elemList.toString());
    }
    
}
