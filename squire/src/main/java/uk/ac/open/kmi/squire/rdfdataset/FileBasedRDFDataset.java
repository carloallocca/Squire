/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	private Object datasetPath; // set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.

	private final Logger log = LoggerFactory.getLogger(getClass());
	private OntModel inf = null;

	public FileBasedRDFDataset(String rdfDatasetFilePath) {

		try {
			this.datasetPath = rdfDatasetFilePath;
			// create a Jena memory based model
			this.inf = ModelFactory.createOntologyModel();
			InputStream in = new FileInputStream(rdfDatasetFilePath);
			if (in == null) {
				throw new IllegalArgumentException("File: " + rdfDatasetFilePath + " not found");
			}
			// ...import the content of the owl file in the Jena model.
			inf.read(in, "");

			// ...compute the set of classes
			// this.classSet = computeClassSet();
			computeClassSet();

			// ...compute the set of individuals
			// this.individualSet = computeIndividualSet();
			computeIndividualSet();

			// ...compute the set of object Property Set
			// this.objectPropertySet = computeObjectPropertySet();
			computeObjectPropertySet();

			// ...compute the set of datatype Property Set
			// this.datatypePropertySet = computeDataTypePropertySet();
			computeDataTypePropertySet();

			// ...compute the set of literals Set
			// this.literalSet = computeLiteralSet();
			computeLiteralSet();

			// ...compute the set of rdf Vocabulary terms
			// this.rdfVocabulary = computeRDFVocabularySet();
			computeRDFVocabularySet();

			// ...compute the set of rdf Vocabulary terms
			// this.propertySet = computePropertySetSet();
			computePropertySetSet();

		} catch (FileNotFoundException ex) {
			log.error("", ex);
		}

	}

	public void computeClassSet() {
		ExtendedIterator<OntClass> classesIter = this.inf.listClasses();
		for (ExtendedIterator c = classesIter; c.hasNext();) {
			OntClass classe = (OntClass) c.next();
			this.classSet.add(classe.getURI());
		}

	}

	public void computeDataTypePropertySet() {
		ExtendedIterator<DatatypeProperty> datatypePropertyIter = this.inf.listDatatypeProperties();
		for (ExtendedIterator datatypeP = datatypePropertyIter; datatypeP.hasNext();) {
			DatatypeProperty datatypeProperty = (DatatypeProperty) datatypePropertyIter.next();
			this.datatypePropertySet.add(datatypeProperty.getURI());
		}
	}

	public void computeIndividualSet() {
		ExtendedIterator<Individual> individualIter = this.inf.listIndividuals();
		for (ExtendedIterator ind = individualIter; ind.hasNext();) {
			Individual individuo = (Individual) ind.next();
			this.individualSet.add(individuo.getURI());
		}
	}

	public void computeLiteralSet() {
		StmtIterator it = this.inf.listStatements();
		while (it.hasNext()) {
			Triple stmt = it.next().asTriple();
			Node subj = stmt.getSubject();
			Node obj = stmt.getObject();

			if (subj.isLiteral()) {
				this.literalSet.add(subj.getLiteralLexicalForm());

			}
			if (obj.isLiteral()) {
				this.literalSet.add(obj.getLiteralLexicalForm());

			}
		}
	}

	public void computeObjectPropertySet() {
		ExtendedIterator<ObjectProperty> objPropertyIter = this.inf.listObjectProperties();
		for (ExtendedIterator objP = objPropertyIter; objP.hasNext();) {
			ObjectProperty objProperty = (ObjectProperty) objPropertyIter.next();
			this.objectPropertySet.add(objProperty.getURI());
		}
	}

	@Override
	public void computePropertySet() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

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
		return this.datasetPath;// throw new UnsupportedOperationException("Not supported yet."); //To change
								// body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object getGraph() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	@Override
	public boolean isIndexed() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	@Override
	public void run() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	@Override
	public Object runAskQuery() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	@Override
	public Object runSelectQuery() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	@Override
	public void setGraph(Object path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	@Override
	public void setPath(Object path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
																		// choose Tools | Templates.
	}

	private void computePropertySetSet() {
		log.warn("Got request to compute general property set. This is unsupported"
				+ " as right now it is the sum of data proeprties and object proeprties.");
	}

}
