/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementWalker;

import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGeneralizationVisitor;

/**
 *
 * @author callocca
 */
public class SPARQLQueryGeneralization {

	private Query originalQuery;
	private Query generalizedQuery;

	private Node node;
	private Var varTemplate;

	public SPARQLQueryGeneralization() {
		super();
	}

	public SPARQLQueryGeneralization(Query q, Node n, Var varTemplate) {
		this.originalQuery = q;
		this.node = n;
		this.varTemplate = varTemplate;
	}

	public void generalizeFromNodeToVarTemplate() {
		if (originalQuery == null) {
			throw new IllegalArgumentException(
					"[SPARQLQueryGeneralization::generalize()]The Query to Generalize is null!!");
		}
		SQGeneralizationVisitor genVisitor = new SQGeneralizationVisitor(node, varTemplate);
		ElementWalker.walk(this.originalQuery.getQueryPattern(), genVisitor);
		this.generalizedQuery = this.originalQuery;
		System.out.println("[SPARQLQueryGeneralization::generalizeFromNodeToVarTemplate()]");
		System.out.println(this.generalizedQuery.toString());
	}

	public Query generalizeFromNodeToVarTemplate(Query q, Node n, Var varTemplate) {
		if (q == null || n == null || varTemplate == null) {
			throw new IllegalArgumentException(
					"[SPARQLQueryGeneralization::generalize()]The Query or the Node or the Var is null!!");
		}
		// Query newQuery=QueryFactory.create(q.toString());
		SQGeneralizationVisitor genVisitor = new SQGeneralizationVisitor(n, varTemplate);
		ElementWalker.walk(q.getQueryPattern(), genVisitor);
		// this.generalizedQuery=this.originalQuery;
		return q;
	}

	public void setOriginalQuery(Query originalQuery) {
		this.originalQuery = originalQuery;
	}

	public void setGeneralizedQuery(Query generalizedQuery) {
		this.generalizedQuery = generalizedQuery;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void setVarTemplate(Var varTemplate) {
		this.varTemplate = varTemplate;
	}

	public Query getOriginalQuery() {
		return originalQuery;
	}

	public Query getGeneralizedQuery() {
		return generalizedQuery;
	}

	public Var getVarTemplate() {
		return varTemplate;
	}

}
