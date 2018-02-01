/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;

/**
 *
 * @author callocca
 */
public class Test {

	public static void main(String args[]) {

		String q1 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?x where " + "{ "
				+ " ex:carlo rdf:type ex:ResearchAssociate . " + " ex:bob rdf:type ex:ResearchAssociate . " + "}";

		// Query query = QueryFactory.create(q1);
		//
		// Node node=
		// NodeFactory.createURI("http://www.example.com/onto-schema#ResearchAssociate");
		// Var varTemplate= Var.alloc("ct1");
		//
		// //SPARQLQueryGeneralization qg= new SPARQLQueryGeneralization(query, node,
		// varTemplate);
		// SPARQLQueryGeneralization qg= new SPARQLQueryGeneralization();
		// Query genQuery=qg.generalizeFromNodeToVarTemplate(query, node, varTemplate);
		// System.out.println("[Test::main()]");
		// System.out.println(query.toString());
		// System.out.println(genQuery.toString());
		//
		String q2 = "PREFIX dc10:  <http://purl.org/dc/elements/1.0/>"
				+ "PREFIX dc11:  <http://purl.org/dc/elements/1.1/>" +

				"SELECT ?title" + "WHERE  { { ?book dc10:title  ?title } UNION { ?book dc11:title  ?title } }";

		String q3 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				+ "PREFIX dc10:  <http://purl.org/dc/elements/1.0/>"
				+ "PREFIX dc11:  <http://purl.org/dc/elements/1.1/>"
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + " SELECT DISTINCT ?x where " + "{ " + "GRAPH ?src "
				+ "    { ?x foaf:mbox <mailto:bob@work.example> . " + "      ?x foaf:nick ?title " + "    }"
				+ " ex:carlo rdf:type ex:ResearchAssociate . " + " ex:bob rdf:type ex:ResearchAssociate . "
				+ "{ ?book dc10:title  ?title } UNION { ?book dc11:title  ?title }"
				+ "OPTIONAL { ?x foaf:mbox ?title } ." + "}";

		Query query3 = QueryFactory.create(q3);

		Node node2 = NodeFactory.createURI("http://www.example.com/onto-schema#ResearchAssociate");
		Var varTemplate2 = Var.alloc("title");

		Node node3 = NodeFactory.createURI("http://www.example.com/onto-schema#carlo");
		Var varTemplate3 = Var.alloc("it1");

		SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
		Query intQuery2 = qi.instantiateVarTemplate(query3, varTemplate2, node2);
		System.out.println("[Test::main()]");
		// System.out.println(query2.toString());
		System.out.println(intQuery2.toString());

		// SPARQLQueryGeneralization qg= new SPARQLQueryGeneralization();
		// Query genQuery=qg.generalizeFromNodeToVarTemplate(query3, node3,
		// varTemplate3);
		// System.out.println(query3.toString());

	}

}
