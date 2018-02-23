package uk.ac.open.kmi.squire.run;

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

	public void execute() {
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
			SparqlIndexedDataset indexed = new SparqlIndexedDataset(url, force);
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
			log.info("Computing classes (and their signatures)...");
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

			log.info(" - #classes = {}", indexed.getClassSet().size());
			log.info(" - #OPs = {}", indexed.getObjectPropertySet().size());
			log.info(" - #DPs = {}", indexed.getDatatypePropertySet().size());
			log.info(" - #Ps = {}", indexed.getPropertySet().size());

			log.info("Indexing signature...");
			RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
			instance.indexSignature(url.toString(), "", indexed, indexed.getPropertySet(), force);
			log.info("<== DONE");
		}
		log.info("Nothing left to do for this task.");
	}

}
