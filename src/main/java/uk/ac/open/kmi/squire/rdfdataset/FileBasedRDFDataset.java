/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 *
 * @author carloallocca
 */
public class FileBasedRDFDataset implements IRDFDataset {

    private Object datasetPath; //set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath. 

    private ArrayList<String> classSet = new ArrayList();
    private ArrayList<String> objectPropertySet = new ArrayList();
    private ArrayList<String> datatypePropertySet = new ArrayList();
    private ArrayList<String> literalSet = new ArrayList();
    private ArrayList<String> individualSet = new ArrayList();
    private ArrayList<String> rdfVocabulary = new ArrayList();
    private ArrayList<String> propertySet = new ArrayList();

    private OntModel inf = null;

    public FileBasedRDFDataset(String rdfDatasetFilePath) {

        try {
            this.datasetPath = rdfDatasetFilePath;
            //create a Jena memory based  model
            this.inf = ModelFactory.createOntologyModel();
            InputStream in = new FileInputStream(rdfDatasetFilePath);
            if (in == null) {
                throw new IllegalArgumentException("File: " + rdfDatasetFilePath + " not found");
            }
            //...import the content of the owl file in the Jena model. 
            inf.read(in, "");

            //...compute the set of classes
            //this.classSet = computeClassSet();
            computeClassSet();

            //...compute the set of individuals
            //this.individualSet = computeIndividualSet();
            computeIndividualSet();

            //...compute the set of object Property Set
            //this.objectPropertySet = computeObjectPropertySet();
            computeObjectPropertySet();

            //...compute the set of datatype Property Set
            //this.datatypePropertySet = computeDataTypePropertySet();
            computeDataTypePropertySet();

            //...compute the set of literals Set
            //this.literalSet = computeLiteralSet();
            computeLiteralSet();

            //...compute the set of rdf Vocabulary terms
            //this.rdfVocabulary = computeRDFVocabularySet();
            computeRDFVocabularySet();

            //...compute the set of rdf Vocabulary terms
            //this.propertySet = computePropertySetSet();
            computePropertySetSet();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileBasedRDFDataset.class.getName()).log(Level.SEVERE, null, ex);
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
    
    
    public void computeClassSet() {
        ExtendedIterator<OntClass> classesIter = this.inf.listClasses();
        for (ExtendedIterator c = classesIter; c.hasNext();) {
            OntClass classe = (OntClass) c.next();
            this.classSet.add(classe.getURI());
        }

    }

    public void computeIndividualSet() {
        ExtendedIterator<Individual> individualIter = this.inf.listIndividuals();
        for (ExtendedIterator ind = individualIter; ind.hasNext();) {
            Individual individuo = (Individual) ind.next();
            this.individualSet.add(individuo.getURI());
        }
    }

    public void computeObjectPropertySet() {
        ExtendedIterator<ObjectProperty> objPropertyIter = this.inf.listObjectProperties();
        for (ExtendedIterator objP = objPropertyIter; objP.hasNext();) {
            ObjectProperty objProperty = (ObjectProperty) objPropertyIter.next();
            this.objectPropertySet.add(objProperty.getURI());
        }
    }

    public void computeDataTypePropertySet() {
        ExtendedIterator<DatatypeProperty> datatypePropertyIter = this.inf.listDatatypeProperties();
        for (ExtendedIterator datatypeP = datatypePropertyIter; datatypeP.hasNext();) {
            DatatypeProperty datatypeProperty = (DatatypeProperty) datatypePropertyIter.next();
            this.datatypePropertySet.add(datatypeProperty.getURI());
        }
    }

    public void computeLiteralSet() {
        StmtIterator it = this.inf.listStatements();
        while (it.hasNext()) {
            Triple stmt = it.next().asTriple();
            Node subj = stmt.getSubject();
            Node obj = stmt.getObject();

            if (subj.isLiteral()) {
                this.literalSet.add(subj.getLiteralLexicalForm());

            }
            if (obj.isLiteral()) {
                this.literalSet.add(obj.getLiteralLexicalForm());

            }
        }
    }

    private void computePropertySetSet() {
        this.propertySet.addAll(this.datatypePropertySet);
        this.propertySet.addAll(this.objectPropertySet);
    }

    public void computeRDFVocabularySet() {
        StmtIterator it = this.inf.listStatements();
        while (it.hasNext()) {
            Triple stmt = it.next().asTriple();
            Node subj = stmt.getSubject();
            Node pred = stmt.getPredicate();
            Node obj = stmt.getObject();
            if(subj.toString().contains("http://www.w3.org/2002/07/owl#")||
                    subj.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")||
                    subj.toString().contains("http://www.w3.org/2000/01/rdf-schema#")||
                    subj.toString().contains("http://www.w3.org/2006/12/owl2-xml#")){
                    if(!this.rdfVocabulary.contains(subj.toString())){
                        this.rdfVocabulary.add(subj.toString());
                    }
            }            
            if(pred.toString().contains("http://www.w3.org/2002/07/owl#")||
                    pred.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")||
                    pred.toString().contains("http://www.w3.org/2000/01/rdf-schema#")||
                    pred.toString().contains("http://www.w3.org/2006/12/owl2-xml#")){
                    if(!this.rdfVocabulary.contains(pred.toString())){
                        this.rdfVocabulary.add(pred.toString());
                    }
            }
            if(obj.toString().contains("http://www.w3.org/2002/07/owl#")||
                    obj.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")||
                    obj.toString().contains("http://www.w3.org/2000/01/rdf-schema#")||
                    obj.toString().contains("http://www.w3.org/2006/12/owl2-xml#")){
                    if(!this.rdfVocabulary.contains(obj.toString())){
                        this.rdfVocabulary.add(obj.toString());
                    }
            }
        }
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
    public void computePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
