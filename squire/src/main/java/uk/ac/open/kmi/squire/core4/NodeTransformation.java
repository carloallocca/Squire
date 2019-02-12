package uk.ac.open.kmi.squire.core4;

import org.apache.jena.graph.Node;

/**
 * A record of transforming every occurrence of a single RDF node, including
 * (and especially) SPARQL variables, into occurrences of another node.
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
