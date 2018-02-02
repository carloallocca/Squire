/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package some.tests;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author carloallocca
 */
public class SPARQLEndPointController {

    private static final String askQuery = "ASK { ?x ?y ?z }"; // A SPARQL ASK query for checking if the IRI corresponds to a SPARQL endpoint
    private String iri; // The IRI of the SERVICE operator
    private Query query; // The query to run at the RDF data that may exist in the IRI.
    private ResultSet resultSet; // A ResultSet object containing the results of running the query to the IRI.
    private QueryExecutionBase qe; // A QueryExecution object for running the query to the model that corresponds to the IRI.
    private String contentType; // The IRI content type

    private final Logger log = LoggerFactory.getLogger(getClass());

    public SPARQLEndPointController(String uri) {
        this.iri = uri;
    }

    
    
    
    // e.g. application/json or application/rdf+xml  or application/sparql-results+json application/x-www-form-urlencoded  application/sparql-update
    // application/x-www-form-urlencoded text/plain
    public void setContentType(String contentType) {
        contentType = "";
        try {
            URL url = new URL(iri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("ACCEPT", contentType);
            connection.connect();
            contentType = connection.getContentType();
            if (contentType == null) {
                contentType = "";
            }
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage());
        } catch (IOException ex) {
            log.error(ex.getMessage());

        }
    }

}
