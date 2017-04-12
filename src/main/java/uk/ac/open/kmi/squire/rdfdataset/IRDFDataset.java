/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.ArrayList;

/**
 *
 * @author callocca
 */
public interface IRDFDataset extends  Runnable{
    
    public Object getEndPointURL();
    public void setPath(Object path);
    
    public Object getGraph();
    public void setGraph(Object path);

    
    public ArrayList<String> getIndividualSet();
    public ArrayList<String> getDatatypePropertySet();
    public ArrayList<String> getClassSet();
    public ArrayList<String> getObjectPropertySet();
    public ArrayList<String> getLiteralSet();
    public ArrayList<String> getRDFVocabulary();
    public ArrayList<String> getPropertySet();

    public void  setIndividualSet(ArrayList<String> indSet);
    public void  setDatatypePropertySet(ArrayList<String> dpSet);
    public void  setClassSet(ArrayList<String> classSet);
    public void  setObjectPropertySet(ArrayList<String> opSet);
    public void  setLiteralSet(ArrayList<String> litSet);
    public void  setRDFVocabulary(ArrayList<String> rdfSet);
    public void  setPropertySet(ArrayList<String> propSet);
    
    
    public boolean isIndexed();
    public void computeClassSet();
    public void computeIndividualSet();
    public void computeDataTypePropertySet();
    public void computeLiteralSet();
    public void computePropertySet();
    public void computeRDFVocabularySet();
    public void computeObjectPropertySet();

    
    
    
    public boolean isInClassSet(String classUri);
    public boolean isInPropertySet(String propertyUri);
    public boolean isInIndividualSet(String indUri);
    public boolean isInObjectPropertySet(String opUri);
    public boolean isInDatatypePropertySet(String dpUri);
    public boolean isInLiteralSet(String lit);
    public boolean isInRDFVocabulary(String rdfEntity);

    
    public Object runSelectQuery();
    public Object runAskQuery();
    
}
