package uk.ac.open.kmi.squire.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlUtilsTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	String valMod = "http://data.open.ac.uk/course/k313";
	String valTitle = "Leadership and management in health and social care";
	String raw = "{  \"head\": {    \"vars\": [ \"mod\" , \"title\" ]  } ,  "
			+ "\"results\": {    \"bindings\": [      {        " + "\"mod\": { \"type\": \"uri\" , " + "\"value\": \""
			+ valMod + "\" } ,        " + "\"title\": { \"type\": \"literal\" , " + "\"value\": \"" + valTitle
			+ "\" }      }    ]  }}";

	@Test
	public void testParseResultsJson() throws Exception {
		List<Var> vars = new ArrayList<>();
		vars.add(Var.alloc("mod"));
		vars.add(Var.alloc("title"));
		List<QuerySolution> sol = SparqlUtils.extractProjectedValues(raw, vars);
		assertFalse(sol.isEmpty());
		log.debug("{}", sol);
		assertSame(1, sol.size());
		assertTrue(sol.get(0).contains("mod"));
		assertTrue(sol.get(0).get("mod").isResource());
		assertEquals(valMod, sol.get(0).get("mod").asResource().getURI());
		assertTrue(sol.get(0).contains("title"));
		log.debug("title is literal? {} (value='{}')", sol.get(0).get("title").isLiteral(), sol.get(0).get("title"));
		assertTrue(sol.get(0).get("title").isLiteral());
		assertEquals(valTitle, sol.get(0).get("title").asLiteral().getValue());
	}

}
