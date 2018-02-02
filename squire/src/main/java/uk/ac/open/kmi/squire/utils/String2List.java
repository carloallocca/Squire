/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author carloallocca
 *
 *
 */
public class String2List {

    public static List<String> transform(String s) {
        if (s == null) throw new IllegalArgumentException("String cannot be null");
        List<String> list = new ArrayList<>();
        if (!s.isEmpty()) {
            String[] sSplit = s.substring(1, s.length() - 1).split(",");
            list = Arrays.asList(sSplit);
            // for (int i = 0; i < sSplit.length; i++)
            // list.add(sSplit[i].trim());
        }
        return list;
    }

}
