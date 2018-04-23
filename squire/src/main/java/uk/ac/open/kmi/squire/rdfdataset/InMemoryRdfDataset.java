package uk.ac.open.kmi.squire.rdfdataset;

/**
 * compute() methods do nothing. The dataset can only be populated through add,
 * remove and clear methods.
 * 
 * @author alessandro
 *
 */
public class InMemoryRdfDataset extends AbstractRdfDataset implements WritableRdfDataset {

	@Override
	public void addDatatypeProperty(String uri) {
		this.datatypePropertySet.add(uri);
	}

	@Override
	public void addIndividual(String uri) {
		this.individualSet.add(uri);
	}

	@Override
	public void addLiteral(String value) {
		this.literalSet.add(value);
	}

	@Override
	public void addObjectProperty(String uri) {
		this.objectPropertySet.add(uri);
	}

	@Override
	public void clearDatatypeProperties() {
		this.datatypePropertySet.clear();
	}

	@Override
	public void clearIndividuals() {
		this.individualSet.clear();
	}

	@Override
	public void clearLiterals() {
		this.literalSet.clear();
	}

	@Override
	public void clearObjectProperties() {
		this.objectPropertySet.clear();
	}

	@Override
	public void computeClassSet() {
	}

	@Override
	public void computeDataTypePropertySet() throws BootedException {
	}

	@Override
	public void computeIndividualSet() {
	}

	@Override
	public void computeLiteralSet() {
	}

	@Override
	public void computeObjectPropertySet() throws BootedException {
	}

	@Override
	public void computePropertySet() {
	}

	@Override
	public void computeRDFVocabularySet() {
	}

	@Override
	public Object getEndPointURL() {
		return null;
	}

	@Override
	public Object getGraph() {
		return null;
	}

	@Override
	public boolean isIndexed() {
		return true;
	}

	@Override
	public void rebuildPropertyCoOccurrenceMap() {
		this.propertyCoOc = buildPropertyCoOccurrence();
	}

	@Override
	public void removeDatatypeProperty(String uri) {
		this.datatypePropertySet.remove(uri);
	}

	@Override
	public void removeIndividual(String uri) {
		this.individualSet.remove(uri);
	}

	@Override
	public void removeLiteral(String value) {
		this.literalSet.remove(value);
	}

	@Override
	public void removeObjectProperty(String uri) {
		this.objectPropertySet.remove(uri);
	}

	@Override
	public void run() {
	}

	@Override
	public void setGraph(Object path) {
	}

	@Override
	public void setPath(Object path) {
	}

}
