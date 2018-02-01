package uk.ac.open.kmi.squire.rdfdataset;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public abstract class AbstractRdfDataset implements IRDFDataset {

    protected List<String> classSet = new ArrayList<>();
    protected List<String> datatypePropertySet = new ArrayList<>();
    protected List<String> individualSet = new ArrayList<>();
    protected List<String> literalSet = new ArrayList<>();
    protected List<String> objectPropertySet = new ArrayList<>();
    protected List<String> propertySet = new ArrayList<>();
    protected List<String> rdfVocabulary = new ArrayList<>();

    @Override
    public List<String> getClassSet() {
        return classSet;
    }

    @Override
    public List<String> getDatatypePropertySet() {
        return datatypePropertySet;
    }

    @Override
    public List<String> getIndividualSet() {
        return individualSet;
    }

    @Override
    public List<String> getLiteralSet() {
        return literalSet;
    }

    @Override
    public List<String> getObjectPropertySet() {
        return objectPropertySet;
    }

    @Override
    public List<String> getPropertySet() {
        return propertySet;
    }

    @Override
    public List<String> getRDFVocabulary() {
        return rdfVocabulary;
    }

    @Override
    public boolean isInClassSet(String classUri) {
        return classSet.contains(classUri);
    }

    @Override
    public boolean isInDatatypePropertySet(String dpUri) {
        return datatypePropertySet.contains(dpUri);
    }

    @Override
    public boolean isInIndividualSet(String indUri) {
        return individualSet.contains(indUri);
    }

    @Override
    public boolean isInLiteralSet(String lit) {
        return literalSet.contains(lit);
    }

    @Override
    public boolean isInObjectPropertySet(String opUri) {
        return objectPropertySet.contains(opUri);
    }

    @Override
    public boolean isInPropertySet(String propertyUri) {
        return propertySet.contains(propertyUri);
    }

    @Override
    public boolean isInRDFVocabulary(String rdfEntity) {
        return rdfVocabulary.contains(rdfEntity);
    }

    @Override
    public void setClassSet(List<String> classSet) {
        this.classSet = classSet;
    }

    @Override
    public void setDatatypePropertySet(List<String> datatypePropertySet) {
        this.datatypePropertySet = datatypePropertySet;
    }

    @Override
    public void setIndividualSet(List<String> individualSet) {
        this.individualSet = individualSet;
    }

    @Override
    public void setLiteralSet(List<String> literalSet) {
        this.literalSet = literalSet;
    }

    @Override
    public void setObjectPropertySet(List<String> objectPropertySet) {
        this.objectPropertySet = objectPropertySet;
    }

    @Override
    public void setPropertySet(List<String> propertySet) {
        this.propertySet = propertySet;
    }

    @Override
    public void setRDFVocabulary(List<String> rdfSet) {
        this.rdfVocabulary = rdfSet;
    }

}
