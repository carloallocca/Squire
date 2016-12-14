/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

/**
 *
 * @author carloallocca
 */
public class VarTypeMap {
    
    
    private String varName;
    private String varType;

    public VarTypeMap(){
        super();
    }


    public VarTypeMap(String varN, String varT){
        this.varName=varN;
        this.varType=varT;
    }

    public String getVarName() {
        return varName;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }
    
    
    
    
}
