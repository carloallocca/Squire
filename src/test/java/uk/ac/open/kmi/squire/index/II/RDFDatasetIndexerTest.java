/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.index.II;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.loggers.SPARQLEndPointIndexLogger;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;

/**
 *
 * @author carloallocca
 */
public class RDFDatasetIndexerTest {

    public RDFDatasetIndexerTest() {
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
    
    
    //org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

    /**
     * Test of addEndPointSignature method, of class SPARQLEndPointIndexer1.
     */
    @Test
    public void testAddRDFDatasetSignature() {
        FileInputStream fstream = null;
        
//        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
//        Log.setLog4j("jena-log4j.properties");
        
        
//        String indexDir = "/Users/carloallocca/Desktop/SPARQEndPointIndex";
        try {

//            RDFDatasetIndexer indexerInstance = RDFDatasetIndexer.getInstance();
            //RDFDatasetIndexer indexerInstance = RDFDatasetIndexer.getInstance(indexDir);
            System.out.println("[RDFDatasetIndexerIITest::testAddEndPointSignature2222]");
            //String indexDir="/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/Led2Pro/SPARQEndPointIndex";
            //String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointlist";
            //This file contains the endpoints for the gold-standard
            String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointlistNew";
            //String fileName = "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointTestlist";
            
            

            
            // Open the file
            fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;

            try {
                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    SPARQLEndPoint endPoint = new SPARQLEndPoint(strLine, "");
                    if (!endPoint.isIndexed()) {
                        SPARQLEndPointIndexLogger.CONSOLE_LOGGER.log(Level.INFO, "the endpoint {0} does not exists ", strLine);
                        endPoint.computeClassSet();
                        
                        System.out.println("classes");
                        System.out.println(endPoint.getClassSet().toString());
                        
                        endPoint.computeObjectPropertySet();
                        System.out.println("Obj");
                        System.out.println(endPoint.getObjectPropertySet().toString());

                        endPoint.computeDataTypePropertySet();
                        System.out.println("dt");
                        System.out.println(endPoint.getDatatypePropertySet().toString());

                        endPoint.computeRDFVocabularySet();

                        //index the signature
                        RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
                        instance.indexSignature(strLine, "",
                                endPoint.getClassSet(),
                                endPoint.getObjectPropertySet(),
                                endPoint.getDatatypePropertySet(),
                                endPoint.getIndividualSet(),
                                endPoint.getLiteralSet(),
                                endPoint.getRDFVocabulary(),
                                endPoint.getPropertySet());
                    } else {
                        SPARQLEndPointIndexLogger.CONSOLE_LOGGER.log(Level.INFO, "the endpoint {0} already exists", strLine);
                    }

                }
            } catch (IOException ex) {
//                System.out.println("[RDFDatasetIndexerTest::testAddEndPointSignature] TEST:CARLO 1");
                Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE, "TEST:IOException ", ex);
            } catch (Exception ex) {
//                System.out.println("[RDFDatasetIndexerTest::testAddEndPointSignature] TEST:CARLO 2");
                Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE, "TEST:Exception", ex);
            }
            br.close();
        } catch (FileNotFoundException ex) {
//            System.out.println("[RDFDatasetIndexerTest::testAddEndPointSignature] TEST:CARLO 3");
            Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE, "TEST:FileNotFoundException ", ex);
        } catch (IOException ex) {
//            System.out.println("[RDFDatasetIndexerTest::testAddEndPointSignature] TEST:CARLO 4");
            Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE, "TEST:IOException 2", ex);
        } finally {
            try {
//                System.out.println("[RDFDatasetIndexerTest::testAddEndPointSignature] TEST:CARLO 5");
                if (fstream != null) {
                    fstream.close();
                }
            } catch (IOException ex) {
//                System.out.println("[RDFDatasetIndexerTest::testAddEndPointSignature] TEST:CARLO 7");
                Logger.getLogger(RDFDatasetIndexerTest.class.getName()).log(Level.SEVERE, "TEST:IOException 2", ex);
            }
        }

    }

//    /**
//     * Test of addSignature method, of class RDFDatasetIndexerII.
//     */
//    @Test
//    public void testAddRDFDatasetSignatureGet() throws Exception {
//        System.out.println("addSignature");
//        String urlAddress = "";
//        String graphName = "";
//        ArrayList<String> classSet = null;
//        ArrayList<String> objectPropertySet = null;
//        ArrayList<String> datatypePropertySet = null;
//        ArrayList<String> individualSet = null;
//        ArrayList<String> literalSet = null;
//        ArrayList<String> rdfVocabulary = null;
//        ArrayList<String> propertySet = null;
//                       //index the signature
//        RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
//        
//        Document d=instance.getSignature("http://data.open.ac.uk/query", "");
//        String classes=d.get("ClassSet");
//        
//        System.out.println("gggggggggggg "+classes);
//                        
//         
//    }
    
//
//    
//    
//    
//    /**
//     * Test of getSignature method, of class RDFDatasetIndexerII.
//     */
//    @Test
//    public void testGetSPARQLEndPointSignature() throws Exception {
//        System.out.println("getSignature");
//        String urlAddress = "";
//        String graphName = "";
//        RDFDatasetIndexerII instance = null;
//        Document expResult = null;
//        Document result = instance.getSignature(urlAddress, graphName);
//    }
}
