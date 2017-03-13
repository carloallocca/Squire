/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author carloallocca
 *
 * see https://github.com/ncbo/sparql-code-examples/tree/master/java
 * see SPARQL Web-Querying Infrastructure: Ready for Action?
 *
 */
public class SPARQLEndPointBasedRDFDatasetTest {

    public SPARQLEndPointBasedRDFDatasetTest() {
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
     * Test of setDatasetPath method, of class SPARQLEndPointBasedRDFDataset.
     *
     *
     */
//    @Test
//    public void testPair1() {
//
//        System.out.println("setDatasetPath");
//
//        //       String urlAddress = "http://dbpedia.org/sparql";
////        String urlAddress = "http://sparql.data.southampton.ac.uk/";
//
//        // Unexpected error making the query: org.apache.http.conn.HttpHostConnectException: Connection to http://sparql.linkedopendata.it refused
////        String urlAddress = "http://193.145.57.56:8890/sparql";
//
//        // Unexpected error making the query: org.apache.http.conn.HttpHostConnectException: Connection to http://sparql.linkedopendata.it refused
////        String urlAddress = "http://sparql.linkedopendata.it/scuole";
//        
//        String urlAddress = "http://data.linkedu.eu/don/sparql";
//
//        
//
//        
//
//        String graphName = "";
//        SPARQLEndPointBasedRDFDataset instance = new SPARQLEndPointBasedRDFDataset(urlAddress, graphName);
//
//        System.out.println("SPARQL endpoint " + urlAddress);
////        System.out.println("getClassSet " +instance.getClassSet().toString());
////        System.out.println("cardinality " +instance.getClassSet().size());
//
//        //System.out.println("getObjectPropertySet " +instance.getObjectPropertySet().toString());
//        //System.out.println("getDatatypePropertySet " +instance.getDatatypePropertySet().toString());
//        //System.out.println("getLiteralSet " +instance.getLiteralSet().toString());
//        //System.out.println("getPropertySet " +instance.getPropertySet().toString());
//        //System.out.println("getRDFVocabulary " +instance.getRDFVocabulary().toString());
//    }
    /**
     * Test of setDatasetPath method, of class SPARQLEndPointBasedRDFDataset.
     *
     * String urlAddress1 = "http://dbpedia.org/sparql"; String urlAddress2 =
     * "http://sparql.data.southampton.ac.uk/";
     */
   
    @Test
    public void newTest(){
        
        String[] urlAddressPair = new String[16];
        int j =0 ;
        
            // classSet cardinality 175
            // ObjectProperty cardinality 595
            // datatypePropertySet cardinality 264
            urlAddressPair[j] = "http://data.open.ac.uk/query";
            
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "https://data.ox.ac.uk/sparql"; 
   
            // classSet cardinality 405
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://linked.opendata.cz/sparql"; 
            
            // classSet cardinality 99
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://services.data.gov.uk/education/sparql"; 
            
            // classSet cardinality 118
            // ObjectProperty cardinality 84
            // datatypePropertySet cardinality 123
            j = j+1;
            urlAddressPair[j] = "http://data.cnr.it/sparql-proxy/"; 

            // classSet cardinality 22
            // ObjectProperty cardinality 24
            // datatypePropertySet cardinality 52
            j = j+1;
            urlAddressPair[j] = "http://data.aalto.fi/sparql"; 
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://sparql.data.southampton.ac.uk/"; 
             
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://www4.wiwiss.fu-berlin.de/eurostat/sparql"; 

            // classSet cardinality 12
            // ObjectProperty cardinality 22
            // datatypePropertySet cardinality 6
            j = j+1;
            urlAddressPair[j] = "http://eurostat.linked-statistics.org/sparql"; 
 
            // classSet cardinality 415
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality
            // j = j+1;
            // urlAddressPair[j] = "https://query.wikidata.org/sparql"; 
 
            // classSet cardinality 35
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality
            // j = j+1;
            // urlAddressPair[j] = "http://data.ordnancesurvey.co.uk/datasets/os-linked-data/apis/sparql"; 
 
            // classSet cardinality 1000
            // ObjectProperty cardinality 142
            // datatypePropertySet cardinality 234
            // j = j+1;
            // urlAddressPair[j] = "https://www.ebi.ac.uk/rdf/services/biosamples/sparql"; 

            // classSet cardinality 7
            // ObjectProperty cardinality 4
            // datatypePropertySet cardinality 4
            j = j+1;
            urlAddressPair[j] = "http://dlib-sparql.edina.ac.uk/endpoint/"; 

            // classSet cardinality 10
            // ObjectProperty cardinality 9
            // datatypePropertySet cardinality 38
            j = j+1;
            urlAddressPair[j] = "http://lod.springer.com/sparql"; 
 
            // classSet cardinality 27
            // ObjectProperty cardinality 37
            // datatypePropertySet cardinality 23
            j = j+1;
            urlAddressPair[j] = "http://edan.si.edu/saam/sparql"; 

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://collection.britishart.yale.edu/sparql/"; 
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://publicspending.net/endpoint"; 
 
            // classSet cardinality 29
            // ObjectProperty cardinality 95
            // datatypePropertySet cardinality 104
            j = j+1;
            urlAddressPair[j] = "http://sparql.asn.desire2learn.com:8890/sparql"; 

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://chem2bio2rdf.org/bindingdb/sparql"; 
 
            // classSet cardinality 45
            // ObjectProperty cardinality 66
            // datatypePropertySet cardinality 25
            j = j+1;
            urlAddressPair[j] = "http://ldf.fi/finlex/sparql"; 
 
            // classSet cardinality 62
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://finance.data.gov.uk/sparql/finance/query"; 

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://lsd.taxonconcept.org/sparql"; 

            // classSet cardinality 601
            // ObjectProperty cardinality 798
            // datatypePropertySet cardinality 3803
            // j = j+1;
            // urlAddressPair[j] = "http://semantic.eea.europa.eu/sparql"; 
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://ecowlim.tfri.gov.tw/sparql/query"; 
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://data.linkededucation.org/request/lak-conference/sparql"; 
            
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://leonard:7002/sparql/"; 
            
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://services.data.gov.uk/education/sparql"; 

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://sparql.linkedopendata.it/musei";

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://collection.britishmuseum.org/Sparql";
            
            // classSet cardinality 89
            // ObjectProperty cardinality 114
            // datatypePropertySet cardinality 104
            j = j+1;
            urlAddressPair[j] = "http://data.szepmuveszeti.hu/sparql";
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://datos.artuim.org/sparql";

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://visualdataweb.infor.uva.es/sparql";
            
            // classSet cardinality 85
            // ObjectProperty cardinality 94
            // datatypePropertySet cardinality 76
            j = j+1;
            urlAddressPair[j] = "http://ldf.fi/ww1lod/sparql";
  
            // classSet cardinality 103
            // ObjectProperty cardinality 97
            // datatypePropertySet cardinality 77
            j = j+1;
            urlAddressPair[j] = "http://linkedarc.net/sparql";

            // classSet cardinality 36
            // ObjectProperty cardinality 199
            // datatypePropertySet cardinality 76
            j = j+1;
            urlAddressPair[j] = "http://rijksmuseum.sealinc.eculture.labs.vu.nl/sparql/";
 
            // classSet cardinality 90
            // ObjectProperty cardinality 109
            // datatypePropertySet cardinality 112
            j = j+1;
            urlAddressPair[j] = "http://aliada.scanbit.net:8891/sparql";

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://openuplabs.tso.co.uk/sparql/gov-crime";

            // classSet cardinality 4
            // ObjectProperty cardinality 11
            // datatypePropertySet cardinality 31
            j = j+1;
            urlAddressPair[j] = "http://greek-lod.auth.gr/police/sparql";
 
            // classSet cardinality 25
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://setaria.oszk.hu/sparql";

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://data.uni-muenster.de/sparql";            

            // classSet cardinality 598
            // ObjectProperty cardinality 6908
            // datatypePropertySet cardinality 10000
            //  = j+1;
            // urlAddressPair[j] = "http://serendipity.utpl.edu.ec/lod/sparql"; 
            
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://lod.openlinksw.com/sparql/";            
            
            // classSet cardinality 106
            // ObjectProperty cardinality 376
            // datatypePropertySet cardinality 299
            j = j+1;
            urlAddressPair[j] = "http://www.semantic-systems-biology.org/biogateway/endpoint";     
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j= j+1;
            // urlAddressPair[j] = "http://wifo5-03.informatik.uni-mannheim.de/drugbank/snorql/";
 
            // classSet cardinality 174
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j= j+1;
            // urlAddressPair[j] = "http://sparql.uniprot.org/";
 
            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j= j+1;
            // urlAddressPair[j] = "http://data.bnf.fr/sparql/";
            
            // classSet cardinality 1905
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://linkedgeodata.org/sparql/"; 
            
            // classSet cardinality 10000
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://dbpedia.org/sparql"; 

            // classSet cardinality 
            // ObjectProperty cardinality 
            // datatypePropertySet cardinality 
            // j = j+1;
            // urlAddressPair[j] = "http://collection.britishmuseum.org/sparql"; 
            
        for (int i = 0; i < urlAddressPair.length; i++) {
            System.out.println(" ");
            System.out.println("[newTest] SPARQL endpoint " + urlAddressPair[i]);
            String graphName = "";
            SPARQLEndPointBasedRDFDataset instance = new SPARQLEndPointBasedRDFDataset(urlAddressPair[i], graphName);
            System.out.println(" ");
            
        }
    }
    
//    @Test
//    public void testPair2() {
//
//        // NOT WORKING SPARQL END POINT :
//        //String urlAddress = "http://sparql.data.southampton.ac.uk/";
//        //String urlAddress = "http://linkedgeodata.org/sparql/";
//        
//        //HTTP 503 error making the query: Service Unavailable
//        //String urlAddress = "http://www4.wiwiss.fu-berlin.de/eurostat/sparql";
//        
//        //String urlAddress = "https://query.wikidata.org/sparql";
//        
//        // Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
//        // String urlAddress ="http://data.nature.com/query";
//        
//        //HTTP 504 error making the query: GATEWAY_TIMEOUT
//        //String urlAddress = "http://data.ordnancesurvey.co.uk/datasets/os-linked-data/apis/sparql";
//        // Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
//        //String urlAddress = "http://collection.britishart.yale.edu/sparql/";
//        // Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
//        //String urlAddress = "http://publicspending.net/endpoint";
//        //Unexpected error making the query: org.apache.http.conn.HttpHostConnectException: Connection to http://sparql.asn.desire2learn.com:8890 refused
//        //String urlAddress = "http://sparql.asn.desire2learn.com:8890/sparql";
//        // https://chem2bio2rdf.wikispaces.com/multiple+sources   
//        //Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
//        //String urlAddress = "http://chem2bio2rdf.org/bindingdb/sparql";
//        // String urlAddress = "http://ldf.fi/finlex/sparql";
//        // Not a JSON object START: [null]
//        // String urlAddress = "http://finance.data.gov.uk/sparql/finance/query";
//        // Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
//        //String urlAddress = "http://openuplabs.tso.co.uk/sparql/gov-reference";
////        String urlAddress = "http://lsd.taxonconcept.org/sparql";
//        // classSet cardinality 552
//        // objectPropertySet cardinality so far: 450
//        // HTTP 502 error making the query: Proxy Error
////       String urlAddress = "http://semantic.eea.europa.eu/sparql";
//        //String urlAddress = "http://ecowlim.tfri.gov.tw/sparql/query";
//        //classSet cardinality 1163
////        String urlAddress = "http://linkedgeodata.org/sparql";
//        //NOT available    
////        String urlAddress = "http://data.linkededucation.org/request/lak-conference/sparql";
//        // Failed to connect to SPARQL endpoint , http://seek.rkbexplorer.com/sparql/
//        //String urlAddress = "http://leonard:7002/sparql/";
//        // cardinality 98
//        //objectPropertySet cardinality so far: 50
//        // Time elapsed: 41.503 sec  <<< ERROR!
////        String urlAddress = "http://services.data.gov.uk/education/sparql";   
////        String urlAddress = "http://sparql.linkedopendata.it/musei";
////        String urlAddress = "http://collection.britishmuseum.org/Sparql";
//        
//        // Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
////        String urlAddress = "http://openuplabs.tso.co.uk/sparql/gov-crime";
//        
////        String urlAddress5 = "http://data.szepmuveszeti.hu/sparql";  
////        String urlAddress = "http://visualdataweb.infor.uva.es/sparql";
////        String urlAddress = "http://aliada.scanbit.net:8891/sparql"; //connession refused
////        String urlAddress = "http://setaria.oszk.hu/sparql";
////        String urlAddress = "http://data.uni-muenster.de/sparql";
//        // I stopped at datatypePropertySet cardinality so far: 2400
// //       String urlAddress = "http://serendipity.utpl.edu.ec/lod/sparql";
//
//        //String urlAddress11 = "http://collection.britishmuseum.org/sparql";
//
//                        // Endpoint returned Content-Type: text/html which is not currently supported for SELECT queries
//        //String urlAddress ="http://data.nature.com/sparql";
//
//        
//
//         
//        //SPARQL ENDPOINT DATASET
//        
//        //1. HIGH EDUCTATION
//       
//                // classSet cardinality 152
//                // ObjectProperty cardinality 595
//                // datatypePropertySet cardinality 264
//                String urlAddress1 = "http://data.open.ac.uk/query";
//                // classSet cardinality 139
//                // ObjectProperty cardinality 220
//                // datatypePropertySet cardinality 134
//                String urlAddress2 = "https://data.ox.ac.uk/sparql/"; //oxford univeristy 
// 
//        // 2. GOVERNMENT STATISTICS 1
//
//                // classSet cardinality 11
//                // ObjectProperty cardinality 21
//                // datatypePropertySet cardinality 5
//                String urlAddress3 = "http://eurostat.linked-statistics.org/sparql";
//                
//
//                // classSet cardinality 39
//                // ObjectProperty cardinality 70
//                // datatypePropertySet cardinality 82                
//                String urlAddress4 = "http://www.europeandataportal.eu/sparql";
//                
//               
//        
//        // 3. POLICE-CRIME         
//                
//                // classSet cardinality 3
//                // ObjectProperty cardinality 10
//                // datatypePropertySet cardinality 30       
//                String urlAddress5 = "http://greek-lod.auth.gr/police/sparql";
//                // classSet cardinality 12
//                // ObjectProperty cardinality 4
//                // datatypePropertySet cardinality 4                 
//                String urlAddress6 = "http://gov.tso.co.uk/crime/sparql"; // This is very good, it as query examples
//                
//                //        http://gov.tso.co.uk/crime/sparql
//                
//        // 4. GEO
//                // classSet cardinality 38
//                // ObjectProperty cardinality 41
//                // datatypePropertySet cardinality 24
//                String urlAddress7 = "http://os.services.tso.co.uk/geo/sparql"; // This is very good, it as query examples
//
//                // classSet cardinality 190
//                // ObjectProperty cardinality 60
//                // datatypePropertySet cardinality 80
//                String urlAddress8="http://geo.linkeddata.es/sparql";
//                
//                //String urlAddress111 = "http://linkedgeodata.org/sparql";
// 
//        // 5. GOVERNMENT STATISTICS 2
//
//                // classSet cardinality 11
//                // ObjectProperty cardinality 21
//                // datatypePropertySet cardinality 5
//                String urlAddress9 = "http://eurostat.linked-statistics.org/sparql";
//                
//                
//                // classSet cardinality 4
//                // ObjectProperty cardinality 50
//                // datatypePropertySet cardinality 8
//                String urlAddress10 = "http://gov.tso.co.uk/statistics/sparql"; // This is very good, it as query examples
//        
//                
//                
//       
//                
//                
//                
//                
//        // 6. MUSEUM AND ART
//                
//                // classSet cardinality 107
//                // ObjectProperty cardinality 122
//                // datatypePropertySet cardinality 106
//                // Time = 5.475 sec
//                String urlAddress11 = "http://datos.artium.org/sparql";
//
//                // DOES NOT MANAGE "ORDER BY" 
//                // classSet cardinality 35
//                // ObjectProperty cardinality  ?
//                // datatypePropertySet cardinality ?                
//                String urlAddress12 = "http://rijksmuseum.sealinc.eculture.labs.vu.nl/sparql/";
//                               
//
//
//        
//        // 7. BIO DOMAIN
//
//                // classSet cardinality ?
//                // ObjectProperty cardinality ? 
//                // datatypePropertySet cardinality ? 
//                String urlAddress13 = "http://wwwdev.ebi.ac.uk/rdf/services/atlas/sparql";
//        
//                // classSet cardinality 30
//                // ObjectProperty cardinality 44
//                // datatypePropertySet cardinality 25
//                String urlAddress14 = "http://edan.si.edu/saam/sparql";
// 
//                //parser
//                String urlAddress15 = "http://chem2bio2rdf.org/bindingdb/sparql";
//                
// 
//        // 8. EDUCATION
//
//                // classSet cardinality 93
//                // ObjectProperty cardinality 58
//                // datatypePropertySet cardinality 149
//                String urlAddress16 = "http://gov.tso.co.uk/education/sparql"; // This is very good, it as query examples
//               
//                // classSet cardinality 98
//                // ObjectProperty cardinality ?
//                // datatypePropertySet cardinality ?
//                String urlAddress17 = "http://services.data.gov.uk/education/sparql";   
//
//        
//                
//        // 9. ARCHAEOLOGICAL DOMAIN  AND World War I as Linked Open Data  (CIDOC-CRM)    
//        
//                // classSet cardinality 84
//                // ObjectProperty cardinality 93
//                // datatypePropertySet cardinality 75       
//                String urlAddress18 = "http://ldf.fi/ww1lod/sparql";               
//                // classSet cardinality 99
//                // ObjectProperty cardinality 88
//                // datatypePropertySet cardinality 66
//                String urlAddress19 = "http://linkedarc.net/sparql";  
//
//                
//        
//        // 10. ENVIRONMENT
//        
//                // classSet cardinality 67
//                // ObjectProperty cardinality 68
//                // datatypePropertySet cardinality 53
//                String urlAddress20 = "http://gov.tso.co.uk/environment/sparql"; // This is very good, it as query examples
//                
//                // classSet cardinality 552
//                // ObjectProperty cardinality ?
//                // datatypePropertySet cardinality ?
//
//                // OK, but I need manually filter " FILTER (!regex(str(?p),'http://www.w3.org/1999/02/22-rdf-syntax-ns') )"+
//                        //" FILTER (!regex(str(?p),'http://www.w3.org/2000/01/rdf-schema') )"+
//                String urlAddress21 = "http://semantic.eea.europa.eu/sparql";
//
//
//
//        
//        // 11. UK-GOV-TRANSPORT
//                // classSet cardinality 107
//                // ObjectProperty cardinality ?
//                // datatypePropertySet cardinality ?
//                String urlAddress22 = "http://gov.tso.co.uk/transport/sparql"; // This is very good, it as query examples
//        
//        // 12. UK-GOV-PATENTS
//                // classSet cardinality 8
//                // ObjectProperty cardinality 10
//                // datatypePropertySet cardinality 12        
//                String urlAddress23 = "http://gov.tso.co.uk/patents/sparql"; // This is very good, it as query examples
//        
//        // 13. UK-GOV-BUSINESS 
//                // classSet cardinality 3
//                // ObjectProperty cardinality 7
//                // datatypePropertySet cardinality 22                
//                String urlAddress24 = "http://gov.tso.co.uk/business/sparql"; // This is very good, it as query examples
//        
//        // 14. UK-GOV-RESEARCH
//                // classSet cardinality 14
//                // ObjectProperty cardinality 13
//                // datatypePropertySet cardinality 31                
//                String urlAddress25 = "http://gov.tso.co.uk/research/sparql"; // This is very good, it as query examples
//        
//        
//        // 15. 
//               
//        String urlAddress26 = "http://worldbank.270a.info/sparql";
//        String urlAddress27 = "http://uis.270a.info/sparql"; 
//   
//        
//        // HTTP 502 error making the query: Proxy Error
////       String urlAddress = "http://openlifedata.org/ncbigene/sparql/";//http://semantic.eea.europa.eu/sparql";
//        
////        String urlAddress = "http://sparql.data.southampton.ac.uk/";
//
//        
//
//        // Service Temporarily Unavailable
////        String urlAddress ="http://resrev.ilrt.bris.ac.uk/data-server-workshop/sparql";        
//        
//           // Aalto University
////           String urlAddress ="http://data.aalto.fi/sparql"; 
//           
//           //String urlAddress ="http://linked.opendata.cz/sparql"; 
//           
////           String urlAddress ="http://data.upf.edu/en/sparql"; // GOOD FOR http://data.upf.edu/en/sparql_examples ///http://linkeduniversities.org/lu/index.php/datasets-and-endpoints/
//           
//
//        //
////        String urlAddress = "http://biomodels.bio2rdf.org/sparql"; // 
////          String urlAddress = "http://bioportal.bio2rdf.org/sparql"; // 
//       
//        
//
//        
//                         
////        String[] urlAddressPair = {urlAddress1, urlAddress2, urlAddress4, urlAddress5, urlAddress6, 
////                                    urlAddress7, urlAddress8, urlAddress9, urlAddress10, urlAddress27}; 
//
//                  String[] urlAddressPair = {urlAddress7}; 
////          String[] urlAddressPair = {urlAddress9}; 
////          String[] urlAddressPair = {urlAddress1};
////          String[] urlAddressPair = {urlAddress27};
// //         String[] urlAddressPair = {urlAddress26};
//
//        
////        String[] urlAddressPair = { "http://gov.tso.co.uk/patents/sparql", "http://reference.data.gov.uk/organograms/sparql", "http://gov.tso.co.uk/education/sparql"};
//
////        String[] urlAddressPair = {urlAddress3,urlAddress1, urlAddress2, 
////            urlAddress4, urlAddress5, urlAddress6, urlAddress7, urlAddress8, urlAddress9};
//        
//        for (int i = 0; i < urlAddressPair.length; i++) {
//            System.out.println(" ");
//            System.out.println("[testPair2] SPARQL endpoint " + urlAddressPair[i]);
//            String graphName = "";
//            SPARQLEndPointBasedRDFDataset instance = new SPARQLEndPointBasedRDFDataset(urlAddressPair[i], graphName);
//            System.out.println(" ");
//            
//        }
//
//        
////        
////        for (int i = 0; i < urlAddressPair.length; i++) {
////            System.out.println(" ");
////            System.out.println("[testPair2] SPARQL endpoint Getting the Signature " + urlAddressPair[i]);
////            String graphName = "";
////            SPARQLEndPointBasedRDFDataset instance = new SPARQLEndPointBasedRDFDataset(urlAddressPair[i], graphName);
////            
////            System.out.println("getClassSet " +instance.getClassSet().toString());
////            System.out.println("getObjectPropertySet " +instance.getObjectPropertySet().toString());
////            System.out.println("getDatatypePropertySet " +instance.getDatatypePropertySet().toString());
////        
////
////            
////            
////            System.out.println(" ");
////            
////            
////        }
//
//        
//        
//        
//        
//        
//    }

}
