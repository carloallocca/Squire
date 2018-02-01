/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.processor;

import org.apache.jena.query.Query;

/**
 *
 * @author carloallocca
 */
public class QueryOperationProcessor {

	private final Query q;
	private final IOperation op;

	public QueryOperationProcessor(Query q, IOperation op) {
		this.q = q;
		this.op = op;
	}

	public static void applyOperation(Query q, IOperation op) {
		Query newQuery = (Query) op.apply();
		// this.op.apply();
	}

}
