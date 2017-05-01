/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources.impl;

import java.io.IOException;
import java.util.ArrayList;
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

    
    @Test
    public void testEducationIWithQueryRootDistance() {
        String qo1 = "Select distinct ?mod ?title where {?mod a <http://purl.org/vocab/aiiso/schema#Module>.?mod <http://purl.org/dc/terms/title> ?title .}";
        String qo2 = "SELECT DISTINCT ?mod ?title ?code WHERE { ?mod a <http://purl.org/vocab/aiiso/schema#Module>. ?mod <http://purl.org/dc/terms/title> ?title . ?mod <http://purl.org/vocab/aiiso/schema#code> ?code . }";
        String qo3 = "Select distinct ?mod ?title ?code ?regulation where {?mod a <http://purl.org/vocab/aiiso/schema#Module>. ?mod <http://purl.org/dc/terms/title> ?title . ?mod <http://purl.org/vocab/aiiso/schema#code> ?code .?mod <http://xcri.org/profiles/catalog/1.2/regulations> ?regulation .}";
        String qo4 = "Select distinct ?mod ?title ?descr where { ?mod a <http://purl.org/vocab/aiiso/schema#Module>. ?mod <http://purl.org/dc/terms/title> ?title . ?mod <http://purl.org/dc/elements/1.1/description> ?descr .}";

        ArrayList<String> queryQoList = new ArrayList();
        queryQoList.add(qo1);
        queryQoList.add(qo2);
        queryQoList.add(qo3);
        queryQoList.add(qo4);

        String source_endpoint = "http://data.open.ac.uk/query";
        String target_endpoint = "https://data.ox.ac.uk/sparql/";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;
        for (String queryQo : queryQoList) {
            IRDFDataset d1, d2;
            try {
                d1 = new SPARQLEndPoint(source_endpoint);
                d2 = new SPARQLEndPoint(target_endpoint);
            } catch (IOException e) {
                throw new WebApplicationException(INTERNAL_SERVER_ERROR);
            }
            QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(queryQo, d1, d2, resultTypeSimilarityDegree,
                    queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
                    Integer.toString(1));
            R1.run();
        }

    }

    // the two RDF datasets are very similar to each other...
//    @Test
    public void testArtWithQueryRootDistance() {
        String qo1 = "SELECT distinct ?physicalThing ?hasType ?descr WHERE { ?physicalThing a <http://erlangen-crm.org/current/E18_Physical_Thing> .?physicalThing <http://erlangen-crm.org/current/P2_has_type> ?hasType . ?physicalThing <http://erlangen-crm.org/current/P1_is_identified_by> ?id . ?id <http://erlangen-crm.org/current/P3_has_note> ?descr.}";
        String qo2 = "SELECT distinct ?creation ?partecipant WHERE {?creation a <http://erlangen-crm.org/current/E65_Creation> .?creation <http://erlangen-crm.org/current/P14_carried_out_by> ?partecipant}";
        String qo3 = "SELECT distinct ?deathEntity ?deathTimeSpan WHERE {?deathEntity a <http://erlangen-crm.org/current/E69_Death> . ?deathEntity <http://erlangen-crm.org/current/P4_has_time-span> ?deathTimeSpan .}";
        String qo4 = "SELECT distinct ?deathEntity  ?deathDate WHERE {?deathEntity a <http://erlangen-crm.org/current/E69_Death> .?deathEntity <http://erlangen-crm.org/current/P4_has_time-span> ?deathTimeSpan .?deathTimeSpan <http://erlangen-crm.org/current/P78_is_identified_by> ?deathTimeSpanID .?deathTimeSpanID <http://erlangen-crm.org/current/P3_has_note> ?deathDate.}";
        String qo5 = "select distinct ?topic ?authoritativeLabel ?hasVariant where {?topic a <http://www.loc.gov/mads/rdf/v1#Topic> .?topic <http://www.loc.gov/mads/rdf/v1#authoritativeLabel> ?authoritativeLabel . ?topic <http://www.loc.gov/mads/rdf/v1#hasVariant> ?hasVariant}";

        ArrayList<String> queryQoList = new ArrayList();
        queryQoList.add(qo1);
        queryQoList.add(qo2);
        queryQoList.add(qo3);
        queryQoList.add(qo4);
        queryQoList.add(qo5);
        

        String source_endpoint = "http://data.szepmuveszeti.hu/sparql";
        String target_endpoint = "http://datos.artium.org:8890/sparql";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;

        for (String queryQo : queryQoList) {
            IRDFDataset d1, d2;
            try {
                d1 = new SPARQLEndPoint(source_endpoint);
                d2 = new SPARQLEndPoint(target_endpoint);

            } catch (IOException e) {
                throw new WebApplicationException(INTERNAL_SERVER_ERROR);
            }
            QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(queryQo, d1, d2, resultTypeSimilarityDegree,
                    queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
                    Integer.toString(1));

            R1.run();
        }
    }
    
//    @Test
    public void testEducationIIWithQueryRootDistance() {
        String qo1 = "SELECT distinct ?thing ?description where {?thing a <http://data.open.ac.uk/podcast/ontology/VideoPodcast>. ?thing <http://purl.org/dc/terms/description> ?description .}";
        String qo2 = "SELECT distinct ?podcast ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download . ?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        String qo3 = "SELECT distinct ?patent ?label ?date ?title ?creator ?status ?authorList WHERE {?patent a <http://purl.org/ontology/bibo/Patent> .?patent <http://www.w3.org/2000/01/rdf-schema#label> ?label .?patent <http://purl.org/dc/terms/date> ?date .?patent <http://purl.org/dc/terms/title> ?title .?patent <http://purl.org/dc/terms/creator> ?creator .?patent <http://purl.org/ontology/bibo/status> ?status .?patent <http://purl.org/ontology/bibo/authorList> ?authorList .}";
        String qo4 = "SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        String qo5 = "select distinct ?x where {?x <http://xmlns.com/foaf/0.1/familyName> ?y .?x <http://xmlns.com/foaf/0.1/firstName> ?z .}";

        ArrayList<String> queryQoList = new ArrayList();
        queryQoList.add(qo1);
        queryQoList.add(qo2);
        queryQoList.add(qo3);
        queryQoList.add(qo4);
        queryQoList.add(qo5);
        

        String source_endpoint = "http://data.open.ac.uk/query";
        String target_endpoint = "http://data.aalto.fi/sparql";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;

        for (String queryQo : queryQoList) {
            IRDFDataset d1, d2;
            try {
                d1 = new SPARQLEndPoint(source_endpoint);
                d2 = new SPARQLEndPoint(target_endpoint);

            } catch (IOException e) {
                throw new WebApplicationException(INTERNAL_SERVER_ERROR);
            }
            QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(queryQo, d1, d2, resultTypeSimilarityDegree,
                    queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
                    Integer.toString(1));

            R1.run();
        }
    }
    
    
    
    
    
    
    
    //    @Test
    public void testMuseumWithQueryRootDistance() {
        String qo1 = "SELECT distinct ?place ?pLabel WHERE {?place a <http://www.europeana.eu/schemas/edm/Place>.?place <http://www.w3.org/2004/02/skos/core#prefLabel> ?pLabel.}";
        String qo2 = "SELECT distinct ?collection ?parentCollection ?parentId ?parentDescr WHERE {?collection <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#OrderedCollection> . ?collection <http://www.w3.org/2004/02/skos/core#member> ?parentCollection .?parentCollection <http://purl.org/dc/elements/1.1/identifier> ?parentId .?parentCollection <http://vocab.getty.edu/ontology#parentString> ?parentDescr .}";
        String qo3 = "SELECT distinct ?concept ?prefLabel WHERE {?concept a <http://www.w3.org/2004/02/skos/core#Concept> .?concept <http://www.w3.org/2004/02/skos/core#prefLabel> ?prefLabel . }";
        
        ArrayList<String> queryQoList = new ArrayList();
        queryQoList.add(qo1);
        queryQoList.add(qo2);
        queryQoList.add(qo3);
        

        String source_endpoint = "http://rijksmuseum.sealinc.eculture.labs.vu.nl/sparql/";
        String target_endpoint = "http://edan.si.edu/saam/sparql/";
        float resultTypeSimilarityDegree = 1;
        float queryRootDistanceDegree = 1;
        float resultSizeSimilarityDegree = 1;
        float querySpecificityDistanceDegree = 1;

        for (String queryQo : queryQoList) {
            IRDFDataset d1, d2;
            try {
                d1 = new SPARQLEndPoint(source_endpoint);
                d2 = new SPARQLEndPoint(target_endpoint);

            } catch (IOException e) {
                throw new WebApplicationException(INTERNAL_SERVER_ERROR);
            }
            QueryRecommendatorForm4 R1 = new QueryRecommendatorForm4(queryQo, d1, d2, resultTypeSimilarityDegree,
                    queryRootDistanceDegree, resultSizeSimilarityDegree, querySpecificityDistanceDegree,
                    Integer.toString(1));

            R1.run();
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    ////////////
    ////////////
    ////////////
    ////////////
    
    
    /**
     * Test of getRecommendedQueryList method, of class
     * QueryStringScorePairResourceImpl. String source_endpoint =
     * "http://data.open.ac.uk/query"; String target_endpoint =
     * "http://data.aalto.fi/sparql";
     *
     */
//    @Test
    public void testGetRecommendedQueryList() {
        System.out.println("getRecommendedQueryList");
        //String qo = "SELECT  distinct ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download .?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        String qo = "SELECT distinct ?patent ?label ?date ?title ?creator ?status ?authorList\n"
                + "WHERE {?patent a <http://purl.org/ontology/bibo/Patent> .?patent <http://www.w3.org/2000/01/rdf-schema#label> ?label .?patent <http://purl.org/dc/terms/date> ?date .?patent <http://purl.org/dc/terms/title> ?title .?patent <http://purl.org/dc/terms/creator> ?creator .?patent <http://purl.org/ontology/bibo/status> ?status .?patent <http://purl.org/ontology/bibo/authorList> ?authorList .}";
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


    /**
     * Test of getRecommendedQueryList method, of class
     * QueryStringScorePairResourceImpl. String source_endpoint =
     * "http://data.szepmuveszeti.hu/sparql"; String target_endpoint =
     * "http://datos.artium.org:8890/sparql";
     *
     */
    //  @Test
    public void testGetRecommendedQueryList1() {
        System.out.println("getRecommendedQueryList");
        //String qo = "SELECT  distinct ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download .?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        String qo = "SELECT distinct ?deathEntity ?deathTimeSpan WHERE {?deathEntity a <http://erlangen-crm.org/current/E69_Death> .?deathEntity <http://erlangen-crm.org/current/P4_has_time-span> ?deathTimeSpan .}";
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

//    @Test
    public void testQueryRootDistance() {

        System.out.println("getRecommendedQueryList");
        //String qo = "SELECT  distinct ?download ?published ?this WHERE {?podcast <http://purl.org/dc/terms/published> ?published .?podcast <http://digitalbazaar.com/media/download> ?download .?podcast <http://purl.org/dc/terms/isPartOf> ?this .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";
        //String qo ="SELECT distinct ?building ?label ?buildName ?postCode WHERE {?building a <http://vocab.deri.ie/rooms#Building> .?building <http://www.w3.org/2000/01/rdf-schema#label> ?label .?building <http://www.geonames.org/ontology#name>  ?buildName .?building <http://www.geonames.org/ontology#postalCode> ?postCode .}";

        String qo = "SELECT DISTINCT ?mod ?title ?code WHERE { ?mod a <http://purl.org/vocab/aiiso/schema#Module>. ?mod <http://purl.org/dc/terms/title> ?title . ?mod <http://purl.org/vocab/aiiso/schema#code> ?code . }";

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

}