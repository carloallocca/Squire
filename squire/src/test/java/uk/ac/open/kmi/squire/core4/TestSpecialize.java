package uk.ac.open.kmi.squire.core4;

import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;

public class TestSpecialize {

	private String qsOrig = "SELECT DISTINCT  ?mod ?title ?code ?regulation WHERE {"
			+ "?mod a <http://purl.org/vocab/aiiso/schema#Module>" + " ; <http://purl.org/dc/terms/title> ?title"
			+ " ; <http://purl.org/vocab/aiiso/schema#code> ?code"
			+ " ; <http://xcri.org/profiles/catalog/1.2/regulations>  ?regulation" + " }";

	private String qsGen = "SELECT DISTINCT ?mod ?title ?code ?regulation WHERE { ?mod a ?ct1"
			+ " ; <http://purl.org/dc/terms/title> ?title ; ?dpt2 ?code ; ?dpt1 ?regulation }";

	@Test
	public void bestFirst() throws Exception {
		BestFirstSpecializer op = new BestFirstSpecializer(QueryFactory.create(qsOrig), QueryFactory.create(qsGen),
				new SparqlIndexedDataset("http://data.open.ac.uk/query", false),
				new SparqlIndexedDataset("https://data.ox.ac.uk/sparql/", false));
		op.specialize();
	}

}
