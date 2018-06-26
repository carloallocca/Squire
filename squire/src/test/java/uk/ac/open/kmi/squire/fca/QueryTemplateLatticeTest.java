package uk.ac.open.kmi.squire.fca;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import uk.ac.open.kmi.squire.core4.GeneralizedQuery;
import uk.ac.open.kmi.squire.vocabulary.Bibo;

public class QueryTemplateLatticeTest {

	private String qsGen = "PREFIX bibo: <http://purl.org/ontology/bibo/>" + " PREFIX dc: <http://purl.org/dc/terms/>"
			+ " SELECT DISTINCT ?title ?date ?author ?status WHERE {"
			+ " ?patent a bibo:Patent ; ?dpt1 ?title ; dc:date ?date ; ?opt1 ?author ; ?opt2 ?status" + " }";

	@Test
	public void testSimple() throws Exception {

		List<QuerySolution> s = new ArrayList<>();
		addSolution(s, Bibo.authorList, DC.contributor, DC.contributor);
		addSolution(s, Bibo.authorList, DC.contributor, DCTerms.date);
		addSolution(s, Bibo.authorList, DC.contributor, DCTerms.title);
		addSolution(s, Bibo.authorList, DC.contributor, RDF.type);
		addSolution(s, Bibo.authorList, DCTerms.date, DC.contributor);
		addSolution(s, Bibo.authorList, DCTerms.date, DCTerms.date);
		addSolution(s, Bibo.authorList, DCTerms.date, DCTerms.title);
		addSolution(s, Bibo.authorList, DCTerms.date, RDF.type);
		addSolution(s, Bibo.authorList, DCTerms.title, DC.contributor);
		addSolution(s, Bibo.authorList, DCTerms.title, DCTerms.date);
		addSolution(s, Bibo.authorList, DCTerms.title, DCTerms.title);
		addSolution(s, Bibo.authorList, DCTerms.title, RDF.type);
		addSolution(s, Bibo.authorList, RDF.type, DC.contributor);
		addSolution(s, Bibo.authorList, RDF.type, DCTerms.date);
		addSolution(s, Bibo.authorList, RDF.type, DCTerms.title);
		addSolution(s, Bibo.authorList, RDF.type, RDF.type);
		addSolution(s, DC.contributor, Bibo.authorList, Bibo.authorList);
		addSolution(s, DC.contributor, Bibo.authorList, DCTerms.date);
		addSolution(s, DC.contributor, Bibo.authorList, DCTerms.title);
		addSolution(s, DC.contributor, Bibo.authorList, RDF.type);
		addSolution(s, DC.contributor, DCTerms.date, Bibo.authorList);
		addSolution(s, DC.contributor, DCTerms.date, DCTerms.date);
		addSolution(s, DC.contributor, DCTerms.date, RDF.type);
		addSolution(s, DC.contributor, DCTerms.date, DCTerms.title);
		addSolution(s, DC.contributor, DCTerms.title, Bibo.authorList);
		addSolution(s, DC.contributor, DCTerms.title, DCTerms.date);
		addSolution(s, DC.contributor, DCTerms.title, DCTerms.title);
		addSolution(s, DC.contributor, DCTerms.title, RDF.type);
		addSolution(s, DC.contributor, RDF.type, Bibo.authorList);
		addSolution(s, DC.contributor, RDF.type, DCTerms.date);
		addSolution(s, DC.contributor, RDF.type, DCTerms.title);
		addSolution(s, DC.contributor, RDF.type, RDF.type);
		addSolution(s, RDF.type, Bibo.authorList, Bibo.authorList);
		addSolution(s, RDF.type, Bibo.authorList, DC.contributor);
		addSolution(s, RDF.type, Bibo.authorList, DCTerms.date);
		addSolution(s, RDF.type, Bibo.authorList, DCTerms.title);
		addSolution(s, RDF.type, DC.contributor, Bibo.authorList);
		addSolution(s, RDF.type, DC.contributor, DC.contributor);
		addSolution(s, RDF.type, DC.contributor, DCTerms.date);
		addSolution(s, RDF.type, DC.contributor, DCTerms.title);
		addSolution(s, RDF.type, DCTerms.date, Bibo.authorList);
		addSolution(s, RDF.type, DCTerms.date, DCTerms.date);
		addSolution(s, RDF.type, DCTerms.date, DC.contributor);
		addSolution(s, RDF.type, DCTerms.date, DCTerms.title);
		addSolution(s, RDF.type, DCTerms.title, Bibo.authorList);
		addSolution(s, RDF.type, DCTerms.title, DC.contributor);
		addSolution(s, RDF.type, DCTerms.title, DCTerms.date);
		addSolution(s, RDF.type, DCTerms.title, DCTerms.title);
		addSolution(s, DCTerms.date, Bibo.authorList, Bibo.authorList);
		addSolution(s, DCTerms.date, Bibo.authorList, DC.contributor);
		addSolution(s, DCTerms.date, Bibo.authorList, DCTerms.title);
		addSolution(s, DCTerms.date, Bibo.authorList, RDF.type);
		addSolution(s, DCTerms.date, DC.contributor, Bibo.authorList);
		addSolution(s, DCTerms.date, DC.contributor, DC.contributor);
		addSolution(s, DCTerms.date, DC.contributor, DCTerms.title);
		addSolution(s, DCTerms.date, DC.contributor, RDF.type);
		addSolution(s, DCTerms.date, DCTerms.title, Bibo.authorList);
		addSolution(s, DCTerms.date, DCTerms.title, DC.contributor);
		addSolution(s, DCTerms.date, DCTerms.title, DCTerms.title);
		addSolution(s, DCTerms.date, DCTerms.title, RDF.type);
		addSolution(s, DCTerms.date, RDF.type, Bibo.authorList);
		addSolution(s, DCTerms.date, RDF.type, DC.contributor);
		addSolution(s, DCTerms.date, RDF.type, DCTerms.title);
		addSolution(s, DCTerms.date, RDF.type, RDF.type);

		Lattice<GeneralizedQuery, Var> lattice = QueryTemplateLattice.buildLattice(QueryFactory.create(this.qsGen), s);
	}

	private void addSolution(final List<QuerySolution> list, RDFNode dpt1, RDFNode opt1, RDFNode opt2) {
		QuerySolutionMap sol = new QuerySolutionMap();
		sol.add("dpt1", dpt1);
		sol.add("opt1", opt1);
		sol.add("opt2", opt2);
		list.add(sol);
	}

}
