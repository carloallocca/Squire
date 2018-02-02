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
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;

public class IndexingTask {

	private Set<String> endpoints;

	private Logger log = LoggerFactory.getLogger(getClass());

	public IndexingTask(Set<String> endpoints) {
		this.endpoints = endpoints;
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
			SparqlIndexedDataset endpoint;
			try {
				endpoint = new SparqlIndexedDataset(url);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			if (endpoint.isIndexed()) {
				log.info(" ... already indexed (classes={};OPs={};DPs={}). Skipping.", endpoint.getClassSet().size(),
						endpoint.getObjectPropertySet().size(), endpoint.getDatatypePropertySet().size());
				continue;
			}

			log.info(" ... NOT indexed. Will index now.");
			log.debug("Computing classes...");
			endpoint.computeClassSet();
			log.debug("Computing object properties...");
			endpoint.computeObjectPropertySet();
			log.debug("Computing datatype properties...");
			endpoint.computeDataTypePropertySet();
			log.debug("Computing RDF vocabulary...");
			endpoint.computeRDFVocabularySet();

			log.debug(" - #classes = {}", endpoint.getClassSet().size());
			log.debug(" - #OPs = {}", endpoint.getObjectPropertySet().size());
			log.debug(" - #DPs = {}", endpoint.getDatatypePropertySet().size());

			log.debug("Indexing signature...");
			RDFDatasetIndexer instance = RDFDatasetIndexer.getInstance();
			instance.indexSignature(url.toString(), "", endpoint.getClassSet(), endpoint.getObjectPropertySet(),
					endpoint.getDatatypePropertySet(), endpoint.getIndividualSet(), endpoint.getLiteralSet(),
					endpoint.getRDFVocabulary(), endpoint.getPropertySet());
			log.info("<== DONE");

		}
		log.info("Nothing left to do for this task.");
	}

}
