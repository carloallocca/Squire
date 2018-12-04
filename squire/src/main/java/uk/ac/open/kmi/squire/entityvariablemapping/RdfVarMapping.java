package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class RdfVarMapping extends AbstractVarMapping<Var, Node> {

	public RdfVarMapping() {
		this(null);
	}

	public RdfVarMapping(VarMapping<Var, Node> toClone) {
		super(toClone);
	}

	@Override
	public Var getOrCreateVar(Node uri, String varPrefix) {
		if (valueToVar == null || index == 0)
			throw new IllegalStateException("The mapping table needs to be initialized before use. Call init() first.");
		if (!valueToVar.containsKey(uri)) {
			// this.classVar = "ct"+Integer.toString(++index);
			Var tmp = Var.alloc(varPrefix + Integer.toString(index++));
			put(tmp, uri);
			return tmp;
		} else
			return valueToVar.get(uri);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("{");
		for (Iterator<Entry<Var, Node>> it = getVarToValueEntries().iterator(); it.hasNext();) {
			Entry<Var, Node> e = it.next();
			s.append(e.getKey() + "<-" + e.getValue());
			if (it.hasNext())
				s.append(", ");
		}
		s.append("}");
		return s.toString();
	}

}
