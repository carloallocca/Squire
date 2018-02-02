/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;
import java.util.List;

import uk.ac.open.kmi.squire.core.SQueryRecommendationWorker;
import uk.ac.open.kmi.squire.rdfdataset.AbstractedRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author callocca
 */
public class TestQTTree {

	public static void main(String args[]) {

		// This is the last testing that is working, date=02-12-2015.

		// String q1 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
		// + "prefix ex:<http://www.example.com/onto-schema#> "
		// + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		// + " SELECT DISTINCT ?s ?type where "
		// + "{ "
		// + " ex:carlo rdf:type ex:ResearchAssociate . "
		// + "?s rdf:type ?type . "
		// + "?s ex:isBossOf ex:carlo . "
		// + "?t ex:isAdvisorOf \"allocca\" . "
		// + "ex:fernando ex:isBossOf \"allocca\" . "
		// + "ex:fernando ex:isAdvisorOf ex:allocca . "
		// + "}";
		//
		// String q2 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
		// + "prefix ex:<http://www.example.com/onto-schema#> "
		// + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		// + " SELECT DISTINCT ?type where "
		// + "{ "
		// + "ex:mathieu rdf:type ex:SenionResearchFellow . "
		// + "?s rdf:type ?type . "
		// + "?s ex:isManagerOf ex:enrico . "
		// + "?t ex:isManagerOf \"motta\" . "
		// + "ex:mathieu ex:isAdvisorOf \"daquin\" . "
		// + "ex:mathieu ex:isAdvisorOf ex:daquin . "
		// + "}";

		// [QTTree::generateQTTree] subject is an individual uri[?it1
		// (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)
		// http://www.example.com/onto-schema#ResearchAssociate]
		// [QTTree::generateQTTree] object is a class uri[?it1
		// (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) ?ct1]
		// [QTTree::generateQTTree] object is a class uri[?it1
		// (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) ?ct1,
		// http://www.example.com/onto-schema#carlo
		// (<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) ?ct1]

		String q1 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?x where " + "{ "
				+ " ex:carlo rdf:type ex:ResearchAssociate . " + "}";

		String q2 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?s where " + "{ "
				+ " ?s rdf:type ex:SeniorResearchFellow . " + "?s ex:isSupervisorOf ex:carlo . " + "}";

		String q3 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?s ?type where "
				+ "{ " + " ex:carlo rdf:type ex:ResearchAssociate . " + "?s rdf:type ?type . "
				+ "?s ex:isBossOf ex:carlo . " + "?t ex:isSupervisorOf  \"allocca\" . "
				+ "ex:fernando ex:isBossOf  \"allocca\" . " + "ex:fernando ex:isSupervisorOf  ex:allocca . " + "}";
		String q4 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?s ?type where "
				+ "{ " + "?s ex:isBossOf ex:carlo . " + "?t ex:isSupervisorOf  \"allocca\" . " + "}";
		String q5 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?b ?s ?type where "
				+ "{ " + "?b ex:isSupervisorOf ex:carlo . " + "?s ex:isBossOf  ?b . " + "}";
		String q6 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?b ?s ?type where "
				+ "{ " + "?b ex:isBossOf ex:carlo . " + "}";
		String q7 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + " SELECT DISTINCT ?b ?s ?type where "
				+ "{ " + "?b ex:isBossOf ex:carlo . " + "?c ex:isLineManagerOf ex:carlo . "
				+ "?s ex:isSupervisorOf  ?b . " + "}";

		String q8 = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				+ "PREFIX dc10:  <http://purl.org/dc/elements/1.0/>"
				+ "PREFIX dc11:  <http://purl.org/dc/elements/1.1/>"
				+ "prefix ex:<http://www.example.com/onto-schema#> "
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + " SELECT DISTINCT ?x where " + "{ " + "GRAPH ?src "
				+ "    { ?x foaf:mbox <mailto:bob@work.example> . " + "      ?x foaf:nick ?title " + "    }"
				+ " ex:carlo rdf:type ex:ResearchAssociate . " + " ex:bob rdf:type ex:ResearchAssociate . "
				+ "{ ?book dc10:title  ?title } UNION { ?book dc11:title  ?title }"
				+ "OPTIONAL { ?x foaf:mbox ?title } ." + "}";

		List<String> queryList = new ArrayList<>();
		queryList.add(q1);
		queryList.add(q2);
		queryList.add(q3);

		// RDFdataset1
		List<String> classSetD1 = new ArrayList<>();
		classSetD1.add("http://www.example.com/onto-schema#PhDResearchStudent");
		classSetD1.add("http://www.example.com/onto-schema#ResearchAssociate");
		classSetD1.add("http://www.example.com/onto-schema#SeniorResearchFellow");

		List<String> objectPropertySetD1 = new ArrayList<>();
		objectPropertySetD1.add("http://www.example.com/onto-schema#isBossOf");
		objectPropertySetD1.add("http://www.example.com/onto-schema#isSupervisorOf");
		objectPropertySetD1.add("http://www.example.com/onto-schema#isLineManagerOf");

		List<String> individualSetD1 = new ArrayList<>();
		individualSetD1.add("http://www.example.com/onto-schema#mathieu");
		individualSetD1.add("http://www.example.com/onto-schema#carlo");
		individualSetD1.add("http://www.example.com/onto-schema#olga");
		individualSetD1.add("http://www.example.com/onto-schema#fernando");
		// individualSetD1.add("http://www.example.com/onto-schema#allocca");

		List<String> datatypePropertySetD1 = new ArrayList<>();
		datatypePropertySetD1.add("http://www.example.com/onto-schema#isSupervisorOf");

		List<String> literalSetD1 = new ArrayList<>();

		literalSetD1.add("allocca");
		literalSetD1.add("dacquin");

		List<String> rdfVocabulary1 = new ArrayList<>();
		rdfVocabulary1.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		// RDFdataset2
		List<String> classSetD2 = new ArrayList<>();
		classSetD2.add("http://www.example.com/onto-schema#PhDStudent");
		classSetD2.add("http://www.example.com/onto-schema#AssociatedResearcher");
		classSetD2.add("http://www.example.com/onto-schema#SeniorResearcherFellow");

		List<String> objectPropertySetD2 = new ArrayList<>();
		objectPropertySetD2.add("http://www.example.com/onto-schema#isCarOf");
		objectPropertySetD2.add("http://www.example.com/onto-schema#isManagerOf");
		objectPropertySetD2.add("http://www.example.com/onto-schema#isAdvisorOf");

		List<String> individualSetD2 = new ArrayList<>();
		individualSetD2.add("http://www.example.com/onto-schema#mathieu1");
		individualSetD2.add("http://www.example.com/onto-schema#carlo1");
		individualSetD2.add("http://www.example.com/onto-schema#olga");
		individualSetD2.add("http://www.example.com/onto-schema#enrico");

		List<String> datatypePropertySetD2 = new ArrayList<>();
		// datatypePropertySetD2.add("hasName");//=new ArrayList();;
		// datatypePropertySetD2.add("hasSurname");//=new ArrayList();;

		List<String> literalSetD2 = new ArrayList<>();
		literalSetD2.add("motta");
		literalSetD2.add("dacquin");

		List<String> rdfVocabulary2 = new ArrayList<>();
		rdfVocabulary2.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		String d1Path = null;
		String d2Path = null;

		// IRDFDataset d1=new
		// AbstractedRDFDataset(classSetD1,individualSetD1,objectPropertySetD1,datatypePropertySetD1,literalSetD1,rdfVocabulary1);
		// IRDFDataset d2=new
		// AbstractedRDFDataset(classSetD2,individualSetD2,objectPropertySetD2,datatypePropertySetD2,literalSetD2,rdfVocabulary2);
		IRDFDataset d1 = new AbstractedRDFDataset(d1Path, classSetD1, individualSetD1, objectPropertySetD1,
				datatypePropertySetD1, literalSetD1, rdfVocabulary1);
		IRDFDataset d2 = new AbstractedRDFDataset(d2Path, classSetD2, individualSetD2, objectPropertySetD2,
				datatypePropertySetD2, literalSetD2, rdfVocabulary2);

		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q2,d1,d2);
		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q3,d1,d2);
		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q4,d1,d2);

		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q7,d1,d2);
		SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q5, d1, d2);

		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q6,d1,d2);

		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q1,d1,d2);
		// sqr1.queryRecommendation1();
		// sqr1.queryRecommendation2();

		// for (String q : queryList) {
		// SQueryRecommendationWorker sqr1 = new SQueryRecommendationWorker(q,d1,d2);
		// sqr1.queryRecommendation1();
		// //Thread t = new Thread(sqr1);
		// //t.start();
		// }

	}

}
