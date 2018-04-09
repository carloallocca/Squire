/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.operation;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

import uk.ac.open.kmi.squire.sparqlqueryvisitor.RemoveOpTransform;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionVisitor;

/**
 *
 * @author carloallocca
 */
public class RemoveTriple implements Operation<Query> {

	private Query query;

	private Triple triple;

	public RemoveTriple(Query q, Triple tp) {
		this.query = q;
		this.triple = tp;
	}

	@Override
	public Query apply() {
		SQGraphPatternExpressionVisitor gpeVisitorO = new SQGraphPatternExpressionVisitor();
		ElementWalker.walk(this.query.getQueryPattern(), gpeVisitorO);
		if (gpeVisitorO.getQueryGPE().size() <= 1) return this.query;
		RemoveOpTransform rOpTransform = new RemoveOpTransform(this.query, this.triple);
		Query qPostOp = QueryTransformOps.transform(this.query, rOpTransform);
		// if(qPostOp.getGroupBy()!=null){
		// //Iterator
		// orderBy=qPostOp.getGroupBy().getVars().iterator();
		// }
		// qPostOp.getAggregators()
		return qPostOp;
	}

	@Override
	public Object[] getOperands() {
		return new Object[] { query, triple };
	}

}
