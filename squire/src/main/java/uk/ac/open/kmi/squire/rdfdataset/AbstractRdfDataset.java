package uk.ac.open.kmi.squire.rdfdataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
		return classSignatures.keySet();
	}

	@Override
	public Map<String, ClassSignature> getClassSignatures() {
		return this.classSignatures;
	}

	@Override
	public Set<String> getDatatypePropertySet() {
		return datatypePropertySet;
	}

	@Override
	public Set<String> getIndividualSet() {
		return individualSet;
	}

	@Override
	public Set<String> getLiteralSet() {
		return literalSet;
	}

	@Override
	public Set<String> getObjectPropertySet() {
		return objectPropertySet;
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
		return result;
	}

	@Override
	public Set<String> getRDFVocabulary() {
		return rdfVocabulary;
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

}
