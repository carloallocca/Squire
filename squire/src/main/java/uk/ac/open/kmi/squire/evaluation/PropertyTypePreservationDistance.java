package uk.ac.open.kmi.squire.evaluation;

import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_DT;
import static uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform.TEMPLATE_VAR_PROP_OBJ;

import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

public class PropertyTypePreservationDistance {

	public float compute(VarMapping<Var, Node> transformations, IRDFDataset target) {

		float dist = 0f;

		for (Entry<Var, Node> e : transformations.getVarToValueEntries()) {
			String vname = e.getKey().getName();
			String nname = e.getValue().getURI();
			if ((vname.startsWith(TEMPLATE_VAR_PROP_OBJ) && target.isInDatatypePropertySet(nname))
					|| (vname.startsWith(TEMPLATE_VAR_PROP_DT) && target.isInObjectPropertySet(nname))) {
				dist += 1 / transformations.count();
			}
		}

		return dist;
	}
}
