/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.lucene.document.Document;
import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.utils.FromStringToArrayList;

/**
 *
 * @author carloallocca
 */
public class SPARQLEndPointBasedRDFDataset implements IRDFDataset {

    private Object datasetPath; //set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath. 
    private String graphName;

    private ArrayList<String> classSet = new ArrayList();
    private ArrayList<String> objectPropertySet = new ArrayList();
    private ArrayList<String> datatypePropertySet = new ArrayList();
    private ArrayList<String> literalSet = new ArrayList();
    private ArrayList<String> individualSet = new ArrayList();
    private ArrayList<String> rdfVocabulary = new ArrayList();
    private ArrayList<String> propertySet = new ArrayList();

    public SPARQLEndPointBasedRDFDataset(String urlAddress, String gName) {
        this.graphName = gName;
        this.datasetPath = urlAddress;

        //TO ADD if(the dataset is not present in the index) then compute
        RDFDatasetIndexer instance = new RDFDatasetIndexer();
        Document d = instance.getSPARQLEndPointSignature(urlAddress, graphName);
        if (d != null) {
            String cSet = d.get("ClassSet");
            this.classSet = FromStringToArrayList.transform(cSet);
            String oPropSet = d.get("ObjectPropertySet");
            this.objectPropertySet = FromStringToArrayList.transform(oPropSet);
            String dPropertySet = d.get("DatatypePropertySet");
            this.datatypePropertySet = FromStringToArrayList.transform(dPropertySet);
            String litSet = d.get("LiteralSet");
            this.literalSet = FromStringToArrayList.transform(litSet);
            String indSet = d.get("IndividualSet");
            this.individualSet = FromStringToArrayList.transform(indSet);
            String rdfVoc = d.get("RDFVocabulary");
            this.rdfVocabulary = FromStringToArrayList.transform(rdfVoc);
            String propSet = d.get("PropertySet");
            this.propertySet = FromStringToArrayList.transform(propSet);
        } else {
            System.out.println("[SPARQLEndPointBasedRDFDataset::SPARQLEndPointBasedRDFDataset, constructor] SPARQL endpoint i" + urlAddress + " is not yet indexed ");

//            computeClassSetNew();
//            computeObjectPropertySetNew();
////            computeObjectPropertySetNewNew();
//            computeDataTypePropertySetNew();

            computeClassSet();
            computeObjectPropertySet();
            computeDataTypePropertySet();
//
//            computePropertySet();
//
//            computeIndividualSet();
//            computeLiteralSet();
            computeRDFVocabularySet();
            // index the RDF Dataset 
            instance.addSPARQLEndPointSignature(urlAddress, graphName, classSet, objectPropertySet, datatypePropertySet, individualSet, literalSet, rdfVocabulary, propertySet);
        }

    }

    public void setDatasetPath(Object datasetPath) {
        this.datasetPath = datasetPath;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setClassSet(ArrayList<String> classSet) {
        this.classSet = classSet;
    }

    public void setObjectPropertySet(ArrayList<String> objectPropertySet) {
        this.objectPropertySet = objectPropertySet;
    }

    public void setDatatypePropertySet(ArrayList<String> datatypePropertySet) {
        this.datatypePropertySet = datatypePropertySet;
    }

    public void setLiteralSet(ArrayList<String> literalSet) {
        this.literalSet = literalSet;
    }

    public void setIndividualSet(ArrayList<String> individualSet) {
        this.individualSet = individualSet;
    }
    public void setRdfVocabulary(ArrayList<String> rdfVocabulary) {
        this.rdfVocabulary = rdfVocabulary;
    }
@Override
    public void setPropertySet(ArrayList<String> propertySet) {
        this.propertySet = propertySet;
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
    
    
    private void computeClassSetNew() {
//        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                + "prefix owl:<http://www.w3.org/2002/07/owl#> "
//                + " SELECT DISTINCT ?class where "
//                + "{ "
//                + " {?class rdf:type owl:Class .} "
//                + " UNION "
//                + " {?class rdf:type rdfs:Class .} " /
//                + "} LIMIT 10";
//        
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        while (resultset) {

            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                    + " SELECT DISTINCT ?class where "
                    + "{ "
                    + " ?ind rdf:type ?class . "
                    + "} ORDER BY ?class limit 100 OFFSET " + Integer.toString(offset);

            //           QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
            QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

////             this is for http://sparql.bioontology.org/sparql but connection refused after 8000 classes...           
//           Query query = QueryFactory.create(qString) ;
//           QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.datasetPath, query);
//           qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    if (sol.get("class").asResource().getURI() != null) {
                        qSol.add(sol.get("class").asResource().getURI());
                    }
                }
                System.out.println("[SPARQLEndPointBasedRDFDataset:computeClassSetNew] classSet cardinality so far: " + qSol.size());
                offset = offset + 100;
            } else {
                resultset = false;
            }
        }
        // Convert the set of class into Arraylist List<String> list = new ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPointBasedRDFDataset:computeClassSetNew] classSet cardinality " + qSol.size());
        this.classSet = new ArrayList<String>(qSol);
//        for (QuerySolution sol : solList) {
//            if (sol.get("class").asResource().getURI() != null) {
//                this.classSet.add(sol.get("class").asResource().getURI());
//            }
//        }

    }

    
    
    private void computeClassSet() {

//        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                + "prefix owl:<http://www.w3.org/2002/07/owl#> "
//                + " SELECT DISTINCT ?class where "
//                + "{ "
//                + " {?class rdf:type owl:Class .} "
//                + " UNION "
//                + " {?class rdf:type rdfs:Class .} " 
//                + "} LIMIT 10";
//        
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                + " SELECT DISTINCT ?class where "
                + "{ "
                + " ?ind a ?class . "
                + "}";

//QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

        ResultSet results = qexec.execSelect();
        List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);

        for (QuerySolution sol : solList) {
            if (sol.get("class").asResource().getURI() != null) {
                this.classSet.add(sol.get("class").asResource().getURI());
            }
        }
        System.out.println("[SPARQLEndPointBasedRDFDataset:computeClassSet] classSet cardinality " + this.classSet.size());

    }

    private void computeIndividualSet() {
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                + " SELECT DISTINCT ?indiv where "
                + "{ "
                + " ?indiv rdf:type ?class . "
                + " {?class rdf:type owl:Class .} "
                + " UNION "
                + " {?class rdf:type rdfs:Class .} "
                + "} LIMIT 10";
        //QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

        ResultSet results = qexec.execSelect();
        List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
        for (QuerySolution sol : solList) {
            if (sol.get("indiv").asResource().getURI() != null) {
                this.individualSet.add(sol.get("indiv").asResource().getURI());
            }
        }
    }

    private void computeObjectPropertySet() {
                    String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?op where "
                    + "{ "
                    + " ?s ?op ?o . "
                    + " FILTER (isURI(?o)) "
                    + "}";

        //QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

        ResultSet results = qexec.execSelect();
        List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
        for (QuerySolution sol : solList) {
            if (sol.get("op").asResource().getURI() != null) {
                this.objectPropertySet.add(sol.get("op").asResource().getURI());
            }
        }
        // Convert the set of class into Arraylist List<String> list = new ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPointBasedRDFDataset:computeObjectPropertySetNew] ObjectProperty cardinality " + this.objectPropertySet.size());
        
    }

    private void computeObjectPropertySetNew() {
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        while (resultset) {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?op where "
                    + "{ "
                    + " ?s ?op ?o . "
                    + " FILTER (isURI(?o)) "
                    + "} ORDER BY ?op limit 50 OFFSET " + Integer.toString(offset);

//          QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
            QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

//            //             this is for http://sparql.bioontology.org/sparql but connection refused after 8000 classes...           
//           Query query = QueryFactory.create(qString) ;
//           QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.datasetPath, query);           
//           qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    if (sol.get("op").asResource().getURI() != null) {
                        qSol.add(sol.get("op").asResource().getURI());
                    }
                }
                System.out.println("[SPARQLEndPointBasedRDFDataset:computeObjectPropertySetNew] objectPropertySet cardinality so far: " + qSol.size());
                offset = offset + 50;
            } else {
                resultset = false;
            }
        }
        // Convert the set of class into Arraylist List<String> list = new ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPointBasedRDFDataset:computeObjectPropertySetNew] ObjectProperty cardinality " + qSol.size());
        this.objectPropertySet = new ArrayList<String>(qSol);

    }

    private void computeObjectPropertySetNewNew() {
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        QueryEngineHTTP qexec = null;
        while (resultset) {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?op where "
                    + "{ "
                    + " ?s ?op ?o . "
                    + " FILTER (isURI(?o)) "
                    + "} ORDER BY ?op limit 50 OFFSET " + Integer.toString(offset);

//          QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
//            QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);
            qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

//            //             this is for http://sparql.bioontology.org/sparql but connection refused after 8000 classes...           
//           Query query = QueryFactory.create(qString) ;
//           QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.datasetPath, query);           
//           qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    if (sol.get("op").asResource().getURI() != null) {
                        qSol.add(sol.get("op").asResource().getURI());
                    }
                }
                System.out.println("[SPARQLEndPointBasedRDFDataset:computeObjectPropertySetNew] objectPropertySet cardinality so far: " + qSol.size());
                offset = offset + 50;
            } else {
                resultset = false;
            }
        }
        qexec.close();
        // Convert the set of class into Arraylist List<String> list = new ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPointBasedRDFDataset:computeObjectPropertySetNew] ObjectProperty cardinality " + qSol.size());
        this.objectPropertySet = new ArrayList<String>(qSol);

    }

    private void computeDataTypePropertySet() {

//        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                + " SELECT DISTINCT ?dtProp where "
//                + "{ "
//                + " {?dtProp rdf:type <http://www.w3.org/2002/07/owl#DatatypeProperty> .} "
//                + " UNION "
//                + " {?s ?dtProp ?o . FILTER (isLiteral(?o) &&  !contains(str(?p),\'http://www.w3.org/2000/01/rdf-schema#\'))} " 
//                + "} LIMIT 10";
//                String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                + " SELECT DISTINCT ?p where "
//                + "{ "
//                + " ?s ?p ?o . "
//                        + "FILTER (isLiteral(?o) &&  "
//                        + "!contains(str(?p),\'http://www.w3.org/2000/01/rdf-schema#\') && " 
//                        + "!contains(str(?p),\'http://www.w3.org/1999/02/22-rdf-syntax-ns#\') && "
//                        + "!contains(str(?p),\'http://www.w3.org/2002/07/owl#\')) "
//                + "} LIMIT 10";
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + " SELECT DISTINCT ?p  where "
                + "{ "
                + " ?s ?p ?o . "
                + " FILTER (isLiteral(?o)) "
                + "}";

        //QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

        ResultSet results = qexec.execSelect();

        List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
        for (QuerySolution sol : solList) {
            if (sol.get("p").asResource().getURI() != null) {
                this.datatypePropertySet.add(sol.get("p").asResource().getURI());
            }
        }
        System.out.println("[SPARQLEndPointBasedRDFDataset:datatypePropertySet] datatypePropertySet cardinality " + this.datatypePropertySet.size());        
    }

    private void computeDataTypePropertySetNew() {
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        while (resultset) {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?dp  where "
                    + "{ "
                    + " ?s ?dp ?o . "
                    + " FILTER (isLiteral(?o)) "
                    + "} ORDER BY ?dp limit 50 OFFSET " + Integer.toString(offset);
//            QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
            QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

//            //             this is for http://sparql.bioontology.org/sparql but connection refused after 8000 classes...           
//           Query query = QueryFactory.create(qString) ;
//           QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.datasetPath, query);
//           qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    String dpString = sol.get("dp").asResource().getURI();
                    if (dpString != null) {
                        qSol.add(dpString);
                    }
                }
                System.out.println("[SPARQLEndPointBasedRDFDataset:datatypePropertySet] datatypePropertySet cardinality so far: " + qSol.size());
                offset = offset + 50;
            } else {
                resultset = false;
            }
        }
        // Convert the set of class into Arraylist List<String> list = new ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPointBasedRDFDataset:datatypePropertySet] datatypePropertySet cardinality " + qSol.size());
        this.datatypePropertySet = new ArrayList<String>(qSol);
    }

    private void computeLiteralSet() {

        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + " SELECT DISTINCT ?lit where "
                + "{ "
                + " ?s ?p ?lit . "
                + " FILTER (isLiteral(?lit) ) "
                + "} LIMIT 10";

//            QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.datasetPath, qString, this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.datasetPath, qString);

        ResultSet results = qexec.execSelect();
        List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
        for (QuerySolution sol : solList) {
            if (sol.get("lit").asLiteral().getValue().toString() != null) {
                this.literalSet.add(sol.get("lit").asLiteral().getValue().toString());
            }
        }
    }

    private void computePropertySet() {
        this.propertySet.addAll(this.datatypePropertySet);
        this.propertySet.addAll(this.objectPropertySet);
    }

    private void computeRDFVocabularySet() {
        //RDF
        this.rdfVocabulary.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        //RDFS
        this.rdfVocabulary.add("http://www.w3.org/2000/01/rdf-schema#Class");
        this.rdfVocabulary.add("http://www.w3.org/2000/01/rdf-schema#Literal");
        this.rdfVocabulary.add("http://www.w3.org/2000/01/rdf-schema#Resource");
        //OWL
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#Class ");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#NamedIndividual");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#ObjectProperty");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#sameAs");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#DatatypeProperty ");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#DataRange ");

    }

    @Override
    public Object getGraph() {
        return this.graphName;
    }

    @Override
    public void setGraph(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRDFVocabulary(ArrayList<String> rdfSet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
