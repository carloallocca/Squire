package uk.ac.open.kmi.squire.fca;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.core4.GeneralizedQuery;

public class GQVConcept extends Concept<GeneralizedQuery, Var> {

	public GQVConcept(Set<Var> intension) {
		super(intension);
	}

}
