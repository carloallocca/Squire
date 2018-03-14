package uk.ac.open.kmi.squire.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.junit.Test;

/**
 * Tests to make sure Jena's assumed characteristics, which are necessary for
 * the implementation to work, are confirmed/preserved.
 */
public class TestJenaAssumptions {

	/*
	 * Two query solutions with equivalent content are equivalent.
	 */
	@Test
	public void qSolEqual() throws Exception {
		QuerySolutionMap sol1 = new QuerySolutionMap(), sol2 = new QuerySolutionMap();
		sol1.add("ct1", FOAF.Person);
		sol1.add("opt1", FOAF.knows);
		sol2.add("ct1", FOAF.Person);
		sol2.add("opt1", FOAF.knows);
		assertEquals(sol1.asMap(), sol2.asMap());
	}

	/*
	 * Two result sets with equivalent query solutions are equivalent.
	 */
	@Test
	public void qSolExists() throws Exception {
		Set<Map<String, RDFNode>> ss = new HashSet<>();
		QuerySolutionMap sol1 = new QuerySolutionMap();
		sol1.add("ct1", FOAF.Person);
		sol1.add("opt1", FOAF.knows);
		ss.add(sol1.asMap());
		QuerySolutionMap sol2 = new QuerySolutionMap();
		sol2.add("ct1", FOAF.Person);
		sol2.add("opt1", FOAF.knows);
		assertTrue(ss.contains(sol2.asMap()));
		ss.add(sol2.asMap());
		assertEquals(1, ss.size());
	}

	/*
	 * Two triple paths made only of the same variables are equal.
	 */
	@Test
	public void tpEquals() throws Exception {
		TriplePath tp1 = makeTpVars("x", "p", "y");
		TriplePath tp2 = makeTpVars("x", "p", "y");
		assertEquals(tp1, tp2);
	}

	/**
	 * Two sets of triple paths made only of the same variables are equal.
	 * 
	 * @throws Exception
	 */
	@Test
	public void tpSetEquals() throws Exception {
		Set<TriplePath> set1 = new HashSet<>();
		set1.add(makeTpVars("x1", "p1", "y1"));
		set1.add(makeTpVars("x2", "p2", "y2"));
		Set<TriplePath> set2 = new HashSet<>();
		set2.add(makeTpVars("x1", "p1", "y1"));
		set2.add(makeTpVars("x2", "p2", "y2"));
		assertEquals(set1, set2);
	}

	private TriplePath makeTpVars(String s, String p, String o) {
		return new TriplePath(new Triple(Var.alloc(s), Var.alloc(p), Var.alloc(o)));

	}

}
