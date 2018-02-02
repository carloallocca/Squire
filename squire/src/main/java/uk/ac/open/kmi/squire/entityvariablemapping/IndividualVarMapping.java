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
public class IndividualVarMapping {

    private Map<String, String> individualURIVarTable=null;// = new HashMap<String, String>();
    private Map<String, String> varIndividualURITable=null;// = new HashMap<String, String>();
    private int succ=0;

    public IndividualVarMapping() {
        individualURIVarTable = new HashMap<String, String>();
        varIndividualURITable = new HashMap<String, String>();
        succ = 1;
    }

//    public void initializeIndividualVarTable() {
//        if (individualURIVarTable == null && varIndividualURITable == null && succ == 0) {
//            individualURIVarTable = new HashMap<String, String>();
//            varIndividualURITable = new HashMap<String, String>();
//            succ = 1;
//        }
//    }

    public void cancelIndividualVarTable() {
        individualURIVarTable = null;
        varIndividualURITable = null;
        succ = 0;
    }

    public String generateIFAbsentIndividualVar(String indURI) throws NullPointerException {
        if (individualURIVarTable == null || succ == 0||varIndividualURITable==null) {
            throw new NullPointerException("The individualURIVarTable needs to be created. Use the class constructor first.");
        }
        if (!(individualURIVarTable.containsKey(indURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "it" + Integer.toString(succ++);
            individualURIVarTable.put(indURI, tmp);
            varIndividualURITable.put(tmp, indURI);
            return tmp;
        } else {
            return individualURIVarTable.get(indURI);
        }
    }

    public String getIndividualFromVar(String varString) throws NullPointerException {
        if (varIndividualURITable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(varIndividualURITable.containsKey(varString))) {
            return null;
        } else {
            return varIndividualURITable.get(varString);
        }
    }

    public Map<String, String> getIndividualURIVarTable() {
        return individualURIVarTable;
    }

    public Map<String, String> getVarIndividualURITable() {
        return varIndividualURITable;
    }

    
    
}
