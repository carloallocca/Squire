/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.Set;

/**
 *
 * @author callocca
 */
public interface IRDFDataset extends Runnable {

	public void clear();

	public void computeClassSet();

	public void computeDataTypePropertySet() throws BootedException;

	public void computeIndividualSet();

	public void computeLiteralSet();

	public void computeObjectPropertySet() throws BootedException;

	public void computePropertySet();

	public void computeRDFVocabularySet();

	public Set<String> getClassSet();

	public Set<String> getDatatypePropertySet();

	public Object getEndPointURL();

	public Object getGraph();

	public Set<String> getIndividualSet();

	public Set<String> getLiteralSet();

	public Set<String> getObjectPropertySet();

	public Set<String> getPropertySet();

	public Set<String> getRDFVocabulary();

	public boolean isInClassSet(String classUri);

	public boolean isInDatatypePropertySet(String dpUri);

	public boolean isIndexed();

	public boolean isInIndividualSet(String indUri);

	public boolean isInLiteralSet(String lit);

	public boolean isInObjectPropertySet(String opUri);

	public boolean isInPropertySet(String propertyUri);

	public boolean isInRDFVocabulary(String rdfEntity);

	public void setClassSet(Set<String> classSet);

	public void setDatatypePropertySet(Set<String> dpSet);

	public void setGraph(Object path);

	public void setIndividualSet(Set<String> indSet);

	public void setLiteralSet(Set<String> litSet);

	public void setObjectPropertySet(Set<String> opSet);

	public void setPath(Object path);

	public void setRDFVocabulary(Set<String> rdfSet);

}
