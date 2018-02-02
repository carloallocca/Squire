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
public class RDFVocVarMapping {

    private  Map<String, String> rdfVocVarTable=null;// = new HashMap<String, String>();
    private  Map<String, String> varRDFVocTable=null;// = new HashMap<String, String>();
    
    private int succ=0;
    
    public RDFVocVarMapping(){
            rdfVocVarTable = new HashMap<>();
            varRDFVocTable = new HashMap<>();
            succ = 1;
        
    }

    public void initializeRDFVocVarTable() {
        if(rdfVocVarTable==null && varRDFVocTable==null && succ==0){
            rdfVocVarTable = new HashMap<>();
            varRDFVocTable = new HashMap<>();
            succ = 1;
        }
    }

    public void cancelRDFVocVarTable() {
        rdfVocVarTable=null;
        varRDFVocTable=null;
        succ=0;
    }

    public String generateIFAbsentRDFVocVar(String classURI) throws NullPointerException {
        if (rdfVocVarTable == null || succ == 0 || varRDFVocTable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. Pls, Call the Class constructor.");
        }
        if (!(rdfVocVarTable.containsKey(classURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "rdf" + Integer.toString(succ++);
            rdfVocVarTable.put(classURI, tmp);
            varRDFVocTable.put(tmp, classURI);
            return tmp;
        } else {
            return rdfVocVarTable.get(classURI);
        }
    }
    
    //this gets the rdf term from its variable
    public String getRDFVocFromVar(String varString) throws NullPointerException {
        if ( varRDFVocTable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(varRDFVocTable.containsKey(varString))) {
            return null;
        } else {
            return varRDFVocTable.get(varString);
        }
    }

    public Map<String, String> getRdfVocVarTable() {
        return rdfVocVarTable;
    }

    public Map<String, String> getVarRDFVocTable() {
        return varRDFVocTable;
    }

    
    

}
