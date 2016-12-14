/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.util.ArrayList;

/**
 *
 * @author carloallocca
 */
public class Test1 {
    
    
        //Input = [ClassA, ClassB]
    
    public static void main(String args[]) {
        
        ArrayList<String> list = FromStringToArrayList.transform(null);
        
        for(String s:list){
            System.out.println(s);
        }
        
    }
    
    
    
}
