package uk.ac.open.kmi.squire.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlUtilsTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	String _valMod = "http://data.open.ac.uk/course/k313";

	String _valTitle = "Leadership and management in health and social care";
	String prefix = "http://example.org/";
	String raw = "{  \"head\": {    \"vars\": [ \"mod\" , \"title\" ]  } ,  "
			+ "\"results\": {    \"bindings\": [      {        " + "\"mod\": { \"type\": \"uri\" , " + "\"value\": \""
			+ _valMod + "\" } ,        " + "\"title\": { \"type\": \"literal\" , " + "\"value\": \"" + _valTitle
			+ "\" }      }    ]  }}";

	@Test
	public void inflateBindingsNoReduction() throws Exception {
		// Build the reduction map
		Map<Var, Set<Var>> reducedVars = new HashMap<>();

		// Build the reduced query solutions
		List<QuerySolution> solsRed = new ArrayList<>();
		addSolution(solsRed, "A", "X");
		addSolution(solsRed, "A", "Y");
		addSolution(solsRed, "B", "Z");
		addSolution(solsRed, "C", "Z");
		addSolution(solsRed, "B", "Y");

		List<QuerySolution> expanded = SparqlUtils.inflateResultSet(solsRed, reducedVars);
		assertFalse(expanded.isEmpty());
		assertSame(5, expanded.size());
		assertTrue(checkSolution(expanded, "A", "X"));
		assertTrue(checkSolution(expanded, "A", "Y"));
		assertTrue(checkSolution(expanded, "B", "Z"));
		assertTrue(checkSolution(expanded, "C", "Z"));
		assertTrue(checkSolution(expanded, "B", "Y"));

		assertFalse(checkSolution(expanded, "A", "Z"));
		assertFalse(checkSolution(expanded, "B", "X"));
		assertFalse(checkSolution(expanded, "C", "X"));
		assertFalse(checkSolution(expanded, "C", "Y"));
	}

	@Test
	public void inflateBindingsEmptyReduction() throws Exception {
		// Build the reduction map
		Map<Var, Set<Var>> reducedVars = new HashMap<>();
		Var v_y1 = Var.alloc("y1");
		reducedVars.put(v_y1, new HashSet<>());

		// Build the reduced query solutions
		List<QuerySolution> solsRed = new ArrayList<>();
		addSolution(solsRed, "A", "X");
		addSolution(solsRed, "A", "Y");
		addSolution(solsRed, "B", "Z");
		addSolution(solsRed, "C", "Z");
		addSolution(solsRed, "B", "Y");

		List<QuerySolution> expanded = SparqlUtils.inflateResultSet(solsRed, reducedVars);
		assertFalse(expanded.isEmpty());
		assertSame(5, expanded.size());
		assertTrue(checkSolution(expanded, "A", "X"));
		assertTrue(checkSolution(expanded, "A", "Y"));
		assertTrue(checkSolution(expanded, "B", "Z"));
		assertTrue(checkSolution(expanded, "C", "Z"));
		assertTrue(checkSolution(expanded, "B", "Y"));

		assertFalse(checkSolution(expanded, "A", "Z"));
		assertFalse(checkSolution(expanded, "B", "X"));
		assertFalse(checkSolution(expanded, "C", "X"));
		assertFalse(checkSolution(expanded, "C", "Y"));
	}

	@Test
	public void inflateBindingsOneDoubleReduction() throws Exception {
		// Build the reduction map
		Map<Var, Set<Var>> reducedVars = new HashMap<>();
		Var v_y1 = Var.alloc("y1");
		reducedVars.put(v_y1, new HashSet<>());
		reducedVars.get(v_y1).add(Var.alloc("y2"));
		reducedVars.get(v_y1).add(Var.alloc("y3"));

		// Build the reduced query solutions
		List<QuerySolution> solsRed = new ArrayList<>();
		addSolution(solsRed, "A", "X");
		addSolution(solsRed, "A", "Y");
		addSolution(solsRed, "B", "Z");
		addSolution(solsRed, "C", "Z");
		addSolution(solsRed, "B", "Y");

		List<QuerySolution> expanded = SparqlUtils.inflateResultSet(solsRed, reducedVars);
		assertFalse(expanded.isEmpty());
		assertTrue(checkSolution(expanded, "A", "X", "X", "X"));
		assertTrue(checkSolution(expanded, "A", "X", "X", "Y"));
		assertTrue(checkSolution(expanded, "A", "X", "Y", "X"));
		assertTrue(checkSolution(expanded, "A", "X", "Y", "Y"));
		assertTrue(checkSolution(expanded, "A", "Y", "X", "X"));
		assertTrue(checkSolution(expanded, "A", "Y", "X", "Y"));
		assertTrue(checkSolution(expanded, "A", "Y", "Y", "X"));
		assertTrue(checkSolution(expanded, "A", "Y", "Y", "Y"));
		assertTrue(checkSolution(expanded, "C", "Z", "Z", "Z"));

		assertFalse(checkSolution(expanded, "A", "X", "X", "Z"));
		assertFalse(checkSolution(expanded, "A", "X", "Y", "Z"));
		assertFalse(checkSolution(expanded, "A", "X", "Z", "X"));
		assertFalse(checkSolution(expanded, "A", "X", "Z", "Y"));
		assertFalse(checkSolution(expanded, "A", "X", "Z", "Z"));
		assertFalse(checkSolution(expanded, "A", "Y", "X", "Z"));
		assertFalse(checkSolution(expanded, "A", "Y", "Y", "Z"));
		assertFalse(checkSolution(expanded, "A", "Y", "Z", "X"));
		assertFalse(checkSolution(expanded, "A", "Y", "Z", "Y"));
		assertFalse(checkSolution(expanded, "A", "Y", "Z", "Z"));
		assertFalse(checkSolution(expanded, "C", "Z", "Z", "X"));
		assertFalse(checkSolution(expanded, "C", "Z", "X", "Z"));
		assertFalse(checkSolution(expanded, "C", "X", "Z", "Z"));
	}

	@Test
	public void inflateBindingsOneSingleReduction() throws Exception {

		// Build the reduction map
		Map<Var, Set<Var>> reducedVars = new HashMap<>();
		Var v_y1 = Var.alloc("y1");
		reducedVars.put(v_y1, new HashSet<>());
		reducedVars.get(v_y1).add(Var.alloc("y2"));

		// Build the reduced query solutions
		List<QuerySolution> solsRed = new ArrayList<>();
		addSolution(solsRed, "A", "X");
		addSolution(solsRed, "A", "Y");
		addSolution(solsRed, "B", "Z");
		addSolution(solsRed, "C", "Z");
		addSolution(solsRed, "B", "Y");

		List<QuerySolution> expanded = SparqlUtils.inflateResultSet(solsRed, reducedVars);
		assertFalse(expanded.isEmpty());
		assertTrue(checkSolution(expanded, "A", "X", "X"));
		assertTrue(checkSolution(expanded, "A", "X", "Y"));
		assertTrue(checkSolution(expanded, "A", "Y", "X"));
		assertTrue(checkSolution(expanded, "A", "Y", "Y"));
		assertTrue(checkSolution(expanded, "B", "Y", "Y"));
		assertTrue(checkSolution(expanded, "B", "Y", "Z"));
		assertTrue(checkSolution(expanded, "B", "Z", "Y"));
		assertTrue(checkSolution(expanded, "B", "Z", "Z"));
		assertTrue(checkSolution(expanded, "C", "Z", "Z"));

		assertFalse(checkSolution(expanded, "A", "X", "Z"));
		assertFalse(checkSolution(expanded, "A", "Y", "Z"));
		assertFalse(checkSolution(expanded, "A", "X", "A"));
		assertFalse(checkSolution(expanded, "A", "X", "B"));
		assertFalse(checkSolution(expanded, "B", "X", "Y"));
		assertFalse(checkSolution(expanded, "B", "X", "Z"));
		assertFalse(checkSolution(expanded, "B", "Y", "X"));
		assertFalse(checkSolution(expanded, "C", "X", "Y"));
		assertFalse(checkSolution(expanded, "C", "X", "Z"));
		assertFalse(checkSolution(expanded, "C", "Z", "X"));
		assertFalse(checkSolution(expanded, "C", "Z", "Y"));
	}

	@Test
	public void inflateBindingsTwoSingleReductions() throws Exception {
		// Build the reduction map
		Map<Var, Set<Var>> reducedVars = new HashMap<>();
		Var v_y1 = Var.alloc("y1");
		reducedVars.put(v_y1, new HashSet<>());
		reducedVars.get(v_y1).add(Var.alloc("y2"));
		Var v_z1 = Var.alloc("z1");
		reducedVars.put(v_z1, new HashSet<>());
		reducedVars.get(v_z1).add(Var.alloc("z2"));

		// Build the reduced query solutions
		List<QuerySolution> solsRed = new ArrayList<>();
		addSolution(solsRed, "A", "X", "I");
		addSolution(solsRed, "A", "Y", "I");
		addSolution(solsRed, "A", "Y", "J");
		addSolution(solsRed, "B", "Z", "K");
		addSolution(solsRed, "C", "Z", "J");
		addSolution(solsRed, "B", "Y", "K");

		List<QuerySolution> expanded = SparqlUtils.inflateResultSet(solsRed, reducedVars);
		assertFalse(expanded.isEmpty());
		assertTrue(checkSolution(expanded, "A", "X", "X", "", "I", "I"));
		assertTrue(checkSolution(expanded, "A", "X", "Y", "", "I", "I"));
		assertTrue(checkSolution(expanded, "A", "Y", "X", "", "I", "I"));
		assertTrue(checkSolution(expanded, "A", "Y", "Y", "", "I", "I"));

		assertFalse(checkSolution(expanded, "A", "X", "I", "", "X", "X"));
		assertFalse(checkSolution(expanded, "B", "X", "", "", "K"));
	}

	@Test
	public void parseResultsJson() throws Exception {
		List<Var> vars = new ArrayList<>();
		vars.add(Var.alloc("mod"));
		vars.add(Var.alloc("title"));
		List<QuerySolution> sol = SparqlUtils.extractProjectedValues(raw, vars);
		assertFalse(sol.isEmpty());
		log.debug("{}", sol);
		assertSame(1, sol.size());
		assertTrue(sol.get(0).contains("mod"));
		assertTrue(sol.get(0).get("mod").isResource());
		assertEquals(_valMod, sol.get(0).get("mod").asResource().getURI());
		assertTrue(sol.get(0).contains("title"));
		log.debug("title is literal? {} (value='{}')", sol.get(0).get("title").isLiteral(), sol.get(0).get("title"));
		assertTrue(sol.get(0).get("title").isLiteral());
		assertEquals(_valTitle, sol.get(0).get("title").asLiteral().getValue());
	}

	private void addSolution(List<QuerySolution> solutions, String p1, String y1) {
		QuerySolutionMap sol = new QuerySolutionMap();
		sol.add("p1", ResourceFactory.createResource(prefix + p1));
		sol.add("y1", ResourceFactory.createResource(prefix + y1));
		solutions.add(sol);
	}

	private void addSolution(List<QuerySolution> solutions, String p1, String y1, String z1) {
		QuerySolutionMap sol = new QuerySolutionMap();
		sol.add("p1", ResourceFactory.createResource(prefix + p1));
		sol.add("y1", ResourceFactory.createResource(prefix + y1));
		sol.add("z1", ResourceFactory.createResource(prefix + z1));
		solutions.add(sol);
	}

	private boolean checkSolution(List<QuerySolution> solutions, String... bind) {
		for (QuerySolution s : solutions) {
			boolean found = true;
			for (int i = 0; i < bind.length; i++) {
				String varName;
				switch (i) {
				case 0:
					varName = "p1";
					break;
				case 1:
					varName = "y1";
					break;
				case 2:
					varName = "y2";
					break;
				case 3:
					varName = "y3";
					break;
				case 4:
					varName = "z1";
					break;
				case 5:
					varName = "z2";
					break;
				default:
					varName = "";
				}
				if (!s.contains(varName) && !bind[i].isEmpty()) {
					log.error("Variable {} not found in solution {}", varName, s);
					return false;
				}
				if (!bind[i].isEmpty())
					found &= s.getResource(varName).equals(ResourceFactory.createResource(prefix + bind[i]));
				if (!found) break;
			}
			if (found) return true;
		}
		return false;
	}

}
