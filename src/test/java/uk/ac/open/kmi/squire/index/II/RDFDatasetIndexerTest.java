/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.index.II;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;

/**
 *
 * @author carloallocca
 */
public class RDFDatasetIndexerTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    // String fileName =
    // "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointlist";
    // This file contains the endpoints for the gold-standard
    // String fileName =
    // "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointTestlist";
    // String fileName =
    // "/Users/carloallocca/Desktop/KMi/KMi Started 2015/KMi2015Development/WebSquire/endpointlistNew";

    String fileName = "endpointlistNew";

    /**
     * Test of addEndPointSignature method, of class SPARQLEndPointIndexer1.
     */
    @Test
    public void testAddRDFDatasetSignature() throws Exception {

        URL res = getClass().getResource('/' + fileName);
        log.debug("Reading endpoint list from location \"{}\"", res);
        assertNotNull(res);

        InputStream
        // fstream = new FileInputStream(fileName);
        fstream = getClass().getResourceAsStream('/' + fileName);
        List<URL> endpoints = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        while ((strLine = br.readLine()) != null)
            endpoints.add(new URL(strLine));
        log.debug("Resource lists {} URLs.", endpoints.size());
        if (endpoints.isEmpty()) log.error("No endpoints found in list. Test cannot continue.");
        assertFalse(endpoints.isEmpty());

        for (URL url : endpoints) {
            log.info("Inspecting endpoint <{}>", url);
            SPARQLEndPoint endpoint = new SPARQLEndPoint(url.toString(), "");
            if (!endpoint.isIndexed()) {
                log.info(" ... NOT indexed. Will index now.");
                log.debug("Computing classes...");
                endpoint.computeClassSet();
                log.debug("Computing object properties...");
                endpoint.computeObjectPropertySet();
                log.debug("Computing datatype properties...");
                endpoint.computeDataTypePropertySet();
                log.debug("Computing RDF vocabulary...");
                endpoint.computeRDFVocabularySet();

                log.debug(" - #classes = {}", endpoint.getClassSet().size());
                log.debug(" - #OPs = {}", endpoint.getObjectPropertySet().size());
                log.debug(" - #DPs = {}", endpoint.getDatatypePropertySet().size());

                log.debug("Indexing signature...");
                RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
                instance.indexSignature(url.toString(), "", endpoint.getClassSet(),
                    endpoint.getObjectPropertySet(), endpoint.getDatatypePropertySet(),
                    endpoint.getIndividualSet(), endpoint.getLiteralSet(), endpoint.getRDFVocabulary(),
                    endpoint.getPropertySet());
                log.info("<== DONE");
            } else log.info(" ... already indexed. Skipping.");
        }

        br.close();
    }

}
