/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author carloallocca
 */
public class FromArrayStringToFile {
    
    public static void save(String fileName, ArrayList<String> list) throws FileNotFoundException {
    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
    for (String s : list)
         pw.println(s);
    pw.close();
}
    
}
