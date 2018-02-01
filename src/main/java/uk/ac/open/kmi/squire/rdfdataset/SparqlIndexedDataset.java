/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.LockObtainFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.utils.String2List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author carloallocca
 */
public class SparqlIndexedDataset extends AbstractRdfDataset {

    private String endpointURL; // set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.
    private String graphName;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Document signatureDoc;

    public SparqlIndexedDataset(String urlAddress) throws IOException, LockObtainFailedException {
        this(urlAddress, "");
    }

    public SparqlIndexedDataset(String urlAddress, String gName) throws IOException,
                                                                LockObtainFailedException {
        this.graphName = gName;
        this.endpointURL = urlAddress;
        // on 03/04/2017, this is what I have added to make it working again
        // createSPARQLEndPoint();
        RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
        this.signatureDoc = instance.getSignature(this.endpointURL, this.graphName);
        if (signatureDoc != null) {
            String cSet = signatureDoc.get("ClassSet");
            this.classSet = String2List.transform(cSet);
            String oPropSet = signatureDoc.get("ObjectPropertySet");
            this.objectPropertySet = String2List.transform(oPropSet);
            String dPropertySet = signatureDoc.get("DatatypePropertySet");
            this.datatypePropertySet = String2List.transform(dPropertySet);
            String litSet = signatureDoc.get("LiteralSet");
            this.literalSet = String2List.transform(litSet);
            String indSet = signatureDoc.get("IndividualSet");
            this.individualSet = String2List.transform(indSet);
            String rdfVoc = signatureDoc.get("RDFVocabulary");
            this.rdfVocabulary = String2List.transform(rdfVoc);
            String propSet = signatureDoc.get("PropertySet");
            this.propertySet = String2List.transform(propSet);
        }
    }

    @Override
    public void computeClassSet() {
        try {
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                             + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                             + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                             + " SELECT DISTINCT ?class where " + "{ " + " ?ind a ?class . " + "}";
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
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
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

    // // the old version, 05 04 2017
    // @Override
    // public void computeClassSet() {
    //
    //
    // // String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
    // // + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
    // // + "prefix owl:<http://www.w3.org/2002/07/owl#> "
    // // + " SELECT DISTINCT ?class where "
    // // + "{ "
    // // + " {?class rdf:type owl:Class .} "
    // // + " UNION "
    // // + " {?class rdf:type rdfs:Class .} "
    // // + "} LIMIT 10";
    // //
    // String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
    // + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
    // + "prefix owl:<http://www.w3.org/2002/07/owl#> " + " SELECT DISTINCT ?class where "
    // + "{ " + " ?ind a ?class . " + "}";
    // QueryExecution qexec = QueryExecutionFactory.sparqlService((String) this.endpointURL, qString,
    // this.graphName);
    // // QueryExecution qexec = new QueryEngineHTTP((String) this.endpointURL, qString);
    // ResultSet results = qexec.execSelect();
    // List<QuerySolution> solList = ResultSetFormatter.toList(results);// .out(, results, q);
    // for (QuerySolution sol : solList) {
    // if (sol.get("class").asResource().getURI() != null) {
    // this.classSet.add(sol.get("class").asResource().getURI());
    // }
    // }
    // System.out.println("[SPARQLEndPoint:computeClassSet] classSet cardinality " + this.classSet.size());
    //
    // }

    @Override
    public void computeDataTypePropertySet() {
        // To mimic the original behaviour, call
        // iterateObjectPropertySet(-1, 0, null)
        iterateDatatypePropertySet(50, 0, null); // No exclusions, creates excessively long queries
    }

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
    public void computeObjectPropertySet() {
        // To mimic the original behaviour, call
        // iterateObjectPropertySet(-1, 0, null)
        iterateObjectPropertySet(50, 0, null); // No exclusions, creates excessively long queries
    }

    @Override
    public void computePropertySet() {
        this.propertySet.addAll(this.datatypePropertySet);
        this.propertySet.addAll(this.objectPropertySet);
    }

    @Override
    public void computeRDFVocabularySet() {
        // RDF
        this.rdfVocabulary.add(RDF.type.getURI());
        // RDFS
        this.rdfVocabulary.add(RDFS.Class.getURI());
        this.rdfVocabulary.add(RDFS.Literal.getURI());
        this.rdfVocabulary.add(RDFS.Resource.getURI());
        // OWL
        this.rdfVocabulary.add(OWL.Class.getURI());
        this.rdfVocabulary.add(OWL2.NamedIndividual.getURI());
        this.rdfVocabulary.add(OWL.ObjectProperty.getURI());
        this.rdfVocabulary.add(OWL.sameAs.getURI());
        this.rdfVocabulary.add(OWL.DatatypeProperty.getURI());
        this.rdfVocabulary.add(OWL.DataRange.getURI());

    }

    @Override
    public Object getEndPointURL() {
        return this.endpointURL;// throw new UnsupportedOperationException("Not supported yet."); //To change
        // body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getGraph() {
        return this.graphName;
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

    @Override
    public Object runAskQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object runSelectQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    @Override
    public void setGraph(Object path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    @Override
    public void setPath(Object path) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
        // choose Tools | Templates.
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

    private void createSPARQLEndPoint() throws IOException, LockObtainFailedException {
        // Document d = this.signatureDoc;
        // if (d != null) {
        // String cSet = d.get("ClassSet");
        // this.classSet = FromStringToArrayList.transform(cSet);
        // String oPropSet = d.get("ObjectPropertySet");
        // this.objectPropertySet = FromStringToArrayList.transform(oPropSet);
        // String dPropertySet = d.get("DatatypePropertySet");
        // this.datatypePropertySet = FromStringToArrayList.transform(dPropertySet);
        // String litSet = d.get("LiteralSet");
        // this.literalSet = FromStringToArrayList.transform(litSet);
        // String indSet = d.get("IndividualSet");
        // this.individualSet = FromStringToArrayList.transform(indSet);
        // String rdfVoc = d.get("RDFVocabulary");
        // this.rdfVocabulary = FromStringToArrayList.transform(rdfVoc);
        // String propSet = d.get("PropertySet");
        // this.propertySet = FromStringToArrayList.transform(propSet);
        // }
        // else{
        if (this.signatureDoc == null) {
            computeClassSet();
            computeObjectPropertySet();
            computeDataTypePropertySet();
            computeRDFVocabularySet();
            this.signatureDoc = RDFDatasetIndexer.getInstance().indexSignature(this.endpointURL, graphName,
                this.classSet, this.objectPropertySet, this.datatypePropertySet, this.individualSet,
                this.literalSet, this.rdfVocabulary, this.propertySet);

        }
    }

    private ArrayList<String> parseSparqlResultsJson(String result, String varString) {

        ArrayList<String> output = new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        JsonArray results = jsonParser.parse(result).getAsJsonObject().get("results").getAsJsonObject()
                .getAsJsonArray("bindings");
        for (JsonElement result1 : results) {
            JsonObject _class = result1.getAsJsonObject().getAsJsonObject(varString);
            String value = _class.get("value").getAsString();
            try {
                URI valueURI = new URI(value);
                output.add(value);
                // System.out.println(valueURI);
            } catch (URISyntaxException ex) {
                log.error("Bad URI synax for string '{}'", value);
            }
        }
        return output;
    }

    /**
     * TODO try to merge with the other iterative method
     * 
     * @param stepLength
     * @param iteration
     * @param exclusions
     *            if NULL, the method will do pure pagination
     */
    protected void iterateDatatypePropertySet(int stepLength, int iteration, Set<Property> exclusions) {
        long before = System.currentTimeMillis();
        if (iteration < 0) throw new IllegalArgumentException("Iteration cannot be negative.");
        StringBuilder qS = new StringBuilder();
        qS.append("SELECT DISTINCT ?op WHERE { [] ?op ?o");
        // handle property filtering
        qS.append(" FILTER ( isLiteral(?o)");
        int count = 0;
        if (exclusions != null && !exclusions.isEmpty()) {
            qS.append(" && ?op NOT IN (");
            for (Iterator<Property> it = exclusions.iterator(); it.hasNext(); count++) {
                if (count > 0) qS.append(",");
                qS.append("<" + it.next().getURI() + ">");
            }
            qS.append(" )");
        }
        qS.append(" )");
        // END handle property filtering
        qS.append(" }");
        if (stepLength > 0) {
            qS.append(" LIMIT ");
            qS.append(stepLength);
            if (iteration > 0 && (exclusions == null || exclusions.isEmpty())) {
                qS.append(" OFFSET ");
                qS.append(stepLength * iteration);
            }
        }
        log.debug("Sending query: {}", qS);
        try {
            String encodedQuery = URLEncoder.encode(qS.toString(), "UTF-8");
            String GET_URL = this.endpointURL + "?query=" + encodedQuery;

            // set the connection timeout value to 30 seconds (30000 milliseconds)
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 300000000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            // DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpGet getRequest = new HttpGet(GET_URL);
            getRequest.addHeader("Accept", "application/sparql-results+json");
            HttpResponse response = httpClient.execute(getRequest);
            List<String> propertyList;
            int stcode = response.getStatusLine().getStatusCode();
            if (200 == stcode) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (response.getEntity().getContent())));
                String output;
                String result = "";
                while ((output = br.readLine()) != null)
                    result = result + output;
                propertyList = parseSparqlResultsJson(result, "op");
            } else {
                // Don't die. Keep whatever was indexed so far.
                String reason = response.getStatusLine().getReasonPhrase();
                log.warn("Got remote response {} - {}", stcode, reason);
                log.warn(
                    "DP indexing failed at iteration {}. Will abort and keep already indexed properties.",
                    iteration);
                propertyList = Collections.emptyList();
            }

            /*
             * Stop iterating if you received fewer results than the limit or if you already have all the RDF
             * resources in the result (the latter means there's something wrong with the order in which
             * results are given, therefore one should sort but it's costly).
             */
            if (propertyList.isEmpty() || this.datatypePropertySet.containsAll(propertyList)) {
                log.debug("All {} RDF resources already present, aborting.", propertyList.size());
                log.info("DONE. {} total datatype properties indexed.", this.datatypePropertySet.size());
            } else {
                this.datatypePropertySet.addAll(propertyList);
                log.info(" ... {} properties indexed so far (last {} in {} ms)",
                    this.datatypePropertySet.size(), propertyList.size(),
                    (System.currentTimeMillis() - before));
                if (stepLength == propertyList.size()) {
                    if (exclusions != null) for (String op : propertyList)
                        exclusions.add(ResourceFactory.createProperty(op));
                    iterateDatatypePropertySet(stepLength, iteration + 1, exclusions);
                } else log.info("DONE. {} total datatype properties indexed.",
                    this.datatypePropertySet.size());
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace(); // TODO Handle properly
        } catch (IOException e) {
            e.printStackTrace(); // TODO Handle properly
        }
    }

    /**
     * 
     * @param stepLength
     * @param iteration
     * @param exclusions
     *            if NULL, the method will do pure pagination
     */
    protected void iterateObjectPropertySet(int stepLength, int iteration, Set<Property> exclusions) {
        long before = System.currentTimeMillis();
        if (iteration < 0) throw new IllegalArgumentException("Iteration cannot be negative.");
        StringBuilder qS = new StringBuilder();
        qS.append("SELECT DISTINCT ?op WHERE { [] ?op ?o");
        // handle property filtering
        qS.append(" FILTER ( isUri(?o)");
        int count = 0;
        if (exclusions != null && !exclusions.isEmpty()) {
            qS.append(" && ?op NOT IN (");
            for (Iterator<Property> it = exclusions.iterator(); it.hasNext(); count++) {
                if (count > 0) qS.append(",");
                qS.append("<" + it.next().getURI() + ">");
            }
            qS.append(" )");
        }
        qS.append(" )");
        // END handle property filtering
        qS.append(" }");
        if (stepLength > 0) {
            qS.append(" LIMIT ");
            qS.append(stepLength);
            if (iteration > 0 && (exclusions == null || exclusions.isEmpty())) {
                qS.append(" OFFSET ");
                qS.append(stepLength * iteration);
            }
        }
        log.debug("Sending query: {}", qS);
        try {
            String encodedQuery = URLEncoder.encode(qS.toString(), "UTF-8");
            String GET_URL = this.endpointURL + "?query=" + encodedQuery;

            // set the connection timeout value to 30 seconds (30000 milliseconds)
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 300000000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            // DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpGet getRequest = new HttpGet(GET_URL);
            getRequest.addHeader("Accept", "application/sparql-results+json");
            HttpResponse response = httpClient.execute(getRequest);
            List<String> propertyList;
            int stcode = response.getStatusLine().getStatusCode();
            if (200 == stcode) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (response.getEntity().getContent())));
                String output;
                String result = "";
                while ((output = br.readLine()) != null)
                    result = result + output;
                propertyList = parseSparqlResultsJson(result, "op");
            } else {
                // Don't die. Keep whatever was indexed so far.
                String reason = response.getStatusLine().getReasonPhrase();
                log.warn("Got remote response {} - {}", stcode, reason);
                log.warn(
                    "Property indexing failed at iteration {}. Will abort and keep already indexed properties.",
                    iteration);
                propertyList = Collections.emptyList();
            }

            /*
             * Stop iterating if you received fewer results than the limit or if you already have all the RDF
             * resources in the result (the latter means there's something wrong with the order in which
             * results are given, therefore one should sort but it's costly).
             */
            if (propertyList.isEmpty() || this.objectPropertySet.containsAll(propertyList)) {
                log.debug("All {} RDF resources already present, aborting.", propertyList.size());
                log.info("DONE. {} total object properties indexed.", this.objectPropertySet.size());
            } else {
                this.objectPropertySet.addAll(propertyList);
                log.info(" ... {} properties indexed so far (last {} in {} ms)",
                    this.objectPropertySet.size(), propertyList.size(), (System.currentTimeMillis() - before));
                if (stepLength == propertyList.size()) {
                    if (exclusions != null) for (String op : propertyList)
                        exclusions.add(ResourceFactory.createProperty(op));
                    iterateObjectPropertySet(stepLength, iteration + 1, exclusions);
                } else log.info("DONE. {} total object properties indexed.", this.objectPropertySet.size());
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace(); // TODO Handle properly
        } catch (IOException e) {
            e.printStackTrace(); // TODO Handle properly
        }
    }

}
