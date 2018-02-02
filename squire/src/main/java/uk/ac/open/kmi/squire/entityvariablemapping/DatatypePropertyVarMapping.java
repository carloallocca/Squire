/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author callocca
 */
public class DatatypePropertyVarMapping {

    private Map<String, String> datatypeProperyURIVarTable = null;// = new HashMap<String, String>();
    private Map<String, String> varDatatypeProperyURITable = null;// = new HashMap<String, String>();
    private int succ = 0;

    public DatatypePropertyVarMapping() {
        datatypeProperyURIVarTable = new HashMap<>();
        varDatatypeProperyURITable = new HashMap<>();
        succ = 1;

    }

//    public void initializeDatatypeProperyVarTable() {
//        if (datatypeProperyURIVarTable == null && varDatatypeProperyURITable == null && succ == 0) {
//            datatypeProperyURIVarTable = new HashMap<String, String>();
//            varDatatypeProperyURITable = new HashMap<String, String>();
//            succ = 1;
//        }
//    }

    public void cancelDatatypeProperyVarTable() {
        datatypeProperyURIVarTable = null;
        varDatatypeProperyURITable = null;
        succ = 0;
    }

    public String generateIFAbsentDatatypePropertyVar(String dpURI) throws NullPointerException {
        if (datatypeProperyURIVarTable == null || succ == 0 || varDatatypeProperyURITable == null) {
            throw new NullPointerException("The datatypePropertyVarTable needs to be created. Use the class constructor first.");        }
        if (!(datatypeProperyURIVarTable.containsKey(dpURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "dpt" + Integer.toString(succ++);
            datatypeProperyURIVarTable.put(dpURI, tmp);
            varDatatypeProperyURITable.put(tmp, dpURI);
            return tmp;
        } else {
            return datatypeProperyURIVarTable.get(dpURI);
        }
    }

    public String getDatatypeProperyFromVar(String varString) throws NullPointerException {
        if (varDatatypeProperyURITable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(varDatatypeProperyURITable.containsKey(varString))) {
            return null;
        } else {
            return varDatatypeProperyURITable.get(varString);
        }
    }

    public  Map<String, String> getDatatypeProperyVarTable() {
        return datatypeProperyURIVarTable;
    }

    public Map<String, String> getVarDatatypeProperyTable() {
        return varDatatypeProperyURITable;
    }

}
