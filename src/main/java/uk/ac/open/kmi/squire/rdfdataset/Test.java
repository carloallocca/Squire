/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.ArrayList;

/**
 *
 * @author carloallocca
 */
public class Test {

    public static void main(String[] args) {

        // classSet cardinality 152
        // ObjectProperty cardinality 595
        // datatypePropertySet cardinality 264
        String urlAddress1 = "http://data.open.ac.uk/query";
        // classSet cardinality 139
        // ObjectProperty cardinality 220
        // datatypePropertySet cardinality 134
        String urlAddress2 = "https://data.ox.ac.uk/sparql/"; //oxford univeristy 

        // classSet cardinality 11
        // ObjectProperty cardinality 21
        // datatypePropertySet cardinality 5
        String urlAddress3 = "http://eurostat.linked-statistics.org/sparql";

        // classSet cardinality 39
        // ObjectProperty cardinality 70
        // datatypePropertySet cardinality 82                
        String urlAddress4 = "http://www.europeandataportal.eu/sparql";

        SPARQLEndPointBasedRDFDataset d1 = new SPARQLEndPointBasedRDFDataset(urlAddress3, "");
        SPARQLEndPointBasedRDFDataset d2 = new SPARQLEndPointBasedRDFDataset(urlAddress4, "");

        RDFDatasetSimilarity simD = new RDFDatasetSimilarity();

        float sim = simD.computeSim(d1, d2);
        System.out.println(sim);

    }

}
