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
 *
 *
 */
public class FromStringToArrayList {

    public static ArrayList transform(String s) {
        ArrayList<String> list = new ArrayList();
        if (""!= s && null != s) {
            String[] sSplit = s.substring(1, s.length() - 1).split(",");
            for (int i = 0; i < sSplit.length; i++) {
                list.add(sSplit[i].trim());
            }
        }
        return list;
    }

}
