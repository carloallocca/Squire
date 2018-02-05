/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.entityvariablemapping.ClassVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.DatatypePropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.IndividualVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.LiteralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.ObjectPropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.RDFVocVarMapping;
import uk.ac.open.kmi.squire.evaluation.QueryGPESim;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeSimilarity;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.ontologymatching.JaroWinklerSimilarity;
import uk.ac.open.kmi.squire.operation.RemoveTriple;
import uk.ac.open.kmi.squire.operation.SPARQLQueryGeneralization;
import uk.ac.open.kmi.squire.operation.SPARQLQueryInstantiation;
import uk.ac.open.kmi.squire.operation.SPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author callocca
 */
public class QueryRecommendation<T> {

	private static String CLASS_TEMPLATE_VAR = "ct";

	private static String OBJ_PROP_TEMPLATE_VAR = "opt";

	private static String DT_PROP_TEMPLATE_VAR = "dpt";
	private static String INDIVIDUAL_TEMPLATE_VAR = "it";
	private static String LITERAL_TEMPLATE_VAR = "lt";
	private static String INSTANCE_OP = "I";

	private static String REMOVE_TP_OP = "R";

	// private TreeNode<T> root;
	private TreeNode<T> rootDataNode;

	private HashMap<String, TreeNode<T>> treeNodeIndex = new HashMap<>();
	private IRDFDataset rdfd1;
	private final Query originalQuery;
	private Query originalQueryCopy;
	private Query queryTemplate;
	private IRDFDataset rdfd2;
	// private ArrayList<String> recommendedQueryList;

	private List<QueryScorePair> queryRecommendatedList = new ArrayList<>();
	private LiteralVarMapping literalVarTable;
	private ClassVarMapping classVarTable;
	private DatatypePropertyVarMapping datatypePropertyVarTable;
	private IndividualVarMapping individualVarTable;

	private ObjectPropertyVarMapping objectProperyVarTable;
	private RDFVocVarMapping rdfVocVarTable;

	public QueryRecommendation(Query query, IRDFDataset d1, IRDFDataset d2) {
		// this.root = node;
		originalQuery = QueryFactory.create(query.toString());
		originalQueryCopy = QueryFactory.create(query.toString());

		// System.out.println("[QueryRecommendation::QueryRecommendation]
		// this.originalQuery = query; "+
		// originalQuery);
		rdfd1 = d1;
		classVarTable = new ClassVarMapping();
		individualVarTable = new IndividualVarMapping();
		literalVarTable = new LiteralVarMapping();
		objectProperyVarTable = new ObjectPropertyVarMapping();
		datatypePropertyVarTable = new DatatypePropertyVarMapping();
		rdfVocVarTable = new RDFVocVarMapping();

		rdfd2 = d2;

	}

	private void addToNodeTreeIndexIFAbsent(TreeNode<Query> currNode) {
		// ////
		List<TriplePath> triplePathCollection = getTriplePathSet(currNode.getData());
		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}
		Collections.sort(s);
		treeNodeIndex.putIfAbsent(s.toString(), (TreeNode<T>) currNode);
	}

	private void addToNodeTreeIndexIFAbsent1(TreeNode<DataNode> childNode) {

		DataNode data = childNode.getData();
		Query q = data.getqR();// currNode.getData();
		List<TriplePath> triplePathCollection = getTriplePathSet(q);
		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}
		Collections.sort(s);
		treeNodeIndex.putIfAbsent(s.toString(), (TreeNode<T>) childNode);
	}

	private float computeInstanciateOperationCost(String entityqO, String entityqR) {
		if (entityqO == null || entityqR == null) {
			return (float) 0.0;
		}
		JaroWinklerSimilarity jwSim = new JaroWinklerSimilarity();
		float sim = jwSim.computeMatchingScore(entityqO, entityqR);
		// return (float) (1.0 - sim);
		return sim;

	}

	public void computeRecommendateQueryScore(TreeNode<T> n, int i) {
		if (n == null) {
			throw new IllegalStateException(
					"[QueryRecommendation,computeRecommendateQueryScore]The Query Tree is empty!!");
		}
		for (int j = 0; j < i; j++) {
			System.out.print("  ");
		}
		if (n.getData() != null) {

			SPARQLQuerySatisfiable qsat = new SPARQLQuerySatisfiable();
			Query qRec = ((DataNode) n.getData()).getqR();

			boolean querySatR1 = qsat.isSatisfiableWRTProjectVar(qRec);

			boolean querySatR2 = qsat.isSatisfiable(qRec, this.rdfd2);

			if (querySatR2 && querySatR1) {

				// DISTANCE QUERY ROOT
				float queryRootDist = ((DataNode) n.getData()).getNodeCost();

				// QUERY RESULT TYPE SIMILARITY
				QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
				// float resulttypeSim = qRTS.computeQueryResultTypeDistance(((DataNode)
				// n.getData()).qO,
				// this.rdfd1, ((DataNode) n.getData()).qR, this.rdfd2);
				float resulttypeSim = qRTS.computeQueryResultTypeDistance(this.originalQuery, this.rdfd1,
						((DataNode) n.getData()).qR, this.rdfd2);

				// System.out.println("[QueryRecommendation,computeRecommendateQueryScore] QUERY
				// RESULT TYPE SIMILARITY "
				// + resulttypeSim);
				// System.out.println("");

				// QUERY RESULT SIZE DISTANCE
				// QueryResultSizeDistance qRSD= new QueryResultSizeDistance();
				// float queryResultSizeSim=qRSD.computeQRSSim(((DataNode) n.getData()).qO,
				// this.rdfd1,
				// ((DataNode) n.getData()).qR, this.rdfd2);
				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] QUERY
				// RESULT SIZE DISTANCE "
				// +queryResultSizeSim);
				// System.out.println("");
				// TOTAL
				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] TOTAL "
				// +(queryRootDist+sim+queryResultSizeSim));
				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] TOTAL "
				// +(queryRootDist+queryResultSizeSim));
				// Query Specificity Distance
				QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
				// float qSpecDistSim = qSpecDist.computeQSDwrtQueryVariable(((DataNode)
				// n.getData()).qO,
				// ((DataNode) n.getData()).qR);

				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] Original
				// Query " +
				// this.originalQuery.toString());
				float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(this.originalQuery,
						((DataNode) n.getData()).qR);
				// System.out.println("[QueryRecommendation,computeRecommendateQueryScore]
				// QuerySpecificityDistanceWRT Var "
				// + qSpecDistSimVar);
				// System.out.println("");

				float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(this.originalQuery,
						((DataNode) n.getData()).qR);
				// System.out.println("[QueryRecommendation,computeRecommendateQueryScore]
				// qSpecDistSimTriplePattern "
				// + qSpecDistSimTriplePattern);
				// System.out.println("");

				// float score = ((Float.MAX_VALUE - (queryRootDist +
				// resulttypeSim))/Float.MAX_VALUE);
				// float score = Math.abs( 1- (queryRootDist + resulttypeSim));
				// float score = ((1-queryRootDist) +
				// resulttypeSim)+qSpecDistSimVar+qSpecDistSimTriplePattern;
				float score = ((queryRootDist) + resulttypeSim) + qSpecDistSimVar + qSpecDistSimTriplePattern;
				// float score = resulttypeSim +
				// qSpecDistSimVar+qSpecDistSimTriplePattern;//This is working
				// as it should but it does not consider the similarity distance between the
				// replased entities

				// System.out.println("[QueryRecommendation,computeRecommendateQueryScore] TOTAL
				// " + score);
				// System.out.println("");
				QueryScorePair queryScorePair = new QueryScorePair(((DataNode) n.getData()).getqR(), score);
				this.queryRecommendatedList.add(queryScorePair);
			}
		}
		for (TreeNode<T> node : n.getChildren()) {
			computeRecommendateQueryScore(node, i + 1);
		}

	}

	private float computeRemoveOperationCost(Query originalQuery, Query childQuery) {
		QueryGPESim queryGPEsim = new QueryGPESim();
		float sim = queryGPEsim.computeQueryPatternsSim(originalQuery, childQuery);
		// return (float) 1.0 - sim;
		return sim;
		// return 0;
	}

	public void generalizeToQueryTemplate() {

		if (this.originalQueryCopy == null) {
			throw new IllegalStateException("[QTTree::generalizeToQueryTemplate(Query query)]The query is null!!");
		}

		Set<Node> subjects = getSubjectsSet(this.originalQueryCopy);
		// System.out.println("[QueryRecommendation::generalizeToQueryTemplate] The list
		// of Subjects: " +
		// subjects.toString());
		Set<Node> predicates = getPredicatesSet(this.originalQueryCopy);
		// System.out.println("[QueryRecommendation::generalizeToQueryTemplate] The list
		// of Predicates: " +
		// predicates.toString());
		Set<Node> objects = getObjectsSet(this.originalQueryCopy);
		// System.out.println("[QueryRecommendation::generalizeToQueryTemplate] The list
		// of Objects: " +
		// objects.toString());

		SPARQLQueryGeneralization qg = new SPARQLQueryGeneralization();

		// SUBJECT
		for (Node subj : subjects) {
			// ...check it is not a variable already or it is not a blank node...
			if (!(subj.isVariable()) && !(subj.isBlank())) {
				// ...check: if the subj is not an element of the D2 then it will genearte a
				// variable
				// otherwise is null;
				// Var templateVarSub = ifSubjectIsNotD2ThenGenerateVariable(subj);
				Var templateVarSub = ifSubjectIsNotD2ThenGenerateVariableNew(subj);
				// ... generalize from a node to VarTemplate
				if (templateVarSub != null) {
					// Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQuery, subj,
					// templateVarSub);
					Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQueryCopy, subj, templateVarSub);
					this.originalQueryCopy = genQuery;
				}
			}
		}

		// System.out.println("[QueryRecommendation::generalizeToQueryTemplate] The
		// subjects have been generalize: "
		// + this.originalQuery.toString());
		// PREDICATE
		for (Node pred : predicates) {
			// ...check it is not a variable already or it is not a blank node...
			if (!(pred.isVariable()) && !(pred.isBlank())) {
				// ...check: if the subj is not an element of the D2 then it will genearte a
				// variable
				// otherwise is null;
				// Var templateVarPred = ifPredicateIsNotD2ThenGenerateVariable(pred);
				Var templateVarPred = ifPredicateIsNotD2ThenGenerateVariableNew(pred);
				// ... generalize from a node to VarTemplate
				if (templateVarPred != null) {
					// Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQuery, pred,
					// templateVarPred);
					Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQueryCopy, pred, templateVarPred);
					this.originalQueryCopy = genQuery;
				}
			}
		}

		// OBEJCT
		for (Node obj : objects) {
			// ...check it is not a variable already or it is not a blank node...
			if (!(obj.isVariable()) && !(obj.isBlank())) {
				// ...check: if the subj is not an element of the D2 then it will genearte a
				// variable
				// otherwise is null;
				// Var templateVarObj = ifObjectIsNotD2ThenGenerateVariable(obj);
				Var templateVarObj = ifObjectIsNotD2ThenGenerateVariableNew(obj);
				// ... generalize from a node to VarTemplate
				if (templateVarObj != null) {
					// Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQuery, obj,
					// templateVarObj);
					Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQueryCopy, obj, templateVarObj);
					this.originalQueryCopy = genQuery;
				}
			}
		}
		System.out.println(" ");
		System.out.println("[QueryRecommendation, generalizeToQueryTemplate()] THE GENERALIZED QUERY: ");
		System.out.println(this.originalQueryCopy.toString());

		this.queryTemplate = this.originalQueryCopy;

	}

	private String getLocalName(String entityqO) {
		String localName = "";
		if (entityqO.startsWith("http://") || entityqO.startsWith("https://")) {
			if (entityqO.contains("#")) {
				localName = entityqO.substring(entityqO.indexOf("#") + 1, entityqO.length());
				return localName;
			}
			localName = entityqO.substring(entityqO.lastIndexOf("/") + 1, entityqO.length());
			return localName;
		} else {
			return entityqO;
		}

	}

	private Set<Node> getObjectsSet(Query originalQuery) {
		if (this.originalQuery == null) {
			throw new IllegalStateException("[QTTree::getSubjectsSet(Query originalQuery)]The query is null!!");
		}
		// Remember distinct objects in this
		final Set<Node> objects = new HashSet<Node>();
		// This will walk through all parts of the query
		ElementWalker.walk(this.originalQuery.getQueryPattern(),
				// For each element
				new ElementVisitorBase() {
					// ...when it's a block of triples...
					public void visit(ElementPathBlock el) {
						// ...go through all the triples...
						Iterator<TriplePath> triples = el.patternElts();
						while (triples.hasNext()) {
							// ...and grab the objects
							objects.add(triples.next().getObject());
						}
					}
				});
		return objects;

	}

	private Set<Node> getPredicatesSet(Query originalQuery) {
		if (this.originalQuery == null) {
			throw new IllegalStateException("[QTTree::getSubjectsSet(Query originalQuery)]The query is null!!");
		}

		// Remember distinct predicates in this
		final Set<Node> predicates = new HashSet<Node>();
		// This will walk through all parts of the query
		ElementWalker.walk(this.originalQuery.getQueryPattern(),
				// For each element
				new ElementVisitorBase() {
					// ...when it's a block of triples...
					public void visit(ElementPathBlock el) {
						// ...go through all the triples...
						Iterator<TriplePath> triples = el.patternElts();
						while (triples.hasNext()) {
							// ...and grab the subject
							predicates.add(triples.next().getPredicate());
						}
					}
				});
		return predicates;
	}

	public List<QueryScorePair> getQueryRecommendatedList() {
		return queryRecommendatedList;
	}

	public Query getQueryTemplate() {
		return queryTemplate;
	}

	public TreeNode<T> getRootTemplate() {
		return rootDataNode;
	}

	private Set<Node> getSubjectsSet(Query originalQuery) {

		if (this.originalQuery == null) {
			throw new IllegalStateException("[QTTree::getSubjectsSet(Query originalQuery)]The query is null!!");
		}

		// Remember distinct subjects in this
		final Set<Node> subjects = new HashSet<Node>();
		// This will walk through all parts of the query
		ElementWalker.walk(this.originalQuery.getQueryPattern(),
				// For each element
				new ElementVisitorBase() {
					// ...when it's a block of triples...
					public void visit(ElementPathBlock el) {
						// ...go through all the triples...
						Iterator<TriplePath> triples = el.patternElts();
						while (triples.hasNext()) {
							// ...and grab the subject
							subjects.add(triples.next().getSubject());
						}
					}
				});
		return subjects;
	}

	private List<TriplePath> getTriplePathSet(Query originalQuery) {
		if (originalQuery == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::getTriplePathSet(Query originalQuery)]The query is null!!");
		}
		// Remember distinct objects in this
		final List<TriplePath> tpSet = new ArrayList<TriplePath>();
		// This will walk through all parts of the query
		ElementWalker.walk(originalQuery.getQueryPattern(),
				// For each element
				new ElementVisitorBase() {
					// ...when it's a block of triples...
					public void visit(ElementPathBlock el) {
						// ...go through all the triples...
						Iterator<TriplePath> triples = el.patternElts();
						while (triples.hasNext()) {
							// ...and grab the objects
							tpSet.add(triples.next());
						}
					}
				});
		return tpSet;
	}

	private Var ifObjectIsNotD2ThenGenerateVariable(Node obj) {

		if (obj == null || rdfd2 == null) {
			throw new IllegalStateException(
					"[QueryRecommandation::ifObjectIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
		}
		final Var result;

		// OBJECT
		if (obj.isURI()) {
			String o = obj.getURI();
			// System.out.println("[QTTree::generalize] The Object is " + obj);
			// if (rdfd1.getClassSet().contains(o) && !(rdfd2.getClassSet().contains(o))) {
			if (!(rdfd2.getClassSet().contains(o))) {
				result = Var.alloc(classVarTable.generateIFAbsentClassVar(o));
				return result;
			} else // if (rdfd1.isInIndividualSet(o) && !(rdfd2.isInIndividualSet(o))) {
			{
				if (!(rdfd2.isInIndividualSet(o))) {
					result = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(o));
					return result;
				} else // if (rdfd1.isInObjectPropertySet(o) && !(rdfd2.isInObjectPropertySet(o))) {
				{
					if (!(rdfd2.isInObjectPropertySet(o))) {
						result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(o));
						return result;
					} else // if (rdfd1.isInDatatypePropertySet(o) && !(rdfd2.isInDatatypePropertySet(o)))
							// {
					{
						if (!(rdfd2.isInDatatypePropertySet(o))) {
							result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(o));
							return result;
						} else // if (rdfd1.isInRDFVocabulary(o) && !(rdfd2.isInRDFVocabulary(o))) {
						{
							if (!(rdfd2.isInRDFVocabulary(o))) {
								result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(o));
								return result;
							} else {
								result = null;
								return result;
							}
						}
					}
				}
			}
		} else if (obj.isLiteral()) {
			String objAsString = obj.getLiteralValue().toString();
			// System.out.println("The Object as a literal is "+objAsString);
			// System.out.println("The literal set of rdfd2 is
			// "+rdfd2.getLiteralSet().toString());

			if (rdfd1.isInLiteralSet(objAsString) && !(rdfd2.isInLiteralSet(objAsString))) {
				result = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(objAsString));
				return result;
			} else {
				result = null;
				return result;
			}

		} else {
			result = (Var) obj;
			return result;
			// object = tp.getObject();
		}

	}

	private Var ifObjectIsNotD2ThenGenerateVariableNew(Node obj) {

		if (obj == null || rdfd2 == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::ifSubjectIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
		}
		final Var result;
		// SUBJECT
		if (obj.isURI()) {
			// s= classURI
			String o = obj.getURI();
			// System.out.println("[QTTree::generalize] The Sub is an URI " + subj);
			if ((rdfd1.getClassSet().contains(o)) && !(rdfd2.getClassSet().contains(o))) {
				result = Var.alloc(classVarTable.generateIFAbsentClassVar(o));
				return result;
			} else if (rdfd1.isInObjectPropertySet(o) && !(rdfd2.isInObjectPropertySet(o))) {
				// if (!(rdfd2.isInObjectPropertySet(o))) {
				result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(o));
				// System.out.println("[QTTree::generalize] The Sub is an Object Property URI");
				return result;
			} else if (rdfd1.isInDatatypePropertySet(o) && !(rdfd2.isInDatatypePropertySet(o))) {
				// if (!(rdfd2.isInDatatypePropertySet(o))) {

				result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(o));
				// System.out.println("[QTTree::generalize] The Sub is an datatype Property
				// URI");
				return result;
			} else if (rdfd1.isInRDFVocabulary(o) && !(rdfd2.isInRDFVocabulary(o))) {
				// if (!(rdfd2.isInRDFVocabulary(o))) {

				result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(o));
				// System.out.println("[QTTree::generalize] The Sub is an RDF voc term URI");
				return result;
			} else {
				// this means that it is an individual
				result = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(o));
				return result;
			}
		} else if (obj.isLiteral()) {
			String subjAsString = obj.getLiteralValue().toString();
			result = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(subjAsString));
			return result;
		} else {
			// subject = tp.getSubject();
			result = (Var) obj;
			return result;
		}
		// return result;

	}

	private Var ifPredicateIsNotD2ThenGenerateVariable(Node pred) {

		if (pred == null || rdfd2 == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
		}
		final Var result;

		if (pred.isURI()) {
			// s= classURI
			String pre = pred.getURI();
			// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
			// The predicate is "
			// + pre);

			// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
			// rdfd1.isInDatatypePropertySet(pre) "
			// + rdfd1.isInDatatypePropertySet(pre));
			// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
			// rdfd1 datatype property List "
			// + rdfd1.getDatatypePropertySet());
			// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
			// rdfd1.isInObjectPropertySet(pre) "
			// + rdfd1.isInObjectPropertySet(pre));
			if (rdfd1.isInObjectPropertySet(pre) && !(rdfd2.isInObjectPropertySet(pre))) {
				// if (!(rdfd2.isInObjectPropertySet(pre)) && !(rdfd2.isInRDFVocabulary(pre)) &&
				// !(rdfd1.isInDatatypePropertySet(pre))) {
				result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(pre));
				return result;
			} else if (rdfd1.isInDatatypePropertySet(pre) && !(rdfd2.isInDatatypePropertySet(pre))) {
				// {
				// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
				// rdfd2.isInDatatypePropertySet(pre) "
				// + rdfd2.isInDatatypePropertySet(pre));
				// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
				// rdfd2 datatype property List "
				// + rdfd2.getDatatypePropertySet());
				// System.out.println("[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable]
				// rdfd2.isInObjectPropertySet(pre) "
				// + rdfd1.isInObjectPropertySet(pre));

				result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(pre));
				return result;

				// if (!(rdfd2.isInDatatypePropertySet(pre)) && !(rdfd2.isInRDFVocabulary(pre)))
				// {
				// result =
				// Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(pre));
				// return result;
				// }
			} else if (rdfd1.isInRDFVocabulary(pre) && !(rdfd2.isInRDFVocabulary(pre))) {
				// if ((rdfd2.isInRDFVocabulary(pre))) {
				result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(pre));
				return result;
			} else {
				// System.out.println("[QTTree::ifPredicateIsNotD2ThenGenerateVariable(Node
				// subj)] The predicate is an URI 22222222 "
				// + pred);
				result = null;
				return result;
			}
			// }
		} else {
			// it means that it is a variable
			// System.out.println("[QTTree::generalize] None of the cases was satisfied for
			// the predicate node "
			// + tp.getPredicate().toString());
			result = (Var) pred;
			return result;
		}

	}

	private Var ifPredicateIsNotD2ThenGenerateVariableNew(Node pred) {

		if (pred == null || rdfd2 == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::ifPredicateIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
		}
		final Var result;

		if (pred.isURI()) {
			String pre = pred.getURI();
			if (rdfd1.isInObjectPropertySet(pre) && !(rdfd2.isInObjectPropertySet(pre))) {
				result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(pre));
				return result;
			} else if (rdfd1.isInDatatypePropertySet(pre) && !(rdfd2.isInDatatypePropertySet(pre))) {
				result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(pre));
				return result;
			} // else if (rdfd1.isInRDFVocabulary(pre) && !(rdfd2.isInRDFVocabulary(pre))) {
				// result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(pre));
				// return result;
				// }
			else {
				result = null;
				return result;
			}
		} else {
			result = (Var) pred;
			return result;
		}

	}

	private Var ifSubjectIsNotD2ThenGenerateVariable(Node subj) {
		if (subj == null || rdfd2 == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::ifSubjectIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
		}
		final Var result;
		// SUBJECT
		if (subj.isURI()) {
			// s= classURI
			String sub = subj.getURI();
			// System.out.println("[QTTree::generalize] The Sub is an URI " + subj);
			if ((rdfd1.getClassSet().contains(subj)) && !(rdfd2.getClassSet().contains(subj))) {
				// if (!rdfd2.getClassSet().contains(o)) {

				result = Var.alloc(classVarTable.generateIFAbsentClassVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is a class URI");
				return result;
			} else if (rdfd1.isInIndividualSet(sub) && !(rdfd2.isInIndividualSet(sub))) {
				// if (!(rdfd2.isInIndividualSet(o))) {
				result = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an individual URI");
				return result;
			} else if (rdfd1.isInObjectPropertySet(sub) && !(rdfd2.isInObjectPropertySet(sub))) {
				// if (!(rdfd2.isInObjectPropertySet(o))) {
				result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an Object Property URI");
				return result;
			} else if (rdfd1.isInDatatypePropertySet(sub) && !(rdfd2.isInDatatypePropertySet(sub))) {
				// if (!(rdfd2.isInDatatypePropertySet(o))) {
				result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an datatype Property
				// URI");
				return result;
			} else if (rdfd1.isInRDFVocabulary(sub) && !(rdfd2.isInRDFVocabulary(sub))) {
				// if (!(rdfd2.isInRDFVocabulary(o))) {
				result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an RDF voc term URI");
				return result;
			} else {
				// subject = tp.getSubject();
				result = null;
				return result;
			}
		} else if (subj.isLiteral()) {
			String subjAsString = subj.getLiteralValue().toString();
			if (rdfd1.isInLiteralSet(subjAsString) && !(rdfd2.isInLiteralSet(subjAsString))) {
				result = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(subjAsString));
				return result;
			} else {
				// subject = tp.getSubject();
				result = null;
				return result;
			}
		} else {
			// subject = tp.getSubject();
			result = (Var) subj;
			return result;
		}
		// return result;
	}

	private Var ifSubjectIsNotD2ThenGenerateVariableNew(Node subj) {
		if (subj == null || rdfd2 == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::ifSubjectIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
		}
		final Var result;
		// SUBJECT
		if (subj.isURI()) {
			// s= classURI
			String sub = subj.getURI();
			// System.out.println("[QTTree::generalize] The Sub is an URI " + subj);
			if ((rdfd1.getClassSet().contains(subj)) && !(rdfd2.getClassSet().contains(subj))) {
				result = Var.alloc(classVarTable.generateIFAbsentClassVar(sub));
				return result;
			} else if (rdfd1.isInObjectPropertySet(sub) && !(rdfd2.isInObjectPropertySet(sub))) {
				// if (!(rdfd2.isInObjectPropertySet(o))) {
				result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an Object Property URI");
				return result;
			} else if (rdfd1.isInDatatypePropertySet(sub) && !(rdfd2.isInDatatypePropertySet(sub))) {
				// if (!(rdfd2.isInDatatypePropertySet(o))) {
				result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an datatype Property
				// URI");
				return result;
			} else if (rdfd1.isInRDFVocabulary(sub) && !(rdfd2.isInRDFVocabulary(sub))) {
				// if (!(rdfd2.isInRDFVocabulary(o))) {
				result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(sub));
				// System.out.println("[QTTree::generalize] The Sub is an RDF voc term URI");
				return result;
			} else {
				// this means that it is an individual
				result = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(sub));
				return result;
			}
		} else if (subj.isLiteral()) {
			String subjAsString = subj.getLiteralValue().toString();
			result = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(subjAsString));
			return result;
		} else {
			// subject = tp.getSubject();
			result = (Var) subj;
			return result;
		}
		// return result;
	}

	private boolean isClassTemplateVariable(String name) {
		return name.startsWith(CLASS_TEMPLATE_VAR); // To change body of generated methods, choose Tools |
													// Templates.
	}

	private boolean isIndividualTemplateVariable(String name) {
		return name.startsWith(INDIVIDUAL_TEMPLATE_VAR); // To change body of generated methods, choose Tools
															// | Templates.
	}

	private boolean isInTreeNodeIndex(TreeNode<Query> currNode) {

		Query q = currNode.getData();

		// System.out.println("[QueryRecommendation:: private void specialize(...)] "
		// + "////////////////////////////////// isInTreeNodeIndex(childNode)) ====
		// Query //////////////");
		// System.out.println(q.toString());
		List<TriplePath> triplePathCollection = this.getTriplePathSet(q);

		// System.out.println("[QueryRecommendation:: private void specialize(...)] "
		// +
		// "////////////////////////////////// isInTreeNodeIndex(childNode)) ====
		// triplePathCollection //////////////");
		// System.out.println(triplePathCollection.toString());
		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}

		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println("isInTreeNodeIndex ==== BEFORE SORTING == " +
		// s.toString());
		Collections.sort(s);

		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println(" ");
		// System.out.println("isInTreeNodeIndex ==== AFTER SORTED == " + s.toString());
		return treeNodeIndex.containsKey(s.toString());
	}

	private boolean isInTreeNodeIndex1(TreeNode<DataNode> childNode) {
		DataNode data = childNode.getData();
		Query q = data.getqR();// currNode.getData();
		List<TriplePath> triplePathCollection = this.getTriplePathSet(q);

		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}

		Collections.sort(s);
		return treeNodeIndex.containsKey(s.toString());
	}

	private boolean isLiteralTemplateVariable(String name) {
		return name.startsWith(LITERAL_TEMPLATE_VAR); // To change body of generated methods, choose Tools |
														// Templates.
	}

	private boolean isPropertyTemplateVariable(String name) {
		return name.startsWith(OBJ_PROP_TEMPLATE_VAR) || name.startsWith(DT_PROP_TEMPLATE_VAR);
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
		} else if (individualVarTable.getVarIndividualURITable().containsKey(entity)) {
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

	public void printQueryTemplateTree(TreeNode<T> n, int i) {
		if (n == null) {
			throw new IllegalStateException("[QueryRecommendation,printQueryTemplateTree]The tree is empty!!");
		}

		for (int j = 0; j < i; j++) {
			System.out.print("  ");
		}

		if (n.getData() != null) {
			System.out.println("[QueryRecommendation,printQueryTemplateTree]" + n.getData().toString());
		}
		for (TreeNode<T> node : n.getChildren()) {
			printQueryTemplateTree(node, i + 1);
		}

	}

	public void printQueryTemplateTree1(TreeNode<T> n, int i) {
		if (n == null) {
			throw new IllegalStateException("[QueryRecommendation,printQueryTemplateTree]The tree is empty!!");
		}
		for (int j = 0; j < i; j++) {
			System.out.print("  ");
		}
		if (n.getData() != null) {
			// System.out.println("");
			// System.out.println("");

			// System.out.println("");
			// System.out.println("[QueryRecommendation,printQueryTemplateTree] Suggested
			// Query " +
			// ((DataNode) n.getData()).getqR().toString()); System.out.println("");
			//
			// System.out.println("[QueryRecommendation,printQueryTemplateTree] Operation
			// List " + ((DataNode)
			// n.getData()).operationList.toString());
			// System.out.println("");
			//
			// System.out.println("[QueryRecommendation,printQueryTemplateTree] EntityqO " +
			// ((DataNode)
			// n.getData()).getEntityqO().toString());
			// System.out.println("");
			//
			// System.out.println("[QueryRecommendation,printQueryTemplateTree] EntityqR " +
			// ((DataNode)
			// n.getData()).getEntityqR().toString());
			// TO DO.
			SPARQLQuerySatisfiable qsat = new SPARQLQuerySatisfiable();
			Query qRec = ((DataNode) n.getData()).getqR();

			boolean querySat = qsat.isSatisfiable(qRec, this.rdfd2);
			System.out.println("[QueryRecommendation,printQueryTemplateTree1] querySat " + querySat);

			if (querySat) {
				System.out.println("");
				System.out.println("[QueryRecommendation,printQueryTemplateTree1] Suggested Query ");
				System.out.println("");
				System.out.println(((DataNode) n.getData()).getqR().toString());

				// DISTANCE QUERY ROOT
				float distQueryRoot = ((DataNode) n.getData()).getNodeCost();
				System.out.println("[QueryRecommendation,printQueryTemplateTree1] DISTANCE QUERY ROOT "
						+ ((DataNode) n.getData()).getNodeCost());
				// System.out.println("");

				// QUERY RESULT TYPE SIMILARITY
				QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
				float resulttypeSim = qRTS.computeQueryResultTypeDistance(((DataNode) n.getData()).qO, this.rdfd1,
						((DataNode) n.getData()).qR, this.rdfd2);
				System.out.println(
						"[QueryRecommendation,printQueryTemplateTree1] QUERY RESULT TYPE SIMILARITY " + resulttypeSim);
				// System.out.println("");

				// QUERY RESULT SIZE DISTANCE
				// QueryResultSizeDistance qRSD= new QueryResultSizeDistance();
				// float queryResultSizeSim=qRSD.computeQRSSim(((DataNode) n.getData()).qO,
				// this.rdfd1,
				// ((DataNode) n.getData()).qR, this.rdfd2);
				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] QUERY
				// RESULT SIZE DISTANCE "
				// +queryResultSizeSim);
				// System.out.println("");
				// TOTAL
				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] TOTAL "
				// +(queryRootDist+sim+queryResultSizeSim));
				// System.out.println("[QueryRecommendation,printQueryTemplateTree1] TOTAL "
				// +(queryRootDist+queryResultSizeSim));
				System.out.println(
						"[QueryRecommendation,printQueryTemplateTree1] TOTAL " + (distQueryRoot + resulttypeSim));
				System.out.println("");

			}

		}
		for (TreeNode node : n.getChildren()) {
			printQueryTemplateTree1(node, i + 1);
		}

	}

	private void specialize(TreeNode<T> parentNode, Collection<String> parentPL, Collection<String> parentCL,
			Collection<String> parentIL, Collection<String> parentLitL) {
		if (parentNode == null) {
			throw new IllegalStateException("[QueryRecommendation::specialize()]A ParentNode is null!!");
		}
		if (parentPL == null && parentCL == null && parentIL == null && parentLitL == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize()]parentPL == null && parentCL == null && parentIL == null && parentLitL == null!!");
		}
		if (parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize()]parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()!!");
		}

		// System.out.println("[QueryRecommendation, private void specialize(...)] We
		// are specializing the input query ");
		Query parentQuery = (Query) parentNode.getData();
		List<TriplePath> tpSet = (List<TriplePath>) getTriplePathSet(parentQuery);

		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		// System.out.println("[QueryRecommendation, private void specialize(...)] The
		// set of triple pattters ");
		// System.out.println(tpSet.toString());

		for (TriplePath tp : tpSet) {
			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				// System.out.println("[QueryRecommendation:: private void specialize(...)] The
				// triple patter is isTemplateVariableFree.");
				return;
			}

			// SUBJECT
			if (tp.getSubject().isVariable()) {
				if (isTemplateVariable(tp.getSubject().getName())) {
					String templateVarString = tp.getSubject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					// if (isClassTemplateVariable(tp.getSubject().getName())) {
					if (isClassTemplateVariable(templateVarString)) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							childCL.remove(clas);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode;
							childNode = new TreeNode(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode = new TreeNode<>(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode = new TreeNode<>(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());
							// ///////////////////////////////////////////////////////////////// THIS IS THE
							// POINT WHERE I MAKE THE INSTANTIATION
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode;
							childNode = new TreeNode<>(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isPropertyTemplateVariable
				} // if isTemplateVariable
			} // if subject

			// It means that there is at least one of the subj, pred or obj that is a
			// Template Variable:
			// PREDICATE
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVarString = tp.getPredicate().getName();

					// System.out.println("[QueryRecommendation:: private void specialize(...)] "
					// + "The predicate is a Template Variable that need to be instanciated");
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
					// It means that we need to instancited for all the properties of parentPL.
					for (String property : parentPL) {

						// System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						Var templateVar = Var.alloc(templateVarString);

						// Step 1: Auxiliary Structures
						ArrayList<String> childPL = new ArrayList<String>();
						childPL.addAll(parentPL);
						childPL.remove(property);

						ArrayList<String> childCL = new ArrayList<String>();
						childCL.addAll(parentCL);
						ArrayList<String> childIL = new ArrayList<String>();
						childIL.addAll(parentIL);
						ArrayList<String> chilLitL = new ArrayList<String>();
						chilLitL.addAll(parentLitL);

						// Step 2: Create a childQuery
						Query childQuery = QueryFactory.create(parentQuery.toString());
						// childQuery = parentQuery; parentQuery

						// System.out.println(" ");
						// System.out.println(" ");
						// System.out.println(" ");
						// System.out.println(" ");
						// System.out.println("childQuery ====BEFORE== " + childQuery.toString());
						// Step 3: Instantiate From VarTemplate To Node
						// Query childQueryInstantiated
						// = qi.instantiateVarTemplate(childQuery, templateVar,
						// NodeFactory.createURI(property));
						// System.out.println("PROPERTY::== " + property);
						// System.out.println("templateVar::== " + templateVar.getName());
						childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
								NodeFactory.createURI(property));

						// System.out.println(" ");
						// System.out.println(" ");
						// System.out.println(" ");
						// System.out.println(" ");
						// System.out.println("childQuery ====AFTER== " + childQuery.toString());
						// Step 4: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						TreeNode<Query> childNode;
						childNode = new TreeNode(childQuery, null);
						// Step 5: recall the function on the child;
						// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						if (!(isInTreeNodeIndex(childNode))) {
							// System.out.println("[QueryRecommendation:: private void specialize(...)] "
							// +
							// "////////////////////////////////// if (!(isInTreeNodeIndex(childNode)))
							// //////////////");
							parentNode.addChild((TreeNode<T>) childNode);
							addToNodeTreeIndexIFAbsent(childNode);
						}
						// Step 5: recall the function on the child;

						// System.out.println("childPL ====================== " + childPL.toString());
						// System.out.println("childCL ====================== " + childCL.toString());
						// System.out.println("childIL ====================== " + childIL.toString());
						// System.out.println("chilLitL ====================== " + chilLitL.toString());
						specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
					}
				}
			} // if predicate

			// OBJECT
			if (tp.getObject().isVariable()) {
				if (isTemplateVariable(tp.getObject().getName())) {
					String templateVarString = tp.getObject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					if (isClassTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							childCL.remove(clas);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode;
							childNode = new TreeNode(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode;
							childNode = new TreeNode(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode;
							childNode = new TreeNode<>(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							List<String> childIL = new ArrayList<>();
							childIL.addAll(parentIL);
							List<String> chilLitL = new ArrayList<>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.
							TreeNode<Query> childNode;
							childNode = new TreeNode<>(childQuery, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent(childNode);
							}
							// Step 5: recall the function on the child;
							specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isPropertyTemplateVariable

				} // if isTemplateVariable

			} // if object

		} // for (TriplePath tp : tpSet)

	}

	// this works without remove operation;
	private void specialize1(TreeNode<T> parentNode, List<String> parentPL, List<String> parentCL,
			List<String> parentIL, List<String> parentLitL) {
		if (parentNode == null) {
			throw new IllegalStateException("[QueryRecommendation::specialize()]A ParentNode is null!!");
		}
		if (parentPL == null && parentCL == null && parentIL == null && parentLitL == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize()]parentPL == null && parentCL == null && parentIL == null && parentLitL == null!!");
		}
		if (parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize()]parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()!!");
		}

		// System.out.println("[QueryRecommendation, private void specialize(...)] We
		// are specializing the input query ");
		DataNode pNode = (DataNode) parentNode.getData();

		Query parentQuery = (Query) pNode.getqR();// .getData();

		List<TriplePath> tpSet = (List<TriplePath>) getTriplePathSet(parentQuery);
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}

		for (TriplePath tp : tpSet) {
			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				// System.out.println("[QueryRecommendation:: private void specialize(...)] The
				// triple patter is isTemplateVariableFree.");
				return;
			}

			// SUBJECT
			if (tp.getSubject().isVariable()) {
				if (isTemplateVariable(tp.getSubject().getName())) {
					String templateVarString = tp.getSubject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					// if (isClassTemplateVariable(tp.getSubject().getName())) {
					if (isClassTemplateVariable(templateVarString)) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							Var templateVar = Var.alloc(templateVarString);
							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							childCL.remove(clas);
							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							List<String> childIL = new ArrayList<>();
							childIL.addAll(parentIL);
							List<String> chilLitL = new ArrayList<>();
							chilLitL.addAll(parentLitL);
							Query childQuery = QueryFactory.create(parentQuery.toString());
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));
							String entityqO = this.classVarTable.getClassFromVar(templateVar.getVarName());
							String entityqR = clas;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);
							TreeNode<DataNode> childNode = new TreeNode(cNode, null);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.individualVarTable.getIndividualFromVar(templateVarString);
							String entityqR = individual;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;
							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
							String entityqR = literal;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());
							// ///////////////////////////////////////////////////////////////// THIS IS THE
							// POINT WHERE I MAKE THE INSTANTIATION
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = "";
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode<>(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isPropertyTemplateVariable
				} // if isTemplateVariable
			} // if subject

			// It means that there is at least one of the subj, pred or obj that is a
			// Template Variable:
			// PREDICATE
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVarString = tp.getPredicate().getName();

					// System.out.println("[QueryRecommendation:: private void specialize(...)] "
					// + "The predicate is a Template Variable that need to be instanciated");
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
					// It means that we need to instancited for all the properties of parentPL.
					for (String property : parentPL) {

						// System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						Var templateVar = Var.alloc(templateVarString);

						// Step 1: Auxiliary Structures
						List<String> childPL = new ArrayList<>();
						childPL.addAll(parentPL);
						childPL.remove(property);

						List<String> childCL = new ArrayList<>();
						childCL.addAll(parentCL);
						List<String> childIL = new ArrayList<>();
						childIL.addAll(parentIL);
						List<String> chilLitL = new ArrayList<>();
						chilLitL.addAll(parentLitL);

						// Step 2: Create a childQuery
						Query childQuery = QueryFactory.create(parentQuery.toString());
						// childQuery = parentQuery; parentQuery

						childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
								NodeFactory.createURI(property));

						String entityqO = null;
						if (templateVarString.contains("opt")) {
							entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
						}
						if (templateVarString.contains("dpt")) {
							entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
						}
						String entityqR = property;
						List<String> childOperationList = new ArrayList<>();

						// System.out.println("pNode.getOperationList()111111111111111
						// "+pNode.getOperationList());
						childOperationList.addAll(pNode.getOperationList());
						childOperationList.add(INSTANCE_OP);
						String op = INSTANCE_OP;

						// Add the code to compute the "nodeCost"
						String entityqO_TMP = getLocalName(entityqO);
						String entityqR_TMP = getLocalName(entityqR);
						float nodeCost = pNode.getNodeCost()
								+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

						DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
								childOperationList, op, nodeCost);

						TreeNode<DataNode> childNode = new TreeNode<>(cNode, null);

						// Step 5: recall the function on the child;
						// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						if (!(isInTreeNodeIndex1(childNode))) {
							// System.out.println("[QueryRecommendation:: private void specialize(...)] "
							// +
							// "////////////////////////////////// if (!(isInTreeNodeIndex(childNode)))
							// //////////////");
							parentNode.addChild((TreeNode<T>) childNode);
							addToNodeTreeIndexIFAbsent1(childNode);
						}
						specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
					}
				}
			} // if predicate

			// OBJECT
			if (tp.getObject().isVariable()) {
				if (isTemplateVariable(tp.getObject().getName())) {
					String templateVarString = tp.getObject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					if (isClassTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							childCL.remove(clas);

							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							List<String> childIL = new ArrayList<>();
							childIL.addAll(parentIL);
							List<String> chilLitL = new ArrayList<>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));

							String entityqO = this.classVarTable.getClassFromVar(templateVarString);
							String entityqR = clas;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode<>(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							List<String> childIL = new ArrayList<>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							List<String> chilLitL = new ArrayList<>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.individualVarTable.getIndividualFromVar(templateVarString);
							String entityqR = individual;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
							String entityqR = literal;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = null;
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode<>(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize1((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isPropertyTemplateVariable

				} // if isTemplateVariable

			} // if object

		} // for (TriplePath tp : tpSet)

	}

	// Working to include also remove operation
	private void specialize2(TreeNode<T> parentNode, List<String> parentPL, List<String> parentCL,
			List<String> parentIL, List<String> parentLitL) {
		if (parentNode == null) {
			throw new IllegalStateException("[QueryRecommendation::specialize()]A ParentNode is null!!");
		}
		if (parentPL == null && parentCL == null && parentIL == null && parentLitL == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize()]parentPL == null && parentCL == null && parentIL == null && parentLitL == null!!");
		}
		if (parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize()]parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()!!");
		}

		// System.out.println("[QueryRecommendation, private void specialize(...)] We
		// are specializing the input query ");
		DataNode pNode = (DataNode) parentNode.getData();

		Query parentQuery = (Query) pNode.getqR();// .getData();
		List<TriplePath> tpSet = (List<TriplePath>) getTriplePathSet(parentQuery);
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}

		for (TriplePath tp : tpSet) {

			System.err.println(tpSet.size());

			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				// System.out.println("[QueryRecommendation:: private void specialize(...)] The
				// triple patter is isTemplateVariableFree.");
				// return;
				continue;
			}

			// // Applying the Remove operation
			// if (tpSet.size() > 1) {
			// Query parentQueryCopy=parentQuery;
			// RemoveTriple instance = new RemoveTriple();
			// Query childQuery = instance.removeTP(parentQueryCopy, tp.asTriple());
			//
			// ArrayList<String> childOperationList = new ArrayList();
			// childOperationList.addAll(pNode.getOperationList());
			// childOperationList.add(REMOVE_TP_OP);
			// String op = REMOVE_TP_OP;
			//
			// ArrayList<String> childCL = new ArrayList<String>();
			// childCL.addAll(parentCL);
			// ArrayList<String> childPL = new ArrayList<String>();
			// childPL.addAll(parentPL);
			// ArrayList<String> childIL = new ArrayList<String>();
			// childIL.addAll(parentIL);
			// ArrayList<String> chilLitL = new ArrayList<String>();
			// chilLitL.addAll(parentLitL);
			//
			// //Add the code to compute the "nodeCost"
			// String entityqO_TMP = "";//getLocalName(entityqO);
			// String entityqR_TMP = "";//getLocalName(entityqR);
			//
			// float nodeCost = pNode.getNodeCost() +
			// computeRemoveOperationCost(this.originalQuery,
			// childQuery);
			//
			// DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO_TMP,
			// entityqR_TMP,
			// childOperationList, op, nodeCost);
			// TreeNode<DataNode> childNode = new TreeNode(cNode, null);
			// if (!(isInTreeNodeIndex1(childNode))) {
			// parentNode.addChild((TreeNode<T>) childNode);
			// addToNodeTreeIndexIFAbsent1(childNode);
			// }
			// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
			// }
			//
			// SUBJECT
			if (tp.getSubject().isVariable()) {
				if (isTemplateVariable(tp.getSubject().getName())) {
					String templateVarString = tp.getSubject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					// if (isClassTemplateVariable(tp.getSubject().getName())) {
					if (isClassTemplateVariable(templateVarString)) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							Var templateVar = Var.alloc(templateVarString);
							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							childCL.remove(clas);
							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							List<String> childIL = new ArrayList<>();
							childIL.addAll(parentIL);
							List<String> chilLitL = new ArrayList<>();
							chilLitL.addAll(parentLitL);
							Query childQuery = QueryFactory.create(parentQuery.toString());
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));
							String entityqO = this.classVarTable.getClassFromVar(templateVar.getVarName());
							String entityqR = clas;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);
							TreeNode<DataNode> childNode = new TreeNode<>(cNode, null);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.individualVarTable.getIndividualFromVar(templateVarString);
							String entityqR = individual;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;
							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
							String entityqR = literal;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());
							// ///////////////////////////////////////////////////////////////// THIS IS THE
							// POINT WHERE I MAKE THE INSTANTIATION
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = "";
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode<>(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isPropertyTemplateVariable
				} // if isTemplateVariable
			} // if subject

			// It means that there is at least one of the subj, pred or obj that is a
			// Template Variable:
			// PREDICATE
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVarString = tp.getPredicate().getName();

					// System.out.println("[QueryRecommendation:: private void specialize(...)] "
					// + "The predicate is a Template Variable that need to be instanciated");
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
					// It means that we need to instancited for all the properties of parentPL.
					for (String property : parentPL) {

						// System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						Var templateVar = Var.alloc(templateVarString);

						// Step 1: Auxiliary Structures
						List<String> childPL = new ArrayList<>();
						childPL.addAll(parentPL);
						childPL.remove(property);

						List<String> childCL = new ArrayList<>();
						childCL.addAll(parentCL);
						List<String> childIL = new ArrayList<>();
						childIL.addAll(parentIL);
						List<String> chilLitL = new ArrayList<>();
						chilLitL.addAll(parentLitL);

						// Step 2: Create a childQuery
						Query childQuery = QueryFactory.create(parentQuery.toString());
						// childQuery = parentQuery; parentQuery

						childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
								NodeFactory.createURI(property));

						String entityqO = null;
						if (templateVarString.contains("opt")) {
							entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
						}
						if (templateVarString.contains("dpt")) {
							entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
						}
						String entityqR = property;
						List<String> childOperationList = new ArrayList<>();

						// System.out.println("pNode.getOperationList()111111111111111
						// "+pNode.getOperationList());
						childOperationList.addAll(pNode.getOperationList());
						childOperationList.add(INSTANCE_OP);
						String op = INSTANCE_OP;

						// Add the code to compute the "nodeCost"
						String entityqO_TMP = getLocalName(entityqO);
						String entityqR_TMP = getLocalName(entityqR);
						float nodeCost = pNode.getNodeCost()
								+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

						DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
								childOperationList, op, nodeCost);

						TreeNode<DataNode> childNode;
						childNode = new TreeNode<>(cNode, null);

						// Step 5: recall the function on the child;
						// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						if (!(isInTreeNodeIndex1(childNode))) {
							// System.out.println("[QueryRecommendation:: private void specialize(...)] "
							// +
							// "////////////////////////////////// if (!(isInTreeNodeIndex(childNode)))
							// //////////////");
							parentNode.addChild((TreeNode<T>) childNode);
							addToNodeTreeIndexIFAbsent1(childNode);
						}
						specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
					}
				}
			} // if predicate

			// OBJECT
			if (tp.getObject().isVariable()) {
				if (isTemplateVariable(tp.getObject().getName())) {
					String templateVarString = tp.getObject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					if (isClassTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							List<String> childCL = new ArrayList<>();
							childCL.addAll(parentCL);
							childCL.remove(clas);

							List<String> childPL = new ArrayList<>();
							childPL.addAll(parentPL);
							List<String> childIL = new ArrayList<>();
							childIL.addAll(parentIL);
							List<String> chilLitL = new ArrayList<>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));

							String entityqO = this.classVarTable.getClassFromVar(templateVarString);
							String entityqR = clas;
							List<String> childOperationList = new ArrayList<>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode<>(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.individualVarTable.getIndividualFromVar(templateVarString);
							String entityqR = individual;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
							String entityqR = literal;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = null;
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
							}
							// Step 5: recall the function on the child;
							specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isPropertyTemplateVariable

				} // if isTemplateVariable

			} // if object

		} // for (TriplePath tp : tpSet)

	}

	// Working to include also remove operation
	private void specialize3(TreeNode<T> parentNode, Collection<String> parentPL, Collection<String> parentCL,
			Collection<String> parentIL, Collection<String> parentLitL) {
		if (parentNode == null) {
			throw new IllegalStateException("[QueryRecommendation::specialize3()]A ParentNode is null!!");
		}
		if (parentPL == null && parentCL == null && parentIL == null && parentLitL == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize3()]parentPL == null && parentCL == null && parentIL == null && parentLitL == null!!");
		}
		if (parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize3()]parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()!!");
		}

		DataNode pNode = (DataNode) parentNode.getData();
		Query parentQuery = (Query) pNode.getqR();// .getData();

		// System.out.println("[QueryRecommendation::specialize3()]
		// parentQuery"+parentQuery.toString());

		List<TriplePath> tpSet = (List<TriplePath>) getTriplePathSet(parentQuery);
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		// APPLYING THE INSTANTIATION
		for (TriplePath tp : tpSet) {
			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				continue;
			}
			// SUBJECT
			if (tp.getSubject().isVariable()) {
				if (isTemplateVariable(tp.getSubject().getName())) {
					String templateVarString = tp.getSubject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
					// if (isClassTemplateVariable(tp.getSubject().getName())) {
					if (isClassTemplateVariable(templateVarString)) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							Var templateVar = Var.alloc(templateVarString);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							childCL.remove(clas);
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							Query childQuery = QueryFactory.create(parentQuery.toString());
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));
							String entityqO = this.classVarTable.getClassFromVar(templateVar.getVarName());
							String entityqR = clas;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);
							TreeNode<DataNode> childNode = new TreeNode(cNode, null);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}

						}
					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.individualVarTable.getIndividualFromVar(templateVarString);
							String entityqR = individual;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;
							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
							String entityqR = literal;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());
							// ///////////////////////////////////////////////////////////////// THIS IS THE
							// POINT WHERE I MAKE THE INSTANTIATION
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = "";
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isPropertyTemplateVariable
				} // if isTemplateVariable
			} // if subject

			// It means that there is at least one of the subj, pred or obj that is a
			// Template Variable:
			// PREDICATE
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVarString = tp.getPredicate().getName();

					// System.out.println("[QueryRecommendation:: private void specialize(...)] "
					// + "The predicate is a Template Variable that need to be instanciated");
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
					// It means that we need to instancited for all the properties of parentPL.
					for (String property : parentPL) {
						// System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						Var templateVar = Var.alloc(templateVarString);

						// Step 1: Auxiliary Structures
						ArrayList<String> childPL = new ArrayList<String>();
						childPL.addAll(parentPL);
						childPL.remove(property);

						ArrayList<String> childCL = new ArrayList<String>();
						childCL.addAll(parentCL);
						ArrayList<String> childIL = new ArrayList<String>();
						childIL.addAll(parentIL);
						ArrayList<String> chilLitL = new ArrayList<String>();
						chilLitL.addAll(parentLitL);

						// Step 2: Create a childQuery
						Query childQuery = QueryFactory.create(parentQuery.toString());
						// childQuery = parentQuery; parentQuery

						childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
								NodeFactory.createURI(property));

						String entityqO = null;
						if (templateVarString.contains("opt")) {
							entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
						}
						if (templateVarString.contains("dpt")) {
							entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
						}
						String entityqR = property;
						ArrayList<String> childOperationList = new ArrayList();

						// System.out.println("pNode.getOperationList()111111111111111
						// "+pNode.getOperationList());
						childOperationList.addAll(pNode.getOperationList());
						childOperationList.add(INSTANCE_OP);
						String op = INSTANCE_OP;

						// Add the code to compute the "nodeCost"
						String entityqO_TMP = getLocalName(entityqO);
						String entityqR_TMP = getLocalName(entityqR);
						float nodeCost = pNode.getNodeCost()
								+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
						// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
						// "+this.originalQuery);
						DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
								childOperationList, op, nodeCost);

						TreeNode<DataNode> childNode;
						childNode = new TreeNode(cNode, null);

						// Step 5: recall the function on the child;
						// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						if (!(isInTreeNodeIndex1(childNode))) {
							// System.out.println("[QueryRecommendation:: private void specialize(...)] "
							// +
							// "////////////////////////////////// if (!(isInTreeNodeIndex(childNode)))
							// //////////////");
							parentNode.addChild((TreeNode<T>) childNode);
							addToNodeTreeIndexIFAbsent1(childNode);
							specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
						// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
					}
				}
			} // if predicate

			// OBJECT
			if (tp.getObject().isVariable()) {
				if (isTemplateVariable(tp.getObject().getName())) {
					String templateVarString = tp.getObject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					if (isClassTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childCL = new ArrayList();
							childCL.addAll(parentCL);
							childCL.remove(clas);

							ArrayList<String> childPL = new ArrayList();
							childPL.addAll(parentPL);
							ArrayList<String> childIL = new ArrayList();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));

							String entityqO = this.classVarTable.getClassFromVar(templateVarString);
							String entityqR = clas;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isClassTemplateVariable
					else if (isIndividualTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String individual : parentIL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							childIL.remove(individual);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(individual));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.individualVarTable.getIndividualFromVar(templateVarString);
							String entityqR = individual;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isIndividualTemplateVariable
					else if (isLiteralTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String literal : parentLitL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							chilLitL.remove(literal);

							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(literal));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
							String entityqR = literal;
							ArrayList<String> childOperationList = new ArrayList<String>();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);
							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = null;
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isPropertyTemplateVariable

				} // if isTemplateVariable

			} // if object

			// APPLYING THE REMOVAL OPERATION
			if (tpSet.size() > 1) {
				Query parentQueryCopy = parentQuery;
				RemoveTriple instance = new RemoveTriple();
				Query childQuery = instance.removeTP(parentQuery, tp.asTriple());

				ArrayList<String> childOperationList = new ArrayList();
				childOperationList.addAll(pNode.getOperationList());
				childOperationList.add(REMOVE_TP_OP);
				String op = REMOVE_TP_OP;

				ArrayList<String> childCL = new ArrayList<String>();
				childCL.addAll(parentCL);
				ArrayList<String> childPL = new ArrayList<String>();
				childPL.addAll(parentPL);
				ArrayList<String> childIL = new ArrayList<String>();
				childIL.addAll(parentIL);
				ArrayList<String> chilLitL = new ArrayList<String>();
				chilLitL.addAll(parentLitL);

				// Add the code to compute the "nodeCost"
				String entityqO_TMP = "";// getLocalName(entityqO);
				String entityqR_TMP = "";// getLocalName(entityqR);

				float nodeCost = pNode.getNodeCost() + computeRemoveOperationCost(this.originalQuery, childQuery);

				// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
				// "+this.originalQuery);
				DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO_TMP, entityqR_TMP,
						childOperationList, op, nodeCost);
				TreeNode<DataNode> childNode = new TreeNode(cNode, null);
				if (!(isInTreeNodeIndex1(childNode))) {
					parentNode.addChild((TreeNode<T>) childNode);
					addToNodeTreeIndexIFAbsent1(childNode);
					// specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
				}
			}

		} // for (TriplePath tp : tpSet)

	}

	// Working to include also remove operation. specialize4 = specialize3
	private void specialize4(TreeNode<T> parentNode, ArrayList<String> parentPL, ArrayList<String> parentCL,
			ArrayList<String> parentIL, ArrayList<String> parentLitL) {
		if (parentNode == null) {
			throw new IllegalStateException("[QueryRecommendation::specialize3()]A ParentNode is null!!");
		}
		if (parentPL == null && parentCL == null && parentIL == null && parentLitL == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize3()]parentPL == null && parentCL == null && parentIL == null && parentLitL == null!!");
		}
		if (parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()) {
			throw new IllegalStateException(
					"[QueryRecommendation::specialize3()]parentPL.isEmpty() && parentCL.isEmpty() && parentIL.isEmpty() && parentLitL.isEmpty()!!");
		}
		DataNode pNode = (DataNode) parentNode.getData();
		Query parentQuery = (Query) pNode.getqR();// .getData();
		List<TriplePath> tpSet = (List<TriplePath>) getTriplePathSet(parentQuery);
		if (tpSet == null || tpSet.isEmpty()) {
			return;
		}
		// APPLYING THE INSTANTIATION
		for (TriplePath tp : tpSet) {
			// The BASES STEP:
			if (isTemplateVariableFree(tp)) {
				continue;
			}
			// SUBJECT
			if (tp.getSubject().isVariable()) {
				if (isTemplateVariable(tp.getSubject().getName())) {
					String templateVarString = tp.getSubject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					// if (isClassTemplateVariable(tp.getSubject().getName())) {
					if (isClassTemplateVariable(templateVarString)) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							Var templateVar = Var.alloc(templateVarString);
							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							childCL.remove(clas);
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);
							Query childQuery = QueryFactory.create(parentQuery.toString());
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));
							String entityqO = this.classVarTable.getClassFromVar(templateVar.getVarName());
							String entityqR = clas;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);
							TreeNode<DataNode> childNode = new TreeNode(cNode, null);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
						}
					} // if isClassTemplateVariable
						// else if (isIndividualTemplateVariable(tp.getSubject().getName())) {
						// // It means that we need to instancited for all the of parentCL.
						// for (String individual : parentIL) {
						// //System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						// Var templateVar = Var.alloc(templateVarString);
						// // Step 1: Auxiliary Structures
						// ArrayList<String> childIL = new ArrayList<String>();
						// childIL.addAll(parentIL);
						// childIL.remove(individual);
						//
						// ArrayList<String> childPL = new ArrayList<String>();
						// childPL.addAll(parentPL);
						// ArrayList<String> childCL = new ArrayList<String>();
						// childCL.addAll(parentCL);
						// ArrayList<String> chilLitL = new ArrayList<String>();
						// chilLitL.addAll(parentLitL);
						//
						// // Step 2: Create a childQuery
						// Query childQuery = QueryFactory.create(parentQuery.toString());
						//
						// childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
						// NodeFactory.createURI(individual));
						// // Step 4: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						//
						// String entityqO =
						// this.individualVarTable.getIndividualFromVar(templateVarString);
						// String entityqR = individual;
						// ArrayList<String> childOperationList = new ArrayList<String>();
						// childOperationList.addAll(pNode.getOperationList());
						// childOperationList.add(INSTANCE_OP);
						// String op = INSTANCE_OP;
						// //Add the code to compute the "nodeCost"
						// String entityqO_TMP = getLocalName(entityqO);
						// String entityqR_TMP = getLocalName(entityqR);
						// float nodeCost = pNode.getNodeCost() +
						// computeInstanciateOperationCost(entityqO_TMP,
						// entityqR_TMP);
						// //System.out.println("[QueryRecommendation::specialize3] this.originalQuery
						// "+this.originalQuery);
						// DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO,
						// entityqR,
						// childOperationList, op, nodeCost);
						//
						// TreeNode<DataNode> childNode;
						// childNode = new TreeNode(cNode, null);
						// // Step 5: recall the function on the child;
						// //specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// if (!(isInTreeNodeIndex1(childNode))) {
						// parentNode.addChild((TreeNode<T>) childNode);
						// addToNodeTreeIndexIFAbsent1(childNode);
						// specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						// // Step 5: recall the function on the child;
						// //specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						//
						// }//if isIndividualTemplateVariable
						// else if (isLiteralTemplateVariable(tp.getSubject().getName())) {
						// // It means that we need to instancited for all the of parentCL.
						// for (String literal : parentLitL) {
						// //System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						// Var templateVar = Var.alloc(templateVarString);
						// // Step 1: Auxiliary Structures
						// ArrayList<String> chilLitL = new ArrayList<String>();
						// chilLitL.addAll(parentLitL);
						// chilLitL.remove(literal);
						//
						// ArrayList<String> childPL = new ArrayList<String>();
						// childPL.addAll(parentPL);
						// ArrayList<String> childCL = new ArrayList<String>();
						// childCL.addAll(parentCL);
						// ArrayList<String> childIL = new ArrayList<String>();
						// childIL.addAll(parentIL);
						//
						// // Step 2: Create a childQuery
						// Query childQuery = QueryFactory.create(parentQuery.toString());
						//
						// childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
						// NodeFactory.createURI(literal));
						// // Step 4: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						//
						// String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
						// String entityqR = literal;
						// ArrayList<String> childOperationList = new ArrayList<String>();
						// childOperationList.addAll(pNode.getOperationList());
						// childOperationList.add(INSTANCE_OP);
						// String op = INSTANCE_OP;
						//
						// //Add the code to compute the "nodeCost"
						// String entityqO_TMP = getLocalName(entityqO);
						// String entityqR_TMP = getLocalName(entityqR);
						// float nodeCost = pNode.getNodeCost() +
						// computeInstanciateOperationCost(entityqO_TMP,
						// entityqR_TMP);
						//
						// //System.out.println("[QueryRecommendation::specialize3] this.originalQuery
						// "+this.originalQuery);
						// DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO,
						// entityqR,
						// childOperationList, op, nodeCost);
						//
						// TreeNode<DataNode> childNode;
						// childNode = new TreeNode(cNode, null);
						//
						// // Step 5: recall the function on the child;
						// //specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// if (!(isInTreeNodeIndex1(childNode))) {
						// parentNode.addChild((TreeNode<T>) childNode);
						// addToNodeTreeIndexIFAbsent1(childNode);
						// specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						// // Step 5: recall the function on the child;
						// //specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						//
						// }//if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getSubject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());
							// ///////////////////////////////////////////////////////////////// THIS IS THE
							// POINT WHERE I MAKE THE INSTANTIATION
							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = "";
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
					} // if isPropertyTemplateVariable
				} // if isTemplateVariable
			} // if subject

			// It means that there is at least one of the subj, pred or obj that is a
			// Template Variable:
			// PREDICATE
			if (tp.getPredicate().isVariable()) {
				if (isTemplateVariable(tp.getPredicate().getName())) {
					String templateVarString = tp.getPredicate().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();
					// It means that we need to instancited for all the properties of parentPL.
					for (String property : parentPL) {
						Var templateVar = Var.alloc(templateVarString);
						// Step 1: Auxiliary Structures
						ArrayList<String> childPL = new ArrayList<String>();
						childPL.addAll(parentPL);
						childPL.remove(property);

						ArrayList<String> childCL = new ArrayList<String>();
						childCL.addAll(parentCL);
						ArrayList<String> childIL = new ArrayList<String>();
						childIL.addAll(parentIL);
						ArrayList<String> chilLitL = new ArrayList<String>();
						chilLitL.addAll(parentLitL);

						// Step 2: Create a childQuery
						Query childQuery = QueryFactory.create(parentQuery.toString());
						// childQuery = parentQuery; parentQuery
						childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
								NodeFactory.createURI(property));
						String entityqO = null;
						if (templateVarString.contains("opt")) {
							entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
						}
						if (templateVarString.contains("dpt")) {
							entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
						}
						String entityqR = property;
						ArrayList<String> childOperationList = new ArrayList();

						childOperationList.addAll(pNode.getOperationList());
						childOperationList.add(INSTANCE_OP);
						String op = INSTANCE_OP;

						// Add the code to compute the "nodeCost"
						String entityqO_TMP = getLocalName(entityqO);
						String entityqR_TMP = getLocalName(entityqR);
						float nodeCost = pNode.getNodeCost()
								+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
						// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
						// "+this.originalQuery);
						DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
								childOperationList, op, nodeCost);

						TreeNode<DataNode> childNode;
						childNode = new TreeNode(cNode, null);

						// Step 5: recall the function on the child;
						// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						if (!(isInTreeNodeIndex1(childNode))) {
							parentNode.addChild((TreeNode<T>) childNode);
							addToNodeTreeIndexIFAbsent1(childNode);
							specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}
						// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
					}
				}
			} // if predicate

			// OBJECT
			if (tp.getObject().isVariable()) {
				if (isTemplateVariable(tp.getObject().getName())) {
					String templateVarString = tp.getObject().getName();
					SPARQLQueryInstantiation qi = new SPARQLQueryInstantiation();

					if (isClassTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String clas : parentCL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childCL = new ArrayList();
							childCL.addAll(parentCL);
							childCL.remove(clas);

							ArrayList<String> childPL = new ArrayList();
							childPL.addAll(parentPL);
							ArrayList<String> childIL = new ArrayList();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(clas));

							String entityqO = this.classVarTable.getClassFromVar(templateVarString);
							String entityqR = clas;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isClassTemplateVariable
						// else if (isIndividualTemplateVariable(tp.getObject().getName())) {
						// // It means that we need to instancited for all the of parentCL.
						// for (String individual : parentIL) {
						// //System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						// Var templateVar = Var.alloc(templateVarString);
						// // Step 1: Auxiliary Structures
						// ArrayList<String> childIL = new ArrayList<String>();
						// childIL.addAll(parentIL);
						// childIL.remove(individual);
						//
						// ArrayList<String> childPL = new ArrayList<String>();
						// childPL.addAll(parentPL);
						// ArrayList<String> childCL = new ArrayList<String>();
						// childCL.addAll(parentCL);
						// ArrayList<String> chilLitL = new ArrayList<String>();
						// chilLitL.addAll(parentLitL);
						//
						// // Step 2: Create a childQuery
						// Query childQuery = QueryFactory.create(parentQuery.toString());
						//
						// childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
						// NodeFactory.createURI(individual));
						// // Step 4: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						//
						// String entityqO =
						// this.individualVarTable.getIndividualFromVar(templateVarString);
						// String entityqR = individual;
						// ArrayList<String> childOperationList = new ArrayList<String>();
						// childOperationList.addAll(pNode.getOperationList());
						// childOperationList.add(INSTANCE_OP);
						// String op = INSTANCE_OP;
						//
						// //Add the code to compute the "nodeCost"
						// String entityqO_TMP = getLocalName(entityqO);
						// String entityqR_TMP = getLocalName(entityqR);
						// float nodeCost = pNode.getNodeCost() +
						// computeInstanciateOperationCost(entityqO_TMP,
						// entityqR_TMP);
						//
						// //System.out.println("[QueryRecommendation::specialize3] this.originalQuery
						// "+this.originalQuery);
						// DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO,
						// entityqR,
						// childOperationList, op, nodeCost);
						//
						// TreeNode<DataNode> childNode;
						// childNode = new TreeNode(cNode, null);
						//
						// // Step 5: recall the function on the child;
						// //specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// if (!(isInTreeNodeIndex1(childNode))) {
						// parentNode.addChild((TreeNode<T>) childNode);
						// addToNodeTreeIndexIFAbsent1(childNode);
						// specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						// // Step 5: recall the function on the child;
						// //specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						//
						// }//if isIndividualTemplateVariable
						// else if (isLiteralTemplateVariable(tp.getObject().getName())) {
						// // It means that we need to instancited for all the of parentCL.
						// for (String literal : parentLitL) {
						// //System.out.println("[QueryRecommendation::+parentPL.toString()" +
						// parentPL.toString());
						// Var templateVar = Var.alloc(templateVarString);
						// // Step 1: Auxiliary Structures
						// ArrayList<String> chilLitL = new ArrayList<String>();
						// chilLitL.addAll(parentLitL);
						// chilLitL.remove(literal);
						//
						// ArrayList<String> childPL = new ArrayList<String>();
						// childPL.addAll(parentPL);
						// ArrayList<String> childCL = new ArrayList<String>();
						// childCL.addAll(parentCL);
						// ArrayList<String> childIL = new ArrayList<String>();
						// childIL.addAll(parentIL);
						//
						// // Step 2: Create a childQuery
						// Query childQuery = QueryFactory.create(parentQuery.toString());
						//
						// childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
						// NodeFactory.createURI(literal));
						// // Step 4: creating a childNode and we add it to the Tree, if it is not added
						// alrady.
						//
						// String entityqO = this.literalVarTable.getLiteralFromVar(templateVarString);
						// String entityqR = literal;
						// ArrayList<String> childOperationList = new ArrayList<String>();
						// childOperationList.addAll(pNode.getOperationList());
						// childOperationList.add(INSTANCE_OP);
						// String op = INSTANCE_OP;
						//
						// //Add the code to compute the "nodeCost"
						// String entityqO_TMP = getLocalName(entityqO);
						// String entityqR_TMP = getLocalName(entityqR);
						// float nodeCost = pNode.getNodeCost() +
						// computeInstanciateOperationCost(entityqO_TMP,
						// entityqR_TMP);
						//
						// //System.out.println("[QueryRecommendation::specialize3] this.originalQuery
						// "+this.originalQuery);
						// DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO,
						// entityqR,
						// childOperationList, op, nodeCost);
						//
						// TreeNode<DataNode> childNode;
						// childNode = new TreeNode(cNode, null);
						// // Step 5: recall the function on the child;
						// //specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// if (!(isInTreeNodeIndex1(childNode))) {
						// parentNode.addChild((TreeNode<T>) childNode);
						// addToNodeTreeIndexIFAbsent1(childNode);
						// specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						// // Step 5: recall the function on the child;
						// // specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						// }
						//
						// }//if isLiteralTemplateVariable
					else if (isPropertyTemplateVariable(tp.getObject().getName())) {
						// It means that we need to instancited for all the of parentCL.
						for (String property : parentPL) {
							// System.out.println("[QueryRecommendation::+parentPL.toString()" +
							// parentPL.toString());
							Var templateVar = Var.alloc(templateVarString);
							// Step 1: Auxiliary Structures
							ArrayList<String> childPL = new ArrayList<String>();
							childPL.addAll(parentPL);
							childPL.remove(property);

							ArrayList<String> childCL = new ArrayList<String>();
							childCL.addAll(parentCL);
							ArrayList<String> childIL = new ArrayList<String>();
							childIL.addAll(parentIL);
							ArrayList<String> chilLitL = new ArrayList<String>();
							chilLitL.addAll(parentLitL);

							// Step 2: Create a childQuery
							Query childQuery = QueryFactory.create(parentQuery.toString());

							childQuery = qi.instantiateVarTemplate(childQuery, templateVar,
									NodeFactory.createURI(property));
							// Step 4: creating a childNode and we add it to the Tree, if it is not added
							// alrady.

							String entityqO = null;
							if (templateVarString.contains("opt")) {
								entityqO = this.objectProperyVarTable.getObjectProperyFromVar(templateVarString);
							}
							if (templateVarString.contains("dpt")) {
								entityqO = this.datatypePropertyVarTable.getDatatypeProperyFromVar(templateVarString);
							}
							String entityqR = property;
							ArrayList<String> childOperationList = new ArrayList();
							childOperationList.addAll(pNode.getOperationList());
							childOperationList.add(INSTANCE_OP);
							String op = INSTANCE_OP;

							// Add the code to compute the "nodeCost"
							String entityqO_TMP = getLocalName(entityqO);
							String entityqR_TMP = getLocalName(entityqR);
							float nodeCost = pNode.getNodeCost()
									+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);

							// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
							// "+this.originalQuery);
							DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO, entityqR,
									childOperationList, op, nodeCost);

							TreeNode<DataNode> childNode;
							childNode = new TreeNode(cNode, null);

							// Step 5: recall the function on the child;
							// specialize((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							if (!(isInTreeNodeIndex1(childNode))) {
								parentNode.addChild((TreeNode<T>) childNode);
								addToNodeTreeIndexIFAbsent1(childNode);
								specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
							}
							// Step 5: recall the function on the child;
							// specialize2((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
						}

					} // if isPropertyTemplateVariable

				} // if isTemplateVariable

			} // if object

			if (tpSet.size() > 1) {
				Query parentQueryCopy = parentQuery;
				RemoveTriple instance = new RemoveTriple();
				Query childQuery = instance.removeTP(parentQuery, tp.asTriple());

				ArrayList<String> childOperationList = new ArrayList();
				childOperationList.addAll(pNode.getOperationList());
				childOperationList.add(REMOVE_TP_OP);
				String op = REMOVE_TP_OP;

				ArrayList<String> childCL = new ArrayList<String>();
				childCL.addAll(parentCL);
				ArrayList<String> childPL = new ArrayList<String>();
				childPL.addAll(parentPL);
				ArrayList<String> childIL = new ArrayList<String>();
				childIL.addAll(parentIL);
				ArrayList<String> chilLitL = new ArrayList<String>();
				chilLitL.addAll(parentLitL);

				// Add the code to compute the "nodeCost"
				String entityqO_TMP = "";// getLocalName(entityqO);
				String entityqR_TMP = "";// getLocalName(entityqR);

				float nodeCost = pNode.getNodeCost() + computeRemoveOperationCost(this.originalQuery, childQuery);

				// System.out.println("[QueryRecommendation::specialize3] this.originalQuery
				// "+this.originalQuery);
				DataNode cNode = new DataNode(this.originalQuery, childQuery, entityqO_TMP, entityqR_TMP,
						childOperationList, op, nodeCost);
				TreeNode<DataNode> childNode = new TreeNode(cNode, null);
				if (!(isInTreeNodeIndex1(childNode))) {
					parentNode.addChild((TreeNode<T>) childNode);
					addToNodeTreeIndexIFAbsent1(childNode);
					// specialize3((TreeNode<T>) childNode, childPL, childCL, childIL, chilLitL);
				}
			}

		} // for (TriplePath tp : tpSet)

	}

	// DONE: this is woking with TreeNode<Query>
	public void specializeToQueryInstance() {

		if (this.queryTemplate == null) {
			throw new IllegalStateException("[QueryRecommendation::specializeToQueryInstance()]The query is null!!");
		}

		// System.out.println("[QueryRecommendation, specializeToQueryInstance()]
		// rdfd2.getPropertySet() " +
		// rdfd2.getPropertySet().toString());
		// System.out.println("[QueryRecommendation, specializeToQueryInstance()]
		// rdfd2.getClassSet() " +
		// rdfd2.getClassSet());
		// System.out.println("[QueryRecommendation, specializeToQueryInstance()]
		// rdfd2.getIndividualSet() " +
		// rdfd2.getIndividualSet());
		// System.out.println("[QueryRecommendation, specializeToQueryInstance()]
		// rdfd2.getLiteralSet() " +
		// rdfd2.getLiteralSet());
		this.rootDataNode = new TreeNode(this.queryTemplate, null);

		// System.out.println("parentPL ====================== " +
		// rdfd2.getPropertySet().toString());
		// System.out.println("parentCL ====================== " +
		// rdfd2.getClassSet().toString());
		// System.out.println("parentIL ====================== " +
		// rdfd2.getIndividualSet().toString());
		// System.out.println("parentLitL ====================== " +
		// rdfd2.getLiteralSet().toString());
		specialize(this.rootDataNode, rdfd2.getPropertySet(), rdfd2.getClassSet(), rdfd2.getIndividualSet(),
				rdfd2.getLiteralSet());
		// specialize(this.rootDataNode,
		// rdfd2.getPropertySet(),
		// new ArrayList<String>(),
		// new ArrayList<String>(),
		// new ArrayList<String>());

	}

	// This is UNDER DEVELOPMENT and woking with TreeNode<DataNode> which generalize
	// TreeNode<Query>
	public void specializeToQueryInstance1() {

		if (this.queryTemplate == null) {
			throw new IllegalStateException("[QueryRecommendation::specializeToQueryInstance()]The query is null!!");
		}

		ArrayList<String> operationList = new ArrayList();
		DataNode rootDN = new DataNode(this.originalQuery, this.queryTemplate, "", "", operationList, "", 0);
		this.rootDataNode = new TreeNode(rootDN, null);
		// specialize1(this.rootDataNode,
		// specialize2(this.rootDataNode,

		// As we have the issue of indexing long String when merging dpPropertySet and
		// opPropertySet, I do not
		// index and I do their merging here
		// ArrayList<String> propertySet = new ArrayList<String>();
		// propertySet.addAll(rdfd2.getDatatypePropertySet());
		// propertySet.addAll(rdfd2.getObjectPropertySet());
		// rdfd2.setPropertySet(propertySet);

		// try {
		// FromArrayStringToFile.save("/Users/carloallocca/Desktop/propertyList.txt",
		// propertySet);
		// FromArrayStringToFile.save("/Users/carloallocca/Desktop/classList.txt",
		// rdfd2.getClassSet());
		// } catch (FileNotFoundException ex) {
		// Logger.getLogger(QueryRecommendation.class.getName()).log(Level.SEVERE, null,
		// ex);
		// }
		// System.out.println("[QueryRecommendation::specializeToQueryInstance1]
		// rdfd2.getPropertySet()"+
		// rdfd2.getPropertySet());
		// System.out.println("[QueryRecommendation::specializeToQueryInstance1]
		// rdfd2.getDatatypePropertySet()"+
		// rdfd2.getDatatypePropertySet());
		// System.out.println("[QueryRecommendation::specializeToQueryInstance1]
		// rdfd2.getObjectPropertySet()"+
		// rdfd2.getObjectPropertySet());

		specialize3(this.rootDataNode, rdfd2.getPropertySet(), rdfd2.getClassSet(), rdfd2.getIndividualSet(),
				rdfd2.getLiteralSet());
	}

}
