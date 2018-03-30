/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

/**
 *
 * @author carloallocca
 */
public class VarTemplateInstanciationOp implements IOperation {

	private final Query q;
	private final String entity;
	private final Var varTemplate;

	public VarTemplateInstanciationOp(Query q, Var varTemplate, String entity) {
		this.q = q;
		this.varTemplate = varTemplate;
		this.entity = entity;
	}

	@Override
	public Object apply() {
		System.out.println(" Substitute the varTemplate" + this.varTemplate + " with the entity " + this.entity
				+ " in the query Q=" + this.q.toString());
		return null; // return this.q modified
	}

}
