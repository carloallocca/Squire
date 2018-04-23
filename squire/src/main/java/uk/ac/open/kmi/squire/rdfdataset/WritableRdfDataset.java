package uk.ac.open.kmi.squire.rdfdataset;

public interface WritableRdfDataset extends IRDFDataset {

	public void addDatatypeProperty(String uri);

	public void addIndividual(String uri);

	public void addLiteral(String value);

	public void addObjectProperty(String uri);

	public void clearDatatypeProperties();

	public void clearIndividuals();

	public void clearLiterals();

	public void clearObjectProperties();

	public void removeDatatypeProperty(String uri);

	public void removeIndividual(String uri);

	public void removeLiteral(String value);

	public void removeObjectProperty(String uri);

}
