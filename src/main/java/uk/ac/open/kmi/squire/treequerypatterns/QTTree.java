/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.entityvariablemapping.ClassVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.DatatypePropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.IndividualVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.LiteralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.ObjectPropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.RDFVocVarMapping;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 * 
 * @author callocca
 */
public class QTTree<T> {

	private TreeNode<T> root;
	private TreeNode<T> rootTemplate;

	private HashMap<String, TreeNode<T>> treeNodeIndex = new HashMap<>();

	private IRDFDataset rdfd1;

	private IRDFDataset rdfd2;

	private LiteralVarMapping literalVarTable;
	private ClassVarMapping classVarTable;
	private DatatypePropertyVarMapping datatypePropertyVarTable;
	private IndividualVarMapping individualVarTable;
	private ObjectPropertyVarMapping objectProperyVarTable;
	private RDFVocVarMapping rdfVocVarTable;

	// DON'T USE IT.
	// public QTTree(TreeNode<T> node, IRDFDataset d1, IRDFDataset d2) {
	// this.root = node;
	// rdfd1 = d1;
	// rdfd2 = d2;
	// classVarTable = new ClassVarMapping();
	// individualVarTable = new IndividualVarMapping();
	// literalVarTable = new LiteralVarMapping();
	// objectProperyVarTable = new ObjectPropertyVarMapping();
	// datatypePropertyVarTable = new DatatypePropertyVarMapping();
	// rdfVocVarTable = new RDFVocVarMapping();
	// }
	public QTTree(String query, TreeNode<T> node, IRDFDataset d1, IRDFDataset d2) {
		this.root = node;
		rdfd1 = d1;
		classVarTable = new ClassVarMapping();
		individualVarTable = new IndividualVarMapping();
		literalVarTable = new LiteralVarMapping();
		objectProperyVarTable = new ObjectPropertyVarMapping();
		datatypePropertyVarTable = new DatatypePropertyVarMapping();
		rdfVocVarTable = new RDFVocVarMapping();
		rdfd2 = d2;
	}

	public void generateQTTree(TreeNode<List<TriplePath>> node) {

		if (node == null) {
			return;
		}
		List<TriplePath> tpSet = (List<TriplePath>) node.getData();
		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println("[QTTree::generateQTTree] === START");
		System.out.println(tpSet.toString());

		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		for (TriplePath tp : tpSet) {

			// The case that the triple pattern contains just variables <?s, ?p ?o>
			if (tp.getSubject().isVariable() && tp.getPredicate().isVariable() && tp.getObject().isVariable()) {
				System.out.println("[QTTree::generateQTTree] The subject, predicate and object are Variable");
				return;
			}

			////////////////////////// Applying the Remove operation
			if (tpSet.size() > 1) {
				TreeNode<List<TriplePath>> currNode = null;
				// We create the triple patterns set for the child node. The set will initially
				// contains the triple patterns
				// except the one we are about processing.
				final List<TriplePath> childTPSet = new ArrayList<>();
				for (TriplePath tp1 : tpSet) {
					if (!(tp1.equals(tp))) {
						childTPSet.add(tp1);
					}
				}
				currNode = new TreeNode<>(childTPSet, null);
				// Check if the childNode has already been added to the tree.
				if (!(isInTreeNodeIndex(currNode))) {
					node.addChild(currNode);
					addToNodeTreeIndexIFAbsent(currNode);
				}
				if (!(currNode == null)) {
					generateQTTree(currNode);
				}
			}

			//////////////////// Applying the Substitution operation
			// SUBJECT s, s=literal or s=URI that can be one of
			// [classURI or ObjPropertyURI or datatypePropertyURI or RDFVocabulary]
			if (tp.getSubject().isURI()) {
				final TreeNode<List<TriplePath>> currNode;
				// We create the triple patterns set for the child node. The set will initially
				// contains the triple patterns
				// except the one we are about processing.
				final List<TriplePath> childTPSet = new ArrayList<>();
				for (TriplePath tp1 : tpSet) {
					if (!(tp1.equals(tp))) {
						childTPSet.add(tp1);
					}
				}
				// s= classURI
				if (rdfd1.getClassSet().contains(tp.getSubject().getURI())) {
					// System.out.println("[QTTree::generateQTTree] subject is an class uri");
					Var classVar = Var.alloc(classVarTable.generateIFAbsentClassVar(tp.getSubject().getURI()));
					childTPSet.add(new TriplePath(new Triple(classVar, tp.getPredicate(), tp.getObject())));
					System.out.println("[QTTree::generateQTTree] subject is an class == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is an class == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					// Check if the childNode has already been added to the tree.
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// s= individualURI
				} else if (rdfd1.isInIndividualSet(tp.getSubject().getURI())) {
					Var indVar = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(tp.getSubject().getURI()));
					childTPSet.add(new TriplePath(new Triple(indVar, tp.getPredicate(), tp.getObject())));
					System.out.println("[QTTree::generateQTTree] subject is a individual uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a individual uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// s = objectPropertyURI
				} else if (rdfd1.isInObjectPropertySet(tp.getSubject().getURI())) {
					Var obpVar = Var
							.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(tp.getSubject().getURI()));
					childTPSet.add(new TriplePath(new Triple(obpVar, tp.getPredicate(), tp.getObject())));
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a objectproperty uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a objectproperty uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// s = datatypePropertyURI
				} else if (rdfd1.isInDatatypePropertySet(tp.getSubject().getURI())) {
					Var dtpVar = Var.alloc(
							datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(tp.getSubject().getURI()));
					childTPSet.add(new TriplePath(new Triple(dtpVar, tp.getPredicate(), tp.getObject())));
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a datatypeProperty uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a datatypeProperty uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// s = RDFVocabularyURI
				} else if (rdfd1.isInRDFVocabulary(tp.getSubject().getURI())) {
					Var rdfVar = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(tp.getSubject().getURI()));
					childTPSet.add(new TriplePath(new Triple(rdfVar, tp.getPredicate(), tp.getObject())));
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a RDFVocabulary uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] subject is a RDFVocabulary  uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
				} else {
					System.out.println(
							"[QTTree::generateQTTree] None of the previous cases was satisfied for the subject node "
									+ tp.getSubject().getURI());
					currNode = null;
				}
				if (!(currNode == null)) {
					generateQTTree(currNode);
				}
			} // endIF subj=URI

			// s= literal
			if (tp.getSubject().isLiteral()) {
				final TreeNode<List<TriplePath>> currNode;
				// We create the triple patterns set for the child node. The set will initially
				// contains the triple patterns
				// except the one we are about processing.
				final List<TriplePath> childTPSet = new ArrayList<>();
				for (TriplePath tp1 : tpSet) {
					if (!(tp1.equals(tp))) {
						childTPSet.add(tp1);
					}
				}
				Var litVar = Var.alloc(
						literalVarTable.generateIFAbsentLiteralVar(tp.getSubject().getLiteral().getValue().toString()));
				childTPSet.add(new TriplePath(new Triple(litVar, tp.getPredicate(), tp.getObject())));
				System.out.println("[QTTree::generateQTTree] subject is a literal == BEFORE");
				System.out.println(tpSet.toString());
				System.out.println("[QTTree::generateQTTree] subject is a literal == AFTER");
				System.out.println(childTPSet.toString());
				currNode = new TreeNode<>(childTPSet, null);
				// Check if the childNode has already been added to the tree.
				if (!(isInTreeNodeIndex(currNode))) {
					node.addChild(currNode);
					addToNodeTreeIndexIFAbsent(currNode);
				}
			}

			// PREDICATE p, p=URI that can be one of
			// [ObjPropertyURI or datatypePropertyURI or RDFVocabulary]
			if (tp.getPredicate().isURI()) {
				final TreeNode<List<TriplePath>> currNode;
				// We create the triple patterns set for the child node. The set will initially
				// contains the triple patterns
				// except the one we are about processing.
				final List<TriplePath> childTPSet = new ArrayList<>();
				for (TriplePath tp1 : tpSet) {
					if (!(tp1.equals(tp))) {
						childTPSet.add(tp1);
					}
				}
				// s= RDFVocabulary
				if (rdfd1.isInRDFVocabulary(tp.getPredicate().getURI())) {
					Var rdfVar = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(tp.getPredicate().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), rdfVar, tp.getObject())));
					System.out.println("[QTTree::generateQTTree] predicate is an rdfvoc uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] predicate is a rdfvoc uri uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// p= ObjPropertyURI or datatypePropertyURI
				} else if (rdfd1.isInObjectPropertySet(tp.getPredicate().getURI())) {
					Var predVar = Var
							.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(tp.getPredicate().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), predVar, tp.getObject())));

					System.out.println("[QTTree::generateQTTree] predicate is a object property uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is a a object property uri uri == AFTER");
					System.out.println(childTPSet.toString());

					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// p = datatypePropertyURI
				} else if (rdfd1.isInDatatypePropertySet(tp.getPredicate().getURI())) {
					Var predVar = Var.alloc(
							datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(tp.getPredicate().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), predVar, tp.getObject())));
					System.out.println("[QTTree::generateQTTree] predicate is a object property uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is a a object property uri uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
				} else {
					System.out.println(
							"[QTTree::generateQTTree] None of the previous cases was satisfied for the predicate node "
									+ tp.getPredicate().getURI());
					currNode = null;
				}
				if (currNode != null) {
					generateQTTree(currNode);
				}
			}
			// OBJECT o, o=literal or o=URI that can be one of
			// [classURI or ObjPropertyURI or datatypePropertyURI or RDFVocabulary]
			if (tp.getObject().isURI()) {
				final TreeNode<List<TriplePath>> currNode;
				// We create the triple patterns set for the child node. The set will initially
				// contains the triple patterns
				// except the one we are about processing.
				final List<TriplePath> childTPSet = new ArrayList<>();
				for (TriplePath tp1 : tpSet) {
					if (!(tp1.equals(tp))) {
						childTPSet.add(tp1);
					}
				}
				// o = classURI
				if (rdfd1.getClassSet().contains(tp.getObject().getURI())) {
					Var classVar = Var.alloc(classVarTable.generateIFAbsentClassVar(tp.getObject().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), classVar)));
					System.out.println("[QTTree::generateQTTree] object is a class uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is a class uri uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);// .setData(newEl);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// o = individualURI
				} else if (rdfd1.isInIndividualSet(tp.getObject().getURI())) {
					Var indVar = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(tp.getObject().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), indVar)));
					System.out.println("[QTTree::generateQTTree] object is an individual uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is an individual uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);// .setData(newEl);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// o = objectpropertyURI
				} else if (rdfd1.isInObjectPropertySet(tp.getObject().getURI())) {
					Var obpVar = Var
							.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(tp.getObject().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), obpVar)));
					System.out.println("[QTTree::generateQTTree] object is an object property uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is an object property uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);// .setData(newEl);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
					// o = datatypepropertyURI
				} else if (rdfd1.isInDatatypePropertySet(tp.getObject().getURI())) {
					Var dtpVar = Var.alloc(
							datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(tp.getObject().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), dtpVar)));
					System.out.println("[QTTree::generateQTTree] object is an object property uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is an object property uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);// .setData(newEl);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
				} // o = RDFVocabularyURI
				else if (rdfd1.isInRDFVocabulary(tp.getObject().getURI())) {
					Var rdfVar = Var.alloc(
							datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(tp.getObject().getURI()));
					childTPSet.add(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), rdfVar)));
					System.out.println("[QTTree::generateQTTree] object is an object property uri == BEFORE");
					System.out.println(tpSet.toString());
					System.out.println("[QTTree::generateQTTree] object is an object property uri == AFTER");
					System.out.println(childTPSet.toString());
					currNode = new TreeNode<>(childTPSet, null);// .setData(newEl);
					if (!(isInTreeNodeIndex(currNode))) {
						node.addChild(currNode);
						addToNodeTreeIndexIFAbsent(currNode);
					}
				} else {
					System.out.println(
							"[QTTree::generateQTTree] None of the previous cases was satisfied for the object node "
									+ tp.getObject().getURI());
					currNode = null;
				}
				if (currNode != null) {
					generateQTTree(currNode);
				}
			}
			// OBJECT o=literal
			if (tp.getObject().isLiteral()) {
				final TreeNode<List<TriplePath>> currNode;
				// We create the triple patterns set for the child node. The set will initially
				// contains the triple patterns
				// except the one we are about processing.
				final List<TriplePath> childTPSet = new ArrayList<>();
				for (TriplePath tp1 : tpSet) {
					if (!(tp1.equals(tp))) {
						childTPSet.add(tp1);
					}
				}
				Var litVar = Var.alloc(
						literalVarTable.generateIFAbsentLiteralVar(tp.getObject().getLiteral().getValue().toString()));
				childTPSet.add(new TriplePath(new Triple(tp.getSubject(), tp.getPredicate(), litVar)));
				System.out.println("[QTTree::generateQTTree] subject is a literal == BEFORE");
				System.out.println(tpSet.toString());
				System.out.println("[QTTree::generateQTTree] subject is a literal == AFTER");
				System.out.println(childTPSet.toString());
				currNode = new TreeNode<>(childTPSet, null);
				// Check if the childNode has already been added to the tree.
				if (!(isInTreeNodeIndex(currNode))) {
					node.addChild(currNode);
					addToNodeTreeIndexIFAbsent(currNode);
				}
			}
		}
	}

	public TreeNode<T> getRoot() {
		return root;
	}

	public void evaluateQTTree(TreeNode<T> n, int i) {
		if (n == null) {
			throw new IllegalStateException("[QTTree,evaluateQTTree]The tree is empty!!");
		}

		FromTreeNodeToQuery<T> nodeToquery = new FromTreeNodeToQuery<>(n);
		nodeToquery.querySelectBuilding();
		for (int j = 0; j < i; j++) {
			// try {
			// Files.write(Paths.get(file), " ".getBytes(), StandardOpenOption.APPEND);
			// Files.write(Paths.get(file), "\n".getBytes(), StandardOpenOption.APPEND);
			//
			// } catch (IOException e) {
			// //exception handling left as an exercise for the reader
			// }

			System.out.print("  ");
		}

		if (n.getData() != null) {
			// try {
			// Files.write(Paths.get(file), n.getData().toString().getBytes(),
			// StandardOpenOption.APPEND);
			// Files.write(Paths.get(file), "\n".toString().getBytes(),
			// StandardOpenOption.APPEND);
			//
			// } catch (IOException e) {
			// //exception handling left as an exercise for the reader
			// }

			System.out.println("[QTTree:evaluateQTTree]" + n.getData().toString());
		}

		for (TreeNode<T> node : n.getChildren())
			evaluateQTTree(node, i + 1);

	}

	public void printQTTree(TreeNode<T> n, int i) {
		if (n == null) {
			throw new IllegalStateException("[QTTree,printQTTree]The tree is empty!!");
		}

		// FromTreeNodeToQuery nodeToquery = new FromTreeNodeToQuery(n);
		// nodeToquery.querySelectBuilding();
		for (int j = 0; j < i; j++) {
			// try {
			// Files.write(Paths.get(file), " ".getBytes(), StandardOpenOption.APPEND);
			// Files.write(Paths.get(file), "\n".getBytes(), StandardOpenOption.APPEND);
			//
			// } catch (IOException e) {
			// //exception handling left as an exercise for the reader
			// }

			System.out.print("  ");
		}

		if (n.getData() != null) {
			// try {
			// Files.write(Paths.get(file), n.getData().toString().getBytes(),
			// StandardOpenOption.APPEND);
			// Files.write(Paths.get(file), "\n".toString().getBytes(),
			// StandardOpenOption.APPEND);
			//
			// } catch (IOException e) {
			// //exception handling left as an exercise for the reader
			// }

			System.out.println("[QTTree:printQTTree]" + n.getData().toString());
		}

		for (TreeNode<T> node : n.getChildren()) {

			printQTTree(node, i + 1);
		}

	}

	// This method builds the generalized node.
	// Input: it takes the root node; //TreeNode<List<TriplePath>>
	// output: it gives the rootTemplate node; //TreeNode<List<TriplePath>>
	public void generalizeToQueryTemplate() {

		if (this.root == null) {
			throw new IllegalStateException("[QTTree::generalizeToQueryTemplate()]The tree is empty!!");
		}
		List<TriplePath> tpSet = (List<TriplePath>) this.root.getData();
		// System.out.println("[QTTree::generalizeToQueryTemplate()] === START");
		// System.out.println(tpSet.toString());
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		List<TriplePath> tpTempleteSet = new ArrayList<>();

		// System.out.println(tpSet.toString());
		for (TriplePath tp : tpSet) {
			// System.out.println("[QTTree::generalizeToQueryTemplate()] We are building the
			// rootTemplate node!!!");

			TriplePath tpTemplate = generalize(tp);
			tpTempleteSet.add(tpTemplate);

		}

		System.out.println("");
		System.out.println("");
		System.out.println("[QTTree::generalizeToQueryTemplate()] ==== BEFORE");
		for (TriplePath tp : tpSet) {
			System.out.println(tp.toString());
		}
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("[QTTree::generalizeToQueryTemplate()] ==== AFTER");
		for (TriplePath tp : tpTempleteSet) {
			System.out.println(tp.toString());
		}
		System.out.println("");
		System.out.println("");
		// we set the rootTemplate.
		this.rootTemplate = new TreeNode(tpTempleteSet, null);

	}

	private TriplePath generalize(TriplePath tp) {
		if (tp == null) {
			throw new IllegalStateException("[QTTree::generalize] A TriplePath to be generalized is null!!");
		}
		TriplePath tpTemplate;
		final Node subject;
		final Node predicate;
		final Node object;

		// SUBJECT
		if (tp.getSubject().isURI()) {
			// s= classURI
			String subj = tp.getSubject().getURI();
			// System.out.println("[QTTree::generalize] The Sub is an URI " + subj);
			if ((rdfd1.getClassSet().contains(subj)) && !(rdfd2.getClassSet().contains(subj))) {
				subject = Var.alloc(classVarTable.generateIFAbsentClassVar(subj));
				// System.out.println("[QTTree::generalize] The Sub is a class URI");
			} else if (rdfd1.isInIndividualSet(subj) && !(rdfd2.isInIndividualSet(subj))) {
				subject = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(subj));
				// System.out.println("[QTTree::generalize] The Sub is an individual URI");
			} else if (rdfd1.isInObjectPropertySet(subj) && !(rdfd2.isInObjectPropertySet(subj))) {
				subject = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(subj));
				// System.out.println("[QTTree::generalize] The Sub is an Object Property URI");
			} else if (rdfd1.isInDatatypePropertySet(subj) && !(rdfd2.isInDatatypePropertySet(subj))) {
				subject = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(subj));
				// System.out.println("[QTTree::generalize] The Sub is an datatype Property
				// URI");
			} else if (rdfd1.isInRDFVocabulary(subj) && !(rdfd2.isInRDFVocabulary(subj))) {
				subject = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(subj));
				// System.out.println("[QTTree::generalize] The Sub is an RDF voc term URI");
			} else {
				subject = tp.getSubject();
			}
		} else {
			if (tp.getSubject().isLiteral()) {
				String subjAsString = tp.getSubject().getLiteralValue().toString();
				if (rdfd1.isInLiteralSet(subjAsString) && !(rdfd2.isInLiteralSet(subjAsString))) {
					subject = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(subjAsString));
				} else {
					subject = tp.getSubject();
				}

			} else {
				subject = tp.getSubject();
			}
		}

		// PREDICATE
		// TODO: I need ot add code for differenciate the case that an URI is an
		// datatype property from
		// an object property, MAYBE?
		if (tp.getPredicate().isURI()) {
			// s= classURI
			String pred = tp.getPredicate().getURI();
			// System.out.println("[QTTree::generalize] The predicate is an URI 111111111 "
			// + pred);
			if (rdfd1.isInObjectPropertySet(pred) && !(rdfd2.isInObjectPropertySet(pred))) {
				predicate = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(pred));
			} else if (rdfd1.isInDatatypePropertySet(pred) && !(rdfd2.isInDatatypePropertySet(pred))) {
				predicate = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(pred));
			} else if (rdfd1.isInRDFVocabulary(pred) && !(rdfd2.isInRDFVocabulary(pred))) {
				predicate = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(pred));
			} else {
				// System.out.println("[QTTree::generalize] The predicate is an URI 22222222 " +
				// pred);
				predicate = tp.getPredicate();
			}
		} else {
			// it means that it is a variable
			// System.out.println("[QTTree::generalize] None of the cases was satisfied for
			// the predicate node " + tp.getPredicate().toString());
			predicate = tp.getPredicate();
		}

		// OBJECT
		if (tp.getObject().isURI()) {
			String obj = tp.getObject().getURI();
			// System.out.println("[QTTree::generalize] The Object is " + obj);
			if (rdfd1.getClassSet().contains(obj) && !(rdfd2.getClassSet().contains(obj))) {
				object = Var.alloc(classVarTable.generateIFAbsentClassVar(obj));
			} else if (rdfd1.isInIndividualSet(obj) && !(rdfd2.isInIndividualSet(obj))) {
				object = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(obj));
			} else if (rdfd1.isInObjectPropertySet(obj) && !(rdfd2.isInObjectPropertySet(obj))) {
				object = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(obj));
			} else if (rdfd1.isInDatatypePropertySet(obj) && !(rdfd2.isInDatatypePropertySet(obj))) {
				object = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(obj));
			} else if (rdfd1.isInRDFVocabulary(obj) && !(rdfd2.isInRDFVocabulary(obj))) {
				object = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(obj));
			} else {
				object = tp.getObject();
			}
		} else {
			if (tp.getObject().isLiteral()) {
				String objAsString = tp.getObject().getLiteralValue().toString();
				// System.out.println("The Object as a literal is "+objAsString);
				// System.out.println("The literal set of rdfd2 is
				// "+rdfd2.getLiteralSet().toString());

				if (rdfd1.isInLiteralSet(objAsString) && !(rdfd2.isInLiteralSet(objAsString))) {
					object = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(objAsString));
				} else {
					object = tp.getObject();
				}

			} else {
				object = tp.getObject();
			}
		}
		if (subject != null && predicate != null && object != null) {
			tpTemplate = new TriplePath(new Triple(subject, predicate, object));
			return tpTemplate;
		}

		return null;

	}

	// TO DO
	public void generalizeToQueryTemplate(Query query) {

		if (query == null) {
			throw new IllegalStateException("[QTTree::generalizeToQueryTemplate(Query query)]The query is null!!");
		}

	}

	// This method builds the query recommendation tree.
	// Input: it takes the rootTemplate;
	// output: it gives the rootTemplate;
	public void specializeToQueryInstance(TreeNode<T> n) {

		if (this.rootTemplate == null) {
			throw new IllegalStateException("[QTTree::specializeToQueryInstance()]The tree is empty!!");
		}
		List<TriplePath> tpSet = (List<TriplePath>) this.rootTemplate.getData();
		System.out.println("[QTTree::specializeToQueryInstance()] === START");
		System.out.println("[QTTree::specializeToQueryInstance()]" + tpSet.toString());

		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}

		System.out.println(
				"[QTTree::specializeToQueryInstance()] We are building the uery Recommendation Tree. WORK IN PROGRESS...");
		for (TriplePath tp : tpSet) {

			// The case that the triple pattern contains just variables <?s, ?p ?o>
			if (tp.getSubject().isVariable() && tp.getPredicate().isVariable() && tp.getObject().isVariable()) {
				System.out.println("[QTTree::generateQTTree] The subject, predicate and object are Variable");
				return;
			}

		}

	}

	// this is the first version that does not apply the FOR-EACH Predicate, but
	// just takes the first each time.
	public void specializeToQueryInstance1(TreeNode<List<TriplePath>> parentNode, List<String> parentPL,
			Map<String, String> parentPTMap, List<String> parentCL, Map<String, String> parentCTMap) {

		if (parentNode == null) {
			throw new IllegalStateException("[QTTree::specializeToQueryInstance()]A ParentNode is null!!");
		}
		List<TriplePath> tpSet = (List<TriplePath>) parentNode.getData();
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		for (TriplePath tp : tpSet) {

			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				System.out.println("[QTTree::generateQTTree] The triple patter is isTemplateVariableFree.");
				return;
			}

			// Step 1: Auxiliary Structures
			ArrayList<String> childPL = new ArrayList<String>();
			childPL.addAll(parentPL);
			Map<String, String> childPTMap = new HashMap<>();
			childPTMap.putAll(parentPTMap);
			List<String> childCL = new ArrayList<String>();
			childCL.addAll(parentCL);
			Map<String, String> childCTMap = new HashMap<>();
			childCTMap.putAll(parentCTMap);

			// PREDICATE
			// it means that there is at leat on of the subj, pred or obj that is a Template
			// Variable:
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVar = tp.getPredicate().getName();

					// Step 1: create the List<TriplePath> for the childNode.
					// We create the triple patterns set for the child node.
					// The set will initially contains the triple patterns
					// except the one we are about processing.
					List<TriplePath> childTriplePattersSet = new ArrayList<>();
					for (TriplePath tp1 : tpSet) {
						if (!(tp1.equals(tp))) {
							childTriplePattersSet.add(tp1);
						}
					}
					System.out.println(
							"[QTTree::generateQTTree] The predicate is a Template Variable that need to be instanciated");
					// Step 2: let's instanciate the variable, if it is not already instanciated.
					if (childPTMap.containsKey(templateVar)) {
						// Var templatePredicateVar = Var.alloc(parentPTMap.get(tpVar));
						// Step 3: adding the new instanciate triple pattern to the
						// childTriplePattersSet
						// Node newP = NodeFactory.createURI(templateVar);
						// Var.alloc(childPTMap.get(templateVar))
						childTriplePattersSet.add(new TriplePath(new Triple(tp.getSubject(),
								NodeFactory.createURI(childPTMap.get(templateVar)), tp.getObject())));
					} else {
						if (!childPL.isEmpty()) {
							String property = childPL.get(0);
							childPL.remove(0);

							// Step 4: adding the new instanciate triple pattern to the
							// childTriplePattersSet
							childTriplePattersSet.add(new TriplePath(
									new Triple(tp.getSubject(), NodeFactory.createURI(property), tp.getObject())));
							// Step 5: adding the mapping in the table.
							childPTMap.put(templateVar, property);

							// Step 6: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							// Step 2: create a childNode, which will have all the triple maps
							// except the one we are processing
							final TreeNode<List<TriplePath>> childNode;
							childNode = new TreeNode<>(childTriplePattersSet, null);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild(childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 6: recall the function on the child;
							specializeToQueryInstance1(childNode, childPL, childPTMap, childCL, childCTMap);

						}

					}

				}

			} // if predicate

		}
	}

	// this is the second version: it does apply the FOR-EACH Predicate, but
	// It does not provide all the solutions. We are going for the version 3
	public void specializeToQueryInstance2(TreeNode<List<TriplePath>> parentNode, List<String> parentPL,
			Map<String, String> parentPTMap, List<String> parentCL, Map<String, String> parentCTMap) {

		if (parentNode == null) {
			throw new IllegalStateException("[QTTree::specializeToQueryInstance()]A ParentNode is null!!");
		}
		List<TriplePath> tpSet = (List<TriplePath>) parentNode.getData();
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		for (TriplePath tp : tpSet) {

			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				System.out.println("[QTTree::generateQTTree] The triple patter is isTemplateVariableFree.");
				return;
			}

			// PREDICATE
			// it means that there is at leat on of the subj, pred or obj that is a Template
			// Variable:
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVar = tp.getPredicate().getName();

					System.out.println(
							"[QTTree::generateQTTree] The predicate is a Template Variable that need to be instanciated");

					if (!parentPL.isEmpty()) {
						for (String property : parentPL) {

							// Step 1: create the List<TriplePath> for the childNode.
							// We create the triple patterns set for the child node.
							// The set will initially contains the triple patterns
							// except the one we are about processing.
							List<TriplePath> childTriplePattersSet = new ArrayList<>();
							for (TriplePath tp1 : tpSet) {
								if (!(tp1.equals(tp))) {
									childTriplePattersSet.add(tp1);
								}
							}
							// Step 1: Auxiliary Structures
							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							Map<String, String> childPTMap = new HashMap<>();
							childPTMap.putAll(parentPTMap);
							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							Map<String, String> childCTMap = new HashMap<>();
							childCTMap.putAll(parentCTMap);

							// String property = childPL.get(0);
							childPL.remove(property);

							// Step 2: let's instanciate the variable, if it is not already instanciated.
							if (childPTMap.containsKey(templateVar)) {
								// Var templatePredicateVar = Var.alloc(parentPTMap.get(tpVar));
								// Step 3: adding the new instanciate triple pattern to the
								// childTriplePattersSet
								// Node newP = NodeFactory.createURI(templateVar);
								// Var.alloc(childPTMap.get(templateVar))
								childTriplePattersSet.add(new TriplePath(new Triple(tp.getSubject(),
										NodeFactory.createURI(childPTMap.get(templateVar)), tp.getObject())));

								// Step 6: creating a childNode and we add it to the Tree, if it is not added
								// alrady.
								// Step 2: create a childNode, which will have all the triple maps
								// except the one we are processing
								final TreeNode<List<TriplePath>> childNode;
								childNode = new TreeNode<>(childTriplePattersSet, null);
								if (!(isInTreeNodeIndex(childNode))) {
									parentNode.addChild(childNode);
									addToNodeTreeIndexIFAbsent(childNode);
								}

								// Step 6: recall the function on the child;
								specializeToQueryInstance2(childNode, childPL, childPTMap, childCL, childCTMap);

							} else {
								// Step 4: adding the new instanciate triple pattern to the
								// childTriplePattersSet
								childTriplePattersSet.add(new TriplePath(
										new Triple(tp.getSubject(), NodeFactory.createURI(property), tp.getObject())));
								// Step 5: adding the mapping in the table.
								childPTMap.put(templateVar, property);

								// Step 6: creating a childNode and we add it to the Tree, if it is not added
								// alrady.
								// Step 2: create a childNode, which will have all the triple maps
								// except the one we are processing
								final TreeNode<List<TriplePath>> childNode;
								childNode = new TreeNode<>(childTriplePattersSet, null);
								if (!(isInTreeNodeIndex(childNode))) {
									parentNode.addChild(childNode);
									addToNodeTreeIndexIFAbsent(childNode);
								}

								// Step 6: recall the function on the child;
								specializeToQueryInstance2(childNode, childPL, childPTMap, childCL, childCTMap);

							}

						}
					} else { // in this case you do not create other nodes as the value already exist
								// (MAYBE?!?!?!?)
						// Step 1: create the List<TriplePath> for the childNode.
						// We create the triple patterns set for the child node.
						// The set will initially contains the triple patterns
						// except the one we are about processing.
						List<TriplePath> childTriplePattersSet = new ArrayList<>();
						for (TriplePath tp1 : tpSet) {
							if (!(tp1.equals(tp))) {
								childTriplePattersSet.add(tp1);
							}
						}

						// Step 1: Auxiliary Structures
						ArrayList<String> childPL = new ArrayList<String>();
						childPL.addAll(parentPL);
						Map<String, String> childPTMap = new HashMap<>();
						childPTMap.putAll(parentPTMap);
						ArrayList<String> childCL = new ArrayList<String>();
						childCL.addAll(parentCL);
						Map<String, String> childCTMap = new HashMap<>();
						childCTMap.putAll(parentCTMap);

						// Step 2: let's instanciate the variable, if it is not already instanciated.
						if (childPTMap.containsKey(templateVar)) {
							// Var templatePredicateVar = Var.alloc(parentPTMap.get(tpVar));
							// Step 3: adding the new instanciate triple pattern to the
							// childTriplePattersSet
							// Node newP = NodeFactory.createURI(templateVar);
							// Var.alloc(childPTMap.get(templateVar))
							childTriplePattersSet.add(new TriplePath(new Triple(tp.getSubject(),
									NodeFactory.createURI(childPTMap.get(templateVar)), tp.getObject())));
						}
						// else {
						//
						// // String property = childPL.get(0);
						// childPL.remove(property);
						//
						// // Step 4: adding the new instanciate triple pattern to the
						// childTriplePattersSet
						// childTriplePattersSet.add(
						// new TriplePath(new Triple(tp.getSubject(), NodeFactory.createURI(property),
						// tp.getObject())));
						// // Step 5: adding the mapping in the table.
						// childPTMap.put(templateVar, property);
						//
						// }
						// Step 6: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						// Step 2: create a childNode, which will have all the triple maps
						// except the one we are processing
						final TreeNode<List<TriplePath>> childNode;
						childNode = new TreeNode<>(childTriplePattersSet, null);
						if (!(isInTreeNodeIndex(childNode))) {
							parentNode.addChild(childNode);
							addToNodeTreeIndexIFAbsent(childNode);
						}

						// Step 6: recall the function on the child;
						specializeToQueryInstance2(childNode, childPL, childPTMap, childCL, childCTMap);

					}

				}

			} // if predicate

		}
	}

	// this is the third version. Yet, it does not produce the right results.
	public void specializeToQueryInstance3(TreeNode<List<TriplePath>> parentNode, List<String> parentPL,
			Map<String, String> parentPTMap, List<String> parentCL, Map<String, String> parentCTMap) {

		if (parentNode == null) {
			throw new IllegalStateException("[QTTree::specializeToQueryInstance()]A ParentNode is null!!");
		}
		List<TriplePath> tpSet = (List<TriplePath>) parentNode.getData();
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		for (TriplePath tp : tpSet) {
			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				System.out.println("[QTTree::generateQTTree] The triple patter is isTemplateVariableFree.");
				return;
			}
			// PREDICATE
			// it means that there is at leat on of the subj, pred or obj that is a Template
			// Variable:
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVar = tp.getPredicate().getName();

					System.out.println(
							"[QTTree::generateQTTree] The predicate is a Template Variable that need to be instanciated");

					if (!parentPL.isEmpty()) {
						for (String property : parentPL) {

							// Step 1: create the List<TriplePath> for the childNode.
							// We create the triple patterns set for the child node.
							// The set will initially contains the triple patterns
							// except the one we are about processing.
							List<TriplePath> childTriplePattersSet = new ArrayList<>();
							for (TriplePath tp1 : tpSet) {
								if (!(tp1.equals(tp))) {
									childTriplePattersSet.add(tp1);
								}
							}
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							Map<String, String> childPTMap = new HashMap<>();
							childPTMap.putAll(parentPTMap);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							Map<String, String> childCTMap = new HashMap<>();
							childCTMap.putAll(parentCTMap);

							// Step 2: let's instanciate the variable, if it is not already instanciated.
							if (childPTMap.containsKey(templateVar)) {
								// Var templatePredicateVar = Var.alloc(parentPTMap.get(tpVar));
								// Step 3: adding the new instanciate triple pattern to the
								// childTriplePattersSet
								// Node newP = NodeFactory.createURI(templateVar);
								// Var.alloc(childPTMap.get(templateVar))
								childTriplePattersSet.add(new TriplePath(new Triple(tp.getSubject(),
										NodeFactory.createURI(childPTMap.get(templateVar)), tp.getObject())));

								// Step 6: creating a childNode and we add it to the Tree, if it is not added
								// alrady.
								// Step 2: create a childNode, which will have all the triple maps
								// except the one we are processing
								TreeNode<List<TriplePath>> childNode;
								childNode = new TreeNode<>(childTriplePattersSet, null);
								if (!(isInTreeNodeIndex(childNode))) {
									parentNode.addChild(childNode);
									addToNodeTreeIndexIFAbsent(childNode);
								}

								// Step 6: recall the function on the child;
								specializeToQueryInstance3(childNode, childPL, childPTMap, childCL, childCTMap);

							} else {

								childPL.remove(property);

								// Step 4: adding the new instanciate triple pattern to the
								// childTriplePattersSet
								childTriplePattersSet.add(new TriplePath(
										new Triple(tp.getSubject(), NodeFactory.createURI(property), tp.getObject())));
								// Step 5: adding the mapping in the table.
								childPTMap.put(templateVar, property);

								// Step 6: creating a childNode and we add it to the Tree, if it is not added
								// alrady.
								// Step 2: create a childNode, which will have all the triple maps
								// except the one we are processing
								TreeNode<List<TriplePath>> childNode;
								childNode = new TreeNode<>(childTriplePattersSet, null);
								if (!(isInTreeNodeIndex(childNode))) {
									parentNode.addChild(childNode);
									addToNodeTreeIndexIFAbsent(childNode);
								}

								// Step 6: recall the function on the child;
								specializeToQueryInstance3(childNode, childPL, childPTMap, childCL, childCTMap);

							}

						}
					} else { // in this case you do not create other nodes as the value already exist
								// (MAYBE?!?!?!?)
						// Step 1: create the List<TriplePath> for the childNode.
						// We create the triple patterns set for the child node.
						// The set will initially contains the triple patterns
						// except the one we are about processing.
						List<TriplePath> childTriplePattersSet = new ArrayList<>();
						for (TriplePath tp1 : tpSet) {
							if (!(tp1.equals(tp))) {
								childTriplePattersSet.add(tp1);
							}
						}

						// Step 1: Auxiliary Structures
						ArrayList<String> childPL = new ArrayList<String>();
						childPL.addAll(parentPL);
						Map<String, String> childPTMap = new HashMap<>();
						childPTMap.putAll(parentPTMap);
						List<String> childCL = new ArrayList<String>();
						childCL.addAll(parentCL);
						Map<String, String> childCTMap = new HashMap<>();
						childCTMap.putAll(parentCTMap);

						// Step 2: let's instanciate the variable, if it is not already instanciated.
						if (childPTMap.containsKey(templateVar)) {
							// Var templatePredicateVar = Var.alloc(parentPTMap.get(tpVar));
							// Step 3: adding the new instanciate triple pattern to the
							// childTriplePattersSet
							// Node newP = NodeFactory.createURI(templateVar);
							// Var.alloc(childPTMap.get(templateVar))
							childTriplePattersSet.add(new TriplePath(new Triple(tp.getSubject(),
									NodeFactory.createURI(childPTMap.get(templateVar)), tp.getObject())));
						}
						// else {
						//
						// // String property = childPL.get(0);
						// childPL.remove(property);
						//
						// // Step 4: adding the new instanciate triple pattern to the
						// childTriplePattersSet
						// childTriplePattersSet.add(
						// new TriplePath(new Triple(tp.getSubject(), NodeFactory.createURI(property),
						// tp.getObject())));
						// // Step 5: adding the mapping in the table.
						// childPTMap.put(templateVar, property);
						//
						// }
						// Step 6: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						// Step 2: create a childNode, which will have all the triple maps
						// except the one we are processing
						final TreeNode<List<TriplePath>> childNode;
						childNode = new TreeNode<>(childTriplePattersSet, null);
						if (!(isInTreeNodeIndex(childNode))) {
							parentNode.addChild(childNode);
							addToNodeTreeIndexIFAbsent(childNode);
						}

						// Step 6: recall the function on the child;
						specializeToQueryInstance3(childNode, childPL, childPTMap, childCL, childCTMap);

					}

				}

			} // if predicate

		}
	}

	// This is the fourth version. It does produce the right results.
	public void specializeToQueryInstance4(TreeNode<List<TriplePath>> parentNode, List<String> parentPL,
			Map<String, String> parentPTMap, List<String> parentCL, Map<String, String> parentCTMap) {

		if (parentNode == null) {
			throw new IllegalStateException("[QTTree::specializeToQueryInstance()]A ParentNode is null!!");
		}
		List<TriplePath> tpSet = (List<TriplePath>) parentNode.getData();
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		for (TriplePath tp : tpSet) {
			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				System.out.println("[QTTree::generateQTTree] The triple patter is isTemplateVariableFree.");
				return;
			}
			// PREDICATE
			// it means that there is at leat on of the subj, pred or obj that is a Template
			// Variable:
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVar = tp.getPredicate().getName();

					System.out.println("[QTTree::generateQTTree] "
							+ "The predicate is a Template Variable that need to be instanciated");

					// checking if it is already instanceated.
					if (parentPTMap.containsKey(templateVar)) {
						// Step 1: create the List<TriplePath> for the childNode.
						// We create the triple patterns set for the child node.
						// The set will initially contains the triple patterns
						// except the one we are about processing.
						List<TriplePath> childTriplePattersSet = new ArrayList<>();
						for (TriplePath tp1 : tpSet) {
							if (!(tp1.equals(tp))) {
								childTriplePattersSet.add(tp1);
							}
						}
						// Step 1: Auxiliary Structures
						ArrayList<String> childPL = new ArrayList<String>();
						childPL.addAll(parentPL);
						Map<String, String> childPTMap = new HashMap<>();
						childPTMap.putAll(parentPTMap);
						List<String> childCL = new ArrayList<>();
						childCL.addAll(parentCL);
						Map<String, String> childCTMap = new HashMap<>();
						childCTMap.putAll(parentCTMap);

						// instanciate the temnplate variable with a value from the parentPTMap.
						childTriplePattersSet.add(new TriplePath(new Triple(tp.getSubject(),
								NodeFactory.createURI(parentPTMap.get(templateVar)), tp.getObject())));
						// Step 6: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						// Step 2: create a childNode, which will have all the triple maps
						// except the one we are processing
						TreeNode<List<TriplePath>> childNode;
						childNode = new TreeNode<>(childTriplePattersSet, null);
						if (!(isInTreeNodeIndex(childNode))) {
							System.out.println("[QTTree::specializeToQueryInstance4] IF");
							parentNode.addChild(childNode);
							addToNodeTreeIndexIFAbsent(childNode);
						}

						// Step 6: recall the function on the child;
						specializeToQueryInstance4(childNode, childPL, childPTMap, childCL, childCTMap);

					} else { // it means that we need to instancited for all the properties of parentPL.
						for (String property : parentPL) {

							// Step 1: create the List<TriplePath> for the childNode.
							// We create the triple patterns set for the child node.
							// The set will initially contains the triple patterns
							// except the one we are about processing.
							List<TriplePath> childTriplePattersSet = new ArrayList<>();
							for (TriplePath tp1 : tpSet) {
								if (!(tp1.equals(tp))) {
									childTriplePattersSet.add(tp1);
								}
							}
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							Map<String, String> childPTMap = new HashMap<>();
							childPTMap.putAll(parentPTMap);
							ArrayList<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							Map<String, String> childCTMap = new HashMap<>();
							childCTMap.putAll(parentCTMap);

							childPL.remove(property);

							// Step 4: adding the new instanciate triple pattern to the
							// childTriplePattersSet
							childTriplePattersSet.add(new TriplePath(
									new Triple(tp.getSubject(), NodeFactory.createURI(property), tp.getObject())));
							// Step 5: adding the mapping in the table.
							childPTMap.put(templateVar, property);

							// Step 6: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							// Step 2: create a childNode, which will have all the triple maps
							// except the one we are processing
							TreeNode<List<TriplePath>> childNode;
							childNode = new TreeNode<>(childTriplePattersSet, null);
							if (!(isInTreeNodeIndex(childNode))) {
								System.out.println("[QTTree::specializeToQueryInstance4] FOR");
								parentNode.addChild(childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}

							// Step 6: recall the function on the child;
							specializeToQueryInstance4(childNode, childPL, childPTMap, childCL, childCTMap);
						}
					}

				}

			} // if predicate

		}
	}

	public TreeNode<T> getRootTemplate() {
		return rootTemplate;
	}

	// private void addNode(TreeNode<T> childNode, TreeNode<T> parentNode) {
	//// if ((this.root == null) && (this.currentParentNode == null)) {
	//// this.root = node;
	//// this.currentParentNode = node;
	//// } else {
	////
	// parentNode.addChild(childNode);
	//// this.currentParentNode = (TreeNode<T>) node;
	//
	//// if (this.currentParentNode.getChildren() == null) {
	//// this.currentParentNode.addChild(node);
	//// this.currentParentNode = (TreeNode<T>) node;
	//// } else {
	//// this.currentParentNode.addSibling(node);
	//// this.currentParentNode = (TreeNode<T>) node;
	//// }
	//// }
	// }
	private boolean isInTreeNodeIndex(TreeNode<List<TriplePath>> currNode) {

		//////
		List<TriplePath> triplePathCollection = currNode.getData();
		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}

		// System.out.println("[QTTree::isInTreeNodeIndex] BEFORE SORTED=== " +
		// s.toString());
		Collections.sort(s);
		// System.out.println("[QTTree::isInTreeNodeIndex] AFTER SORTED=== " +
		// s.toString());
		// System.out.println("[QTTree::isInTreeNodeIndex] IS IT IN === " +
		// treeNodeIndex.containsKey(s.toString()));

		return treeNodeIndex.containsKey(s.toString());
		//////

		// List<TriplePath> triplePathCollection = currNode.getData();
		// Set<String> triplePatternSet = new HashSet<>();
		// for (TriplePath tp : triplePathCollection) {
		// triplePatternSet.add(tp.toString());
		// }
		// Set<String> sorted = new TreeSet<String>(triplePatternSet);
		// if (treeNodeIndex.containsKey(sorted.toString())) {
		// return true;
		// }
		// return false;
	}

	private void addToNodeTreeIndexIFAbsent(TreeNode<List<TriplePath>> currNode) {
		//////
		List<TriplePath> triplePathCollection = currNode.getData();
		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}
		// System.out.println("[QTTree::addToNodeTreeIndexIFAbsent] BEFORE SORTED=== " +
		// s.toString());
		Collections.sort(s);
		// System.out.println("[QTTree::addToNodeTreeIndexIFAbsent] AFTER SORTED=== " +
		// s.toString());
		treeNodeIndex.putIfAbsent(s.toString(), (TreeNode<T>) currNode);
		//////

		// List<TriplePath> triplePathCollection = currNode.getData();
		// Set<String> triplePatternSet = new HashSet<String>();
		// for (TriplePath tp : triplePathCollection) {
		// triplePatternSet.add(tp.toString());
		// }
		// Set<String> sorted = new TreeSet<String>(triplePatternSet);
		// treeNodeIndex.put(sorted.toString(), (TreeNode<T>) currNode);
	}

	private boolean isTemplateVariable(String entity) {

		// System.out.println("entity "+entity);
		// System.out.println("varTemplate-predicate: " +
		// objectProperyVarTable.getVarObjectProperyTable().toString());
		// if (entity.startsWith("?")) {
		// if
		// (objectProperyVarTable.getVarObjectProperyTable().containsKey(entity.substring(1,
		// entity.length()))) {
		if (objectProperyVarTable.getVarObjectProperyTable().containsKey(entity)) {
			return true;
		} else if (classVarTable.getVarClassTable().containsKey(entity)) {
			return true;
		} else if (datatypePropertyVarTable.getVarDatatypeProperyTable().containsKey(entity)) {
			return true;
		}

		// }
		return false;
	}

	private boolean isTemplateVariableFree(TriplePath tp) {

		if (tp.getSubject().isVariable()) {
			if (isTemplateVariable(tp.getSubject().getName())) {
				return false;
			}
		}
		if (tp.getPredicate().isVariable()) {
			if (isTemplateVariable(tp.getPredicate().getName())) {
				return false;
			}
		}
		if (tp.getObject().isVariable()) {
			if (isTemplateVariable(tp.getObject().getName())) {
				return false;
			}
		}
		return true;
	}

}
