package uk.ac.open.kmi.squire.fca;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Concept<O, A> {

	protected Set<O> extension = new HashSet<>();

	protected Set<A> intension = new HashSet<>();

	protected Map<Set<A>, Concept<O, A>> infs;

	public void addInferior(Concept<O, A> inf) {
		this.infs.put(inf.getIntension(), inf);
	}

	public Concept(Set<A> intension) {
		this.infs = new HashMap<>();
		this.intension = intension;
	}

	public void addInstance(O inst) {
		this.extension.add(inst);
	}

	public Set<O> getExtension() {
		return this.extension;
	}

	public Concept<O, A> getInferior(Set<A> intension) {
		return infs.get(intension);
	}

	public Collection<Concept<O, A>> getInferiors() {
		return Collections.unmodifiableCollection(this.infs.values());
	}

	public Map<Set<A>, Concept<O, A>> getInferiorsMap() {
		return this.infs;
	}

	public Set<A> getIntension() {
		return this.intension;
	}

	public double getWeight() {
		return this.intension.size();
	}

	public boolean hasInferior(Set<A> intension) {
		return infs.containsKey(intension);
	}

	public boolean isBottomConcept() {
		return this.getWeight() == 0.0;
	}

	@Override
	public String toString() {
		return this.getIntension().toString();
	}

}
