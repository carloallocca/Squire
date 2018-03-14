package uk.ac.open.kmi.squire.querytemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.junit.Test;

import uk.ac.open.kmi.squire.querytemplate.StarTemplate.NotAStarQuery;

public class StarTemplateTest {

	@Test
	public void differentVariables() throws Exception {
		String qs1 = "SELECT ?x1 ?z1 WHERE { ?x1 ?p1 ?z1 }";
		String qs2 = "SELECT ?x2 ?z2 WHERE { ?z2 ?p2 ?x2 }";
		StarTemplate tpl1 = StarTemplate.fromQuery(QueryFactory.create(qs1));
		StarTemplate tpl2 = StarTemplate.fromQuery(QueryFactory.create(qs2));
		assertEquals(tpl1, tpl2);
	}

	@Test
	public void includesStarPattern() throws Exception {
		String qs = "SELECT ?x ?z WHERE { ?x ?p ?y ; a ?t . ?y ?p1 ?z }";
		Query query = QueryFactory.create(qs);
		try {
			StarTemplate.fromQuery(query);
			fail();
		} catch (NotAStarQuery ex) {}
	}

	@Test
	public void notStarQuery() throws Exception {
		String qs = "SELECT ?x ?z WHERE { ?x ?p ?z . ?z a ?t }";
		Query query = QueryFactory.create(qs);
		try {
			StarTemplate.fromQuery(query);
			fail();
		} catch (NotAStarQuery ex) {}
	}

	@Test
	public void queryWithPathPattern() throws Exception {
		String qs = "SELECT ?x ?z WHERE { ?x <http://dio.cane/1>/<http://dio.cane/2> ?t }";
		Query query = QueryFactory.create(qs);
		try {
			StarTemplate.fromQuery(query);
			fail();
		} catch (UnsupportedOperationException ex) {}
	}

	@Test
	public void reusedVariables() throws Exception {
		String qs1 = "SELECT ?x1 ?z1 WHERE { ?x1 ?p1 ?z1 }";
		String qs2 = "SELECT ?x1 ?z1 WHERE { ?x1 ?p1 ?x1 }";
		StarTemplate tpl1 = StarTemplate.fromQuery(QueryFactory.create(qs1));
		StarTemplate tpl2 = StarTemplate.fromQuery(QueryFactory.create(qs2));
		assertFalse(tpl1.equals(tpl2));
	}

	@Test
	public void sameVariables() throws Exception {
		String qs1 = "SELECT ?x1 ?z1 WHERE { ?x1 ?p1 ?z1 }";
		String qs2 = "SELECT ?x1 WHERE { ?x1 ?p1 ?z1 }";
		StarTemplate tpl1 = StarTemplate.fromQuery(QueryFactory.create(qs1));
		StarTemplate tpl2 = StarTemplate.fromQuery(QueryFactory.create(qs2));
		assertEquals(tpl1, tpl2);
	}

	@Test
	public void starQuery() throws Exception {
		String qs = "SELECT ?x ?z WHERE { ?x a ?t ; ?p ?z }";
		Query query = QueryFactory.create(qs);
		StarTemplate.fromQuery(query);

	}

}
