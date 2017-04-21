/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources.impl;

import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mksmart.squire.websquire.v1.resources.JobStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.core4.QueryRecommendatorForm4;
import uk.ac.open.kmi.squire.jobs.JobManager;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;

/**
 *
 * @author carloallocca
 */
public class QueryStringScorePairResourceImplTest {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    
    
    public QueryStringScorePairResourceImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getRecommendedQueryList method, of class QueryStringScorePairResourceImpl.
     * String source_endpoint = "http://data.open.ac.uk/query";
        String target_endpoint = "http://data.aalto.fi/sparql";
        
    */
    
//    @Test
    public void testGetRecommendedQueryList() {
        System.out.println("getRecommendedQueryList");
        //String qo = "SELECT  distinct ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download .?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        String qo ="SELECT distinct ?patent ?label ?date ?title ?creator ?status ?authorList\n" +
        "WHERE {?patent a <http://purl.org/ontology/bibo/Patent> .?patent <http://www.w3.org/2000/01/rdf-schema#label> ?label .?patent <http://purl.org/dc/terms/date> ?date .?patent <http://purl.org/dc/terms/title> ?title .?patent <http://purl.org/dc/terms/creator> ?creator .?patent <http://purl.org/ontology/bibo/status> ?status .?patent <http://purl.org/ontology/bibo/authorList> ?authorList .}";
        String source_endpoint = "http://data.open.ac.uk/query";
        String target_endpoint = "http://data.aalto.fi/sparql";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;
        
        JobManager jobMan = JobManager.getInstance();
        
        IRDFDataset d1, d2;
        try {
            d1 = new SPARQLEndPoint(source_endpoint);
            d2 = new SPARQLEndPoint(target_endpoint);
        } catch (IOException e) {
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }

        QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(qo, d1, d2, resultTypeSimilarityDegree,
        queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
        Integer.toString(1));
        
        R1.run();
            
    }

    @Test
    public void testGetRecommendedQueryList3() {
    
        System.out.println("getRecommendedQueryList");
        //String qo = "SELECT  distinct ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download .?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        
        String qo ="SELECT DISTINCT ?mod ?title ?code WHERE { ?mod a <http://purl.org/vocab/aiiso/schema#Module>. ?mod <http://purl.org/dc/terms/title> ?title . ?mod <http://purl.org/vocab/aiiso/schema#code> ?code . }";
        
        String source_endpoint = "http://data.open.ac.uk/query";
        String target_endpoint = "https://data.ox.ac.uk/sparql/";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;
        
        JobManager jobMan = JobManager.getInstance();
        
        IRDFDataset d1, d2;
        try {
            d1 = new SPARQLEndPoint(source_endpoint);
            d2 = new SPARQLEndPoint(target_endpoint);
                        
        } catch (IOException e) {
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }

        QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(qo, d1, d2, resultTypeSimilarityDegree,
        queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
        Integer.toString(1));
        
        R1.run();
            
    }
    

    
    
    
    
        /**
     * Test of getRecommendedQueryList method, of class QueryStringScorePairResourceImpl.
     * String source_endpoint = "http://data.szepmuveszeti.hu/sparql";
        String target_endpoint = "http://datos.artium.org:8890/sparql";
        
    */
    
  //  @Test
    public void testGetRecommendedQueryList1() {
        System.out.println("getRecommendedQueryList");
        //String qo = "SELECT  distinct ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download .?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        String qo ="SELECT distinct ?deathEntity ?deathTimeSpan WHERE {?deathEntity a <http://erlangen-crm.org/current/E69_Death> .?deathEntity <http://erlangen-crm.org/current/P4_has_time-span> ?deathTimeSpan .}";
        String source_endpoint = "http://data.szepmuveszeti.hu/sparql";
        String target_endpoint = "http://datos.artium.org:8890/sparql";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;
        
        JobManager jobMan = JobManager.getInstance();
        
        IRDFDataset d1, d2;
        try {
            d1 = new SPARQLEndPoint(source_endpoint);
            d2 = new SPARQLEndPoint(target_endpoint);
        } catch (IOException e) {
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }

        QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(qo, d1, d2, resultTypeSimilarityDegree,
        queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
        Integer.toString(1));
        
        R1.run();
        
    }
    
    

    
}
