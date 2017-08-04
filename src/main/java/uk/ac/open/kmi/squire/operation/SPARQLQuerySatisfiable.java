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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.core4.AbstractQueryRecommendationObservable;
import uk.ac.open.kmi.squire.core4.IQueryRecommendationObservable;
import uk.ac.open.kmi.squire.core4.IQueryRecommendationObserver;
import uk.ac.open.kmi.squire.core4.VarNameVarValuePair;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
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

    public boolean isSatisfiable(Query q, IRDFDataset d) {
        String datasetPath = (String) d.getEndPointURL();
        Query qTMP = QueryFactory.create(q.toString());
        qTMP.setLimit(2);

        if (datasetPath == null || datasetPath.isEmpty()) {
            return false;
        } // TO ADD: check if it is an instance of FileBasedRDFDataset or SPARQLEndPoint
        else if (d instanceof SparqlIndexedDataset) {
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
    public boolean isSatisfiableWRTResultsWithToken(Query q, IRDFDataset rdfd2) throws java.net.ConnectException {

        String datasetPath = (String) rdfd2.getEndPointURL();
        Query qTMP = QueryFactory.create(q.toString());
        qTMP.setLimit(1);
        if (datasetPath == null || datasetPath.isEmpty()) {
            this.notifyQuerySatisfiableValue(false);
            return false;
        }

        log.info("This is query to che the satisfiability: " + qTMP.toString());
        try {
            String encodedQuery = URLEncoder.encode(qTMP.toString(), "UTF-8");
            String GET_URL = rdfd2.getEndPointURL() + "?query=" + encodedQuery;
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(GET_URL);
            //getRequest.addHeader("accept", "application/sparql-results+json");
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
            httpClient.getConnectionManager().shutdown();
            log.info("solution space result " + result);

            ArrayList<QuerySolution> resultList = writeQueryResultsAsJenaQuerySolution(result, q.getProjectVars());
            //log.info("This is the solution space query: " + q.toString());
            log.info("solution space result size " + resultList.size());

            if (resultList.size() >= 1) {
                this.notifyQuerySatisfiableValue(true);
                return true;
            } else {
                this.notifyQuerySatisfiableValue(false);
                return false;
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private ArrayList<QuerySolution> writeQueryResultsAsJenaQuerySolution(String result, List<Var> projectVars) {
        ArrayList<QuerySolution> output = new ArrayList<>();
        
        if(result.isEmpty()){
            return output;
        }
        String[] resBindings = result.split("bindings");

//        try{
//        log.info("result ::" +result);
//        log.info("resBindings ::" +resBindings[0]);
//        log.info("resBindings ::" +resBindings[1]);
//        }catch(Exception ex){
//            log.info(ex.getMessage());
//        }


        
        //{    "bindings  
        //1: ": [          ]  }}
        String[] solutionAsStrings = new String[0];

        String regEx = "\\[\\s*(.*)\\s*\\]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(resBindings[1]);
        if (matcher.find()) {
            log.info(matcher.group(1));
            solutionAsStrings = resBindings[1]
                    .substring(0, resBindings[1].indexOf("]") - 1)
                    .split("}\\s*}\\s*\\,?");
        } else {
            return output;
        }

        for (int i = 0; i <= solutionAsStrings.length - 1; i++) {
            String[] solValueArray = solutionAsStrings[i].split("}\\s*\\,?");

            ArrayList<VarNameVarValuePair> varNameVarValuePairList = new ArrayList<>();

            for (String solValueArray1 : solValueArray) {
                log.info("solution:: " + solValueArray1);
//               String[] varNameAndvarValueParts = solValueArray1.split("\"type\"\\s*:\\s*\"uri\"\\s*,\\s*\"value\\s*\"\\s*:");

//                if(varNameAndvarValueParts.length<2){
//                    log.info("this one 1:: " +solValueArray1);
//                    varNameAndvarValueParts=solValueArray1.split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"value\\s*\"\\s*:");
//                }
//                if(varNameAndvarValueParts.length<2){
//                    log.info("this one 2:: " +solValueArray1);
//                    varNameAndvarValueParts=solValueArray1
//                            .split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"\\w*:\\s*\\w*\"\\s*:\\s*\"\\w*\"\\s*,\\s*\"value\\s*\"\\s*:");
//
//                }
//                if(varNameAndvarValueParts.length<2){
//                    
//                    log.info("this one 3:: " +solValueArray1);
//                    varNameAndvarValueParts=solValueArray1
//                            .split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"\\w*\":\\s*\"(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\"\\s*,\\s*\"value\\s*\"\\s*:");
//
//                }
//                if(varNameAndvarValueParts.length<2){
//                    log.info("this one 4:: " +solValueArray1);
//
//                    varNameAndvarValueParts=solValueArray1.split("\"type\"\\s*:\\s*\"bnode\"\\s*,\\s*\"value\\s*\"\\s*:");
//
//                }
//                if(varNameAndvarValueParts.length<2){
//                    log.info("this one 5:: " +solValueArray1);
//                    varNameAndvarValueParts=solValueArray1
//                            .split("\"type\"\\s*:\\s*\"literal\"\\s*,\\s*\"\\w*:\\s*\\w*\"\\s*:\\s*\"\\w*-\\w*\"\\s*,\\s*\"value\\s*\"\\s*:");
//
//                }
                // "published": { "datatype": "http://www.w3.org/2001/XMLSchema#dateTime" , "type": "typed-literal" , "value": "2-11-30T00:00:00Z" 
                String cleanedVarName = "", cleanedVarValue = "";

                //    if (varNameAndvarValueParts.length < 2) {
                log.info("this one 6:: " + solValueArray1);
//                    varNameAndvarValueParts = solValueArray1
//                            .split("\"datatype\"\\s*:\\s*\"(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\"\\s*,\\s*\"type\"\\s*:\\s*\"typed-literal\"\\s*,\\s*\"value\\s*\"\\s*:");

                String regEx1 = "\"(\\w+)\"\\s*:\\s*\\{.*\"value\"\\s*:\\s*\"(.*)\"";
                Pattern pattern1 = Pattern.compile(regEx1);
                Matcher matcher1 = pattern1.matcher(solValueArray1);

                while (matcher1.find()) {
                    log.info("Variable name : {}", matcher1.group(1));
                    cleanedVarName = matcher1.group(1);
                    log.info("Variable value : {}", matcher1.group(2));
                    cleanedVarValue = matcher1.group(2);
                }

                //}
//                
//                
//                
//                //TODO: (DONE) i need to add the code to extract each part of the solution...
//                String varNamePart = varNameAndvarValueParts[0];//.split(":")[0];
//                String varValuePart = varNameAndvarValueParts[1];
//                //log.info("varNamePart " +varNamePart.substring(varNamePart.indexOf("\"")+1, varNamePart.lastIndexOf("\"")));
//                String extractedVarName = "";
//                if (varNamePart.contains("[")) {
//                    String[] varNameNew = varNamePart.split("\\[");
//                    extractedVarName = varNameNew[1];
//                } else {
//                    extractedVarName = varNamePart;
//                }
//                String cleanedVarName = StringUtils.substringBetween(extractedVarName, "\"", "\"");
//                String cleanedVarValue = StringUtils.substringBetween(varValuePart, "\"", "\"");
                log.info("cleanedVarName : " + cleanedVarName);
                log.info("cleanedVarValue : " + cleanedVarValue);
                if (!cleanedVarName.isEmpty() && !cleanedVarValue.isEmpty() ) {
                    VarNameVarValuePair newPairItem = new VarNameVarValuePair(cleanedVarName, cleanedVarValue);
                    varNameVarValuePairList.add(newPairItem);

                }
                else{
                    return output;
                }
            }
            try {
                QuerySolutionMap qs = new QuerySolutionMap();
                for (VarNameVarValuePair v : varNameVarValuePairList) {
                    String cleanedVarName = v.getVarName();
                    String cleanedVarValue = v.getVarValue();

                    //                   if(isValidateURI(cleanedVarValue)){
//                        final URI uri = URI.create(cleanedVarValue);
                    RDFNode rdfNode = new ResourceImpl(cleanedVarValue);
                    qs.add(cleanedVarName, rdfNode);
//                    }
                }
                log.info("varNameVarValuePairList::: " +varNameVarValuePairList.size());
                log.info("projectVars.size::: " +projectVars.size());
                
               //if (varNameVarValuePairList.size() == projectVars.size()) {
                    output.add(qs);
               //}

            } catch (Exception e1) {
                log.info(e1.getMessage());
            }
            log.info("output size:::" +output.size());
            return output;
        }
        return output;
    }

    private boolean isValidateURI(String cleanedVarValue) {
        final IRIFactory u = IRIFactory.iriImplementation();
        try {
            u.create(cleanedVarValue);// = IRIResolver(cleanedVarValue);
            return true;
        } catch (Exception e1) {
            return false;
        }

    }

//    //based on Jena.
//    public boolean isSatisfiableWRTResultsWithToken(Query q, IRDFDataset d) throws Exception{
//        String datasetPath = (String) d.getEndPointURL();
//        Query qTMP = QueryFactory.create(q.toString());
//        qTMP.setLimit(2);
//        if (datasetPath == null || datasetPath.isEmpty()) {
//            this.notifyQuerySatisfiableValue(false);
//            return false;
//        } else if (d instanceof SPARQLEndPoint) {
//            List<QuerySolution> resList;
////                QueryExecution qexec = new QueryEngineHTTP((String) d.getEndPointURL(), q);
////                ResultSet results = qexec.execSelect();
////                resList = ResultSetFormatter.toList(results); //.out(, results, q);
////                return resList.size() >= 1;               
//            try {
//                QueryExecution qexec = QueryExecutionFactory.sparqlService(datasetPath, qTMP, (String) d.getGraph());
//                
//                ResultSet results = qexec.execSelect();
//                
////                System.err.println("[SPARQLQuerySatisfiable::isSatisfiableWRTResultsWithToken]Query Returned.");        
//
//                resList = ResultSetFormatter.toList(results); //.out(, results, q);
//                if (resList.size() >= 1) {
//                    this.notifyQuerySatisfiableValue(true);
//                    return true;
//                } else {
//                    this.notifyQuerySatisfiableValue(false);
//                    return false;
//                }
//            } catch (Exception ex) {
//                System.err.println("[SPARQLQuerySatisfiable::isSatisfiableWRTResultsWithToken]" +ex.getMessage());       
//                Logger.getLogger(SPARQLQuerySatisfiable.class.getName()).log(Level.SEVERE, null, ex);
//                this.notifyQuerySatisfiableValue(false);
//                throw ex;
////                return false;
//            }
//        } else {
//            try {
//                OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);// m = ModelFactory.createOntologyModel();
//                InputStream in = new FileInputStream(datasetPath);
//                if (in == null) {
//                    throw new IllegalArgumentException("File: " + datasetPath + " not found");
//                }
//                //...import the content of the owl file in the Jena model. 
//                m.read(in, "");
//                //...querying ...
//                QueryExecution qexec = QueryExecutionFactory.create(qTMP, m);
//                if (qTMP.isSelectType()) {
//                    ResultSet results = qexec.execSelect();
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
            List<String> outputVarList = qTMP.getResultVars();
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

            if (!outputVarList.isEmpty()) {
                String firstVar = outputVarList.get(0);
                ArrayList<String> resList = parseSparqlResultsJson(result, firstVar);
                return resList.size() > 0;
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
