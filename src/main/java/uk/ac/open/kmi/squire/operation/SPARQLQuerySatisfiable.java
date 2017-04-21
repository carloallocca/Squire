/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.core4.AbstractQueryRecommendationObservable;
import uk.ac.open.kmi.squire.core4.IQueryRecommendationObservable;
import uk.ac.open.kmi.squire.core4.IQueryRecommendationObserver;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;

/**
 *
 * @author callocca
 */
public class SPARQLQuerySatisfiable extends AbstractQueryRecommendationObservable {


    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    public SPARQLQuerySatisfiable(String token) {
        this.token = token;
    }

    
    
    public SPARQLQuerySatisfiable() {
        super();
    }


    public boolean isSatisfiableWRTResults(Query q, IRDFDataset d) {
        String datasetPath = (String) d.getEndPointURL();
        Query qTMP = QueryFactory.create(q.toString());
        qTMP.setLimit(2);

        if (datasetPath == null || datasetPath.isEmpty()) {
            return false;
        } // TO ADD: check if it is an instance of FileBasedRDFDataset or SPARQLEndPoint
        else if (d instanceof SPARQLEndPoint) {
            List<QuerySolution> resList;
//                QueryExecution qexec = new QueryEngineHTTP((String) d.getEndPointURL(), q);
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

//    public boolean isSatisfiableWRTResultsWithToken(Query q, IRDFDataset d) throws Exception{
//        boolean result=isSatWRTResultsWithToken(q, d);
//        this.notifyQuerySatisfiableValue(result);
//        return result;
//    }

    

    public boolean isSatisfiableWRTResultsWithToken(Query q, IRDFDataset d) throws Exception{
        String datasetPath = (String) d.getEndPointURL();
        Query qTMP = QueryFactory.create(q.toString());
        qTMP.setLimit(2);
        if (datasetPath == null || datasetPath.isEmpty()) {
            this.notifyQuerySatisfiableValue(false);
            return false;
        } else if (d instanceof SPARQLEndPoint) {
            List<QuerySolution> resList;
//                QueryExecution qexec = new QueryEngineHTTP((String) d.getEndPointURL(), q);
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
    
    
    
    
    
       private boolean isSatWRTResultsWithToken(Query q, IRDFDataset d) {
        try {
            Query qTMP = QueryFactory.create(q.toString());
            qTMP.setLimit(2);
            List<String> outputVarList=qTMP.getResultVars();
            String encodedQuery = URLEncoder.encode(qTMP.toString(), "UTF-8");
            String GET_URL = d.getEndPointURL() + "?query=" + encodedQuery;
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(GET_URL);
            getRequest.addHeader("accept", "application/sparql-results+json");
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            String output;
            String result = "";
            while ((output = br.readLine()) != null) {
                result = result + output;
            }

            if(!outputVarList.isEmpty()){
                String firstVar=outputVarList.get(0);
                ArrayList<String> resList = parseSparqlResultsJson(result, firstVar);
                return resList.size()>0;   
            }
            httpClient.getConnectionManager().shutdown();                        
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
 
    private ArrayList<String> parseSparqlResultsJson(String result, String varString) {

        ArrayList<String> output = new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        JsonArray results = jsonParser.parse(result)
                .getAsJsonObject().get("results")
                .getAsJsonObject().getAsJsonArray("bindings");
        for (JsonElement result1 : results) {
            JsonObject _class = result1.getAsJsonObject().getAsJsonObject(varString);
            String value = _class.get("value").getAsString();
            try {
                URI valueURI = new URI(value);
                output.add(value);
//                System.out.println(valueURI);
            } catch (URISyntaxException ex) {
                log.error("Bad URI synax for string '{}'", value);
            }
        }
        return output;
    }

    
    
    
    
    
    
   

}
