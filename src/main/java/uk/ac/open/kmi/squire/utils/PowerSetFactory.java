/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.core2.QueryAndContextNode;

/**
 *
 * @author carloallocca
 * @param <T>
 */
public class PowerSetFactory<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static <T> List<List<T>> powerset(Collection<T> list) {
        List<List<T>> ps = new ArrayList<List<T>>();
        ps.add(new ArrayList<T>());   // add the empty set

        // for every item in the original list
        for (T item : list) {
            List<List<T>> newPs = new ArrayList<List<T>>();

            for (List<T> subset : ps) {
                // copy all of the current powerset's subsets
                newPs.add(subset);

                // plus the subsets appended with the current item
                List<T> newSubset = new ArrayList<T>(subset);
                newSubset.add(item);
                newPs.add(newSubset);
            }

            // powerset is now powerset of list.subList(0, list.indexOf(item)+1)
            ps = newPs;
        }
        return ps;
    }

    public static <T> List<List<T>> order(List<List<T>> list) {
        if (list.size() > 0) {
            Collections.sort(list, new Comparator<List<T>>() {
                @Override
                public int compare(final List<T> object1, final List<T> object2) {
                    //return Integer.compare(object1.size(),object2.size()); //ordering ascendende
                    return -1*Integer.valueOf(object1.size()).compareTo(object2.size()); //ordering discendete
                    
                }
            });
        }
        return list;
    }

}
