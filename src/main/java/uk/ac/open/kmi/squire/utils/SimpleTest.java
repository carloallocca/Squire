/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author carloallocca
 */
public class SimpleTest {

    private String service = null;
//    private String apikey = null;

    public SimpleTest(String service, String apikey) {
        this.service = service;
  //      this.apikey = apikey;
    }
    public SimpleTest(String service) {
        this.service = service;
  //      this.apikey = apikey;
    }

    public String executeQuery(String queryText, String acceptFormat) throws Exception {
//        String httpQueryString = String.format("query=%s&apikey=%s",
//                URLEncoder.encode(queryText, "UTF-8"),
//                URLEncoder.encode(this.apikey, "UTF-8"));
    
        String httpQueryString = String.format("query=%s",
                URLEncoder.encode(queryText, "UTF-8"));

        URL url = new URL(this.service + "?" + httpQueryString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", acceptFormat);

        conn.connect();
        
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buff = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            buff.append(line);
            buff.append("\n");
        }
        conn.disconnect();
        return buff.toString();
    }

//        public String executeQueryNew(String queryText, String acceptFormat) throws Exception {
//                String httpQueryString = String.format("query=%s",
//                URLEncoder.encode(queryText, "UTF-8"));
//
//                URL url = new URL(this.service + "?" + httpQueryString);
//                
//                String GET_URL=url.toString();
//                
//                CloseableHttpClient httpClient = HttpClients.createDefault();
//		
//               
//                
//                HttpGet httpGet = new HttpGet(GET_URL);
//                
//               
//		//httpGet.addHeader("User-Agent", USER_AGENT);
//		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
//
//                
//		System.out.println("GET Response Status:: "
//				+ httpResponse.getStatusLine().getStatusCode());
//
//		BufferedReader reader = new BufferedReader(new InputStreamReader(
//				httpResponse.getEntity().getContent()));
//
//		String inputLine;
//		StringBuffer response = new StringBuffer();
//
//		while ((inputLine = reader.readLine()) != null) {
//			response.append(inputLine);
//		}
//		reader.close();
//
//		// print result
//		System.out.println(response.toString());
//		httpClient.close();
//
//        
//            return null;
//        }

    
    
    public static void main(String[] args) throws Exception {

        //String sparqlService = "http://collection.britishmuseum.org/sparql";                
        //String sparqlService = "http://sparql.bioontology.org/sparql";
        
        // OK 
        //String sparqlService = "http://chem2bio2rdf.org/bindingdb/sparql";
        
        // OK, but I need manually filter " FILTER (!regex(str(?p),'http://www.w3.org/1999/02/22-rdf-syntax-ns') )"+
                        //" FILTER (!regex(str(?p),'http://www.w3.org/2000/01/rdf-schema') )"+
       // String sparqlService = "http://semantic.eea.europa.eu/sparql";

//        String sparqlService = "http://openuplabs.tso.co.uk/sparql/gov-reference";
        
//        String sparqlService = "http://setaria.oszk.hu/sparql";

        // Service Temporarily Unavailable</h1><p>
       // String sparqlService = "http://data.uni-muenster.de/sparql";
        
        //String apikey = "YOUR API KEY HERE";


        //503 Service Unavailable
//        String sparqlService = "http://data.szepmuveszeti.hu/sparql";  
        
        // OK
//        String urlAddress = "http://services.data.gov.uk/education/sparql";   

      // does not work: failed: Operation timed out  
//        String urlAddress = "http://sparql.linkedopendata.it/musei";  

    // String urlAddress = "http://publicspending.net/endpoint";
    
        //OK
        // String urlAddress = "http://finance.data.gov.uk/sparql/finance/query";

//        String urlAddress = "http://sparql.data.southampton.ac.uk/";

//                String urlAddress ="http://data.upf.edu/en/sparql"; // GOOD FOR http://data.upf.edu/en/sparql_examples
   
        //String urlAddress ="HTTP://BIOPORTAL.BIO2RDF.ORG/SPARQL"; // GOOD FOR http://data.upf.edu/en/sparql_examples
        String urlAddress = "http://rijksmuseum.sealinc.eculture.labs.vu.nl/sparql/";
        
    // OK
//  String urlAddress = "http://data.ordnancesurvey.co.uk/datasets/os-linked-data/apis/sparql";
        /*
		 * More query examples here:
		 * http://sparql.bioontology.org/examples
         */
        String query =  " SELECT distinct ?p " +
                        " WHERE { " +
                        " ?s ?p ?o . " +
                        " FILTER ((isURI(?o)))"+
                        " } Limit 100";


//                        " FILTER (!regex(str(?p),'http://www.openlinksw.com/schemas/virtrdf') )"+//                        " FILTER (!regex(str(?p),'http://www.w3.org/1999/02/22-rdf-syntax-ns') )"+
//                        " FILTER (!regex(str(?p),'http://www.w3.org/2000/01/rdf-schema') )"+
//                        " FILTER (!regex(str(?p),'http://www.w3.org/2002/07/owl') )"+
//                        " FILTER (!regex(str(?p),'ukfhrs') )"+
        
        
        
        
// //                        " FILTER (!regex(str(?p),'http://www.w3.org/2002/07/owl#') )"+        
//       http://www.w3.org/2002/07/owl#complementOf
        // 

//      SimpleTest test = new SimpleTest(sparqlService, apikey);
        SimpleTest test = new SimpleTest(urlAddress);

        //Accept formats can be: "text/plain", "application/json", text/tab-separated-values
        // "application/rdfxml", "text/csv", text/tab-separated-values  application/sparql-results+xml
 //       String response = test.executeQueryNew(query, "application/sparql-results+xml");
 //       System.out.println(response);
    }
}
