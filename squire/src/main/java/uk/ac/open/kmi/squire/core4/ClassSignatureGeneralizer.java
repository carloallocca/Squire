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

import uk.ac.open.kmi.squire.operation.SparqlQueryGeneralization;
import uk.ac.open.kmi.squire.rdfdataset.ClassSignature;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * Tries to compute a least general generalization by taking into account
 * whether there are triples on the rdf:type predicate with classes as objects.
 * If there are none, it does nothing.
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
	public Set<Query> generalize(Query q) {

		Query[] qGeneral;
		Set<Query> preProc = super.generalize(q);
		if (preProc.isEmpty()) qGeneral = new Query[] { QueryFactory.create(q) };
		else qGeneral = preProc.toArray(new Query[0]);
		// The operation that will be applied over and over.
		SparqlQueryGeneralization qg = new SparqlQueryGeneralization();

		Map<Node, Set<Node>> typesPerSubjectSurviving = new HashMap<>();
		MappedQuery[] mq = new MappedQuery[] { new MappedQuery(q) };

		/*
		 * If there are multiple triple patterns on rdf:type with the same subject, take
		 * all those targeted for generalization and collapse them into one.
		 */
		Map<Node, ClassSignature> keptD2Signatures = new HashMap<>();

		for (Entry<Node, Set<Node>> entry : mq[0].getTypesPerSubject().entrySet()) {
			log.debug("Subject {}", entry.getKey());
			log.debug(" - #classes = {}", entry.getValue().size());

			for (Node claz : entry.getValue()) {
				log.debug(" ... {}", claz);
				Var v = ifObjectIsNotD2ThenGenerateVariableNew(claz);
				log.debug(" ... - var = {}", v);
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

		mq[0] = new MappedQuery(qGeneral[0]);

		final Set<Query> generalized = new HashSet<>();
		// Treat properties based on concrete types
		ElementWalker.walk(qGeneral[0].getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					TriplePath tp = triples.next();
					log.debug("{}", tp);
					Node s = tp.getSubject();
					if (mq[0].getTypesPerSubject().containsKey(s)) for (Node type : mq[0].getTypes(s)) {
						if (keptD2Signatures.containsKey(type)) {
							log.debug("Signature for <{}> in D2: {}", type,
									keptD2Signatures.get(type).listPathOrigins());
							// So, what is the predicate? Can it stay?
							if (tp.getPredicate().isURI() && !RDF.type.asNode().equals(tp.getPredicate())) {
								if (keptD2Signatures.get(type).hasProperty(tp.getPredicate().getURI()))
									log.debug(" ... Yay! It stays.");
								else {
									log.debug(" ... sorry, it goes.");
									// XXX should arg1 be true if there is no common class?
									Var v = makeTplVariableFromPredicate(tp.getPredicate(), false);
									if (v != null) qGeneral[0] = qg.perform(qGeneral[0], tp.getPredicate(), v);
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

		// If the types have been generalized (but a type expression still exists in the
		// query), treat the properties based on whether they occur in the same type.
		for (Node sub : mq[0].getRootSubjects()) {
			Set<String> namedTypes = new HashSet<>();
			for (Node type : mq[0].getTypes(sub))
				if (type.isConcrete()) namedTypes.add(type.getURI());
			// The cases of rdf:type triples having no concrete objects
			if (namedTypes.isEmpty()) {

				// group properties in query depending on their presence and co-occurrences

				// First compute the unification, i.e. the largest subset of co-occurring
				// properties
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

				log.debug("Picking the largest groups:");
				if (!groupsBySize.isEmpty()) for (Set<String> group : groupsBySize.firstEntry().getValue()) {
					log.debug(" - Group: {}", group);
					// Produce the query from the group
					Set<Node> genUs = new HashSet<>();
					Query groupQ = QueryFactory.create(mq[0].getQuery());
					for (Node n : mq[0].getPathOrigins(sub))
						if (n.isURI() && !RDF.type.asNode().equals(n) && !group.contains(n.getURI())) {
							log.debug(" ... should generalize on property <{}>", n);
							genUs.add(n);
							Var v = makeTplVariableFromPredicate(n, false);
							if (v != null) groupQ = qg.perform(groupQ, n, v);
						}
					generalized.add(groupQ);
				}
			}

		}
		if (generalized.isEmpty()) generalized.add(mq[0].getQuery());
		log.debug("Generalized queries follow:");
		int i = 0;
		for (Iterator<Query> it = generalized.iterator(); it.hasNext(); i++)
			log.debug(" - q{} : {}", i, it.next());
		return Collections.unmodifiableSet(generalized);
	}

}
