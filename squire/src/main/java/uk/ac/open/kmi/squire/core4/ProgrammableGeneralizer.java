package uk.ac.open.kmi.squire.core4;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.operation.SparqlQueryGeneralization;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * A {@link Generalizer} that is able to return multiple generalized queries
 * depending on the parameters passed to the generalize operation.
 * 
 * @author alessandro
 *
 */
public class ProgrammableGeneralizer extends Generalizer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public ProgrammableGeneralizer(Query query, IRDFDataset d1, IRDFDataset d2) {
		super(query, d1, d2);
	}

	/**
	 * 
	 * @param preservedNodes
	 *            the maximum number of concrete nodes (URIs, Literals etc.) that
	 *            the resulting general queries should have. A value of zero will
	 *            result in a completely general query. A negative value lets the
	 *            generalization algorithm decide.
	 * @return
	 */
	public Set<Query> generalizeMultiple() {
		Set<Query> result = new HashSet<>();
		Query qGeneral = super.generalize();
		result.add(qGeneral);
		generalizeStep(qGeneral, result);
		log.debug("Total {} generalized queries", result.size());
		return result;
	}

	private void generalizeStep(Query q, final Set<Query> results) {
		SparqlQueryGeneralization op = new SparqlQueryGeneralization();
		ElementWalker.walk(q.getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> it = el.patternElts();
				while (it.hasNext()) {
					TriplePath tp = it.next();
					visit(tp);

				}
			}

			private void visit(TriplePath tp) {
				log.debug("Triple path: {}", tp);
				if (tp.getSubject().isConcrete())
					log.debug("Subject <{}> is concrete and can be generalized", tp.getSubject());
				if (tp.getPredicate().isConcrete()) {
					boolean doit = true;
					Node p = tp.getPredicate();
					log.debug("Predicate <{}> is concrete and can be generalized", p);
					if (tp.getObject().isVariable()) {
						Var o = (Var) tp.getObject();
						if (o.getName().startsWith(TEMPLATE_VAR_INDIVIDUAL)
								|| o.getName().startsWith(TEMPLATE_VAR_LITERAL)
								|| o.getName().startsWith(TEMPLATE_VAR_CLASS)) {
							log.debug("However, object {} is already a template variable, so will skip for now.",
									tp.getObject());
							doit = false;
						}
					}
					if (doit) {
						Var v = makeTplVariableFromPredicate(p, false);
						// Remember to apply the operation to a clone of the query.
						Query newQ = op.perform(QueryFactory.create(q.toString()), p, v);
						results.add(newQ);
						log.debug("Addinq query to result set:\r\n{}", newQ);
						generalizeStep(newQ, results);

					}
				}
				if (tp.getObject().isConcrete())
					log.debug("Object <{}> is concrete and can be generalized", tp.getObject());
			}
		});
	}

}
