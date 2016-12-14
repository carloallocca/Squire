/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import org.apache.jena.sparql.core.Var;

/**
 *
 * @author carloallocca
 */
public class VarTemplateAndEntityQoQr {
    
    private Var vt;
    private String entityQo;
    private String entityQr;
    
    public VarTemplateAndEntityQoQr(){
        super();
    }
    
    public VarTemplateAndEntityQoQr(Var vt, String entityQo, String entityQr){
        this.vt=vt;
        this.entityQo=entityQo;
        this.entityQr=entityQr;
    }

    public Var getVt() {
        return vt;
    }

    public String getEntityQo() {
        return entityQo;
    }

    public String getEntityQr() {
        return entityQr;
    }

    public void setVt(Var vt) {
        this.vt = vt;
    }

    public void setEntityQo(String entityQo) {
        this.entityQo = entityQo;
    }

    public void setEntityQr(String entityQr) {
        this.entityQr = entityQr;
    }
    
    
    
    
}
