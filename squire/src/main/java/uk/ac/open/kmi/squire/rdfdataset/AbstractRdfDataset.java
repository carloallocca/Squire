package uk.ac.open.kmi.squire.rdfdataset;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author Alessandro Adamou <alexdma@apache.org>
 *
 */
public abstract class AbstractRdfDataset implements IRDFDataset {

	protected Map<String, ClassSignature> classSignatures = new HashMap<>();
	protected Set<String> datatypePropertySet = new HashSet<>();
	protected Set<String> individualSet = new HashSet<>();
	protected Set<String> literalSet = new HashSet<>();
	protected Set<String> objectPropertySet = new HashSet<>();

	protected Map<String, Map<String, Integer>> propertyCoOc = new HashMap<>();

	protected Set<String> rdfVocabulary = new HashSet<>();

	@Override
	public void clear() {
		this.classSignatures.clear();
		this.objectPropertySet.clear();
		this.datatypePropertySet.clear();
		this.individualSet.clear();
		this.literalSet.clear();
		this.rdfVocabulary.clear();
	}

	@Override
	public Set<String> getClassSet() {
		return Collections.unmodifiableSet(classSignatures.keySet());
	}

	@Override
	public Map<String, ClassSignature> getClassSignatures() {
		return this.classSignatures;
	}

	@Override
	public int getCoOccurrences(String property1, String property2) {
		if (propertyCoOc.containsKey(property1) && propertyCoOc.get(property1).containsKey(property2))
			return propertyCoOc.get(property1).get(property2).intValue();
		else if (propertyCoOc.containsKey(property2) && propertyCoOc.get(property2).containsKey(property1))
			return propertyCoOc.get(property2).get(property1).intValue();
		return 0;
	}

	@Override
	public Map<String, Integer> getCoOccurringProperties(String property) {
		if (propertyCoOc.containsKey(property)) return Collections.unmodifiableMap(propertyCoOc.get(property));
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getDatatypePropertySet() {
		return Collections.unmodifiableSet(datatypePropertySet);
	}

	@Override
	public Set<String> getIndividualSet() {
		return Collections.unmodifiableSet(individualSet);
	}

	@Override
	public Set<String> getLiteralSet() {
		return Collections.unmodifiableSet(literalSet);
	}

	@Override
	public Set<String> getObjectPropertySet() {
		return Collections.unmodifiableSet(objectPropertySet);
	}

	/**
	 * Can be overridden if necessary. For example, implementations may fall back to
	 * this general property set if they are unable to distinguish datatype and
	 * object properties by usage.
	 */
	@Override
	public Set<String> getPropertySet() {
		Set<String> result = new HashSet<>();
		result.addAll(getDatatypePropertySet());
		result.addAll(getObjectPropertySet());
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Set<String> getRDFVocabulary() {
		return Collections.unmodifiableSet(rdfVocabulary);
	}

	@Override
	public boolean isInClassSet(String classUri) {
		return classSignatures.containsKey(classUri);
	}

	@Override
	public boolean isInDatatypePropertySet(String dpUri) {
		return datatypePropertySet.contains(dpUri);
	}

	@Override
	public boolean isInIndividualSet(String indUri) {
		return individualSet.contains(indUri);
	}

	@Override
	public boolean isInLiteralSet(String lit) {
		return literalSet.contains(lit);
	}

	@Override
	public boolean isInObjectPropertySet(String opUri) {
		return objectPropertySet.contains(opUri);
	}

	@Override
	public boolean isInPropertySet(String propertyUri) {
		return datatypePropertySet.contains(propertyUri) || objectPropertySet.contains(propertyUri);
	}

	@Override
	public boolean isInRDFVocabulary(String rdfEntity) {
		return rdfVocabulary.contains(rdfEntity);
	}

	protected Map<String, Map<String, Integer>> buildPropertyCoOccurrence() {
		Map<String, Map<String, Integer>> res = new HashMap<>();
		// Co-occurrence with details on the classes.
		Map<String, Map<String, Set<String>>> cooc = new HashMap<>();
		for (Entry<String, ClassSignature> entry : classSignatures.entrySet()) {
			ClassSignature sign = entry.getValue();
			for (String p1 : sign.listPathOrigins()) {
				if (!cooc.containsKey(p1)) cooc.put(p1, new HashMap<>());
				for (String p2 : sign.listPathOrigins())
					if (p1 != p2) {
						if (!cooc.get(p1).containsKey(p2)) cooc.get(p1).put(p2, new HashSet<>());
						cooc.get(p1).get(p2).add(sign.getOwlClass());
						if (!cooc.containsKey(p2)) cooc.put(p2, new HashMap<>());
						if (!cooc.get(p2).containsKey(p1)) cooc.get(p2).put(p1, new HashSet<>());
						cooc.get(p2).get(p1).add(sign.getOwlClass());
					}

			}
		}
		for (Entry<String, Map<String, Set<String>>> entry : cooc.entrySet()) {
			res.put(entry.getKey(), new HashMap<>());
			Map<String, Integer> row = res.get(entry.getKey());
			for (Entry<String, Set<String>> e2 : entry.getValue().entrySet())
				if (!row.containsKey(e2.getKey())) row.put(e2.getKey(), e2.getValue().size());
		}
		return res;
	}

}
