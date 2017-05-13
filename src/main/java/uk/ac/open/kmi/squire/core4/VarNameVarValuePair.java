/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author carloallocca
 */
public class VarNameVarValuePair {
 
    private final String varName;
    private final String varValue;
    
    
    public VarNameVarValuePair(String newVarName, String newVarValue){
        this.varName=newVarName;
        this.varValue=newVarValue;
    }

    
    public String getVarName() {
        return varName;
    }

    public String getVarValue() {
        return varValue;
    }
    
    
    
    
                
}
