/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;

/**
 *
 * @author carloallocca
 */
public class QueryRecommendatorFormTest {

    public QueryRecommendatorFormTest() {
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
     * Test of recommend method, of class QueryRecommendatorForm.
     */
//    @Test
    public void testRecommend() {

        
        try {
            String urlAddress1 = "http://data.open.ac.uk/query";
            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?mod ?title ?code "
                    + "where { "
                    + " ?mod a <http://purl.org/vocab/aiiso/schema#Module>. "
                    + " ?mod <http://purl.org/dc/terms/title> ?title . "
                    + "  ?mod <http://purl.org/vocab/aiiso/schema#code> ?code ."
                    + "}";
            
            String urlAddress2 = "https://data.ox.ac.uk/sparql/"; //oxford univeristy
            
            float resultTypeSimilarityDegree = 1;
            float queryRootDistanceDegree = 2;
            float resultSizeSimilarityDegree = 3;
            float querySpecificityDistanceDegree = 1;
            
            IRDFDataset d1 = new SPARQLEndPoint(urlAddress1, "");
            IRDFDataset d2 = new SPARQLEndPoint(urlAddress2, "");
            
            QueryRecommendatorForm instance = new QueryRecommendatorForm(qString, d1, d2,
                    resultTypeSimilarityDegree,
                    queryRootDistanceDegree,
                    resultSizeSimilarityDegree,
                    querySpecificityDistanceDegree);
            
            List<QueryStringScorePair> result = instance.recommend();
            
            System.out.println("////////////// PRINTING THE " +result.size()+ " RECOMMENDATED QUERIES ///////////////");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            if (result != null) {
                for (QueryStringScorePair q : result) {
                    System.out.println("==== [QueryRecommendatorFormTest::testRecommend] Rec. Query, Score:=" + q.getScore());
                    System.out.println(q.getQuery());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(QueryRecommendatorFormTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
