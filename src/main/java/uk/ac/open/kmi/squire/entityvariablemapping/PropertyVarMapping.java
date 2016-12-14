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
public class PropertyVarMapping {
    

    private Map<String, String> properyURIVarTable = null;// = new HashMap<String, String>();
    private Map<String, String> varProperyTable = null;// = new HashMap<String, String>();
    private int succ = 0;

    public PropertyVarMapping() {
        properyURIVarTable = new HashMap<String, String>();
        varProperyTable = new HashMap<String, String>();
        succ = 1;
    }

    public void putALL(HashMap<String, String> properyURIVarTable, HashMap<String, String> varProperyTable ){
        this.properyURIVarTable.putAll(properyURIVarTable);
        this.varProperyTable.putAll(varProperyTable);
    }
    
    
    public void initializeObjectPropertyURIVarTable() {
        if (properyURIVarTable == null && varProperyTable == null && succ == 0) {
            properyURIVarTable = new HashMap<String, String>();
            varProperyTable = new HashMap<String, String>();
            succ = 1;
        }
    }


    public String generateIFAbsentObjectPropertyVar(String objpropURI) throws NullPointerException {
        if (properyURIVarTable == null || succ == 0) {
            throw new NullPointerException("The objectProperyVarTable needs to be created. Pls, use the class constructor first.");
        }
        if (!(properyURIVarTable.containsKey(objpropURI))) {
            //this.classVar = "ct"+Integer.toString(++succ);
            String tmp = "pt" + Integer.toString(succ++);
            properyURIVarTable.put(objpropURI, tmp);
            varProperyTable.put(tmp, objpropURI);
            return tmp;
        } else {
            return properyURIVarTable.get(objpropURI);
        }
    }

//        public String instanciateIFAbsentPropertyTemplateVar(String varTemplatePropURI) throws NullPointerException {
//        if (properyURIVarTable == null || succ == 0) {
//            throw new NullPointerException("The objectProperyVarTable needs to be created. Pls, use the class constructor first.");
//        }
//        if (!(properyURIVarTable.containsKey(objpropURI))) {
//            //this.classVar = "ct"+Integer.toString(++succ);
//            String tmp = "pt" + Integer.toString(succ++);
//            properyURIVarTable.put(objpropURI, tmp);
//            varProperyTable.put(tmp, objpropURI);
//            return tmp;
//        } else {
//            return properyURIVarTable.get(objpropURI);
//        }
//    }

    
    public String getObjectProperyFromVar(String varString) throws NullPointerException {
        if (varProperyTable == null) {
            throw new NullPointerException("The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
        }
        if (!(varProperyTable.containsKey(varString))) {
            return null;
        } else {
            return varProperyTable.get(varString);
        }
    }

    public Map<String, String> getObjectProperyVarTable() {
        return properyURIVarTable;
    }

    public Map<String, String> getVarObjectProperyTable() {
        return varProperyTable;
    }

}
