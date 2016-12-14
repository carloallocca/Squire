/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package future.test;

import java.util.LinkedList;

/**
 *
 * @author carloallocca
 */
public class App {

    public static void main(String[] args) throws InterruptedException {

        LinkedList<Integer> list = new LinkedList<Integer>();
    
        final Processor processor = new Processor(1, list);

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    processor.produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    processor.consume();
                    System.out.println("[App::main]Ten More Results...");
                    System.out.println(processor.getUpdatedList().toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        t1.start();
        t2.start();
        
//        t1.join();
//        t2.join();
    }
}