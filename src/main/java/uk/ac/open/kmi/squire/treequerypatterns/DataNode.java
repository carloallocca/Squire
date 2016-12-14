/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;
import org.apache.jena.query.Query;

/**
 *
 * @author carloallocca
 */
public class DataNode {
    Query qO;
    Query qR;
    String entityqO;
    String entityqR;
    ArrayList<String> operationList;//=new ArrayList();
    String op; //It can be either R (for Removal) or I (Instanciation). 
    float nodeCost;


  public DataNode(Query qO, Query qR, String entityqO, String entityqR, ArrayList<String> operationList,String op, float nodeCost){
    this.qO= qO; 
    this.qR=qR;
    this.entityqO=entityqO;
    this.entityqR=entityqR;
    this.operationList= operationList;
    this.op=op;
    this.nodeCost=nodeCost;
  }

  
    public void setqO(Query qO) {
        this.qO = qO;
    }

    public void setqR(Query qR) {
        this.qR = qR;
    }

    public void setEntityqO(String entityqO) {
        this.entityqO = entityqO;
    }

    public void setEntityqR(String entityqR) {
        this.entityqR = entityqR;
    }

    public void setOperationList(ArrayList<String> operationList) {
        this.operationList = operationList;
    }

    public void setNodeCost(float nodeCost) {
        this.nodeCost = nodeCost;
    }
  
  
  
  

    public Query getqO() {
        return qO;
    }

    public Query getqR() {
        return qR;
    }

    public String getEntityqO() {
        return entityqO;
    }

    public String getEntityqR() {
        return entityqR;
    }

    public ArrayList<String> getOperationList() {
        return operationList;
    }

    public float getNodeCost() {
        return nodeCost;
    }

  
  
  
    
}


 