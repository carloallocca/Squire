/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.operation.GeneralizeNode;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * A {@link Generalizer} that simply tries to get a query rid of all the
 * classes, properties etc. that are never present in the target dataset. Its
 * {@link #generalize(Query)} method produces a single query that is reasonably
 * likely to (1) be satisfied by the target dataset, and (2) be specialized into
 * the optimal recommendation.
 * <p>
 * Note however that neither of the above properties is guaranteed. For example,
 * it may retain two properties that are present in the target dataset, but
 * never co-exist for the same entity.
 * </p>
 * This generalizer has the following policy:
 * <ol>
 * <li>Object properties always become object property templates, similarly for
 * datatype properties.
 * <li>If the property category is unknown, a generic property template is
 * applied
 * <li>The class signatures are ignored, i.e. it does not check if there are
 * enough object/datatype properties for the target dataset to satisfy the
 * condition of (1).
 * <li>Triple patterns are never removed
 * </ol>
 * 
 * @author carloallocca
 */
public class BasicGeneralizer extends AbstractMappedQueryTransform implements Generalizer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected final IRDFDataset rdfd1, rdfd2;

	public BasicGeneralizer(IRDFDataset d1, IRDFDataset d2) {
		super();
		this.rdfd1 = d1;
		if (d2 == null)
			throw new IllegalArgumentException("Target dataset cannot be null.");
		this.rdfd2 = d2;
	}

	@Override
	public Set<Query> generalize(Query query) {
		if (query == null)
			throw new IllegalArgumentException("Query cannot be null.");
		// The generalized query is created from a clone of the original one.
		Query qGeneral = QueryFactory.create(query);
		// Instantiated once, applied wherever possible.
		GeneralizeNode qg = new GeneralizeNode();
		// SUBJECT
		for (Node subj : getEntitySet(query, NodeRole.SUBJECT))
			if (subj.isConcrete() && !subj.isBlank()) {
				Var tplVar = ifSubjectIsNotD2ThenGenerateVariableNew(subj);
				if (tplVar != null)
					qGeneral = qg.perform(qGeneral, subj, tplVar);
			}
		// PREDICATE
		for (Node pred : getEntitySet(query, NodeRole.PREDICATE))
			if (pred.isConcrete() && !pred.isBlank()) {
				if (!this.rdfd2.getRDFVocabulary().contains(pred.getURI())) {
					Var tplVar = makeTplVariableFromPredicate(pred, true);
					if (tplVar != null)
						qGeneral = qg.perform(qGeneral, pred, tplVar);
				}
			}
		// OBJECT
		for (Node obj : getEntitySet(query, NodeRole.OBJECT))
			if (obj.isConcrete() && !obj.isBlank()) {
				Var tplVar = ifObjectIsNotD2ThenGenerateVariableNew(obj);
				if (tplVar != null)
					qGeneral = qg.perform(qGeneral, obj, tplVar);
			}
		return Collections.singleton(qGeneral);
	}

	private Set<Node> getEntitySet(Query q, NodeRole nodeType) {
		final Set<Node> objects = new HashSet<>(); // Remember distinct objects in this
		// This will walk through all parts of the query
		ElementWalker.walk(q.getQueryPattern(), new ElementVisitorBase() {
			@Override
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					TriplePath tp = triples.next();
					Node n;
					switch (nodeType) {
					case SUBJECT:
						n = tp.getSubject();
						break;
					case PREDICATE:
						n = tp.getPredicate();
						break;
					case OBJECT:
						n = tp.getObject();
						break;
					default:
						n = null;
					}
					if (n != null)
						objects.add(n);
				}
			}
		});
		return objects;
	}

	protected Var ifObjectIsNotD2ThenGenerateVariableNew(Node obj) {
		if (obj == null)
			throw new IllegalArgumentException("Object node cannot be null.");
		final Var varName;
		if (obj.isURI()) {
			String o = obj.getURI();
			if (rdfd1.getClassSet().contains(obj) && !rdfd2.getClassSet().contains(obj))
				varName = classVarTable.getOrCreateVar(obj, TEMPLATE_VAR_CLASS);
			else if (rdfd1.isInObjectPropertySet(o) && !rdfd2.isInObjectPropertySet(o))
				varName = objectProperyVarTable.getOrCreateVar(obj, TEMPLATE_VAR_PROP_OBJ);
			else if (rdfd1.isInDatatypePropertySet(o) && !rdfd2.isInDatatypePropertySet(o))
				varName = datatypePropertyVarTable.getOrCreateVar(obj, TEMPLATE_VAR_PROP_DT);
			else if (rdfd1.isInRDFVocabulary(o) && !rdfd2.isInRDFVocabulary(o))
				varName = rdfVocVarTable.getOrCreateVar(obj, "rdf");
			else
				return null;
		} else if (obj.isLiteral()) {
			Node lit = NodeFactory.createLiteral(obj.getLiteral());
			varName = literalVarTable.getOrCreateVar(lit, AbstractMappedQueryTransform.TEMPLATE_VAR_LITERAL);
		} else
			return null;
		if (varName == null)
			throw new IllegalStateException("Object node generated a null variable name.");
		return Var.alloc(varName);
	}

	protected Var ifSubjectIsNotD2ThenGenerateVariableNew(Node subj) {
		if (subj == null)
			throw new IllegalArgumentException("Subject node cannot be null.");
		final Node varName;
		if (subj.isURI()) {
			String sub = subj.getURI();
			if (rdfd1.getClassSet().contains(sub) && !rdfd2.getClassSet().contains(sub))
				varName = classVarTable.getOrCreateVar(subj, TEMPLATE_VAR_CLASS);
			else if (rdfd1.isInObjectPropertySet(sub) && !rdfd2.isInObjectPropertySet(sub))
				varName = objectProperyVarTable.getOrCreateVar(subj, TEMPLATE_VAR_PROP_OBJ);
			else if (rdfd1.isInDatatypePropertySet(sub) && !rdfd2.isInDatatypePropertySet(sub))
				varName = datatypePropertyVarTable.getOrCreateVar(subj, TEMPLATE_VAR_PROP_DT);
			else if (rdfd1.isInRDFVocabulary(sub) && !rdfd2.isInRDFVocabulary(sub))
				varName = rdfVocVarTable.getOrCreateVar(subj, "rdf");
			else
				// We assume by exclusion that sub is an individual.
				// XXX is that assumption correct?
				varName = individualVarTable.getOrCreateVar(subj, TEMPLATE_VAR_INDIVIDUAL);
		} else if (subj.isLiteral()) {
			Node lit = NodeFactory.createLiteral(subj.getLiteral());
			varName = literalVarTable.getOrCreateVar(lit, AbstractMappedQueryTransform.TEMPLATE_VAR_LITERAL);
		} else
			return null;
		if (varName == null)
			throw new IllegalStateException("Subject node generated a null variable name.");
		return Var.alloc(varName);
	}

	/**
	 * Creates a template variable for the given property URI if that property
	 * exists in the source dataset and one of the following conditions is met:
	 * <ul>
	 * <li>the property is not present in the target dataset, or
	 * <li>flag onlyIfNotInTargetDS is set to false.
	 * </ul>
	 * 
	 * @param predicate
	 *            the predicate URI (if not a URI the method will return null).
	 * @param onlyIfNotInTargetDS
	 *            an override flag that forces the variable to be generated once
	 *            it's found in the source dataset, regardless of its presence in
	 *            the target dataset.
	 * @return
	 */
	protected Var makeTplVariableFromPredicate(Node predicate, boolean onlyIfNotInTargetDS) {
		if (predicate == null)
			throw new IllegalArgumentException("Predicate node cannot be null.");
		log.trace("Presence of properties in target dataset {} matter", onlyIfNotInTargetDS ? "DOES" : "does NOT");
		final Node varName;
		if (!predicate.isURI())
			return null;
		String p = predicate.getURI();
		log.debug("Inspecting predicate '{}' for generalization.", p);
		log.trace("rdfd1 object property list : {}", rdfd1.getObjectPropertySet());
		log.trace("rdfd1 datatype property list : {}", rdfd1.getDatatypePropertySet());
		log.trace("rdfd2 object property list : {}", rdfd2.getObjectPropertySet());
		log.trace("rdfd2 datatype property list : {}", rdfd2.getDatatypePropertySet());

		// XXX what if the property exist in the other dataset but is used as an
		// object/data property unlike the first dataset?
		if (rdfd1.isInObjectPropertySet(p) && (!onlyIfNotInTargetDS || !rdfd2.isInObjectPropertySet(p)))
			varName = objectProperyVarTable.getOrCreateVar(predicate, TEMPLATE_VAR_PROP_OBJ);
		else if (rdfd1.isInDatatypePropertySet(p) && (!onlyIfNotInTargetDS || !rdfd2.isInDatatypePropertySet(p)))
			varName = datatypePropertyVarTable.getOrCreateVar(predicate, TEMPLATE_VAR_PROP_DT);
		else if (rdfd1.isInPropertySet(p)) {
			// If we don't care if the property exists in the target dataset, generate the
			// template variable, but do take a peek at the target dataset anyway, to decide
			// what kind of property it shall be.
			if (!onlyIfNotInTargetDS) {
				String prefix;
				VarMapping<Var, Node> table;
				if (rdfd2.isInObjectPropertySet(p)) {
					prefix = TEMPLATE_VAR_PROP_OBJ;
					table = objectProperyVarTable;
				} else if (rdfd2.isInDatatypePropertySet(p)) {
					prefix = TEMPLATE_VAR_PROP_DT;
					table = datatypePropertyVarTable;
				} else {
					prefix = TEMPLATE_VAR_PROP_PLAIN;
					table = plainPropertyVarTable;
				}
				varName = table.getOrCreateVar(predicate, prefix);
			} else if (!(rdfd2.isInPropertySet(p) || rdfd2.isInObjectPropertySet(p)
					|| rdfd2.isInDatatypePropertySet(p))) {
				log.debug(" ... is a plain property in <{}> and not in <{}>", rdfd1, rdfd2);
				varName = plainPropertyVarTable.getOrCreateVar(predicate, TEMPLATE_VAR_PROP_PLAIN);
			} else {
				log.debug(" ... is present in target dataset <{}> and override is not enabled. Will not generalize.",
						rdfd2);
				return null;
			}
		}
		// Add a case for "plain" properties
		else {
			log.debug(" ... is either present both in <{}> and <{}>, or in neither. Will not generalize.", rdfd1,
					rdfd2);
			return null;
		}
		if (varName == null)
			throw new IllegalStateException("Predicate node generated a null variable name.");
		return Var.alloc(varName);
	}

}
