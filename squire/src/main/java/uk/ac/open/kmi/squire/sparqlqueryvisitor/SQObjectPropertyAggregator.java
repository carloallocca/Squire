package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.ListIterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class SQObjectPropertyAggregator extends AbstractSQAggregator<String> {

	public SQObjectPropertyAggregator(IRDFDataset d1) {
		if (d1 == null) throw new IllegalArgumentException("The dataset cannot be null");
		this.datasetEntitySet = d1.getObjectPropertySet();
	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) throw new IllegalArgumentException("The ElementPathBlock is null");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp = it.next();
			Node predicate = tp.getPredicate();
			// PREDICATE
			if (predicate.isURI() && this.datasetEntitySet.contains(predicate.getURI()))
				this.queryEntitySet.add(predicate.getURI());
		}
	}

}
