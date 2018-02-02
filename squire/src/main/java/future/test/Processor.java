/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package future.test;

import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author carloallocca
 * 
 * An example of thread syncronatation with wait and notify, the case of the producer and consumer;
 * 
 */
public class Processor {
    
    private LinkedList<Integer> list = new LinkedList<Integer>();
    private LinkedList<Integer> updatedList;
    
    private final int LIMIT = 10;
    private Object lock = new Object();

    private int value;
    
    public Processor(int value,  LinkedList<Integer> updateList){
        this.value=value;
        this.updatedList=updateList;
    }
    
    
    // it is our specify()
    public void produce() throws InterruptedException {
        //int value = 0;
        while (true) {
            synchronized (lock) {            
                while(list.size() == LIMIT) {
                    lock.wait();
                }
                list.add(this.value++);
                lock.notify();
            }
        }
    }
    
    public LinkedList<Integer> consume() throws InterruptedException {
        
        Random random = new Random();

        while (true) {
            synchronized (lock) {
                while(list.size() == 0) {
                    lock.wait();
                }
                for(Integer integer:list){
                    updatedList.add(integer);
                }
                
                System.out.print("List size is: " + list.size());
//                int value = list.removeLast();//.removeFirst();
//                System.out.println("; value is: " + value);
                System.out.println("; the list is: " + list.toString());
                lock.notify();
            }           
            Thread.sleep(random.nextInt(1000));
        }
    }

    public LinkedList<Integer> getUpdatedList() {
        return updatedList;
    }
    
    
    
    
    
}

