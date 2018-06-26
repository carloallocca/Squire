package uk.ac.open.kmi.squire.fca;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core4.GeneralizedQuery;
import uk.ac.open.kmi.squire.operation.InstantiateTemplateVar;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.TemplateVariableScanner;

public class QueryTemplateLattice extends Lattice<GeneralizedQuery, Var> {

	private static Map<Set<Var>, Concept<GeneralizedQuery, Var>> conceptTable;

	/**
	 * The operator should be stateless now, but XXX double-check just in case.
	 */
	private static InstantiateTemplateVar op_inst = new InstantiateTemplateVar();

	private static final Logger log = LoggerFactory.getLogger(QueryTemplateLattice.class);

	public static Lattice<GeneralizedQuery, Var> buildLattice(Query generalQuery, List<QuerySolution> solutions) {
		long before = System.currentTimeMillis();
		conceptTable = new HashMap<>();
		Lattice<GeneralizedQuery, Var> l = new QueryTemplateLattice();
		Set<Var> tplVars = getQueryTemplateVariableSet(generalQuery);
		// From each of the solutions, work your way down from the general query
		int c = 0, i = 1;
		GeneralizedQuery topGQ = new GeneralizedQuery(generalQuery);
		Concept<GeneralizedQuery, Var> top = new Concept<GeneralizedQuery, Var>(tplVars);
		top.addInstance(topGQ);
		l.setTop(top);
		log.debug("Setting top concept {}", top);
		conceptTable.put(top.getIntension(), top);
		log.trace("Top concept has weight {}", l.getTop().getWeight());
		for (QuerySolution sol : solutions) {
			log.trace("Solution {}: {}", c++, sol);
			buildLatticeStep(topGQ, sol, l.getTop(), i++);
		}
		log.debug("Lattice built in {} ms and {} iterations.", System.currentTimeMillis() - before, i);
		conceptTable.clear();
		return l;
	}

	/**
	 * The recursive step of top-down lattice construction.
	 * 
	 * @param q
	 * @param sol
	 * @param step
	 */
	private static void buildLatticeStep(GeneralizedQuery q, QuerySolution sol, Concept<GeneralizedQuery, Var> step,
			int iteration) {
		Set<Var> intension = step.getIntension();
		// Only the bottom concept has an empty intension, which stops recursion.
		for (Var v : intension) {
			if (!intension.contains(v)) throw new IllegalStateException(
					"Variable " + v + " was not in superior concept intension " + intension);
			// The new intension is the same minus the variable at hand.
			Set<Var> intInf = new HashSet<>(intension);
			intInf.remove(v);
			final Concept<GeneralizedQuery, Var> conc;
			if (!conceptTable.containsKey(intInf)) conceptTable.put(intInf, new Concept<>(intInf));
			conc = conceptTable.get(intInf);
			if (!step.hasInferior(intInf)) {
				step.addInferior(conc);
				log.trace("Added inferior {} to step {}", conc, step);
			} else log.trace("Step {} already has inferior {}", step, intInf.toArray());
			Concept<GeneralizedQuery, Var> inf = step.getInferior(intInf);
			RDFNode node = sol.get(v.getName());
			if (node != null && node.asNode().isURI()) {
				Query qChild = q.getQuery().cloneQuery();
				qChild = op_inst.instantiateVarTemplate(qChild, v, node.asNode());
				GeneralizedQuery gq = new GeneralizedQuery(qChild);
				log.trace("is this an instance of {}?\r\n{}", inf, gq.getQuery());
				inf.addInstance(gq);
				buildLatticeStep(gq, sol, inf, iteration++);
			} else log.error("Unexpected state of node {} for template variable '{}'", node, step);
		}
	}

	/**
	 * Returns each time a new set, which is modifiable so that template variables
	 * can be subtracted progressively.
	 * 
	 * @param qR
	 * @return
	 */
	private static Set<Var> getQueryTemplateVariableSet(Query qR) {
		TemplateVariableScanner v = new TemplateVariableScanner();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getTemplateVariables();
	}

}
