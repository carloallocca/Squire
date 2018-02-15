/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.StringUtils;

/**
 *
 * @author carloallocca
 */
public class SparqlIndexedDataset extends AbstractRdfDataset {

	private String endpointURL; // set the path of the RDF dataset. e.g SPARQL endpoint url, or FilePath.
	private String graphName;
	private boolean replacing = false;

	private final Logger log = LoggerFactory.getLogger(getClass());

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
		load();
	}

	@Override
	public void clear() {
		/*
		 * This variant also clears its separate property set.
		 */
		super.clear();
		this.propertySet.clear();
	}

	@Override
	public void computeClassSet() {
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] a ?x");
		qS.append(" FILTER ( TRUE"); // unclosed FILTER

		/*
		 * To mimic the original behaviour, call iterativeComputation(qS.toString(),
		 * getClassSet(),-1, 0, null)
		 */
		// No exclusions, creates excessively long queries
		try {
			iterativeComputation(qS.toString(), getClassSet(), 50, 0, null);
		} catch (BootedException e) {
			log.error("We were kicked out immediately while trying to compute classes."
					+ " Have no fallback strategy for that.");
		}
	}

	@Override
	public void computeDataTypePropertySet() throws BootedException {
		StringBuilder qS = new StringBuilder();
		qS.append("SELECT DISTINCT ?x WHERE { [] ?x ?o");
		qS.append(" FILTER ( isLiteral(?o)"); // handle property filtering
		// iterateObjectPropertySet(50, 0, null);

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
		return this.endpointURL;// throw new UnsupportedOperationException("Not supported yet."); //To change
		// body of generated methods, choose Tools | Templates.
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

	private Set<String> loadSingle(String field) {
		String val = signatureDoc.get(field);
		return val == null ? new HashSet<>() : StringUtils.commaSeparated2List(val);
	}

	/**
	 * @param partialQuery
	 *            the partial query expects to do a "SELECT ?x" and to end with an
	 *            unclosed FILTER in the WHERE clause
	 * @param stepLength
	 * @param iteration
	 * @param exclusions
	 *            if NULL, the method will do pure pagination
	 */
	protected void iterativeComputation(final String partialQuery, final Set<String> tgtResourceSet, int stepLength,
			int iteration, Set<Property> exclusions) throws BootedException {
		long before = System.currentTimeMillis();
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
		log.debug("Sending query: {}", qS);
		try {
			String encodedQuery = URLEncoder.encode(qS.toString(), "UTF-8");
			String url = this.endpointURL + "?query=" + encodedQuery;
			// set the connection timeout value to 30 seconds (30000 milliseconds)
			int timeout = 30;
			RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
					.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("Accept", "application/sparql-results+json");
			HttpResponse response = httpClient.execute(getRequest);
			List<String> itemList;
			int stcode = response.getStatusLine().getStatusCode();
			if (200 == stcode) {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output;
				String result = "";
				while ((output = br.readLine()) != null)
					result = result + output;
				itemList = SparqlUtils.getValuesFromSparqlJson(result, "x");
			} else {
				// Don't die. Keep whatever was indexed so far.
				String reason = response.getStatusLine().getReasonPhrase();
				log.warn("Got remote response {} - {}", stcode, reason);
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
			if (itemList.isEmpty() || tgtResourceSet.containsAll(itemList)) {
				if (!itemList.isEmpty())
					log.debug("All {} RDF resources already present, closing loop.", itemList.size());
				log.info("DONE. {} total resources indexed.", tgtResourceSet.size());
			} else { // the recursive call.
				tgtResourceSet.addAll(itemList);
				log.info(" ... {} resources indexed so far (last {} in {} ms)", tgtResourceSet.size(), itemList.size(),
						(System.currentTimeMillis() - before));
				if (stepLength == itemList.size()) {
					if (exclusions != null) for (String op : itemList)
						exclusions.add(ResourceFactory.createProperty(op));
					iterativeComputation(partialQuery, tgtResourceSet, stepLength, iteration + 1, exclusions);
				} else log.info("DONE. {} total resources indexed for this category.", tgtResourceSet.size());
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace(); // TODO Handle properly
		} catch (IOException e) {
			e.printStackTrace(); // TODO Handle properly
		}
	}

	protected void load() {
		if (signatureDoc != null) {
			this.classSet = loadSingle("ClassSet");
			this.objectPropertySet = loadSingle("ObjectPropertySet");
			this.datatypePropertySet = loadSingle("DatatypePropertySet");
			this.literalSet = loadSingle("LiteralSet");
			this.individualSet = loadSingle("IndividualSet");
			this.rdfVocabulary = loadSingle("RDFVocabulary");
			this.propertySet = loadSingle("PropertySet");
		}
	}

}
