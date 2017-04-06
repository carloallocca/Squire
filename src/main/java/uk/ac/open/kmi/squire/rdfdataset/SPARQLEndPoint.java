/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.LockObtainFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.utils.FromStringToArrayList;

/**
 *
 * @author carloallocca
 */
public class SPARQLEndPoint implements IRDFDataset {

    private String endpointURL; // set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.
    private String graphName;

    private ArrayList<String> classSet = new ArrayList();
    private ArrayList<String> objectPropertySet = new ArrayList();
    private ArrayList<String> datatypePropertySet = new ArrayList();
    private ArrayList<String> literalSet = new ArrayList();
    private ArrayList<String> individualSet = new ArrayList();
    private ArrayList<String> rdfVocabulary = new ArrayList();
    private ArrayList<String> propertySet = new ArrayList();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public SPARQLEndPoint(String urlAddress) throws IOException, LockObtainFailedException {
        this(urlAddress, "");
    }

    public SPARQLEndPoint(String urlAddress, String gName) throws IOException, LockObtainFailedException {
        this.graphName = gName;
        this.endpointURL = urlAddress;
        //on 03/04/2017, this is what I have added to make it working again
        //createSPARQLEndPoint();
        RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
        this.signatureDoc = instance.getSignature(this.endpointURL, this.graphName);
        if (signatureDoc != null) {
            String cSet = signatureDoc.get("ClassSet");
            this.classSet = FromStringToArrayList.transform(cSet);
            String oPropSet = signatureDoc.get("ObjectPropertySet");
            this.objectPropertySet = FromStringToArrayList.transform(oPropSet);
            String dPropertySet = signatureDoc.get("DatatypePropertySet");
            this.datatypePropertySet = FromStringToArrayList.transform(dPropertySet);
            String litSet = signatureDoc.get("LiteralSet");
            this.literalSet = FromStringToArrayList.transform(litSet);
            String indSet = signatureDoc.get("IndividualSet");
            this.individualSet = FromStringToArrayList.transform(indSet);
            String rdfVoc = signatureDoc.get("RDFVocabulary");
            this.rdfVocabulary = FromStringToArrayList.transform(rdfVoc);
            String propSet = signatureDoc.get("PropertySet");
            this.propertySet = FromStringToArrayList.transform(propSet);
        }
    }

    @Override
    public boolean isIndexed() {

        RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
        Document d = instance.getSignature(this.endpointURL, this.graphName);
        return d != null;
    }

    @Override
    public void run() {
        System.out.println("[SPARQLEndPoint:run()] run is in execution....");
        try {
            if (!isIndexed()) {
                createSPARQLEndPoint();
            }
        } catch (Exception ex) {
            if (ex instanceof ClosedByInterruptException) {
                log.warn(" A task with token  was interrupted."
                        + " This may have been requested by a client.");
            } else {
                log.error("Caught exception of type " + ex.getClass().getName() + " : " + ex.getMessage()
                        + " - doing nothing with it.", ex);
            }
        }
    }

    private Document signatureDoc;

    private void createSPARQLEndPoint() throws IOException, LockObtainFailedException {
//       Document d = this.signatureDoc;
//        if (d != null) {
//            String cSet = d.get("ClassSet");
//            this.classSet = FromStringToArrayList.transform(cSet);
//            String oPropSet = d.get("ObjectPropertySet");
//            this.objectPropertySet = FromStringToArrayList.transform(oPropSet);
//            String dPropertySet = d.get("DatatypePropertySet");
//            this.datatypePropertySet = FromStringToArrayList.transform(dPropertySet);
//            String litSet = d.get("LiteralSet");
//            this.literalSet = FromStringToArrayList.transform(litSet);
//            String indSet = d.get("IndividualSet");
//            this.individualSet = FromStringToArrayList.transform(indSet);
//            String rdfVoc = d.get("RDFVocabulary");
//            this.rdfVocabulary = FromStringToArrayList.transform(rdfVoc);
//            String propSet = d.get("PropertySet");
//            this.propertySet = FromStringToArrayList.transform(propSet);
//        }
//        else{
        if (this.signatureDoc == null) {
            computeClassSet();
            computeObjectPropertySet();
            computeDataTypePropertySet();
            computeRDFVocabularySet();
            this.signatureDoc = RDFDatasetIndexer.getInstance().indexSignature(this.endpointURL, graphName,
                    this.classSet, this.objectPropertySet,
                    this.datatypePropertySet, this.individualSet, this.literalSet,
                    this.rdfVocabulary, this.propertySet);

        }
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    @Override
    public void setClassSet(ArrayList<String> classSet) {
        this.classSet = classSet;
    }

    @Override
    public void setObjectPropertySet(ArrayList<String> objectPropertySet) {
        this.objectPropertySet = objectPropertySet;
    }

    @Override
    public void setDatatypePropertySet(ArrayList<String> datatypePropertySet) {
        this.datatypePropertySet = datatypePropertySet;
    }

    @Override
    public void setLiteralSet(ArrayList<String> literalSet) {
        this.literalSet = literalSet;
    }

    @Override
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
        return this.endpointURL;// throw new UnsupportedOperationException("Not supported yet."); //To change
        // body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPath(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
        // choose Tools | Templates.
    }

    @Override
    public void computeClassSet() {
        try {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where "
                    + "{ " + " ?ind a ?class . " + "}";
            String encodedQuery = URLEncoder.encode(qString, "UTF-8");
            String GET_URL = this.endpointURL + "?query=" + encodedQuery;
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
            ArrayList<String> classList = parseSparqlResultsJson(result, "class");
            this.classSet.addAll(classList);
            httpClient.getConnectionManager().shutdown();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void computeObjectPropertySet() {
        try {
            //I need to filter our uri with this namespace: http://www.w3.org/1999/02/22-rdf-syntax-ns#
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?op where " + "{ " + " ?s ?op ?o . " + " FILTER (isURI(?o)) "
                    + "}";

            String encodedQuery = URLEncoder.encode(qString, "UTF-8");
            String GET_URL = this.endpointURL + "?query=" + encodedQuery;

            // set the connection timeout value to 30 seconds (30000 milliseconds)
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 300000000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
//            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpGet getRequest = new HttpGet(GET_URL);

            getRequest.addHeader("accept", "application/sparql-results+json");
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {

                String reason = response.getStatusLine().getReasonPhrase();
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

            ArrayList<String> objectPropertyList = parseSparqlResultsJson(result, "op");
            this.objectPropertySet.addAll(objectPropertyList);

            httpClient.getConnectionManager().shutdown();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void computeDataTypePropertySet() {

        try {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?p  where " + "{ " + " ?s ?p ?o . " + " FILTER (isLiteral(?o)) "
                    + "} LIMIT 30";

            String encodedQuery = URLEncoder.encode(qString, "UTF-8");
            String GET_URL = this.endpointURL + "?query=" + encodedQuery;
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

            ArrayList<String> datatypePropertyList = parseSparqlResultsJson(result, "p");
            this.datatypePropertySet.addAll(datatypePropertyList);

            httpClient.getConnectionManager().shutdown();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

//    // the old version, 05 04 2017
//    @Override
//    public void computeClassSet() {
//
//
//        // String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//        // + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//        // + "prefix owl:<http://www.w3.org/2002/07/owl#> "
//        // + " SELECT DISTINCT ?class where "
//        // + "{ "
//        // + " {?class rdf:type owl:Class .} "
//        // + " UNION "
//        // + " {?class rdf:type rdfs:Class .} "
//        // + "} LIMIT 10";
//        //
//        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                         + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                         + "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where "
//                         + "{ " + " ?ind a ?class . " + "}";
//        QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
//            this.graphName);
//        // QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);
//        ResultSet results = qexec.execSelect();
//        List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
//        for (QuerySolution sol : solList) {
//            if (sol.get("class").asResource().getURI() != null) {
//                this.classSet.add(sol.get("class").asResource().getURI());
//            }
//        }
//        System.out.println("[SPARQLEndPoint:computeClassSet] classSet cardinality " + this.classSet.size());
//
//    }
    @Override
    public void computeIndividualSet() {
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?indiv where "
                + "{ " + " ?indiv rdf:type ?class . " + " {?class rdf:type owl:Class .} "
                + " UNION " + " {?class rdf:type rdfs:Class .} " + "} LIMIT 10";
        // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
        // this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);

        ResultSet results = qexec.execSelect();
        List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
        for (QuerySolution sol : solList) {
            if (sol.get("indiv").asResource().getURI() != null) {
                this.individualSet.add(sol.get("indiv").asResource().getURI());
            }
        }
    }

//old version, 05 04 2017    
//    @Override
//    public void computeObjectPropertySet() {
//        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                         + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                         + " SELECT DISTINCT ?op where " + "{ " + " ?s ?op ?o . " + " FILTER (isURI(?o)) "
//                         + "}";
//
//        // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
//        // this.graphName);
//        QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);
//
//        ResultSet results = qexec.execSelect();
//        List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
//        for (QuerySolution sol : solList) {
//            if (sol.get("op").asResource().getURI() != null) {
//                this.objectPropertySet.add(sol.get("op").asResource().getURI());
//            }
//        }
//        // Convert the set of class into Arraylist List<String> list = new
//        // ArrayList<String>(listOfTopicAuthors);
//        System.out.println("[SPARQLEndPoint:computeObjectPropertySetNew] ObjectProperty cardinality "
//                           + this.objectPropertySet.size());
//
//    }
//old version 05-05-2017    
//    @Override
//    public void computeDataTypePropertySet() {
//
//        // String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//        // + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//        // + " SELECT DISTINCT ?dtProp where "
//        // + "{ "
//        // + " {?dtProp rdf:type <http://www.w3.org/2002/07/owl#DatatypeProperty> .} "
//        // + " UNION "
//        // +
//        // " {?s ?dtProp ?o . FILTER (isLiteral(?o) &&  !contains(str(?p),\'http://www.w3.org/2000/01/rdf-schema#\'))} "
//        // + "} LIMIT 10";
//        // String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//        // + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//        // + " SELECT DISTINCT ?p where "
//        // + "{ "
//        // + " ?s ?p ?o . "
//        // + "FILTER (isLiteral(?o) &&  "
//        // + "!contains(str(?p),\'http://www.w3.org/2000/01/rdf-schema#\') && "
//        // + "!contains(str(?p),\'http://www.w3.org/1999/02/22-rdf-syntax-ns#\') && "
//        // + "!contains(str(?p),\'http://www.w3.org/2002/07/owl#\')) "
//        // + "} LIMIT 10";
//        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                         + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                         + " SELECT DISTINCT ?p  where " + "{ " + " ?s ?p ?o . " + " FILTER (isLiteral(?o)) "
//                         + "}";
//
//        // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
//        // this.graphName);
//        QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);
//
//        ResultSet results = qexec.execSelect();
//
//        List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
//        for (QuerySolution sol : solList) {
//            if (sol.get("p").asResource().getURI() != null) {
//                this.datatypePropertySet.add(sol.get("p").asResource().getURI());
//            }
//        }
//        System.out.println("[SPARQLEndPoint:datatypePropertySet] datatypePropertySet cardinality "
//                           + this.datatypePropertySet.size());
//    }
    @Override
    public void computeLiteralSet() {

        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + " SELECT DISTINCT ?lit where " + "{ " + " ?s ?p ?lit . "
                + " FILTER (isLiteral(?lit) ) " + "} LIMIT 10";

        // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
        // this.graphName);
        QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);

        ResultSet results = qexec.execSelect();
        List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
        for (QuerySolution sol : solList) {
            if (sol.get("lit").asLiteral().getValue().toString() != null) {
                this.literalSet.add(sol.get("lit").asLiteral().getValue().toString());
            }
        }
    }

    @Override
    public void computePropertySet() {
        this.propertySet.addAll(this.datatypePropertySet);
        this.propertySet.addAll(this.objectPropertySet);
    }

    @Override
    public void computeRDFVocabularySet() {
        // RDF
        this.rdfVocabulary.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        // RDFS
        this.rdfVocabulary.add("http://www.w3.org/2000/01/rdf-schema#Class");
        this.rdfVocabulary.add("http://www.w3.org/2000/01/rdf-schema#Literal");
        this.rdfVocabulary.add("http://www.w3.org/2000/01/rdf-schema#Resource");
        // OWL
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#Class ");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#NamedIndividual");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#ObjectProperty");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#sameAs");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#DatatypeProperty ");
        this.rdfVocabulary.add("http://www.w3.org/2002/07/owl#DataRange ");

    }

    private void computeObjectPropertySetNew() {
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        while (resultset) {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?op where " + "{ " + " ?s ?op ?o . "
                    + " FILTER (isURI(?o)) " + "} ORDER BY ?op limit 50 OFFSET "
                    + Integer.toString(offset);

            // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
            // this.graphName);
            QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);

            // // this is for http://sparql.bioontology.org/sparql but connection refused after 8000
            // classes...
            // Query query = QueryFactory.create(qString) ;
            // QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.endpointURL,
            // query);
            // qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    if (sol.get("op").asResource().getURI() != null) {
                        qSol.add(sol.get("op").asResource().getURI());
                    }
                }
                System.out
                        .println("[SPARQLEndPoint:computeObjectPropertySetNew] objectPropertySet cardinality so far: "
                                + qSol.size());
                offset = offset + 50;
            } else {
                resultset = false;
            }
        }
        // Convert the set of class into Arraylist List<String> list = new
        // ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPoint:computeObjectPropertySetNew] ObjectProperty cardinality "
                + qSol.size());
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
                    + " SELECT DISTINCT ?op where " + "{ " + " ?s ?op ?o . "
                    + " FILTER (isURI(?o)) " + "} ORDER BY ?op limit 50 OFFSET "
                    + Integer.toString(offset);

            // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
            // this.graphName);
            // QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);
            qexec = new QueryEngineHTTP((String) this.endpointURL, qString);

            // // this is for http://sparql.bioontology.org/sparql but connection refused after 8000
            // classes...
            // Query query = QueryFactory.create(qString) ;
            // QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.endpointURL,
            // query);
            // qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    if (sol.get("op").asResource().getURI() != null) {
                        qSol.add(sol.get("op").asResource().getURI());
                    }
                }
                System.out
                        .println("[SPARQLEndPoint:computeObjectPropertySetNew] objectPropertySet cardinality so far: "
                                + qSol.size());
                offset = offset + 50;
            } else {
                resultset = false;
            }
        }
        qexec.close();
        // Convert the set of class into Arraylist List<String> list = new
        // ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPoint:computeObjectPropertySetNew] ObjectProperty cardinality "
                + qSol.size());
        this.objectPropertySet = new ArrayList<String>(qSol);

    }

    private void computeDataTypePropertySetNew() {
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        while (resultset) {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?dp  where " + "{ " + " ?s ?dp ?o . "
                    + " FILTER (isLiteral(?o)) " + "} ORDER BY ?dp limit 50 OFFSET "
                    + Integer.toString(offset);
            // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
            // this.graphName);
            QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);

            // // this is for http://sparql.bioontology.org/sparql but connection refused after 8000
            // classes...
            // Query query = QueryFactory.create(qString) ;
            // QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.endpointURL,
            // query);
            // qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    String dpString = sol.get("dp").asResource().getURI();
                    if (dpString != null) {
                        qSol.add(dpString);
                    }
                }
                System.out
                        .println("[SPARQLEndPoint:datatypePropertySet] datatypePropertySet cardinality so far: "
                                + qSol.size());
                offset = offset + 50;
            } else {
                resultset = false;
            }
        }
        // Convert the set of class into Arraylist List<String> list = new
        // ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPoint:datatypePropertySet] datatypePropertySet cardinality "
                + qSol.size());
        this.datatypePropertySet = new ArrayList<String>(qSol);
    }

    private void computeClassSetNew() {
        // String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
        // + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
        // + "prefix owl:<http://www.w3.org/2002/07/owl#> "
        // + " SELECT DISTINCT ?class where "
        // + "{ "
        // + " {?class rdf:type owl:Class .} "
        // + " UNION "
        // + " {?class rdf:type rdfs:Class .} " /
        // + "} LIMIT 10";
        //
        int offset = 1;
        boolean resultset = true;
        Set<String> qSol = new HashSet();
        while (resultset) {

            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                    + " SELECT DISTINCT ?class where " + "{ " + " ?ind rdf:type ?class . "
                    + "} ORDER BY ?class limit 100 OFFSET " + Integer.toString(offset);

            // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
            // this.graphName);
            QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);

            // // this is for http://sparql.bioontology.org/sparql but connection refused after 8000
            // classes...
            // Query query = QueryFactory.create(qString) ;
            // QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest((String) this.endpointURL,
            // query);
            // qexec.addParam("apikey", "4f603778-5373-4ea3-8e84-d9e8bdb7dec1");
            ResultSet results = qexec.execSelect();
            List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
            if (solList.size() > 0) {
                for (QuerySolution sol : solList) {
                    if (sol.get("class").asResource().getURI() != null) {
                        qSol.add(sol.get("class").asResource().getURI());
                    }
                }
                System.out.println("[SPARQLEndPoint:computeClassSetNew] classSet cardinality so far: "
                        + qSol.size());
                offset = offset + 100;
            } else {
                resultset = false;
            }
        }
        // Convert the set of class into Arraylist List<String> list = new
        // ArrayList<String>(listOfTopicAuthors);
        System.out.println("[SPARQLEndPoint:computeClassSetNew] classSet cardinality " + qSol.size());
        this.classSet = new ArrayList<String>(qSol);
        // for (QuerySolution sol : solList) {
        // if (sol.get("class").asResource().getURI() != null) {
        // this.classSet.add(sol.get("class").asResource().getURI());
        // }
        // }

    }

    @Override
    public Object getGraph() {
        return this.graphName;
    }

    @Override
    public void setGraph(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
        // choose Tools | Templates.
    }

    @Override
    public void setRDFVocabulary(ArrayList<String> rdfSet) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
        // choose Tools | Templates.
    }

}
