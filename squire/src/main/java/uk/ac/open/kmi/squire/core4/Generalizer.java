/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import uk.ac.open.kmi.squire.operation.SPARQLQueryGeneralization;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class Generalizer extends QueryOperator {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Query originalQuery;

	private final IRDFDataset rdfd1, rdfd2;

	public Generalizer(Query query, IRDFDataset d1, IRDFDataset d2) {
		super();
		if (query == null) throw new IllegalArgumentException("Query cannot be null.");
		this.originalQuery = query;
		this.rdfd1 = d1;
		if (d2 == null) throw new IllegalArgumentException("Target dataset cannot be null.");
		this.rdfd2 = d2;
	}

	/*
	 * Typically done once per original query
	 */
	public Query generalize() {
		// The generalized query is created from a clone of the original one.
		Query qGeneral = QueryFactory.create(this.originalQuery.toString());
		// Three separate generalization steps are performed
		SPARQLQueryGeneralization qg = new SPARQLQueryGeneralization();
		// SUBJECT
		for (Node subj : getEntitySet(NodeRole.SUBJECT))
			if (!(subj.isVariable()) && !(subj.isBlank())) {
				Var templateVarSub = ifSubjectIsNotD2ThenGenerateVariableNew(subj);
				if (templateVarSub != null) qGeneral = qg.perform(qGeneral, subj, templateVarSub);
			}
		// PREDICATE
		for (Node pred : getEntitySet(NodeRole.PREDICATE))
			if (!(pred.isVariable()) && !(pred.isBlank())) {
				if (!this.rdfd2.getRDFVocabulary().contains(pred.getURI())) {
					Var templateVarPred = ifPredicateIsNotD2ThenGenerateVariableNew(pred);
					if (templateVarPred != null) qGeneral = qg.perform(qGeneral, pred, templateVarPred);
				}
			}
		// OBJECT
		for (Node obj : getEntitySet(NodeRole.OBJECT))
			if (!(obj.isVariable()) && !(obj.isBlank())) {
				Var templateVarObj = ifObjectIsNotD2ThenGenerateVariableNew(obj);
				if (templateVarObj != null) qGeneral = qg.perform(qGeneral, obj, templateVarObj);
			}
		return qGeneral;
	}

	private Set<Node> getEntitySet(NodeRole nodeType) {
		final Set<Node> objects = new HashSet<>(); // Remember distinct objects in this
		// This will walk through all parts of the query
		ElementWalker.walk(this.originalQuery.getQueryPattern(), new ElementVisitorBase() {
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
					if (n != null) objects.add(n);
				}
			}
		});
		return objects;
	}

	private Var ifObjectIsNotD2ThenGenerateVariableNew(Node obj) {
		if (obj == null) throw new IllegalArgumentException("Object node cannot be null.");
		final String varName;
		if (obj.isURI()) {
			String o = obj.getURI();
			if ((rdfd1.getClassSet().contains(o)) && !(rdfd2.getClassSet().contains(o)))
				varName = classVarTable.generateVarIfAbsent(o);
			else if (rdfd1.isInObjectPropertySet(o) && !(rdfd2.isInObjectPropertySet(o)))
				varName = objectProperyVarTable.generateVarIfAbsent(o);
			else if (rdfd1.isInDatatypePropertySet(o) && !(rdfd2.isInDatatypePropertySet(o)))
				varName = datatypePropertyVarTable.generateVarIfAbsent(o);
			else if (rdfd1.isInRDFVocabulary(o) && !(rdfd2.isInRDFVocabulary(o)))
				varName = rdfVocVarTable.generateVarIfAbsent(o);
			else return null;
		} else if (obj.isLiteral()) {
			varName = literalVarTable.generateVarIfAbsent(obj.getLiteralValue().toString());
		} else return null;
		if (varName == null) throw new IllegalStateException("Object node generated a null variable name.");
		return Var.alloc(varName);
	}

	private Var ifPredicateIsNotD2ThenGenerateVariableNew(Node pred) {
		if (pred == null) throw new IllegalArgumentException("Predicate node cannot be null.");
		final String varName;
		if (!pred.isURI()) return null;
		String p = pred.getURI();
		log.debug("Inspecting predicate '{}' for generalization.", p);
		log.trace("rdfd1 object property list : {}", rdfd1.getObjectPropertySet());
		log.trace("rdfd1 datatype property list : {}", rdfd1.getDatatypePropertySet());
		log.trace("rdfd2 object property list : {}", rdfd2.getObjectPropertySet());
		log.trace("rdfd2 datatype property list : {}", rdfd2.getDatatypePropertySet());
		if (rdfd1.isInObjectPropertySet(p) && !(rdfd2.isInObjectPropertySet(p))) {
			log.debug(" ... is an object property in <{}> and not in <{}>", rdfd1, rdfd2);
			varName = objectProperyVarTable.generateVarIfAbsent(p);
		} else if (rdfd1.isInDatatypePropertySet(p) && !(rdfd2.isInDatatypePropertySet(p))) {
			log.debug(" ... is a datatype property in <{}> and not in <{}>", rdfd1, rdfd2);
			varName = datatypePropertyVarTable.generateVarIfAbsent(p);
		} else {
			log.debug(" ... is either present both in <{}> and <{}>, or in neither. Will not generalize.", rdfd1,
					rdfd2);
			return null;
		}
		if (varName == null) throw new IllegalStateException("Predicate node generated a null variable name.");
		return Var.alloc(varName);
	}

	private Var ifSubjectIsNotD2ThenGenerateVariableNew(Node subj) {
		if (subj == null) throw new IllegalArgumentException("Subject node cannot be null.");
		final String varName;
		if (subj.isURI()) {
			String sub = subj.getURI();
			if ((rdfd1.getClassSet().contains(sub)) && !(rdfd2.getClassSet().contains(sub)))
				varName = classVarTable.generateVarIfAbsent(sub);
			else if (rdfd1.isInObjectPropertySet(sub) && !(rdfd2.isInObjectPropertySet(sub)))
				varName = objectProperyVarTable.generateVarIfAbsent(sub);
			else if (rdfd1.isInDatatypePropertySet(sub) && !(rdfd2.isInDatatypePropertySet(sub)))
				varName = datatypePropertyVarTable.generateVarIfAbsent(sub);
			else if (rdfd1.isInRDFVocabulary(sub) && !(rdfd2.isInRDFVocabulary(sub)))
				varName = rdfVocVarTable.generateVarIfAbsent(sub);
			else
				// We assume by exclusion that sub is an individual.
				// XXX is that assumption correct?
				varName = individualVarTable.generateVarIfAbsent(sub);
		} else if (subj.isLiteral()) {
			varName = literalVarTable.generateVarIfAbsent(subj.getLiteralValue().toString());
		} else return null;
		if (varName == null) throw new IllegalStateException("Subject node generated a null variable name.");
		return Var.alloc(varName);
	}

}
