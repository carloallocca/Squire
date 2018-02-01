/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;

import uk.ac.open.kmi.squire.operation.SPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.treequerypatterns.QueryRecommendation;

/**
 *
 * @author callocca
 */
public class SQueryRecommendationWorker implements IQueryRecommendation, Runnable {

	private String queryString;
	private IRDFDataset rdfd1;
	private IRDFDataset rdfd2;

	public SQueryRecommendationWorker(String qString, IRDFDataset d1, IRDFDataset d2) {
		queryString = qString;
		rdfd1 = d1;
		rdfd2 = d2;

	}

	// public List<Query> queryRecommendation(String querySPARQL) {
	@Override
	public List<Query> queryRecommendation() {

		// //Step 1. Computing the query template.
		// System.out.println("[SQueryRecommendationWorker, queryRecommendation()]We are
		// processing the query " + this.queryString);
		// IQueryTemplate qT = new SelectQueryTemplate(queryString, rdfd1, rdfd2);
		// Query qt = qT.generateQueryTemplate();
		// System.out.println("[SQueryRecommendationWorker::queryRecommendation()] This
		// is the query Template" + qt);
		//
		// //Step 2. Computing the query template.
		return null;
	}

	// public List<Query> queryRecommendation1() {
	//
	// //System.out.println("[SQueryRecommendationWorker, queryRecommendation()]We
	// are processing the query " + this.queryString);
	// // From a String to a SPARQL query Object
	// System.out.println(" ");
	// Query query = QueryFactory.create(queryString);
	// System.out.println(" ");
	//
	// System.out.println("[SQueryRecommendationWorker, queryRecommendation()]We are
	// processing the query ");
	// System.out.println(query);
	// System.out.println(" ");
	//
	// // Identify the Group Graph Patterns
	// ElementPathBlockVisitor epb = new ElementPathBlockVisitor();
	// ElementWalker.walk(query.getQueryPattern(), epb);
	// System.out.println(" ");
	// System.out.println("[SQueryRecommendationWorker, queryRecommendation()] which
	// has got the following list of triple patterns ");
	// System.out.println(epb.getElementPathBlock());
	// List<TriplePath> tpList = epb.getElementPathBlock().getPattern().getList();
	//
	// TreeNode<List<TriplePath>> root = new TreeNode(tpList, null);
	//
	// //QTTree qttree=new QTTree(root, rdfd1, rdfd2);
	// QTTree qttree = new QTTree(queryString, root, rdfd1, rdfd2);
	// qttree.generateQTTree(root);
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// qttree.evaluateQTTree(qttree.getRoot(), 0);//evaluateQTTree
	//
	// //IQueryTemplate qT = new SelectQueryTemplate(queryString, rdfd1, rdfd2);
	// //Query qt = qT.generateQueryTemplate();
	// //System.out.println("[SQueryRecommendationWorker::queryRecommendation()]
	// This is the query Template" + qt);
	// //Step 2. Computing the query template.
	// return null;
	// }

	// this is working with TreeNode<List<TriplePath>>
	// public List<Query> queryRecommendation2() {
	//
	// //System.out.println("[SQueryRecommendationWorker, queryRecommendation()]We
	// are processing the query " + this.queryString);
	// // From a String to a SPARQL query Object
	// System.out.println(" ");
	// Query query = QueryFactory.create(queryString);
	// System.out.println(" ");
	//
	// System.out.println("[SQueryRecommendationWorker, queryRecommendation()]We are
	// processing the query ");
	// System.out.println(query);
	// System.out.println(" ");
	//
	// // Identify the Group Graph Patterns
	// ElementPathBlockVisitor epb = new ElementPathBlockVisitor();
	// ElementWalker.walk(query.getQueryPattern(), epb);
	// System.out.println(" ");
	// System.out.println("[SQueryRecommendationWorker, queryRecommendation()] which
	// has got the following list of triple patterns ");
	// System.out.println(epb.getElementPathBlock());
	// List<TriplePath> tpList = epb.getElementPathBlock().getPattern().getList();
	//
	// TreeNode<List<TriplePath>> root = new TreeNode(tpList, null);
	//
	// //QTTree qttree=new QTTree(root, rdfd1, rdfd2);
	// QTTree qttree = new QTTree(queryString, root, rdfd1, rdfd2);
	// //qttree.generateQTTree(root);
	//
	// // GENRALIZE
	// qttree.generalizeToQueryTemplate();
	//
	// // building the tree of specialize queries.
	// //qttree.specializeToQueryInstance(qttree.getRootTemplate());
	// HashMap<String, String> parentPTMap = new HashMap();
	// HashMap<String, String> parentCTMap = new HashMap();
	//
	//// System.out.println("1111111111111111111
	// "+rdfd2.getPropertySet().toString());
	//// System.out.println("2222222222222222222 "+rdfd2.getClassSet().toString());
	// // SPECIALIZE
	//// qttree.specializeToQueryInstance1(qttree.getRootTemplate(),
	// qttree.specializeToQueryInstance4(qttree.getRootTemplate(),
	// rdfd2.getPropertySet(), parentPTMap,
	// rdfd2.getClassSet(), parentCTMap);
	//
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	//// qttree.evaluateQTTree(qttree.getRoot(), 0);//evaluateQTTree
	// qttree.printQTTree(qttree.getRootTemplate(), 0);//evaluateQTTree
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	//
	// qttree.evaluateQTTree(qttree.getRootTemplate(), 0);//evaluateQTTree
	//
	// return null;
	// }

	// This is working with TreeNode<Query>, but does not manage the removal
	// operation.
	// public List<Query> queryRecommendation3() {
	//
	// List<Query> resultQueryList=new ArrayList<Query>();
	//
	// Query query;
	// //...checking if the input query is parsable....
	// try {
	// query = QueryFactory.create(queryString);
	// } catch (org.apache.jena.query.QueryParseException ex) {
	// //QueryParseException
	// throw new
	// QueryParseException("[SQueryRecommendationWorker::queryRecommendation3] The
	// input query is not parsable!!!", -1, -1);
	// }
	// //...checking if the input query is satisfiable w.r.t. D1 ....
	// SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
	// if (qs.isSatisfiable(query, rdfd1)) {
	// System.out.println(query);
	// System.out.println(" ");
	//
	// QueryRecommendation qr = new QueryRecommendation(query, rdfd1, rdfd2);
	// //... generalizing the input query into a SPARLQ Template Query ....
	// qr.generalizeToQueryTemplate();
	//
	//
	// //... generalizing the input query into a SPARLQ Template Query ....
	// qr.specializeToQueryInstance();
	//
	//
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" /////////////////////// PRINTING THE TREE
	// ///////////////");
	// System.out.println(" ");
	// System.out.println(" ");
	//
	//
	// qr.printQueryTemplateTree1(qr.getRootTemplate(),
	// 0);//.printQueryTemplateTree(qr.getRootTemplate(), 0);//evaluateQTTree
	//
	//
	// } else {
	// System.out.println("[SQueryRecommendationWorker::queryRecommendation3]The
	// input query is not satisfiable w.r.t the input dataset... ");
	// }
	//
	//// // building the tree of specialize queries.
	//// //qttree.specializeToQueryInstance(qttree.getRootTemplate());
	//// HashMap<String, String> parentPTMap=new HashMap();
	//// HashMap<String, String> parentCTMap=new HashMap();
	////
	//// // SPECIALIZE
	////// qttree.specializeToQueryInstance1(qttree.getRootTemplate(),
	//// qrtree.specializeToQueryInstance4(qttree.getRootTemplate(),
	//// rdfd2.getPropertySet(), parentPTMap,
	//// rdfd2.getClassSet(),parentCTMap);
	////
	////
	////
	//
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	////// qttree.evaluateQTTree(qttree.getRoot(), 0);//evaluateQTTree
	//// qttree.printQTTree(qttree.getRootTemplate(), 0);//evaluateQTTree
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	//// System.out.println(" ");
	////
	//// qttree.evaluateQTTree(qttree.getRootTemplate(), 0);//evaluateQTTree
	// return resultQueryList;
	// }

	// This is UNDER DEVELOPMENTS and working with TreeNode<DataNode>, we are going
	// to include the management of the removal operation too.
	// public List<Query> queryRecommendation4() {
	//
	// List<Query> resultQueryList=new ArrayList<Query>();
	// Query query;
	// //...checking if the input query is parsable....
	// try {
	// query = QueryFactory.create(queryString);
	// } catch (org.apache.jena.query.QueryParseException ex) {
	// //QueryParseException
	// throw new
	// QueryParseException("[SQueryRecommendationWorker::queryRecommendation4] The
	// input query is not parsable!!!", -1, -1);
	// }
	// //...checking if the input query is satisfiable w.r.t. D1 ....
	// SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
	// if (qs.isSatisfiable(query, rdfd1)) {
	// //System.out.println(query);
	// //System.out.println(" ");
	//
	// System.out.println(" ");
	// System.out.println("[SQueryRecommendationWorker::queryRecommendation4]
	// Original Query ");
	// System.out.println(query.toString());
	//
	// QueryRecommendation qr = new QueryRecommendation(query, rdfd1, rdfd2);
	// //... generalizing the input query into a SPARLQ Template Query ....
	// qr.generalizeToQueryTemplate();
	//
	//
	// //... generalizing the input query into a SPARLQ Template Query ....
	// //qr.specializeToQueryInstance();
	//
	// System.out.println(" ");
	// System.out.println("[SQueryRecommendationWorker::queryRecommendation4] We are
	// specializing the input query ... ");
	// qr.specializeToQueryInstance1();
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" ");
	// System.out.println(" /////////////////////// PRINTING THE TREE
	// ///////////////");
	// System.out.println(" ");
	// System.out.println(" ");
	// qr.printQueryTemplateTree1(qr.getRootTemplate(), 0);
	//
	//
	// } else {
	// System.out.println("[SQueryRecommendationWorker::queryRecommendation4]The
	// input query is not satisfiable w.r.t the input dataset... ");
	// }
	//
	// return resultQueryList;
	// }

	// This is UNDER DEVELOPMENTS and working with TreeNode<DataNode>, we are going
	// to include the management of the removal operation too.
	public List<QueryScorePair> queryRecommendation5() {

		List<QueryScorePair> resultQueryList = new ArrayList();
		Query query;
		// ...checking if the input query is parsable....
		try {
			query = QueryFactory.create(queryString);
			System.out.println("");
			System.out.println("[SQueryRecommendationWorker::queryRecommendation5] THE SOURCE QUERY ");
			System.out.println("");
			System.out.println(query.toString());
		} catch (org.apache.jena.query.QueryParseException ex) { // QueryParseException
			throw new QueryParseException(
					"[SQueryRecommendationWorker::queryRecommendation5] THE SOURCE QUERY is not parsable!!!", -1, -1);
		}
		// ...checking if the input query is satisfiable w.r.t. D1 ....
		SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
		if (qs.isSatisfiable(query, rdfd1)) {

			QueryRecommendation qr = new QueryRecommendation(query, rdfd1, rdfd2);
			// ... generalizing the input query into a SPARLQ Template Query ....
			qr.generalizeToQueryTemplate();

			System.out.println(" ");
			System.out.println(
					"[SQueryRecommendationWorker::queryRecommendation5] We are specializing the input query ... ");
			qr.specializeToQueryInstance1();
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");
			System.out.println(" ");

			qr.computeRecommendateQueryScore(qr.getRootTemplate(), 0);
			// sort the result list
			Collections.sort(qr.getQueryRecommendatedList(), QueryScorePair.queryScoreComp);

			return qr.getQueryRecommendatedList();
		} else {
			System.out.println(
					"[SQueryRecommendationWorker::queryRecommendation5]The input query is not satisfiable w.r.t the input dataset... ");
		}

		return resultQueryList;
	}

	@Override
	public void run() {
		// this.queryRecommendation();
		// this.queryRecommendation1();
	}

	@Override
	public List<Query> queryRecommendation(String querySPARQL, IRDFDataset d1, IRDFDataset d2) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

	@Override
	public List<Query> queryRecommendation(String querySPARQL) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
																		// Tools | Templates.
	}

}
