/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package future.test;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carloallocca
 */
public class FactorialCalculator implements Callable {

    private final int number;

    public FactorialCalculator(int number) {
        this.number = number;
    }

    @Override
    public Long call() throws Exception {

        long output = 0;
        try {
            output = factorial(number);

        } catch (InterruptedException ex) {
            Logger.getLogger(FactorialCalculator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    private long factorial(int number) throws InterruptedException {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be greater than zero");
        }

        long result = 1;
        while (number > 0) {
            Thread.sleep(1); // adding delay for example 
            result = result * number;
            number--;
        }
        return result;
    }

}
