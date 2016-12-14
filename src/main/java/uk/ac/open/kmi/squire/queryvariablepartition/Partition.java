/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.queryvariablepartition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author callocca
 */
public class Partition {

    
    public List<List<List<String>>> partitions(
            List<String> inputSet, int depth) {
        List<List<List<String>>> res = new ArrayList<>();
        if (inputSet.isEmpty()) {
            List<List<String>> empty = new ArrayList<>();
            res.add(empty);
            return res;
        }
        // Note that this algorithm only works if inputSet.size() < 31
        // since you overflow int space beyond that. This is true even
        // if you use Math.pow and cast back to int. The original
        // Python code does not have this limitation because Python
        // will implicitly promote to a long, which in Python terms is
        // an arbitrary precision integer similar to Java's BigInteger.
        int limit = 1 << (inputSet.size() - 1);
        // Note the separate variable to avoid resetting
        // the loop variable on each iteration.
        for (int j = 0; j < limit; ++j) {
            List<List<String>> parts = new ArrayList<>();
            List<String> part1 = new ArrayList<>();
            List<String> part2 = new ArrayList<>();
            parts.add(part1);
            parts.add(part2);
            int i = j;
            for (String item : inputSet) {
                parts.get(i&1).add(item);
                i >>= 1;
            }
            for (List<List<String>> b : partitions1(part2, depth + 1)) {
                List<List<String>> holder = new ArrayList<>();
                holder.add(part1);
                holder.addAll(b);
                res.add(holder);
            }
        }
        return res;
    }

    
    
    private static List<List<List<String>>> partitions1(
            List<String> inputSet, int depth) {
        List<List<List<String>>> res = new ArrayList<>();
        if (inputSet.isEmpty()) {
            List<List<String>> empty = new ArrayList<>();
            res.add(empty);
            return res;
        }
        // Note that this algorithm only works if inputSet.size() < 31
        // since you overflow int space beyond that. This is true even
        // if you use Math.pow and cast back to int. The original
        // Python code does not have this limitation because Python
        // will implicitly promote to a long, which in Python terms is
        // an arbitrary precision integer similar to Java's BigInteger.
        int limit = 1 << (inputSet.size() - 1);
        // Note the separate variable to avoid resetting
        // the loop variable on each iteration.
        for (int j = 0; j < limit; ++j) {
            List<List<String>> parts = new ArrayList<>();
            List<String> part1 = new ArrayList<>();
            List<String> part2 = new ArrayList<>();
            parts.add(part1);
            parts.add(part2);
            int i = j;
            for (String item : inputSet) {
                parts.get(i&1).add(item);
                i >>= 1;
            }
            for (List<List<String>> b : partitions1(part2, depth + 1)) {
                List<List<String>> holder = new ArrayList<>();
                holder.add(part1);
                holder.addAll(b);
                res.add(holder);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        for (List<List<String>> partitions :
//                 partitions(Arrays.asList("1", "2", "3", "4","5", "6", "7", "8","9", "10", 
//                         "11", "12", "13", "14","15", "16", "17", "18","19", "20",
//                         "21", "22", "23", "24","25", "26", "27", "28","29", "30",
//                         "31", "32", "33", "34","35", "36", "37", "38","39", "40"), 0)) {
            
            partitions1(Arrays.asList("a", "b", "c"), 0)) {
            System.out.println(partitions);
            for(List<String> part:partitions){
                for(String var:part){
                    System.out.print(var);
                }
                System.out.println("Add the code for building a query");
                
                System.out.println(" ");
            }
        }
    }
} 