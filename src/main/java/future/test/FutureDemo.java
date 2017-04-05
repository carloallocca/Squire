/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package future.test;

import java.util.concurrent.Callable; 
import java.util.concurrent.ExecutionException; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.Future; 
import java.util.logging.Level; 
import java.util.logging.Logger;


/**
 *
 * @author carloallocca
 */
public class FutureDemo {
    
    private static final ExecutorService threadpool = Executors.newFixedThreadPool(3);
    
    public static void main(String args[]) throws InterruptedException, ExecutionException { 
        
        FactorialCalculator task = new FactorialCalculator(60);
        
        System.out.println("Submitting Task ..."); 
        Future future = threadpool.submit(task);
        
        System.out.println("Task is submitted");
        
        
        while (!future.isDone()) { 
            System.out.println("Task is not completed yet....");
            System.out.println("The current value is...."+future.get());

            Thread.sleep(1); //sleep for 1 millisecond before checking again 
            
            
        }
        System.out.println("Task is completed, let's check result");
        long factorial = (long) future.get();
        System.out.println("Factorial of 10 is : " + factorial);

        threadpool.shutdown();
          
    } 
            
    
    
}
