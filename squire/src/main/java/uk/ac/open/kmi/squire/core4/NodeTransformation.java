package uk.ac.open.kmi.squire.core4;

import org.apache.jena.graph.Node;

/**
 * A record of transforming every instance of one RDF node into another,
 * including (and especially) SPARQL variables.
 * 
 * @author carloallocca
 *
 */
public class NodeTransformation {

	private Node from;
	private Node to;

	public NodeTransformation(Node from, Node to) {
		this.from = from;
		this.to = to;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("{");
		s.append(getFrom());
		s.append("<-");
		s.append(getTo());
		s.append("}");
		return s.toString();
	}

}
