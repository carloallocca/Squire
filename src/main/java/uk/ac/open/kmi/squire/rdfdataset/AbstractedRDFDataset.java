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
public class AbstractedRDFDataset implements IRDFDataset {

    private Object datasetPath; //set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath. 
     
    private final ArrayList<String> classSet;//=new ArrayList();
    private final ArrayList<String> objectPropertySet;//=new ArrayList();;
    private final ArrayList<String> datatypePropertySet;//=new ArrayList();;
    private final ArrayList<String> literalSet;//=new ArrayList();;
    private final ArrayList<String> individualSet;//=new ArrayList();;
    private final ArrayList<String> rdfVocabulary;//=new ArrayList();;
    private ArrayList<String> propertySet = new ArrayList();

    
    
    
    
    public AbstractedRDFDataset(ArrayList<String> cSet, ArrayList<String> iSet, ArrayList<String> opSet, ArrayList<String> dpSet, ArrayList<String> lSet, ArrayList<String> rdfVoc) {

        if (cSet == null) {
            classSet = new ArrayList();//cSet;            
        } else {
            classSet = cSet;
        }

        if (opSet == null) {
            objectPropertySet = new ArrayList();//cSet;            
        } else {
            objectPropertySet = opSet;
        }
        if (dpSet == null) {
            datatypePropertySet = new ArrayList();//cSet;            
        } else {
            datatypePropertySet = dpSet;
        }

        if (lSet == null) {
            literalSet = new ArrayList();//cSet;            
        } else {
            literalSet = lSet;
        }

        if (iSet == null) {
            individualSet = new ArrayList();//cSet;            
        } else {
            individualSet = iSet;
        }
        if (rdfVoc == null) {
            rdfVocabulary = new ArrayList();//cSet;            
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
    
    public AbstractedRDFDataset(String dPath, ArrayList<String> cSet, ArrayList<String> iSet, ArrayList<String> opSet, ArrayList<String> dpSet, ArrayList<String> lSet, ArrayList<String> rdfVoc) {
        
        this.datasetPath=dPath;
        
        if (cSet == null) {
            classSet = new ArrayList();//cSet;            
        } else {
            classSet = cSet;
        }

        if (opSet == null) {
            objectPropertySet = new ArrayList();//cSet;            
        } else {
            objectPropertySet = opSet;
        }
        if (dpSet == null) {
            datatypePropertySet = new ArrayList();//cSet;            
        } else {
            datatypePropertySet = dpSet;
        }

        if (lSet == null) {
            literalSet = new ArrayList();//cSet;            
        } else {
            literalSet = lSet;
        }

        if (iSet == null) {
            individualSet = new ArrayList();//cSet;            
        } else {
            individualSet = iSet;
        }
        if (rdfVoc == null) {
            rdfVocabulary = new ArrayList();//cSet;            
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
    public ArrayList<String> getIndividualSet() {
        return individualSet;
    }

    @Override
    public ArrayList<String> getDatatypePropertySet() {
        return datatypePropertySet;
    }

    @Override
    public ArrayList<String> getClassSet() {
        return classSet;
    }

    @Override
    public ArrayList<String> getObjectPropertySet() {
        return objectPropertySet;
    }

    @Override
    public ArrayList<String> getLiteralSet() {
        return literalSet;
    }

    @Override
    public Object runSelectQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object runAskQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public ArrayList<String> getRDFVocabulary() {
        return this.rdfVocabulary;
    }

    @Override
    public boolean isInRDFVocabulary(String rdfEntity) {
        return rdfVocabulary.contains(rdfEntity);
    }

    @Override
    public ArrayList<String> getPropertySet() {
        return this.propertySet;
    }

    @Override
    public boolean isInPropertySet(String propertyUri) {
        return propertySet.contains(propertyUri);
    }

    @Override
    public Object getPath() {
        return this.datasetPath;//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPath(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getGraph() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGraph(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIndividualSet(ArrayList<String> indSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDatatypePropertySet(ArrayList<String> dpSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClassSet(ArrayList<String> classSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setObjectPropertySet(ArrayList<String> opSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLiteralSet(ArrayList<String> litSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRDFVocabulary(ArrayList<String> rdfSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPropertySet(ArrayList<String> propSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isIndexed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeClassSet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeIndividualSet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeDataTypePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeLiteralSet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeRDFVocabularySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void computeObjectPropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
