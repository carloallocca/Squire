package uk.ac.open.kmi.squire.operation;

import org.apache.jena.query.Query;

public class TooGeneralException extends RuntimeException {

	private Query q;

	public TooGeneralException(Query q) {
		this.q = q;
	}

	public Query getQuery() {
		return q;
	}

}
