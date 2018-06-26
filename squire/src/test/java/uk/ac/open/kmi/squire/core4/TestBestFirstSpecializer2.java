package uk.ac.open.kmi.squire.core4;

import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;

/**
 * Same tests, different input
 * 
 * @author alessandro
 *
 */
public class TestBestFirstSpecializer2 {

	private String qsOrig = "PREFIX bibo: <http://purl.org/ontology/bibo/>" + " PREFIX dc: <http://purl.org/dc/terms/>"
			+ " SELECT DISTINCT ?title ?date ?author ?status WHERE {"
			+ " ?patent a bibo:Patent ; <http://www.w3.org/2000/01/rdf-schema#label> ?title ; dc:date ?date ; dc:creator ?author"
			+ " . ?patent bibo:status ?status" + " }";

	private String qsGen = "PREFIX bibo: <http://purl.org/ontology/bibo/>" + " PREFIX dc: <http://purl.org/dc/terms/>"
			+ " SELECT DISTINCT ?title ?date ?author ?status WHERE {"
			+ " ?patent a bibo:Patent ; ?dpt1 ?title ; dc:date ?date ; ?opt1 ?author ; ?opt2 ?status" + " }";

	@Test
	public void bestFirst() throws Exception {
		BestFirstSpecializer op = new BestFirstSpecializer(QueryFactory.create(qsOrig), QueryFactory.create(qsGen),
				new SparqlIndexedDataset("http://data.open.ac.uk/query", false),
				new SparqlIndexedDataset("http://data.aalto.fi/sparql", false), false);
		op.specialize();
	}

}
