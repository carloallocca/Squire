/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.querytemplate;

import static uk.ac.open.kmi.squire.core4.QueryOperator.TEMPLATE_VAR_CLASS;
import static uk.ac.open.kmi.squire.core4.QueryOperator.TEMPLATE_VAR_INDIVIDUAL;
import static uk.ac.open.kmi.squire.core4.QueryOperator.TEMPLATE_VAR_LITERAL;

import java.util.ListIterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

import uk.ac.open.kmi.squire.entityvariablemapping.GeneralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author callocca Select Query Template Visitor class implements the logic for
 *         visiting a graph pattern expression for making an query template.
 */
public class SQTVisitor extends ElementVisitorBase implements IQueryVisitor {

	private IRDFDataset rdfd1, rdfd2;

	private VarMapping literalVarTable;
	private VarMapping classVarTable;
	private VarMapping datatypePropertyVarTable;
	private VarMapping individualVarTable;
	private VarMapping objectProperyVarTable;

	public SQTVisitor(IRDFDataset d1, IRDFDataset d2) {
		rdfd1 = d1;
		rdfd2 = d2;
		classVarTable = new GeneralVarMapping();
		individualVarTable = new GeneralVarMapping();
		literalVarTable = new GeneralVarMapping();
		objectProperyVarTable = new GeneralVarMapping();
		datatypePropertyVarTable = new GeneralVarMapping();
	}

	// In theory: The outer-most graph pattern in a query is called the query
	// pattern.
	// It is grammatically identified by GroupGraphPattern in "WhereClause ::=
	// 'WHERE'? GroupGraphPattern"
	// ElementTriplesBlock is A SPARQL BasicGraphPattern. ex:
	// SELECT ?name ?mbox
	// WHERE { // the outer-most graph pattern is a GroupGraphPattern - a set of
	// graph pattern - and in jena is ElementPathBlock
	// { ?x foaf:name ?name . } this is a BasicGraphPatterns - a set triple pattern
	// - and in jena is ElementTriplesBlock
	// { ?x foaf:mbox ?mbox . } this is a BasicGraphPatterns - a set triple pattern
	// - and in jena is ElementTriplesBlock
	// }
	//
	// ElementPathBlock is an GroupGraphPattern of a SPARQL Query.
	@Override
	public void visit(ElementPathBlock el) {
		ListIterator<TriplePath> it = el.getPattern().iterator();
		while (it.hasNext()) {
			final TriplePath tp = it.next();
			System.out.println("The triple ==> " + tp.toString());
			Node subj = tp.getSubject();
			Node pred = tp.getPredicate();
			Node obj = tp.getObject();

			// Case 1: <ex:bob ex:hasSyster ex:alice >: this is in case I want to select the
			// address of bobs that have a syster Alice
			// <ex:bob rdf:type ex:Person> : this is in case ex
			if (subj.isURI() && pred.isURI() && obj.isURI()) {

				String subject = subj.getURI();
				String predicate = pred.getURI();
				String object = obj.getURI();

				// Case 1.1: <ex:mathieu rdf:type ex:SeniorResearchFellow>
				// if subject is an individual and predicate=="rdf:type" and object is a Class
				// then
				// Questa mi seleziona l'insieme delle classi: select distinct ?class where
				// {<subject> a ?class}
				// e per controllare se l'oggetto is a class basta controllare se e'
				// nell'insieme appena calcolato.
				if (rdfd1.getIndividualSet().contains(subject)
						&& predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
						&& rdfd1.getClassSet().contains(object)) {
					Var individualVar = Var
							.alloc(individualVarTable.generateVarIfAbsent(subj.getURI(), TEMPLATE_VAR_INDIVIDUAL));
					Var classVar = Var.alloc(classVarTable.generateVarIfAbsent(obj.getURI(), TEMPLATE_VAR_CLASS));
					it.set(new TriplePath(new Triple(individualVar, tp.getPredicate(), classVar)));

				}
				// Case 1.2: <ex:mathieu ex:isBossOf ex:carlo>
				// if subject is an individual and predicate is an ObjectProperty and object is
				// an individual then
				if (rdfd1.getIndividualSet().contains(subject) && rdfd1.getObjectPropertySet().contains(predicate)
						&& rdfd1.getIndividualSet().contains(object)) {
					Var individualVar = Var
							.alloc(individualVarTable.generateVarIfAbsent(subj.getURI(), TEMPLATE_VAR_INDIVIDUAL));
					Var classVar = Var.alloc(classVarTable.generateVarIfAbsent(obj.getURI(), TEMPLATE_VAR_CLASS));
					it.set(new TriplePath(new Triple(individualVar, tp.getPredicate(), classVar)));
				}
				//
				// System.out.println("subj.isURI() && pred.isURI() && obj.isURI()" +
				// subj.getURI());
				// Var individualVar = Var.alloc("individualVar");
				// Var literalVar = Var.alloc("literalVar");
				// Var predicateVar = Var.alloc("predicateVar");
				// it.set(new TriplePath(new Triple(individualVar, predicateVar, literalVar)));
				//// if ( tp.getSubject().equals( s )) {
				// //it.add( new TriplePath( new Triple( individualVar, predicateVar, literalVar
				// )));
				//
				//// }
			}
			// Case 2: <ex:fernando ex:hasP2 "allocca" >

			else if (subj.isURI() && pred.isURI() && obj.isLiteral()) {
				System.out.println("subj.isURI() && pred.isURI() && obj.isLiteral()");
				// Var literalVar1 = Var.alloc(
				// LiteralVarMapping.generateIFAbsentLiteralVar(obj.toString()));
				Var literalVar1 = Var.alloc(literalVarTable.generateVarIfAbsent(obj.toString(), TEMPLATE_VAR_LITERAL));
				it.set(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), literalVar1)));

			}
			// Case 3: < :hasPizza rdfs:domain ?domain ;>
			// <ex:bob rdf:type ?class>
			// <ex:bob ex:hasSyster ?syster>
			// <ex:Female rdfs:subClassOf ?sub>
			else if (subj.isURI() && pred.isURI() && obj.isVariable()) {
				System.out.println("subj.isURI() && pred.isURI() && obj.isVariable()");
			}

			// Case 4
			else if (subj.isVariable() && pred.isURI() && obj.isURI()) {
				System.out.println("subj.isVariable() && pred.isURI() && obj.isURI()");
			}
			// Case 5
			else if (subj.isVariable() && pred.isURI() && obj.isLiteral()) {
				System.out.println("subj.isVariable() && pred.isURI() && obj.isLiteral()" + subj.getName());
				// Var varVar1 = Var.alloc( subj.getName() );
				// Path path = new P_Link(tp.getPredicate()) ;
				// Var predVar1 = Var.alloc( pred.getURI() );
				// Var literalVar1 = Var.alloc(
				// LiteralVarMapping.generateIFAbsentLiteralVar(obj.toString()));
				Var literalVar1 = Var.alloc(literalVarTable.generateVarIfAbsent(obj.toString(), TEMPLATE_VAR_LITERAL));
				it.set(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), literalVar1)));

			}
			// Case 6
			else if (subj.isVariable() && pred.isURI() && obj.isVariable()) {
				System.out.println("subj.isVariable() && pred.isURI() && obj.isVariable()");
			}

			// Case 7
			else if (subj.isLiteral() && pred.isURI() && obj.isURI()) {
				System.out.println("subj.isLiteral() && pred.isURI() && obj.isURI()");
			}
			// Case 8
			else if (subj.isLiteral() && pred.isURI() && obj.isLiteral()) {
				System.out.println("subj.isLiteral() && pred.isURI() && obj.isLiteral()");
			}
			// Case 9
			else if (subj.isLiteral() && pred.isURI() && obj.isVariable()) {
				System.out.println("subj.isLiteral() && pred.isURI() && obj.isVariable()");
			}

			// Case 10
			else if (subj.isURI() && pred.isVariable() && obj.isURI()) {
				System.out.println("subj.isURI() && pred.isVariable() && obj.isURI()");
			}
			// Case 11
			else if (subj.isURI() && pred.isVariable() && obj.isLiteral()) {
				System.out.println("subj.isURI() && pred.isVariable() && obj.isLiteral()");
			}
			// Case 12
			else if (subj.isURI() && pred.isVariable() && obj.isVariable()) {
				System.out.println("subj.isURI() && pred.isVariable() && obj.isVariable()");
			}

			// Case 13
			else if (subj.isVariable() && pred.isVariable() && obj.isURI()) {
				System.out.println("subj.isVariable() && pred.isVariable() && obj.isURI()");
			}
			// Case 14
			else if (subj.isVariable() && pred.isVariable() && obj.isLiteral()) {
				System.out.println("subj.isVariable() && pred.isVariable() && obj.isLiteral()");
			}
			// Case 15
			else if (subj.isVariable() && pred.isVariable() && obj.isVariable()) {
				System.out.println("subj.isVariable() && pred.isVariable() && obj.isVariable()");
			}

			// Case 16
			else if (subj.isLiteral() && pred.isVariable() && obj.isURI()) {
				System.out.println("subj.isLiteral() && pred.isVariable() && obj.isURI()");
			}
			// Case 17
			else if (subj.isLiteral() && pred.isVariable() && obj.isLiteral()) {
				System.out.println("subj.isLiteral() && pred.isVariable() && obj.isLiteral()");
			}
			// Case 18
			else if (subj.isLiteral() && pred.isVariable() && obj.isVariable()) {
				System.out.println("subj.isLiteral() && pred.isVariable() && obj.isVariable()");
			}

			System.out.println(" ");
			// final Var s = Var.alloc( "s" );
			// if ( tp.getSubject().equals( s )) {
			// it.add( new TriplePath( new Triple( s, s, s )));
			// }
		}
	}

}
