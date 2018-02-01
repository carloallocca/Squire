/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.querytemplate;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementWalker;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author callocca
 */
public class SelectQueryTemplate implements IQueryTemplate {

	private IRDFDataset rdfd1;
	private IRDFDataset rdfd2;
	private String queryString;

	public SelectQueryTemplate(String qString, IRDFDataset d1, IRDFDataset d2) {
		rdfd1 = d1;
		rdfd2 = d2;
		queryString = qString;
	}

	@Override
	public Query generateQueryTemplate() {

		Query query = QueryFactory.create(queryString);
		System.out.println("[SelectQueryTemplate::generateQueryTemplate]== before ==\n" + query);
		IQueryVisitor queryVisitor = new SQTVisitor(rdfd1, rdfd2);
		ElementWalker.walk(query.getQueryPattern(), (ElementVisitor) queryVisitor);

		System.out.println("[SelectQueryTemplate::generateQueryTemplate]== after ==\n" + query);

		return query;
	}

	@Override
	public Query generateQueryTemplate(String queryString) {
		// throw new UnsupportedOperationException("Not supported yet."); //To change
		// body of generated methods, choose Tools | Templates.

		Query query = QueryFactory.create(queryString);
		System.out.println("== before ==\n" + query);
		IQueryVisitor queryVisitor = new SQTVisitor(rdfd1, rdfd2);
		ElementWalker.walk(query.getQueryPattern(), (ElementVisitor) queryVisitor);

		// ElementWalker.walk( query.getQueryPattern(),
		// new ElementVisitorBase() {
		// @Override
		// public void visit(ElementPathBlock el) {
		// ListIterator<TriplePath> it = el.getPattern().iterator();
		// while ( it.hasNext() ) {
		// final TriplePath tp = it.next();
		// final Var s = Var.alloc( "s" );
		// if ( tp.getSubject().equals( s )) {
		// it.add( new TriplePath( new Triple( s, s, s )));
		// }
		// }
		// }
		// });
		System.out.println("== after ==\n" + query);
		return null;
	}

	@Override
	public Query generateQueryTemplate(String querySPARQL, IRDFDataset d1, IRDFDataset d2) {
		// Query query = QueryFactory.create( queryString );
		// System.out.println( "== before ==\n"+query );
		// ElementWalker.walk(query.getQueryPattern(), (ElementVisitor)
		// this.queryVisitor);
		// System.out.println( "== after ==\n"+query );
		return null;

	}

}
