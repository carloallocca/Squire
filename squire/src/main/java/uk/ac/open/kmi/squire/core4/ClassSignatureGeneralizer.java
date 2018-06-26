package uk.ac.open.kmi.squire.core4;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.operation.GeneralizeNode;
import uk.ac.open.kmi.squire.rdfdataset.ClassSignature;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * Tries to compute a least general generalization by taking into account the
 * association between classes and properties of their instances in the query
 * pattern. This generalizer acts upon triples on the rdf:type predicate, i.e.
 * assumed to have classes as objects. If there are none, it acts like a
 * {@link BasicGeneralizer} (TODO should do nothing instead).
 * 
 * @author Alessandro Adamou<alexdma@apache.org>
 *
 */
public class ClassSignatureGeneralizer extends BasicGeneralizer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public ClassSignatureGeneralizer(IRDFDataset dsSource, IRDFDataset dsTarget) {
		super(dsSource, dsTarget);
		if (dsTarget == null) throw new IllegalArgumentException("Target dataset cannot be null.");
	}

	@Override
	public Set<Query> generalize(final Query q) {
		final Set<Query> generalized = new HashSet<>();

		// TODO remove the call to the super generalization...
		Query[] qGeneral;
		Set<Query> preProc = super.generalize(q);
		if (preProc.isEmpty()) qGeneral = new Query[] { QueryFactory.create(q) };
		else qGeneral = preProc.toArray(new Query[0]);
		Map<Node, Set<Node>> typesPerSubjectSurviving = new HashMap<>();
		MappedQuery[] mq = new MappedQuery[] { new MappedQuery(q) };
		// The operator that will be applied over and over.
		GeneralizeNode qg = new GeneralizeNode();

		/*
		 * If there are multiple triple patterns on rdf:type with the same subject, take
		 * all those targeted for generalization and collapse them into one.
		 */
		Map<Node, ClassSignature> keptD2Signatures = new HashMap<>();
		for (Entry<Node, Set<Node>> entry : mq[0].getTypesPerSubject().entrySet()) {
			log.debug("Subject {} (#classes = {})", entry.getKey(), entry.getValue().size());
			for (Node claz : entry.getValue()) {
				log.debug("Processing RDf type node {}", claz);
				Var v = ifObjectIsNotD2ThenGenerateVariableNew(claz);
				log.debug(" ... produced template variable {}", v);
				if (v == null) {
					if (claz.isURI()) {
						if (!keptD2Signatures.containsKey(claz)
								&& rdfd2.getClassSignatures().containsKey(claz.getURI()))
							keptD2Signatures.put(claz, rdfd2.getClassSignatures().get(claz.getURI()));
					}
					if (!typesPerSubjectSurviving.containsKey(entry.getKey()))
						typesPerSubjectSurviving.put(entry.getKey(), new HashSet<>());
					typesPerSubjectSurviving.get(entry.getKey()).add(claz);
				} else qGeneral[0] = qg.perform(qGeneral[0], claz, v);
			}
		}

		mq[0] = new MappedQuery(qGeneral[0]); // Update the mapped version of the query

		// Treat properties based on concrete types
		ElementWalker.walk(qGeneral[0].getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					TriplePath tp = triples.next();
					log.debug("Inspecting triple pattern [{}]", tp);
					Node s = tp.getSubject();
					if (mq[0].getTypesPerSubject().containsKey(s)) for (Node type : mq[0].getTypes(s)) {
						if (keptD2Signatures.containsKey(type)) {
							log.debug(" ... checking signature for type <{}> in target dataset.", type);
							log.trace("{}", keptD2Signatures.get(type).listPathOrigins());
							// Check if the predicate of that TP stays or goes.
							Node p = tp.getPredicate();
							if (tp.getPredicate().isURI() && !RDF.type.asNode().equals(p)) {
								if (keptD2Signatures.get(type).hasProperty(p.getURI()))
									log.debug(" ... KEEP - Signature contains predicate <{}>", p);
								else {
									log.debug(" ... GENERALIZE - Signature does not contain predicate <{}>", p);
									// XXX should arg1 be true if there is no common class?
									Var v = makeTplVariableFromPredicate(p, false);
									log.debug(" ... replacing with template variable {}", v);
									if (v != null) qGeneral[0] = qg.perform(qGeneral[0], p, v);
								}
							}
						} else log.warn(
								"WTF? There is no signature in D2 for type <{}> - this should have already been dealt with.",
								type);
					}
				}
			}
		});

		log.debug("Intermediate generalized query:\r\n{}", mq[0].getQuery());

		/*
		 * If the types have been generalized (but a type expression still exists in the
		 * query), treat the properties based on whether they occur in the same type.
		 */
		for (Node sub : mq[0].getRootSubjects()) {
			Set<String> namedTypes = new HashSet<>();
			for (Node type : mq[0].getTypes(sub))
				if (type.isURI()) namedTypes.add(type.getURI());

			// The case where there is at least one rdf:type TP with a named object
			if (namedTypes.isEmpty()) {
				// Group properties in the query depending on their presence and co-occurrences.
				// First compute the unification, i.e. the largest subset of co-occurring
				// properties.
				log.debug("Grouping properties by co-occurrence...");
				TreeMap<Integer, Set<Set<String>>> groupsBySize = new TreeMap<>();
				for (Node n : mq[0].getPathOrigins(sub)) {
					if (RDF.type.asNode().equals(n)) continue;
					Set<String> group = new HashSet<>();
					if (n.isURI()) group.add(n.getURI());
					for (Entry<String, Integer> entry : rdfd2.getCoOccurringProperties(n.getURI()).entrySet()) {
						Node nx = NodeFactory.createURI(entry.getKey());
						if (mq[0].getPathOrigins(sub).contains(nx) && entry.getValue() > 0) group.add(entry.getKey());
					}
					if (!groupsBySize.containsKey(group.size())) groupsBySize.put(group.size(), new HashSet<>());
					groupsBySize.get(group.size()).add(group);
				}
				// Produce the final generalized queries from the largest groups.
				if (groupsBySize.isEmpty()) log.warn("Sorry, no suitable property grouping could be created.");
				else {
					log.debug("Picking the largest groups:");
					for (Set<String> group : groupsBySize.firstEntry().getValue()) {
						log.debug(" - group: {}", group);
						Set<Node> genUs = new HashSet<>();
						Query groupQ = QueryFactory.create(mq[0].getQuery());
						for (Node n : mq[0].getPathOrigins(sub))
							if (n.isURI() && !RDF.type.asNode().equals(n) && !group.contains(n.getURI())) {
								log.debug(" - ... generalizing on property node <{}>", n);
								genUs.add(n);
								Var v = makeTplVariableFromPredicate(n, false);
								log.debug(" - ... produced template variable <{}>", v);
								if (v != null) groupQ = qg.perform(groupQ, n, v);
							}
						generalized.add(groupQ);
					}
				}
			}
		}

		// If the above has produced nothing, return the query to the point where it was
		// processed.
		if (generalized.isEmpty()) generalized.add(mq[0].getQuery());
		log.debug("Generalized queries follow:");
		int i = 0;
		for (Iterator<Query> it = generalized.iterator(); it.hasNext(); i++)
			log.debug(" - q{} : {}", i, it.next());
		
		return Collections.unmodifiableSet(generalized);
	}

}
