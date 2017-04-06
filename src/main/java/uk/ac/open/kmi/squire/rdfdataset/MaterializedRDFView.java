/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.ArrayList;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.lucene.document.Document;
import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;

/**
 *
 * @author FOKOU Geraud
 * 
 * This class build an IRDFDataset object from a construct SPARQL query
 */
public class MaterializedRDFView implements IRDFDataset {
    
    /**
     * Construct query which returns the graph view 
     */
    private String constructQuery ; 
    
    /**
     * Dataset from where the graph view is extracted 
     */
    private IRDFDataset dataset ;
    
    /**
     * The view model shows by the construct query and which will be used as dataset
     */
    private Model graphViewModel ;

    private ArrayList<String> classSet = new ArrayList();
    private ArrayList<String> objectPropertySet = new ArrayList();
    private ArrayList<String> datatypePropertySet = new ArrayList();
    private ArrayList<String> literalSet = new ArrayList();
    private ArrayList<String> individualSet = new ArrayList();
    private ArrayList<String> rdfVocabulary = new ArrayList();
    private ArrayList<String> propertySet = new ArrayList();

    public MaterializedRDFView(String query, IRDFDataset sourcedataset){
        
        dataset = sourcedataset ;
        constructQuery = query ;
        QueryExecution qexec = new QueryEngineHTTP((String) dataset.getPath(), constructQuery);
        graphViewModel = qexec.execConstruct();
        
        //graphViewModel = (Model) sourcedataset.runConstructQuery(constructQuery) ;
        this.setClassSet(dataset.getClassSet());
        this.setObjectPropertySet(this.getObjectPropertySet());
        this.setDatatypePropertySet(dataset.getDatatypePropertySet());
        this.setLiteralSet(dataset.getLiteralSet());
        this.setIndividualSet(dataset.getIndividualSet());
        this.setRDFVocabulary(dataset.getRDFVocabulary());
        //
        this.setPropertySet(dataset.getPropertySet());
    }

    
    @Override
    public Object getPath() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setPath(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object getGraph() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setGraph(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); 
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
    public ArrayList<String> getRDFVocabulary() {
        return this.rdfVocabulary; 
    }

    @Override
    public ArrayList<String> getPropertySet() {
        return this.propertySet; 
    }

    @Override
    public void setIndividualSet(ArrayList<String> indSet) {
        this.individualSet = indSet; 
    }

    @Override
    public void setDatatypePropertySet(ArrayList<String> dpSet) {
        this.datatypePropertySet = dpSet; 
    }

    @Override
    public void setClassSet(ArrayList<String> classSet) {
        this.classSet = classSet; 
    }

    @Override
    public void setObjectPropertySet(ArrayList<String> opSet) {
        this.objectPropertySet = opSet; 
    }

    @Override
    public void setLiteralSet(ArrayList<String> litSet) {
        this.literalSet = litSet; 
    }

    @Override
    public void setRDFVocabulary(ArrayList<String> rdfSet) {
        this.rdfVocabulary = rdfSet; 
    }

    @Override
    public void setPropertySet(ArrayList<String> propSet) {
        this.propertySet = propSet;
    }

    @Override
    public boolean isIndexed() {
        RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
        Document d = instance.getSignature(this.dataset.getPath().toString(), this.dataset.getGraph().toString());
        return d != null; 
    }

    @Override
    public void computeClassSet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void computeIndividualSet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void computeDataTypePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void computeLiteralSet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void computePropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void computeRDFVocabularySet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void computeObjectPropertySet() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isInClassSet(String classUri) {
        return classSet.contains(classUri);
    }

    @Override
    public boolean isInPropertySet(String propertyUri) {
        return propertySet.contains(propertyUri);
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
    public boolean isInRDFVocabulary(String rdfEntity) {
        return rdfVocabulary.contains(rdfEntity); 
    }

    @Override
    public Object runSelectQuery() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Object runAskQuery() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    
}
