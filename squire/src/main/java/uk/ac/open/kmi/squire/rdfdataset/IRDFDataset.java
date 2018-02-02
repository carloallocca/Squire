/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.List;

/**
 *
 * @author callocca
 */
public interface IRDFDataset extends Runnable {

    public void computeClassSet();

    public void computeDataTypePropertySet();

    public void computeIndividualSet();

    public void computeLiteralSet();

    public void computeObjectPropertySet();

    public void computePropertySet();

    public void computeRDFVocabularySet();

    public List<String> getClassSet();

    public List<String> getDatatypePropertySet();

    public Object getEndPointURL();

    public Object getGraph();

    public List<String> getIndividualSet();

    public List<String> getLiteralSet();

    public List<String> getObjectPropertySet();

    public List<String> getPropertySet();

    public List<String> getRDFVocabulary();

    public boolean isInClassSet(String classUri);

    public boolean isInDatatypePropertySet(String dpUri);

    public boolean isIndexed();

    public boolean isInIndividualSet(String indUri);

    public boolean isInLiteralSet(String lit);

    public boolean isInObjectPropertySet(String opUri);

    public boolean isInPropertySet(String propertyUri);

    public boolean isInRDFVocabulary(String rdfEntity);

    public Object runAskQuery();

    public Object runSelectQuery();

    public void setClassSet(List<String> classSet);

    public void setDatatypePropertySet(List<String> dpSet);

    public void setGraph(Object path);

    public void setIndividualSet(List<String> indSet);

    public void setLiteralSet(List<String> litSet);

    public void setObjectPropertySet(List<String> opSet);

    public void setPath(Object path);

    public void setPropertySet(List<String> propSet);

    public void setRDFVocabulary(List<String> rdfSet);

}
