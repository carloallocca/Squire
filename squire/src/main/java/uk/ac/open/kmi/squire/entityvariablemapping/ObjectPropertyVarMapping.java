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
public class ObjectPropertyVarMapping {

    private Map<String, String> objectProperyURIVarTable = null;// = new HashMap<String, String>();
    private Map<String, String> varObjectProperyTable = null;// = new HashMap<String, String>();
    private int succ = 0;

    public ObjectPropertyVarMapping() {
        objectProperyURIVarTable = new HashMap<String, String>();
        varObjectProperyTable = new HashMap<String, String>();
        succ = 1;
    }

    public void initializeObjectPropertyURIVarTable() {
        if (objectProperyURIVarTable == null && varObjectProperyTable == null && succ == 0) {
            objectProperyURIVarTable = new HashMap<String, String>();
            varObjectProperyTable = new HashMap<String, String>();
            succ = 1;
        }
    }

    public void cancelObjectProperyVarTable() {
        objectProperyURIVarTable = null;
        varObjectProperyTable = null;
        succ = 0;
    }

    public String generateIFAbsentObjectPropertyVar(String objpropURI) throws NullPointerException {
        if (objectProperyURIVarTable == null || succ == 0) {
            throw new NullPointerException("The objectProperyVarTable needs to be created. Pls, use the class constructor first.");
        }
        if (!(objectProperyURIVarTable.containsKey(objpropURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "opt" + Integer.toString(succ++);
            objectProperyURIVarTable.put(objpropURI, tmp);
            varObjectProperyTable.put(tmp, objpropURI);
            return tmp;
        } else {
            return objectProperyURIVarTable.get(objpropURI);
        }
    }

    public String getObjectProperyFromVar(String varString) throws NullPointerException {
        if (varObjectProperyTable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(varObjectProperyTable.containsKey(varString))) {
            return null;
        } else {
            return varObjectProperyTable.get(varString);
        }
    }

    public Map<String, String> getObjectProperyVarTable() {
        return objectProperyURIVarTable;
    }

    public Map<String, String> getVarObjectProperyTable() {
        return varObjectProperyTable;
    }

}
