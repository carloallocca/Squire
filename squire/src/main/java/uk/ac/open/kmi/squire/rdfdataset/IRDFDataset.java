/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.Map;
import java.util.Set;

/**
 * TODO use generics for paths and graphs.
 *
 * @author callocca
 */
public interface IRDFDataset extends Runnable {

	public void clear();

	/**
	 * Depending on implementation it may or may not compute the signatures for the
	 * respective classes.
	 */
	public void computeClassSet();

	public void computeDataTypePropertySet() throws BootedException;

	public void computeIndividualSet();

	public void computeLiteralSet();

	public void computeObjectPropertySet() throws BootedException;

	public void computePropertySet();

	public void computeRDFVocabularySet();

	public Set<String> getClassSet();

	public Map<String, ClassSignature> getClassSignatures();

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

	public void setGraph(Object path);

	public void setPath(Object path);

}
