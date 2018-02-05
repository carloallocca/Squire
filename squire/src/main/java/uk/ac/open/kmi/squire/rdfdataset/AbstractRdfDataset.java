package uk.ac.open.kmi.squire.rdfdataset;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public abstract class AbstractRdfDataset implements IRDFDataset {

	protected Set<String> classSet = new HashSet<>();
	protected Set<String> datatypePropertySet = new HashSet<>();
	protected Set<String> individualSet = new HashSet<>();
	protected Set<String> literalSet = new HashSet<>();
	protected Set<String> objectPropertySet = new HashSet<>();
	protected Set<String> rdfVocabulary = new HashSet<>();

	@Override
	public void clear() {
		this.classSet.clear();
		this.objectPropertySet.clear();
		this.datatypePropertySet.clear();
		this.individualSet.clear();
		this.literalSet.clear();
		this.rdfVocabulary.clear();
	}

	@Override
	public Set<String> getClassSet() {
		return classSet;
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
		Set<String> result = new HashSet<>(getDatatypePropertySet());
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
		return classSet.contains(classUri);
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

	@Override
	public void setClassSet(Set<String> classSet) {
		this.classSet = classSet;
	}

	@Override
	public void setDatatypePropertySet(Set<String> datatypePropertySet) {
		this.datatypePropertySet = datatypePropertySet;
	}

	@Override
	public void setIndividualSet(Set<String> individualSet) {
		this.individualSet = individualSet;
	}

	@Override
	public void setLiteralSet(Set<String> literalSet) {
		this.literalSet = literalSet;
	}

	@Override
	public void setObjectPropertySet(Set<String> objectPropertySet) {
		this.objectPropertySet = objectPropertySet;
	}

	@Override
	public void setRDFVocabulary(Set<String> rdfSet) {
		this.rdfVocabulary = rdfSet;
	}

}
