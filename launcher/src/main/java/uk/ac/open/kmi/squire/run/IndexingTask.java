package uk.ac.open.kmi.squire.run;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;
import uk.ac.open.kmi.squire.rdfdataset.BootedException;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;

public class IndexingTask {

	private Set<String> endpoints;

	private boolean force = false;

	private Logger log = LoggerFactory.getLogger(getClass());

	public IndexingTask(Set<String> endpoints, boolean force) {
		this.endpoints = endpoints;
		this.force = force;
	}

	public IndexingTask(Set<String> endpoints) {
		this(endpoints, false);
	}

	public void run() {
		List<String> failed = new LinkedList<>();
		for (String ep : endpoints)
			try {
				new URL(ep);
			} catch (MalformedURLException ex) {
				failed.add(ep);
			}
		if (!failed.isEmpty()) {
			for (String f : failed)
				log.error("Not a URL: {}", f);
			throw new RuntimeException("Some of the arguments are not URLs. Aborting index.");
		}
		for (String url : endpoints) {
			log.info("Inspecting endpoint <{}>", url);
			SparqlIndexedDataset indexed;
			try {
				indexed = new SparqlIndexedDataset(url);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (indexed.isIndexed()) {
				log.info(" ... already indexed (classes={};OPs={};DPs={};Ps={}).", indexed.getClassSet().size(),
						indexed.getObjectPropertySet().size(), indexed.getDatatypePropertySet().size(),
						indexed.getPropertySet().size());
				if (force) {
					log.info("Re-indexing forced by user.");
					indexed.clear();
				} else {
					log.info("Skipping.");
					continue;
				}
			} else log.info(" ... NOT indexed. Will index now.");
			log.info("Computing classes...");
			indexed.computeClassSet();
			try {
				log.info("Computing object properties...");
				indexed.computeObjectPropertySet();
				log.info("Computing datatype properties...");
				indexed.computeDataTypePropertySet();
			} catch (BootedException ex) {
				log.warn("Kicked out during property partitioning."
						+ " Falling back to undistinguished property computation.");
				indexed.computePropertySet();
			}
			log.info("Computing RDF vocabulary...");
			indexed.computeRDFVocabularySet();

			log.debug(" - #classes = {}", indexed.getClassSet().size());
			log.debug(" - #OPs = {}", indexed.getObjectPropertySet().size());
			log.debug(" - #DPs = {}", indexed.getDatatypePropertySet().size());
			log.debug(" - #Ps = {}", indexed.getPropertySet().size());

			log.debug("Indexing signature...");
			RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
			instance.indexSignature(url.toString(), "", indexed.getClassSet(), indexed.getObjectPropertySet(),
					indexed.getDatatypePropertySet(), indexed.getIndividualSet(), indexed.getLiteralSet(),
					indexed.getRDFVocabulary(), indexed.getPropertySet());
			log.info("<== DONE");

		}
		log.info("Nothing left to do for this task.");
	}

}
