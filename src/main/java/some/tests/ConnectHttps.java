/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package some.tests;

import java.net.URLEncoder;

/**
 *
 * @author carloallocca
 *         http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.html
 */
public class ConnectHttps {

	public static void main(String[] args) throws Exception {

		String myQuery = "^IXIC";
		String query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o} LIMIT 10";

		//
		// URI uri = new URI( String.format(
		// query,
		// URLEncoder.encode( myQuery , "UTF8" ) ) );

		System.out.println(URLEncoder.encode(query, "UTF-8"));
	}

}
