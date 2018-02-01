/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author callocca
 */
public class AbstractedRDFDataset implements IRDFDataset {

    private Object datasetPath; // set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.

    private final List<String> classSet;// =new ArrayList();
    private final List<String> objectPropertySet;// =new ArrayList();;
    private final List<String> datatypePropertySet;// =new ArrayList();;
    private final List<String> literalSet;// =new ArrayList();;
    private final List<String> individualSet;// =new ArrayList();;
    private final List<String> rdfVocabulary;// =new ArrayList();;
    private List<String> propertySet = new ArrayList();

    public AbstractedRDFDataset(ArrayList<String> cSet,
                                ArrayList<String> iSet,
                                ArrayList<String> opSet,
                                ArrayList<String> dpSet,
                                ArrayList<String> lSet,
                                ArrayList<String> rdfVoc) {

        if (cSet == null) {
            classSet = new ArrayList<>();// cSet;
        } else {
            classSet = cSet;
        }

        if (opSet == null) {
            objectPropertySet = new ArrayList<>();// cSet;
        } else {
            objectPropertySet = opSet;
        }
        if (dpSet == null) {
            datatypePropertySet = new ArrayList<>();// cSet;
        } else {
            datatypePropertySet = dpSet;
        }

        if (lSet == null) {
            literalSet = new ArrayList<>();// cSet;
        } else {
            literalSet = lSet;
        }

        if (iSet == null) {
            individualSet = new ArrayList<>();// cSet;
        } else {
            individualSet = iSet;
        }
        if (rdfVoc == null) {
            rdfVocabulary = new ArrayList<>();// cSet;
        } else {
            rdfVocabulary = rdfVoc;
        }
        // Building the set of the all properties.
        if (propertySet != null) {
            if (this.objectPropertySet != null) {
                propertySet.addAll(this.objectPropertySet);
            }
            if (this.datatypePropertySet != null) {
                propertySet.addAll(this.datatypePropertySet);
            }
        }
    }

    public AbstractedRDFDataset(String dPath,
                                ArrayList<String> cSet,
                                ArrayList<String> iSet,
                                ArrayList<String> opSet,
                                ArrayList<String> dpSet,
                                ArrayList<String> lSet,
                                ArrayList<String> rdfVoc) {

        this.datasetPath = dPath;

        if (cSet == null) {
            classSet = new ArrayList<>();// cSet;
        } else {
            classSet = cSet;
        }

        if (opSet == null) {
            objectPropertySet = new ArrayList<>();// cSet;
        } else {
            objectPropertySet = opSet;
        }
        if (dpSet == null) {
            datatypePropertySet = new ArrayList<>();// cSet;
        } else {
            datatypePropertySet = dpSet;
        }

        if (lSet == null) {
            literalSet = new ArrayList<>();// cSet;
        } else {
            literalSet = lSet;
        }

        if (iSet == null) {
            individualSet = new ArrayList<>();// cSet;
        } else {
            individualSet = iSet;
        }
        if (rdfVoc == null) {
            rdfVocabulary = new ArrayList<>();// cSet;
        } else {
            rdfVocabulary = rdfVoc;
        }
        // Building the set of the all properties.
        if (propertySet != null) {
            if (this.objectPropertySet != null) {
                propertySet.addAll(this.objectPropertySet);
            }
            if (this.datatypePropertySet != null) {
                propertySet.addAll(this.datatypePropertySet);
            }
        }
    }

    @Override
    public List<String> getIndividualSet() {
        return individualSet;
    }

    @Override
    public List<String> getDatatypePropertySet() {
        return datatypePropertySet;
    }

    @Override
    public List<String> getClassSet() {
        return classSet;
    }

    @Override
    public List<String> getObjectPropertySet() {
        return objectPropertySet;
    }

    @Override
    public List<String> getLiteralSet() {
        return literalSet;
    }

    @Override
    public Object runSelectQuery() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public Object runAskQuery() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public boolean isInClassSet(String classUri) {
        return classSet.contains(classUri);
    }

    @Override
    public boolean isInIndividualSet(String indUri) {
        return individualSet.contains(indUri);
    }

    @Override
    public boolean isInObjectPropertySet(String opUri) {
        return objectPropertySet.contains(opUri);
    }

    @Override
    public boolean isInDatatypePropertySet(String dpUri) {
        return datatypePropertySet.contains(dpUri);
    }

    @Override
    public boolean isInLiteralSet(String lit) {
        return literalSet.contains(lit);
    }

    @Override
    public List<String> getRDFVocabulary() {
        return this.rdfVocabulary;
    }

    @Override
    public boolean isInRDFVocabulary(String rdfEntity) {
        return rdfVocabulary.contains(rdfEntity);
    }

    @Override
    public List<String> getPropertySet() {
        return this.propertySet;
    }

    @Override
    public boolean isInPropertySet(String propertyUri) {
        return propertySet.contains(propertyUri);
    }

    @Override
    public Object getEndPointURL() {
        return this.datasetPath;// throw new UnsupportedOperationException("Not supported yet."); //To change
                                // body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPath(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public Object getGraph() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setGraph(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setIndividualSet(List<String> indSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setDatatypePropertySet(List<String> dpSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setClassSet(List<String> classSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setObjectPropertySet(List<String> opSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setLiteralSet(List<String> litSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setRDFVocabulary(List<String> rdfSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void setPropertySet(List<String> propSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public boolean isIndexed() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computeClassSet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computeIndividualSet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computeDataTypePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computeLiteralSet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computeRDFVocabularySet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void computeObjectPropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                       // choose Tools | Templates.
    }

}
