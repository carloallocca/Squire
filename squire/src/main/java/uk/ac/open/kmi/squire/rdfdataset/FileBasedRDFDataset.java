/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author carloallocca
 */
public class FileBasedRDFDataset extends AbstractRdfDataset {

	/**
	 * set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.
	 */
	private Object datasetPath;

	private OntModel inf = null;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public FileBasedRDFDataset(String rdfDatasetFilePath) {
		InputStream in = null;
		try {
			in = new FileInputStream(rdfDatasetFilePath);
			this.datasetPath = rdfDatasetFilePath;
			this.inf = ModelFactory.createOntologyModel();
			// ...import the content of the owl file in the Jena model.
			inf.read(in, "");

			// Do computations
			computeClassSet();
			computeObjectPropertySet();
			computeDataTypePropertySet();
			computeIndividualSet();
			computeLiteralSet();
			computePropertySet();
			computeRDFVocabularySet();
		} catch (FileNotFoundException e) {
			log.error("File not found: {}", rdfDatasetFilePath);
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				log.error("Failed to close stream after reading. Reason follows.", e);
			}
		}
	}

	@Override
	public void computeClassSet() {
		for (ExtendedIterator<OntClass> it = this.inf.listClasses(); it.hasNext();) {
			String key = it.next().getURI();
			this.classSignatures.put(key, new ClassSignature(key));
		}
	}

	@Override
	public void computeDataTypePropertySet() {
		for (ExtendedIterator<DatatypeProperty> it = this.inf.listDatatypeProperties(); it.hasNext();)
			this.datatypePropertySet.add(it.next().getURI());
	}

	@Override
	public void computeIndividualSet() {
		for (ExtendedIterator<Individual> it = this.inf.listIndividuals(); it.hasNext();)
			this.individualSet.add(it.next().getURI());
	}

	@Override
	public void computeLiteralSet() {
		for (StmtIterator it = this.inf.listStatements(); it.hasNext();) {
			Triple stmt = it.next().asTriple();
			// Subjects cannot be literals
			Node obj = stmt.getObject();
			if (obj.isLiteral()) this.literalSet.add(obj.getLiteralLexicalForm());
		}
	}

	@Override
	public void computeObjectPropertySet() {
		for (ExtendedIterator<ObjectProperty> it = this.inf.listObjectProperties(); it.hasNext();)
			this.objectPropertySet.add(it.next().getURI());
	}

	@Override
	public void computePropertySet() {
		log.warn("Got request to compute general property set. This is unsupported"
				+ " as right now it is the sum of data properties and object proeprties.");
	}

	@Override
	public void computeRDFVocabularySet() {
		StmtIterator it = this.inf.listStatements();
		while (it.hasNext()) {
			Triple stmt = it.next().asTriple();
			Node subj = stmt.getSubject();
			Node pred = stmt.getPredicate();
			Node obj = stmt.getObject();
			if (subj.toString().contains("http://www.w3.org/2002/07/owl#")
					|| subj.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
					|| subj.toString().contains("http://www.w3.org/2000/01/rdf-schema#")
					|| subj.toString().contains("http://www.w3.org/2006/12/owl2-xml#")) {
				if (!this.rdfVocabulary.contains(subj.toString())) {
					this.rdfVocabulary.add(subj.toString());
				}
			}
			if (pred.toString().contains("http://www.w3.org/2002/07/owl#")
					|| pred.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
					|| pred.toString().contains("http://www.w3.org/2000/01/rdf-schema#")
					|| pred.toString().contains("http://www.w3.org/2006/12/owl2-xml#")) {
				if (!this.rdfVocabulary.contains(pred.toString())) {
					this.rdfVocabulary.add(pred.toString());
				}
			}
			if (obj.toString().contains("http://www.w3.org/2002/07/owl#")
					|| obj.toString().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
					|| obj.toString().contains("http://www.w3.org/2000/01/rdf-schema#")
					|| obj.toString().contains("http://www.w3.org/2006/12/owl2-xml#")) {
				if (!this.rdfVocabulary.contains(obj.toString())) {
					this.rdfVocabulary.add(obj.toString());
				}
			}
		}
	}

	@Override
	public Object getEndPointURL() {
		return this.datasetPath;
	}

	@Override
	public Object getGraph() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isIndexed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void rebuildPropertyCoOccurrenceMap() {
		this.propertyCoOc = buildPropertyCoOccurrence();
	}

	@Override
	public void run() {
		throw new UnsupportedOperationException("This class is not associated to a process.");
	}

	@Override
	public void setGraph(Object path) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPath(Object path) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
