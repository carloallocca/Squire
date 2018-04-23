package uk.ac.open.kmi.squire.core4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.WritableRdfDataset;

public class TestClassSignatureGeneralizer {

	/**
	 * Node or node type. Utility class to unify the check by type or by value.
	 * 
	 * @author alessandro
	 *
	 */
	private class NorT {

		private Object me;

		public NorT(Class<? extends Node> type) {
			this.me = type;
		}

		public NorT(Node value) {
			this.me = value;
		}

		@SuppressWarnings("unchecked")
		public Class<? extends Node> asClass() {
			if (!isClass())
				throw new UnsupportedOperationException("This is not a Class but a " + me.getClass().getName());
			return (Class<? extends Node>) me;
		}

		public Node asNode() {
			if (!isNode())
				throw new UnsupportedOperationException("This is not a Node but a " + me.getClass().getName());
			return (Node) me;
		}

		public boolean isClass() {
			return me instanceof Class;
		}

		public boolean isNode() {
			return me instanceof Node;
		}

	}

	private static Map<String, WritableRdfDataset> datasets;

	private static final String prefix = "http://example.org/dataset/";

	/**
	 * Constructs the testbed and checks that it has what the tests need.
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		String patent = "http://purl.org/ontology/bibo/Patent";
		datasets = DummyDatasets.populate("/signatures2.json");
		assertEquals(2, datasets.size());
		assertTrue(datasets.containsKey(prefix + "1"));
		IRDFDataset ds = datasets.get(prefix + "1");
		assertTrue(ds.getClassSet().contains(FOAF.Document.getURI()));
		assertEquals(2, ds.getClassSignatures().get(FOAF.Document.getURI()).listPathOrigins().size());
		assertTrue(ds.getClassSet().contains(patent));
		assertEquals(5, ds.getClassSignatures().get(patent).listPathOrigins().size());
		assertTrue(datasets.containsKey(prefix + "2"));
		ds = datasets.get(prefix + "2");
		assertTrue(ds.getClassSet().contains(FOAF.Document.getURI()));
		assertEquals(6, ds.getClassSignatures().get(FOAF.Document.getURI()).listPathOrigins().size());
		assertTrue(ds.getClassSet().contains(patent));
		assertEquals(9, ds.getClassSignatures().get(patent).listPathOrigins().size());
	}

	private Generalizer _op;

	private final NorT bibo_Patent = new NorT(NodeFactory.createURI("http://purl.org/ontology/bibo/Patent"));

	private final NorT ol_Article = new NorT(
			NodeFactory.createURI("http://data.open.ac.uk/openlearn/ontology/OpenLearnArticle"));

	private final NorT rdf_type = new NorT(NodeFactory.createURI(RDF.type.getURI()));

	/**
	 * Denotes "a variable" when we don't care which.
	 */
	private final NorT type_var = new NorT(Var.class);

	private final NorT var_date = new NorT(NodeFactory.createVariable("date"));

	private final NorT var_s = new NorT(NodeFactory.createVariable("s"));

	private final NorT var_title = new NorT(NodeFactory.createVariable("title"));

	@Before
	public void before() throws Exception {
		_op = new ClassSignatureGeneralizer(datasets.get(prefix + "1"), datasets.get(prefix + "2"));
	}

	/**
	 * Class and one of two properties exists in both datasets, regardless of its
	 * usage.
	 * 
	 * The TP with the property in common stays, while the other is replaced
	 */
	@Test
	public void classAndOnePropertyInCommon() throws Exception {
		String q = "PREFIX bibo: <http://purl.org/ontology/bibo/>" + " SELECT DISTINCT ?author ?title WHERE {"
				+ "?s a bibo:Patent ; <http://purl.org/dc/elements/1.1/contributor> ?author"
				+ " ; <http://purl.org/dc/terms/title> ?title" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertTrue(find(q1, var_s, rdf_type, bibo_Patent));
		assertTrue(find(q1, var_s, new NorT(DCTerms.title.asNode()), var_title));
		assertFalse(find(q1, var_s, new NorT(DC.contributor.asNode()), type_var));
		assertTrue(find(q1, var_s, type_var, null));
	}

	/**
	 * Class and two properties exist in both datasets, regardless of their usage.
	 * 
	 * All the TPs stay as they are
	 */
	@Test
	public void classAndTwoPropertiesInCommon() throws Exception {
		String q = "PREFIX bibo: <http://purl.org/ontology/bibo/>" + " SELECT DISTINCT ?author ?title WHERE {"
				+ "?s a bibo:Patent ; bibo:authorList ?author ; <http://purl.org/dc/terms/title> ?title" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertTrue(find(q1, var_s, rdf_type, bibo_Patent));
		assertTrue(find(q1, var_s, new NorT(NodeFactory.createURI("http://purl.org/ontology/bibo/authorList")),
				new NorT(NodeFactory.createVariable("author"))));
		assertTrue(find(q1, var_s, new NorT(DCTerms.title.asNode()), var_title));
		assertFalse(find(q1, var_s, type_var, null));
	}

	/**
	 * Class and one of two properties exists in both datasets, regardless of its
	 * usage.
	 * 
	 * The TP with the property in common stays, while the other is replaced
	 */
	@Test
	public void classNoAndOnePropertyInCommon() throws Exception {
		// dc:title is present, dc:subject is not
		String q = "PREFIX dc: <http://purl.org/dc/terms/>" + " SELECT DISTINCT ?author ?subject WHERE {" + "?s a <"
				+ ol_Article.asNode() + "> ; dc:title ?title" + " ; dc:subject ?subject" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertFalse(find(q1, var_s, rdf_type, ol_Article));
		assertTrue(find(q1, var_s, rdf_type, type_var));
		assertTrue(find(q1, var_s, new NorT(DCTerms.title.asNode()), var_title));
		assertFalse(find(q1, var_s, new NorT(DCTerms.subject.asNode()), type_var));
		assertTrue(find(q1, var_s, type_var, null));
	}

	/**
	 * One RDF type not present in the target dataset.
	 * 
	 * It must be replaced by a generalized TP.
	 */
	// @Test
	public void oneTypeNotPresent() throws Exception {
		String q = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  " + "SELECT DISTINCT ?author WHERE {" + "?s a <"
				+ ol_Article.asNode() + "> ; foaf:maker ?author" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertFalse(find(q1, var_s, rdf_type, ol_Article));
		assertTrue(find(q1, var_s, rdf_type, type_var));
	}

	/**
	 * One RDF type present in the target dataset as well.
	 * 
	 * It must stay as it is.
	 */
	@Test
	public void oneTypePresent() throws Exception {
		String q = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  " + "SELECT DISTINCT ?author WHERE {"
				+ "?s a foaf:Document . ?s foaf:maker ?author" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertTrue(find(q1, var_s, rdf_type, new NorT(NodeFactory.createURI(FOAF.Document.getURI()))));
		assertFalse(find(q1, var_s, rdf_type, type_var));
	}

	/**
	 * One type not appearing in the target dataset; two properties, both used in
	 * the target dataset but never for the same type.
	 * 
	 * The type assertion is generalized, and two queries are produced, one for each
	 * property.
	 */
	@Test
	public void twoPropertiesNeverTogether() throws Exception {
		String q = "SELECT DISTINCT ?title ?pic WHERE {" + " ?s a <" + ol_Article.asNode() + ">"
				+ " ; <http://purl.org/dc/terms/title> ?title" + " ; <http://xmlns.com/foaf/0.1/depiction> ?pic" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(2, qG.size());
		NorT varc = new NorT(Var.class);
		for (Iterator<Query> it = qG.iterator(); it.hasNext();) {
			Query q1 = it.next();
			assertFalse(find(q1, var_s, rdf_type, ol_Article));
			assertTrue(find(q1, var_s, rdf_type, varc));
		}
	}

	/**
	 * One type appearing in the target dataset; two properties, both used in the
	 * target dataset but only one for that type.
	 * 
	 * The generalization must preserve the type and common property, and generalize
	 * the other.
	 */
	@Test
	public void twoPropertiesNotForSameTypePresent() throws Exception {
		String q = "SELECT DISTINCT ?title ?date WHERE {" + " ?s a <http://purl.org/ontology/bibo/Patent>"
				+ " ; <http://www.w3.org/2000/01/rdf-schema#label> ?title" + " ; <http://purl.org/dc/terms/date> ?date"
				+ " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertTrue(find(q1, var_s, rdf_type, bibo_Patent));
		assertTrue(find(q1, var_s, type_var, var_title));
		assertFalse(find(q1, var_s, new NorT(NodeFactory.createURI(RDFS.label.getURI())), var_title));
		assertTrue(find(q1, var_s, new NorT(DCTerms.date.asNode()), var_date));
		assertFalse(find(q1, var_s, type_var, var_date));
	}

	/**
	 * Two RDF types for a subject, both are present in the target dataset as well.
	 * 
	 * Both RDF type assertions must remain as they are.
	 */
	@Test
	public void twoTypesBothPresent() throws Exception {
		String q = "PREFIX bibo: <http://purl.org/ontology/bibo/>" + " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				+ " SELECT DISTINCT ?author WHERE {" + " ?s a bibo:Patent , foaf:Document ; foaf:maker ?author" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertTrue(find(q1, var_s, rdf_type, bibo_Patent));
		assertTrue(find(q1, var_s, rdf_type, new NorT(NodeFactory.createURI(FOAF.Document.getURI()))));
		assertFalse(find(q1, var_s, rdf_type, type_var));
	}

	/**
	 * Two RDF types for a subject, but neither is present in the target dataset.
	 * 
	 * There must be a single generalized rdf:type triple pattern.
	 */
	@Test
	public void twoTypesNeitherPresent() throws Exception {
		String q = "PREFIX ol: <http://data.open.ac.uk/openlearn/ontology/>"
				+ " PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + " SELECT DISTINCT ?author WHERE {"
				+ " ?s a ol:OpenLearnArticle , ol:Podcast ; foaf:maker ?author" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertFalse(find(q1, var_s, rdf_type,
				new NorT(NodeFactory.createURI("http://data.open.ac.uk/openlearn/ontology/Podcast"))));
		assertFalse(find(q1, var_s, rdf_type, ol_Article));
		assertTrue(find(q1, var_s, rdf_type, type_var));
	}

	/**
	 * Two RDF types for a subject, but only one is present in the target dataset as
	 * well.
	 * 
	 * There must be one type assertion on the common class only + one generalized
	 * triple pattern.
	 */
	@Test
	public void twoTypesOnePresent() throws Exception {
		String q = "SELECT DISTINCT ?title WHERE {" + "?s a <" + ol_Article.asNode()
				+ ">, <http://xmlns.com/foaf/0.1/Document> " + " ; <http://purl.org/dc/terms/title> ?title" + " }";
		Set<Query> qG = _op.generalize(QueryFactory.create(q));
		assertEquals(1, qG.size());
		Query q1 = qG.iterator().next();
		assertTrue(find(q1, var_s, rdf_type, new NorT(NodeFactory.createVariable("ct1"))));
		assertTrue(find(q1, var_s, rdf_type, new NorT(NodeFactory.createURI(FOAF.Document.getURI()))));
		assertFalse(find(q1, var_s, rdf_type, ol_Article));
	}

	private boolean find(Query q, NorT s, NorT p, NorT o) {
		final boolean[] found = new boolean[] { false };
		q.getQueryPattern().visit(new ElementVisitorBase() {
			@Override
			public void visit(ElementGroup el) {
				for (Element el2 : el.getElements())
					el2.visit(this);
			}

			@Override
			public void visit(ElementPathBlock el) {
				for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
					TriplePath tp = it.next();
					if (check(s, tp.getSubject()) && check(p, tp.getPredicate()) && check(o, tp.getObject())) {
						found[0] = true;
						return;
					}
				}
			}

			private boolean check(NorT nort, Node node) {
				return nort == null || nort.isClass() && nort.asClass().isAssignableFrom(node.getClass())
						|| nort.isNode() && nort.asNode().equals(node);
			}
		});
		return found[0];
	}

}
