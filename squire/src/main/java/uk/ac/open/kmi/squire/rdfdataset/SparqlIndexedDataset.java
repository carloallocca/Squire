/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.LockObtainFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.index.RDFDatasetIndexer.Fieldd;
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.SparqlUtils.SparqlException;
import uk.ac.open.kmi.squire.utils.StringUtils;

/**
 *
 * @author carloallocca
 */
public class SparqlIndexedDataset extends AbstractRdfDataset {

	private String endpointURL; // set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.
	private String graphName;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private boolean replacing = false;

	private Document signatureDoc;

	/**
	 * A separate set for keeping track of
	 */
	protected Set<String> propertySet = new HashSet<>();

	public SparqlIndexedDataset(String urlAddress) {
		this(urlAddress, "", false);
	}

	public SparqlIndexedDataset(String urlAddress, boolean replacing) {
		this(urlAddress, "", replacing);
	}

	public SparqlIndexedDataset(String urlAddress, String graphName) {
		this(urlAddress, graphName, false);
	}

	public SparqlIndexedDataset(String urlAddress, String graphName, boolean replacing) {
		this.graphName = graphName;
		this.endpointURL = urlAddress;
		this.replacing = replacing;
		// on 03/04/2017, this is what I have added to make it working again
		// createSPARQLEndPoint();
		RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
		this.signatureDoc = instance.getSignature(this.endpointURL, this.graphName);
		loadAll();
	}

	@Override
	public void clear() {
		/*
		 * This variant also clears its separate property set.
		 */
		super.clear();
		this.propertySet.clear();
	}

	/**
	 * This implementation also retrieves the immediate class signature for each
	 * class.
	 */
	@Override
	public void computeClassSet() {
		StringBuilder qS = new StringBuilder();
		// qS.append("SELECT DISTINCT ?x WHERE { [] a ?x");
		qS.append("SELECT DISTINCT ?x ?p1 WHERE { ?s a ?x OPTIONAL { ?s ?p1 [] }");
		qS.append(" FILTER ( TRUE ");
		// qS.append(" FILTER ( ! (");
		// qS.append(" STRSTARTS( str(?x), \"" + RDFS.uri + "\" ) ");
		// qS.append(" || STRSTARTS( str(?x), \"" + RDF.uri + "\" ) ");
		// qS.append(" || STRSTARTS( str(?x), \"" + OWL.NS + "\" ) ");
		// qS.append(")");

		// No exclusions, creates excessively long queries
		boolean complete;
		try {
			Map<String, Set<String>> tempClasses = new HashMap<>();
			/*
			 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
			 * getClassSet(),-1, 0, null)
			 */
			complete = iterativeComputation(qS.toString(), tempClasses, 100, 0, null);
			for (Entry<String, Set<String>> e : tempClasses.entrySet()) {
				String k = e.getKey();
				if (!classSignatures.containsKey(k)) classSignatures.put(k, new ClassSignature(k));
				ClassSignature cs = classSignatures.get(k);
				for (String prop : e.getValue())
					if (prop != null && !cs.hasProperty(prop)) cs.addProperty(prop);
			}
		} catch (BootedException e) {
			// TODO fall back to computing plain classes.
			log.error("We were kicked out immediately while trying to compute classes."
					+ " Falling back to per-class startegy.");
			complete = false;
		}
		if (!complete) fallbackClassSet();
		int assoc = 0;
		for (Entry<String, ClassSignature> e : classSignatures.entrySet())
			assoc += e.getValue().listPathOrigins().size();
		log.info("Class indexing complete: indexed {} classes and a total of {} property associations.",
				classSignatures.size(), assoc);
	}

	@Override
	public void computeDataTypePropertySet() throws BootedException {
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] ?x ?o");
		qS.append(" FILTER ( isLiteral(?o)"); // handle property filtering

		/*
		 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
		 * getDatatypePropertySet(),-1, 0, null)
		 */
		// No exclusions, creates excessively long queries
		iterativeComputation(qS.toString(), getDatatypePropertySet(), 50, 0, null);
	}

	@Override
	public void computeIndividualSet() {
		StringBuilder qS = new StringBuilder();
		qS.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		qS.append(" PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		qS.append(" PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		qS.append(" SELECT DISTINCT ?x WHERE { ?x a ?t");
		// Avoid most common non-individual RDF types
		qS.append(" FILTER ( ?x NOT IN( rdfs:Class, owl:Class, rdfs:Resource, owl:DataRange,"
				+ " rdf:Property, owl:ObjectProperty, owl:DatatypeProperty, owl:AnnotationProperty) ");

		/*
		 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
		 * getIndividualSet(),-1, 0, null)
		 */
		// No exclusions, creates excessively long queries
		try {
			iterativeComputation(qS.toString(), getIndividualSet(), 50, 0, null);
		} catch (BootedException e) {
			log.error("We were kicked out immediately while trying to compute individuals."
					+ " Have no fallback strategy for that.");
		}
	}

	@Override
	public void computeLiteralSet() {
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] ?p ?x");
		qS.append(" FILTER ( isLiteral(?x)"); // handle literal filtering

		/*
		 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
		 * getLiteralSet(),-1, 0, null)
		 */
		// No exclusions, creates excessively long queries
		try {
			iterativeComputation(qS.toString(), getLiteralSet(), 50, 0, null);
		} catch (BootedException e) {
			log.error("We were kicked out immediately while trying to compute literals."
					+ " Have no fallback strategy for that.");
		}
	}

	@Override
	public void computeObjectPropertySet() throws BootedException {
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] ?x ?o");
		qS.append(" FILTER ( isUri(?o)"); // handle property filtering
		// iterateObjectPropertySet(50, 0, null);

		/*
		 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
		 * getObjectPropertySet(),-1, 0, null)
		 */
		// No exclusions, creates excessively long queries
		iterativeComputation(qS.toString(), getObjectPropertySet(), 50, 0, null);
	}

	@Override
	public void computePropertySet() {
		log.warn("Got request to compute general property set." + " This should be used as a last resort.");
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] ?x []");
		qS.append(" FILTER ( TRUE"); // handle property filtering
		// iterateObjectPropertySet(50, 0, null);

		/*
		 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
		 * getDatatypePropertySet(),-1, 0, null)
		 */
		// No exclusions, creates excessively long queries
		try {
			iterativeComputation(qS.toString(), this.propertySet, 50, 0, null);
		} catch (BootedException e) {
			log.error("We were kicked out immediately while trying to compute properties."
					+ " Have no fallback strategy for that.");
		}
	}

	@Override
	public void computeRDFVocabularySet() {
		// RDF
		this.rdfVocabulary.add(RDF.type.getURI());
		// RDFS
		this.rdfVocabulary.add(RDFS.Class.getURI());
		this.rdfVocabulary.add(RDFS.Literal.getURI());
		this.rdfVocabulary.add(RDFS.Resource.getURI());
		// OWL
		this.rdfVocabulary.add(OWL.Class.getURI());
		this.rdfVocabulary.add(OWL2.NamedIndividual.getURI());
		this.rdfVocabulary.add(OWL.ObjectProperty.getURI());
		this.rdfVocabulary.add(OWL.sameAs.getURI());
		this.rdfVocabulary.add(OWL.DatatypeProperty.getURI());
		this.rdfVocabulary.add(OWL.DataRange.getURI());
	}

	@Override
	public Object getEndPointURL() {
		return this.endpointURL;
	}

	@Override
	public Object getGraph() {
		return this.graphName;
	}

	@Override
	public Set<String> getPropertySet() {
		if (this.propertySet.isEmpty()) return super.getPropertySet();
		return this.propertySet;
	}

	@Override
	public boolean isIndexed() {
		return this.signatureDoc != null;
	}

	@Override
	public boolean isInPropertySet(String propertyUri) {
		if (this.propertySet.contains(propertyUri)) return true;
		return super.isInPropertySet(propertyUri);
	}

	@Override
	public void run() {
		System.out.println("[SPARQLEndPoint:run()] run is in execution....");
		try {
			if (!isIndexed()) {
				createSPARQLEndPoint(this.replacing);
			}
		} catch (Exception ex) {
			if (ex instanceof ClosedByInterruptException) {
				log.warn(" A task with token  was interrupted." + " This may have been requested by a client.");
			} else {
				log.error("Caught exception of type " + ex.getClass().getName() + " : " + ex.getMessage()
						+ " - doing nothing with it.", ex);
			}
		}
	}

	public void setEndpointURL(String endpointURL) {
		this.endpointURL = endpointURL;
	}

	@Override
	public void setGraph(Object path) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	@Override
	public void setPath(Object path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
		// choose Tools | Templates.
	}

	@Override
	public String toString() {
		if (!(endpointURL == null || endpointURL.isEmpty()) || !(graphName == null || graphName.isEmpty())) {
			StringBuilder s = new StringBuilder();
			if (endpointURL != null) {
				s.append(endpointURL);
				if (graphName != null && !graphName.isEmpty()) s.append("::");
			}
			if (graphName != null && !graphName.isEmpty()) s.append(graphName);
			return s.toString();
		}
		return super.toString();
	}

	private String buildQuery(String partialQuery, int stepLength, int iteration, Set<Property> exclusions) {
		if (iteration < 0) throw new IllegalArgumentException("Iteration cannot be negative.");
		int count = 0;
		StringBuilder qS = new StringBuilder(partialQuery);
		if (exclusions != null && !exclusions.isEmpty()) {
			qS.append(" && ?x NOT IN (");
			for (Iterator<Property> it = exclusions.iterator(); it.hasNext(); count++) {
				if (count > 0) qS.append(",");
				qS.append("<" + it.next().getURI() + ">");
			}
			qS.append(" )");
		}
		qS.append(" )");
		// END handle property filtering
		qS.append(" }");
		if (stepLength > 0) {
			qS.append(" LIMIT ");
			qS.append(stepLength);
			if (iteration > 0 && (exclusions == null || exclusions.isEmpty())) {
				qS.append(" OFFSET ");
				qS.append(stepLength * iteration);
			}
		}
		return qS.toString();
	}

	private void createSPARQLEndPoint(boolean overwrite) throws IOException, LockObtainFailedException {
		if (this.signatureDoc == null) {
			computeClassSet();
			try {
				computeObjectPropertySet();
				computeDataTypePropertySet();
			} catch (BootedException e) {
				// Fall back to computing properties in general
				// if failing to compute them by type.
				computePropertySet();
			}
			computeRDFVocabularySet();
			// XXX Ugly call.
			this.signatureDoc = RDFDatasetIndexer.getInstance().indexSignature(this.endpointURL, graphName, this,
					overwrite);
		}
	}

	/**
	 * Called after computing class signatures altogether with pagination has
	 * failed. This strategy gets all the classes first, then queries piecemeal for
	 * the signatures of each. This results in more queries, but of the type that
	 * endpoints are more likely to answer in reasonable time, if they don't ban us
	 * for too many queries.
	 */
	private void fallbackClassSet() {
		log.warn("Got request to compute signatures per class."
				+ " This is a polling strategy that should only be used when the aggressive approach fails.");
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] a ?x");
		qS.append(" FILTER ( TRUE ");
		// qS.append(" FILTER ( ! (");
		// qS.append(" STRSTARTS( str(?x), \"" + RDFS.uri + "\" ) ");
		// qS.append(" || STRSTARTS( str(?x), \"" + RDF.uri + "\" ) ");
		// qS.append(" || STRSTARTS( str(?x), \"" + OWL.NS + "\" ) ");
		// qS.append(")");
		Set<String> plainClasses = new HashSet<>();
		try {
			iterativeComputation(qS.toString(), plainClasses, 50, 0, null);
		} catch (BootedException e) {
			log.error("We were kicked out immediately while trying to compute classes.");
			log.error(" ... Have no fallback strategy for that. Giving up.");
		}
		for (String clazz : plainClasses) {
			log.info("Indexing signature for class <{}>", clazz);
			if (!classSignatures.containsKey(clazz)) classSignatures.put(clazz, new ClassSignature(clazz));
			ClassSignature cs = classSignatures.get(clazz);
			qS = new StringBuilder();
			qS.append("SELECT DISTINCT ?x WHERE { ?s a <" + clazz + "> OPTIONAL { ?s ?x [] }");
			qS.append(" FILTER ( TRUE ");
			Set<String> props = new HashSet<>();
			try {
				iterativeComputation(qS.toString(), props, 50, 0, null);
			} catch (BootedException e) {
				log.error("We were kicked out immediately while trying to compute properties.");
				log.error(" ... class was <{}>", clazz);
				log.error(" ... Have no fallback strategy for that. Giving up on this class.");
			} finally {
				// Let's use whatever we managed to obtain...
				for (String prop : props)
					if (prop != null && !cs.hasProperty(prop)) cs.addProperty(prop);
			}
		}
	}

	private void loadClassSignatures() {
		String val = signatureDoc.get(Fieldd.CLASS_SIGNATURES.toString());
		if (val != null) {
			JsonObject o = JSON.parse(val);
			for (String className : o.keys()) {
				ClassSignature sign = new ClassSignature(className);
				for (String prop : o.get(className).getAsObject().keys())
					sign.addProperty(prop);
				classSignatures.put(className, sign);
			}
		} else {
			// Fall back to legacy method, for older indices
			log.warn("No class signature field found. Falling back to legacy method.");
			val = signatureDoc.get(Fieldd.ClassSet.toString());
			if (val != null) for (String className : StringUtils.commaSeparated2List(val)) {
				classSignatures.put(className, new ClassSignature(className));
			}
		}
	}

	private Set<String> loadSingle(String field) {
		String val = signatureDoc.get(field);
		return val == null ? new HashSet<>() : StringUtils.commaSeparated2List(val);
	}

	/**
	 * This is the variant that uses a Map as a container.
	 * 
	 * @param partialQuery
	 *            the partial query is expected to do a "SELECT ?x ?p1" and to end
	 *            with an unclosed FILTER in the WHERE clause. Other variables can
	 *            be projected, though support for them will depend on
	 *            implementation.
	 * @param stepLength
	 *            how many items it should try to fetch on every step
	 * @param iteration
	 *            what number of step this is (starts at 0)
	 * @param exclusions
	 *            the properties that should be excluded from the counting, e.g.
	 *            because they have already been indexed. If NULL, the method will
	 *            do pure pagination.
	 * @return true iff the computation is deemed complete.
	 */
	protected boolean iterativeComputation(final String partialQuery, final Map<String, Set<String>> targetContainer,
			int stepLength, int iteration, Set<Property> exclusions) throws BootedException {
		long before = System.currentTimeMillis();
		boolean complete = true;
		if (iteration < 0) throw new IllegalArgumentException("Iteration cannot be negative.");
		String q = buildQuery(partialQuery, stepLength, iteration, exclusions);
		log.debug("Sending query: {}", q);
		String[][] items;
		try {
			String res = SparqlUtils.doRawQuery(q, this.endpointURL);
			items = SparqlUtils.extractSelectValuePairs(res, "x", "p1");
		} catch (SparqlException e1) {
			// Don't die. Keep whatever was indexed so far.
			log.warn("Got remote response : {}", e1.getMessage());
			// If it was the first attempt though, give up and raise an exception.
			if (iteration == 0) throw new BootedException();
			log.warn("Indexing failed at iteration {}. Will stop polling and keep already indexed resources.",
					iteration);
			items = new String[0][0];
			complete = false; // However do mark the computation as incomplete.
		}

		/*
		 * Stop iterating if you received fewer results than the limit or if you already
		 * have all the RDF resources in the result (the latter means there's something
		 * wrong with the order in which results are given, therefore one should sort
		 * but it's costly).
		 */
		boolean doRepeat = false;
		if (items.length > 0)
			// Inspect for new bindings: if at least one is found, do another round
			for (int i = 0; i < items.length; i++) {
			String k = items[i][0];
			if (!targetContainer.containsKey(k)) {
			targetContainer.put(k, new HashSet<>());
			doRepeat = true;
			}
			if (!targetContainer.get(k).contains(items[i][1])) {
			targetContainer.get(k).add(items[i][1]);
			doRepeat = true;
			}
			}
		int assoc = 0;
		for (Entry<String, Set<String>> e : targetContainer.entrySet())
			assoc += e.getValue().size();
		if (!doRepeat) {
			log.debug("All {} unique associations already present, closing loop.", items.length);
			log.info("DONE. {} total associations indexed.", assoc);
		} else { // the recursive call.
			log.info(" ... {} associations indexed so far (last {} in {} ms)", assoc, items.length,
					(System.currentTimeMillis() - before));
			if (stepLength == items.length) {
				if (exclusions != null) for (int i = 0; i < items.length; i++)
					exclusions.add(ResourceFactory.createProperty(items[i][1]));
				complete = iterativeComputation(partialQuery, targetContainer, stepLength, iteration + 1, exclusions);
			} else log.info("DONE. {} total associations indexed for this category.", assoc);
		}
		return complete;
	}

	/**
	 * @param partialQuery
	 *            the partial query is expected to do a "SELECT ?x" and to end with
	 *            an unclosed FILTER in the WHERE clause.
	 * @param stepLength
	 *            how many items it should try to fetch on every step
	 * @param iteration
	 *            what number of step this is (starts at 0)
	 * @param exclusions
	 *            the properties that should be excluded from the counting, e.g.
	 *            because they have already been indexed. If NULL, the method will
	 *            do pure pagination.
	 */
	protected void iterativeComputation(final String partialQuery, final Set<String> targetContainer, int stepLength,
			int iteration, Set<Property> exclusions) throws BootedException {
		long before = System.currentTimeMillis();
		if (iteration < 0) throw new IllegalArgumentException("Iteration cannot be negative.");
		String q = buildQuery(partialQuery, stepLength, iteration, exclusions);
		log.debug("Sending query: {}", q);
		List<String> itemList;
		try {
			String res = SparqlUtils.doRawQuery(q, this.endpointURL);
			itemList = SparqlUtils.extractSelectVariableValues(res, "x");
		} catch (SparqlException e1) {
			// Don't die. Keep whatever was indexed so far.
			log.warn("Got remote response : {}", e1.getMessage());
			// If it was the first attempt though, give up and raise an exception.
			if (iteration == 0) throw new BootedException();
			log.warn("Indexing failed at iteration {}. Will stop polling and keep already indexed resources.",
					iteration);
			itemList = Collections.emptyList();
		}

		/*
		 * Stop iterating if you received fewer results than the limit or if you already
		 * have all the RDF resources in the result (the latter means there's something
		 * wrong with the order in which results are given, therefore one should sort
		 * but it's costly).
		 */
		if (itemList.isEmpty() || targetContainer.containsAll(itemList)) {
			if (!itemList.isEmpty()) log.debug("All {} RDF resources already present, closing loop.", itemList.size());
			log.info("DONE. {} total resources indexed.", targetContainer.size());
		} else { // the recursive call.
			targetContainer.addAll(itemList);
			log.info(" ... {} resources indexed so far (last {} in {} ms)", targetContainer.size(), itemList.size(),
					(System.currentTimeMillis() - before));
			if (stepLength == itemList.size()) {
				if (exclusions != null) for (String op : itemList)
					exclusions.add(ResourceFactory.createProperty(op));
				iterativeComputation(partialQuery, targetContainer, stepLength, iteration + 1, exclusions);
			} else log.info("DONE. {} total resources indexed for this category.", targetContainer.size());
		}

	}

	protected void loadAll() {
		if (signatureDoc != null) {
			// this.classSet = loadSingle("ClassSet");
			loadClassSignatures();
			this.objectPropertySet = loadSingle("ObjectPropertySet");
			this.datatypePropertySet = loadSingle("DatatypePropertySet");
			this.literalSet = loadSingle("LiteralSet");
			this.individualSet = loadSingle("IndividualSet");
			this.rdfVocabulary = loadSingle("RDFVocabulary");
			this.propertySet = loadSingle("PropertySet");
		}
	}

}
