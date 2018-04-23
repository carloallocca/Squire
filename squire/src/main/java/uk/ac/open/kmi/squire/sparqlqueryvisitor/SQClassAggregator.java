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
public class SQClassAggregator extends AbstractSQAggregator<String> {

	public SQClassAggregator(IRDFDataset d1) {
		if (d1 == null) throw new IllegalArgumentException("The dataset cannot be null");
		this.datasetEntitySet = d1.getClassSet();
	}

	@Override
	public void visit(ElementPathBlock el) {
		if (el == null) throw new IllegalArgumentException("The ElementPathBlock is null");
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp = it.next();
			Node subject = tp.getSubject();
			// SUBJECT
			if (subject.isURI() && this.datasetEntitySet.contains(subject.getURI()))
				this.queryEntitySet.add(subject.getURI());
			// OBJECT
			Node object = tp.getObject();
			if (object.isURI() && this.datasetEntitySet.contains(object.getURI()))
				this.queryEntitySet.add(object.getURI());
		}
	}

}
