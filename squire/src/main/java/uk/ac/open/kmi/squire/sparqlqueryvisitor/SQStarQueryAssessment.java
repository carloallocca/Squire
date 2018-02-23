package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryVisitor;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.ClassSignature;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * 
 * 
 * @author alessandro
 *
 */
public class SQStarQueryAssessment extends ElementVisitorBase implements QueryVisitor {

	private IRDFDataset tgt;

	private Logger log = LoggerFactory.getLogger(getClass());

	public SQStarQueryAssessment(IRDFDataset targetDataset) {
		this.tgt = targetDataset;
	}

	@Override
	public void visit(ElementPathBlock el) {
		// Classes in this star TP
		Map<Node, Set<String>> classesPerSubject = new HashMap<>();

		// Scan for subject-class bindings.
		for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
			TriplePath tp = it.next();
			Node sub = tp.getSubject();
			log.debug("Putting subject '{}' in map", sub);
			if (!classesPerSubject.containsKey(sub)) classesPerSubject.put(sub, new HashSet<>());

			if (tp.getPredicate().isURI() && RDF.type.getURI().equals(tp.getPredicate().getURI())) {
				log.info("Got at RDF:type for class {}", tp.getObject());
			}

			if (tp.getPredicate().isURI() && RDF.type.getURI().equals(tp.getPredicate().getURI())
					&& tp.getObject().isURI()) {
				classesPerSubject.get(sub).add(tp.getObject().getURI());
			}
		}
		// Scan again for unsatisfiable predicates for instances of those types
		for (Iterator<TriplePath> it = el.patternElts(); it.hasNext();) {
			TriplePath tp = it.next();
			Node sub = tp.getSubject();
			log.debug("is subject '{}' in map? {}", sub, classesPerSubject.containsKey(sub));
			Node pred = tp.getPredicate();

			if (pred.isURI()) {
				log.debug("predicate: {}", pred);
				for (String clazz : classesPerSubject.get(sub)) {
					log.debug("I want to check class <{}>", clazz);
					ClassSignature cs = tgt.getClassSignatures().get(clazz);
					if (cs != null && !cs.listPathOrigins().contains(pred.getURI())) {
						log.warn("Property <{}> is not part of the indexed signature for class <{}>", pred.getURI(),
								clazz);
						log.warn("For the record, here are the known properties:\r\n{}", cs.listPathOrigins());
						log.warn("I woundn't recommend going down this branch.");
					}
				}

			}
		}

		System.out.println("DIOCANE " + el);
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		System.out.println("DIOPORCO " + el);
	}

	@Override
	public void startVisit(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitPrologue(Prologue prologue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitResultForm(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitSelectResultForm(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitConstructResultForm(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitDescribeResultForm(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitAskResultForm(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitDatasetDecl(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitQueryPattern(Query query) {
		System.out.println("DIO MUFLONE");
		ElementWalker.walk(query.getQueryPattern(), this);
	}

	@Override
	public void visitGroupBy(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitHaving(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitOrderBy(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLimit(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitOffset(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitValues(Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finishVisit(Query query) {
		// TODO Auto-generated method stub

	}

}
