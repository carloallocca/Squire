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
public class LiteralVarMapping {
    
    private  Map<String, String> literalURIVarTable = null;// = new HashMap<String, String>();
    private  Map<String, String> uriLiteralTable = null;// = new HashMap<String, String>();
    private  int succ = 0;

    public LiteralVarMapping(){
        literalURIVarTable = new HashMap<String, String>();
        uriLiteralTable = new HashMap<String, String>();
        succ = 1;
    }
    
//    public void initializeLiteralVarTable() {
//        if(literalURIVarTable==null && uriLiteralTable==null && succ==0){
//            literalURIVarTable = new HashMap<String, String>();
//            uriLiteralTable = new HashMap<String, String>();
//            succ = 1;
//        }
//    }
    
    public void cancelLiteralVarTable() {
        literalURIVarTable=null;
        uriLiteralTable=null; 
        succ=0;
    }


    public String generateIFAbsentLiteralVar(String litURI) throws NullPointerException {
        if (literalURIVarTable == null || succ == 0 || uriLiteralTable==null) {
            throw new NullPointerException("The literalURIVarTable needs to be created. Pls, use the class constructor first.");
        }
        if (!(literalURIVarTable.containsKey(litURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "lt" + Integer.toString(succ++);
            literalURIVarTable.put(litURI, tmp);
            uriLiteralTable.put(tmp,litURI);
            return tmp;
        } else {
            return literalURIVarTable.get(litURI);
        }
    }    
    
    public String getLiteralFromVar(String varString) throws NullPointerException {
        if ( uriLiteralTable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(uriLiteralTable.containsKey(varString))) {
            return null;
        } else {
            return uriLiteralTable.get(varString);
        }
    }

    public Map<String, String> getLiteralVarTable() {
        return literalURIVarTable;
    }

    public Map<String, String> getVarLiteralTable() {
        return uriLiteralTable;
    }

    
}
