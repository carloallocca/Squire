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
public class ClassVarMapping {

    private  Map<String, String> classURIVarTable=null;// = new HashMap<String, String>();
    private  Map<String, String> varClassURITable=null;// = new HashMap<String, String>();
    
    private int succ=0;
    
    public ClassVarMapping(){
            classURIVarTable = new HashMap<>();
            varClassURITable = new HashMap<>();
            succ = 1;
        
    }

    public void initializeClassVarTable() {
        if(classURIVarTable==null && varClassURITable==null && succ==0){
            classURIVarTable = new HashMap<>();
            varClassURITable = new HashMap<>();
            succ = 1;
        }
    }

    public void cancelClassVarTable() {
        classURIVarTable=null;
        varClassURITable=null;
        succ=0;
    }

    public String generateIFAbsentClassVar(String classURI) throws NullPointerException {
        if (classURIVarTable == null || succ == 0 || varClassURITable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. Pls, Call the Class constructor.");
        }
        if (!(classURIVarTable.containsKey(classURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "ct" + Integer.toString(succ++);
            classURIVarTable.put(classURI, tmp);
            varClassURITable.put(tmp, classURI);
            
            return tmp;
        } else {
            return classURIVarTable.get(classURI);
        }
    }
    
    
    public String getClassFromVar(String varString) throws NullPointerException {
        if ( varClassURITable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(varClassURITable.containsKey(varString))) {
            return null;
        } else {
            return varClassURITable.get(varString);
        }
    }

    public Map<String, String> getClassVarTable() {
        return classURIVarTable;
    }

    public  Map<String, String> getVarClassTable() {
        return varClassURITable;
    }
    
    

}
