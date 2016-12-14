/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.syntax.ElementWalker;
import uk.ac.open.kmi.squire.core4.AbstractQueryRecommendationObservable;
import uk.ac.open.kmi.squire.core4.IQueryRecommendationObservable;
import uk.ac.open.kmi.squire.core4.IQueryRecommendationObserver;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPointBasedRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;

/**
 *
 * @author callocca
 */
public class SPARQLQuerySatisfiable extends AbstractQueryRecommendationObservable {



    public SPARQLQuerySatisfiable(String token) {
        this.token = token;
    }

    
    
    public SPARQLQuerySatisfiable() {
        super();
    }


    public boolean isSatisfiableWRTResults(Query q, IRDFDataset d) {
        String datasetPath = (String) d.getPath();
        Query qTMP = QueryFactory.create(q.toString());
        qTMP.setLimit(2);

//        System.out.println("[SPARQLQuerySatisfiable::isSatisfiableWRTResults]"+datasetPath);
        if (datasetPath == null || datasetPath.isEmpty()) {
            //return false; //it should return false
            return false;
        } // TO ADD: check if it is an instance of FileBasedRDFDataset or SPARQLEndPointBasedRDFDataset
        else if (d instanceof SPARQLEndPointBasedRDFDataset) {
            List<QuerySolution> resList;
//                QueryExecution qexec = new QueryEngineHTTP((String) d.getPath(), q);
//                ResultSet results = qexec.execSelect();
//                resList = ResultSetFormatter.toList(results); //.out(, results, q);
//                return resList.size() >= 1;

            try {
                QueryExecution qexec = QueryExecutionFactory.sparqlService(datasetPath, qTMP, (String) d.getGraph());
                ResultSet results = qexec.execSelect();
                resList = ResultSetFormatter.toList(results); //.out(, results, q);
                return resList.size() >= 1;
            } catch (Exception ex) {
                Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            // return resList.size() >= 1;
        } else {

            //InputStream in=null ;
            try {

                OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);// m = ModelFactory.createOntologyModel();
                InputStream in = new FileInputStream(datasetPath);
                if (in == null) {
                    throw new IllegalArgumentException("File: " + datasetPath + " not found");
                }
                //...import the content of the owl file in the Jena model. 
                m.read(in, "");

                //...querying ...
                QueryExecution qexec = QueryExecutionFactory.create(qTMP, m);
                if (qTMP.isSelectType()) {
                    ResultSet results = qexec.execSelect();

                    // Output query results	
                    List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
                    qexec.close();
                    return resList.size() >= 1;
                }
                return false;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
//                try {
//                    in.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
        return false;
    }

    public boolean isSatisfiableWRTResultsWithToken(Query q, IRDFDataset d) throws Exception{
        String datasetPath = (String) d.getPath();
        Query qTMP = QueryFactory.create(q.toString());
        qTMP.setLimit(2);
        if (datasetPath == null || datasetPath.isEmpty()) {
            this.notifyQuerySatisfiableValue(false);
            return false;
        } else if (d instanceof SPARQLEndPointBasedRDFDataset) {
            List<QuerySolution> resList;
//                QueryExecution qexec = new QueryEngineHTTP((String) d.getPath(), q);
//                ResultSet results = qexec.execSelect();
//                resList = ResultSetFormatter.toList(results); //.out(, results, q);
//                return resList.size() >= 1;               
            try {
                QueryExecution qexec = QueryExecutionFactory.sparqlService(datasetPath, qTMP, (String) d.getGraph());
                
                ResultSet results = qexec.execSelect();
                
//                System.err.println("[SPARQLQuerySatisfiable::isSatisfiableWRTResultsWithToken]Query Returned.");        
                        
                resList = ResultSetFormatter.toList(results); //.out(, results, q);
                if (resList.size() >= 1) {
                    this.notifyQuerySatisfiableValue(true);
                    return true;
                } else {
                    this.notifyQuerySatisfiableValue(false);
                    return false;
                }
            } catch (Exception ex) {
                System.err.println("[SPARQLQuerySatisfiable::isSatisfiableWRTResultsWithToken]" +ex.getMessage());       
                Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
                this.notifyQuerySatisfiableValue(false);
                throw ex;
//                return false;
            }
        } else {
            try {
                OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);// m = ModelFactory.createOntologyModel();
                InputStream in = new FileInputStream(datasetPath);
                if (in == null) {
                    throw new IllegalArgumentException("File: " + datasetPath + " not found");
                }
                //...import the content of the owl file in the Jena model. 
                m.read(in, "");
                //...querying ...
                QueryExecution qexec = QueryExecutionFactory.create(qTMP, m);
                if (qTMP.isSelectType()) {
                    ResultSet results = qexec.execSelect();
                    // Output query results	
                    List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
                    qexec.close();
                    return resList.size() >= 1;
                }
                return false;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
//                try {
//                    in.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
        return false;
    }

    public boolean isSatisfiableWRTProjectVar(Query qRec) {

        List<String> qOvarList = computeQueryVariableSet(qRec);
//        System.out.println("[SPARQLQuerySatisfiable::isSatisfiableWRTProjectVar] 1 " + qOvarList.toString());

        List<Var> projectVarList = qRec.getProjectVars();
        List<String> projectVarListString = new ArrayList();

        for (Var varProj : projectVarList) {
            projectVarListString.add(varProj.getVarName());
        }
//        System.out.println("[SPARQLQuerySatisfiable::isSatisfiableWRTProjectVar] 2 " + projectVarListString.toString());
        return qOvarList.containsAll(projectVarListString);
    }

    private List<String> computeQueryVariableSet(Query qO) {
        SQVariableVisitor v = new SQVariableVisitor();
        //... This will walk through all parts of the query
        ElementWalker.walk(qO.getQueryPattern(), v);
//        System.out.println("[QuerySpecificityDistance::computeQueryVariableSet] v.getQueryClassSet()  " + v.getQueryClassSet().toString());
        return v.getQueryVariableSet();
    }
    
    
    
    
    
    
        
    
    
    
    
    
    
    
    
    
    

    //    public boolean isSatisfiableWRTResults(Query q, IRDFDataset d) {
//        String datasetPath = (String) d.getPath();
////        System.out.println("[SPARQLQuerySatisfiable::isSatisfiableWRTResults]"+datasetPath);
//        if (datasetPath == null || datasetPath.isEmpty()) {
//            //return false; //it should return false
//            return false;
//        } // TO ADD: check if it is an instance of FileBasedRDFDataset or SPARQLEndPointBasedRDFDataset
//        else if (datasetPath.startsWith("http://") || datasetPath.startsWith("https://")) {
//            //manage the case of SPARQL endpoint
////            String ontology_service = "http://ambit.uni-plovdiv.bg:8080/ontology";
////            String endpoint = "otee:Endpoints";
////            String endpointsSparql
////                    = "PREFIX ot:<http://www.opentox.org/api/1.1#>\n"
////                    + "	PREFIX ota:<http://www.opentox.org/algorithms.owl#>\n"
////                    + "	PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
////                    + "	PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"
////                    + "	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
////                    + "	PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
////                    + "	PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"
////                    + "		select ?url ?title\n"
////                    + "		where {\n"
////                    + "		?url rdfs:subClassOf %s.\n"
////                    + "		?url dc:title ?title.\n"
////                    + "		}\n";
////
////            QueryExecution x = QueryExecutionFactory.sparqlService(ontology_service, String.format(endpointsSparql, endpoint));
////            ResultSet results = x.execSelect();
////            ResultSetFormatter.out(System.out, results);
//
//            return true;
//        } else {
//
//            //InputStream in=null ;
//            try {
//
//                OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);// m = ModelFactory.createOntologyModel();
//                InputStream in = new FileInputStream(datasetPath);
//                if (in == null) {
//                    throw new IllegalArgumentException("File: " + datasetPath + " not found");
//                }
//                //...import the content of the owl file in the Jena model. 
//                m.read(in, "");
//
//                //...querying ...
//                QueryExecution qexec = QueryExecutionFactory.create(q, m);
//                if (q.isSelectType()) {
//                    ResultSet results = qexec.execSelect();
//
//                    // Output query results	
//                    List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
//                    qexec.close();
//                    return resList.size() >= 1;
//                }
//                return false;
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
//            } finally {
////                try {
////                    in.close();
////                } catch (IOException ex) {
////                    Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
////                }
//            }
//        }
//        return false;
//    }


}
