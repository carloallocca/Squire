package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.syntax.ElementVisitorBase;

public abstract class AbstractSQAggregator<E> extends ElementVisitorBase {

	protected Collection<E> datasetEntitySet;

	protected Set<E> queryEntitySet = new HashSet<>();

	public Set<E> getMembersInQuery() {
		return queryEntitySet;
	}

}
