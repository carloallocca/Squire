/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.core2.QueryTempVarSolutionSpace;
import uk.ac.open.kmi.squire.core2.VarTemplateAndEntityQoQr;
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
import uk.ac.open.kmi.squire.operation.SPARQLQueryInstantiation;
import uk.ac.open.kmi.squire.operation.SPARQLQuerySatisfiable;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;
import uk.ac.open.kmi.squire.utils.PowerSetFactory;

/**
 *
 * @author carloallocca
 */
public class QuerySpecializer4 extends AbstractQueryRecommendationObservable {

	// private static final String _TEST_RESULT_DIR_PREFIX =
	// "/Users/carloallocca/Desktop/KMi/KMi Started
	// 2015/KMi2015Development/WebSquire/TestResults/";

	/*
	 * Will log to working directory no matter what.
	 * 
	 * The goal anyway is to remove file writing from this class entirely. The
	 * Reporter class should handle all of it externally.
	 */
	private static final String _TEST_RESULT_DIR_PREFIX = "TestResults/";

	// private static final String TEST_RESULT_METRICS =
	// "QueryRootDistanceSimilarity/";

	// private static final String _TEST_RESULT_METRICS =
	// "QueryRootDistanceSimilarity/";
	// private static final String _TEST_RESULT_METRICS =
	// "QuerySpecificityDistanceSimilarity/";
	// private static final String _TEST_RESULT_METRICS =
	// "QueryResultTypeSimilarity/";
	private static final String TEST_RESULT_METRICS = "All/";

	private final static String TEST_ONE = "EducationI/";
	private final static String TEST_TWO = "Art/";
	private final static String TEST_THREE = "EducationII/";
	private final static String TEST_FOUR = "Museum/";
	private final static String TEST_FIVE = "GovernmentOpenData/";

	private static final String TEST_RESULT_NAME_PREFIX = "testResult";

	private static int testResultIndex = 0;
	private static int class_instanciation_number = 0;
	private static String INSTANCE_OP = "I";

	private static String REMOVE_TP_OP = "R";
	private static final String CLASS_TEMPLATE_VAR = "ct";

	private static final String OBJ_PROP_TEMPLATE_VAR = "opt";
	private static final String DT_PROP_TEMPLATE_VAR = "dpt";

	private static final String INDIVIDUAL_TEMPLATE_VAR = "it";

	private static final String LITERAL_TEMPLATE_VAR = "lt";
	// private String fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS +
	// TEST_ONE + TEST_RESULT_NAME_PREFIX;
	// private String fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS +
	// TEST_TWO + TEST_RESULT_NAME_PREFIX;
	private String fullPrefix = _TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS + TEST_THREE + TEST_RESULT_NAME_PREFIX;
	// private String fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS +
	// TEST_FOUR + TEST_RESULT_NAME_PREFIX;
	// private String fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS +
	// TEST_FIVE + TEST_RESULT_NAME_PREFIX;

	private final List<QueryAndContextNode> specializableQueryAndContextNodeList = new ArrayList<>();
	private final List<QueryAndContextNode> recommandedQueryList = new ArrayList<>();

	private HashMap<String, Query> queryIndex = new HashMap<>();
	private IRDFDataset rdfd1, rdfd2;
	private Query qO, qR;
	private float resultTypeSimilarityDegree, queryRootDistanceDegree, resultSizeSimilarityDegree,
			querySpecificityDistanceDegree;

	private LiteralVarMapping literalVarTable;
	private ClassVarMapping classVarTable;
	private DatatypePropertyVarMapping datatypePropertyVarTable;
	private IndividualVarMapping individualVarTable;
	private ObjectPropertyVarMapping objectProperyVarTable;
	private RDFVocVarMapping rdfVocVarTable;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private long stopTime;
	private long elapsedTime;
	private long startTime;

	public QuerySpecializer4(Query qo, Query qr, IRDFDataset d1, IRDFDataset d2, ClassVarMapping cVM,
			ObjectPropertyVarMapping opVM, DatatypePropertyVarMapping dpVM, IndividualVarMapping indVM,
			LiteralVarMapping lVM, RDFVocVarMapping rdfVM, float resultTypeSimilarityDegree,
			float queryRootDistanceDegree, float resultSizeSimilarityDegree, float querySpecificityDistanceDegree,
			String token) {

		startTime = System.currentTimeMillis();

		QuerySpecializer4.testResultIndex++;
		QuerySpecializer4.class_instanciation_number = QuerySpecializer4.class_instanciation_number + 1;

		this.qO = QueryFactory.create(qo.toString());
		this.qR = QueryFactory.create(qr.toString());

		this.rdfd1 = d1;
		this.rdfd2 = d2;

		this.classVarTable = cVM;
		this.individualVarTable = indVM;
		this.literalVarTable = lVM;
		this.objectProperyVarTable = opVM;
		this.datatypePropertyVarTable = dpVM;
		this.rdfVocVarTable = rdfVM;

		this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
		this.queryRootDistanceDegree = queryRootDistanceDegree;
		this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
		this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;

		this.token = token;

		// A. Compute the query recommentedQueryScore:
		// 1)...QueryRootDistance ... we start with both value set to 0
		float queryRootDist = 0;
		float queryRootDistSim = 0;

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, this.qR);
		float qSpecDistTP = qSpecDist.computeQSDwrtQueryTP(this.qO, this.qR);
		float qSpecificitySim = 1 - (qSpecDistVar + qSpecDistTP);

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;

		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree * resultTypeSim);
		float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * resultTypeSim) + (querySpecificityDistanceDegree * (qSpecificitySim)));
		//
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDist) +
		// (resultTypeSimilarityDegree * resulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar + qSpecDistTP)));
		// float recommentedQueryScore = resulttypeSim + qSpecDistVar+qSpecDistTP;//This
		// is working as it should but it does not consider the similarity distance
		// between the replased entities
		// log.debug("[QueryGeneralizer4::QuerySpecializer4] recommentedQueryScore " +
		// recommentedQueryScore);
		String op = ""; // It can be either R (for Removal) or I (Instanciation).
		ArrayList<String> operationList = new ArrayList();

		// B. Compute the qRTemplateVariableSet and qRTriplePatternSet
		Set<Var> qRTemplateVariableSet = getQueryTemplateVariableSet(this.qR);
		Set<TriplePath> qRTriplePatternSet = getQueryTriplePathSet(this.qR);

		// C. Compute the QueryTempVarSolutionSpace
		QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
		List<QuerySolution> qTsol = temVarValueSpace.computeTempVarSolutionSpace(qr, this.rdfd2);

		// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
		// <http://purl.org/dc/terms/title> ),
		List<QuerySolution> qTsolCleaned = eliminateSolutionBindedToTheSameValue(qTsol);

		// Map<Var, Set<RDFNode>>
		// qTsolMap=temVarValueSpace.computeTempVarSolutionSpace(qr, this.rdfd2, null);
		// D. Build the QueryAndContextNode from the query
		QueryAndContextNode qAndcNode = new QueryAndContextNode();

		// ...set the original query and the recommendated query;
		qAndcNode.setqO(qo);
		qAndcNode.setqR(qr);

		qAndcNode.setRdfD1(d1);
		qAndcNode.setRdfD2(d2);

		qAndcNode.setEntityqO("");
		qAndcNode.setEntityqR("");

		//// ...SET THE CLASS, OBJECT AND DATATYPE PROPERTIES SETs...;
		// qAndcNode.setcSetD2(d2.getClassSet());
		// qAndcNode.setDpSetD2(d2.getDatatypePropertySet());
		// qAndcNode.setOpSetD2(d2.getObjectPropertySet());
		// qAndcNode.setlSetD2(d2.getLiteralSet());
		// qAndcNode.setIndSetD2(d2.getIndividualSet());
		//// // As we have the issue of indexing long String when merging dpPropertySet
		//// and opPropertySet, I do not index and I do their merging here
		// ArrayList<String> propertySet = new ArrayList();
		// propertySet.addAll(d2.getDatatypePropertySet());
		// propertySet.addAll(d2.getObjectPropertySet());
		// d2.setPropertySet(propertySet);
		// qAndcNode.setpSetD2(propertySet);
		// qAndcNode.setRdfVD2(d2.getRDFVocabulary());
		// ...set the score measurements
		qAndcNode.setQueryRootDistance(queryRootDist);
		qAndcNode.setQueryRootDistanceSimilarity(queryRootDistSim); // do also for the other measurements,
																	// computeTempVarSolutionSpace them...

		// qAndcNode.setQuerySpecificityDistanceSimilarity(qSpecDistVar + qSpecDistTP);
		qAndcNode.setQuerySpecificityDistanceSimilarity(qSpecificitySim);

		qAndcNode.setQueryResultTypeSimilarity(resultTypeSim);
		qAndcNode.setQueryResultSizeSimilarity(queryResultSizeSimilarity);
		qAndcNode.setqRScore(recommentedQueryScore);

		qAndcNode.setOp(op);
		qAndcNode.setOperationList(operationList);

		// qAndcNode.setqRTemplateVariableSet(qRTemplateVariableSet);
		// qAndcNode.setqRTriplePathSet(qRTriplePatternSet);
		// ...set the QueryTempVarSolutionSpace
		qAndcNode.setSolutionSpace(qTsolCleaned);
		// qAndcNode.setQueryTempVarValueMap(qTsolMap);

		// log.info("[QueryGeneralizer4::QuerySpecializer4] qTsol size = " +
		// qTsol.size());
		// E. Sorted Insert of the QueryAndContextNode into the
		// specializableQueryAndContextNodeList
		// if(qTsol.size()>=1){
		addSpecializableQueryList(qAndcNode);
		// this.recommandedQueryList.add(qAndcNode);

		new File(fullPrefix).mkdirs(); // XXX This will create an additional unused directory
		String newTextFile = fullPrefix + Integer.toString(QuerySpecializer4.testResultIndex) + ".txt";
		log.info("newTextFile 1 =======" + newTextFile);

		try {
			FileWriter fw = new FileWriter(newTextFile, true);
			fw.write("\n");
			fw.write("\n");
			fw.write("Process Start Time " + startTime);
			fw.write("\n");
			fw.write("\n");
			fw.write("\n");
			fw.write("============= SPARQL ENDPOINTS =============");
			fw.write("\n");
			fw.write("\n");
			fw.write("endpoint source == " + qAndcNode.getRdfD1().getEndPointURL());
			fw.write("\n");
			fw.write("endpoint target == " + qAndcNode.getRdfD2().getEndPointURL());
			fw.write("\n");
			fw.write("\n");
			fw.write("============= NEW QUERY SOURCE =============");
			fw.write("\n");
			fw.write(qAndcNode.getqO().toString());
			fw.write("\n");
			fw.write("\n");
			fw.write("============= NEW QUERY RECOMMENDATIONS =============");
			fw.write("\n");
			fw.write("\n");
			fw.write("qR score == " + qAndcNode.getqRScore());
			fw.write("\n");
			fw.write(qAndcNode.getqR().toString());
			fw.close();
		} catch (IOException e) {
			// FIXME WTF
			log.error(e.getMessage());
			// exception handling left as an exercise for the reader
			throw new RuntimeException(e);
		}

	}

	public List<QueryAndContextNode> getRecommandedQueryList() {
		return recommandedQueryList;
	}

	public List<QueryAndContextNode> specialize() throws Exception {
		log.info("::template query space size "
				+ this.specializableQueryAndContextNodeList.get(0).getQueryTempVarSolutionSpace().size());
		if ((this.specializableQueryAndContextNodeList.size() == 1)
				&& (this.specializableQueryAndContextNodeList.get(0).getQueryTempVarSolutionSpace().isEmpty())
				&& (isIProcessable(this.specializableQueryAndContextNodeList.get(0)))) {

			// log.info("WE WILL START THE SUB PROCESS SPECIALIZATION...");
			// 1. Get and Remove the QueryAndContextNode with qRScore max
			QueryAndContextNode parentQueryAndContextNode = getMaxQueryAndContextNode();
			// log.info("getqO "+parentQueryAndContextNode.getqO());
			// log.info("getqR "+parentQueryAndContextNode.getqR());
			// ADD the code for generating the subqueries with removed triple patterns
			// (QueryAndContextNode)
			// The power set of the triple pattern set.
			// P.S. Look at the code down in the section 3.
			Query parentqRCopy = QueryFactory.create(parentQueryAndContextNode.getqR());
			Set<TriplePath> triplePathSet = getQueryTriplePathSet(parentqRCopy);
			// List<Var> qRTemplateVariableSet=parentqRCopy.getProjectVars();
			List<String> qRTemplateVariableSet = parentqRCopy.getResultVars();

			List<List<TriplePath>> triplePathPowerSet = PowerSetFactory.powerset(triplePathSet);
			List<List<TriplePath>> triplePathPowerSetOrdered = PowerSetFactory.order(triplePathPowerSet);

			// log.info("triplePathPowerSetOrdered" + triplePathPowerSetOrdered.toString());
			for (List<TriplePath> triplePathSubSet : triplePathPowerSetOrdered) {
				// for (int i=0; i<15; i++) {
				// List<TriplePath> triplePathSubSet = triplePathPowerSetOrdered.get(i);
				log.info("triplePathSubSet ::" + triplePathSubSet.toString());
				if (!triplePathSubSet.isEmpty()) {
					SelectBuilder sb = new SelectBuilder();
					// adding the triple patters
					for (TriplePath tp : triplePathSubSet) {
						// apply the removal operation:
						sb.addWhere(tp.asTriple());
					}
					// adding the output variable
					for (String var : qRTemplateVariableSet) {
						sb.addVar(var);
					}
					sb.setDistinct(true);
					Query subQuery = sb.build();

					// add here the rest of the code,
					// including the fact that satisfiacibile and crete
					// a node child from the node parent
					// Check if it is alredy indexed and therefore generated
					log.info("subQuery Remove operation::: " + subQuery.toString());
					if (!(isQueryIndexed(subQuery))) {
						// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...

						SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();

						boolean b = false;

						try {
							b = qs.isSatisfiableWrtResults(subQuery, rdfd2);
							log.info("isSatisfiableWRTResultsWithToken :: " + b);
						} catch (Exception ex) {
							log.info(ex.getMessage());
						}

						if (b) {
							log.info("subQuery Remove operation::: " + subQuery);
							QueryAndContextNode childNode = createNewQueryAndContextNodeForRemovalOp(subQuery,
									parentQueryAndContextNode);
							log.info("childNode Solution List... " + childNode.getQueryTempVarSolutionSpace().size());

							addSpecializableQueryList(childNode);
							// this.specializableQueryAndContextNodeList.add(childNode);

							// add qWithoutTriple to the index
							addQueryToIndexIFAbsent(subQuery);
							// printQuerySolutionSpaceMap(parentQueryAndContextNode);

						} else {
							addQueryToIndexIFAbsent(subQuery);
						}
					}
				}
			}
		}
		// The trivial case:
		SPARQLQuerySatisfiable qs1 = new SPARQLQuerySatisfiable();
		if ((this.specializableQueryAndContextNodeList.size() == 1)
				&& (this.specializableQueryAndContextNodeList.get(0).getQueryTempVarSolutionSpace().isEmpty())
				&& (qs1.isSatisfiableWrtResults(this.specializableQueryAndContextNodeList.get(0).getqO(),
						rdfd2))) {

			QueryAndContextNode childNode = createEmptyQueryAndContextNode(
					this.specializableQueryAndContextNodeList.get(0).getqO());
			addSpecializableQueryList(childNode);
			this.recommandedQueryList.add(childNode);
			notifyQueryRecommendation(this.specializableQueryAndContextNodeList.get(0).getqO(), 1);
			notifyQueryRecommendationCompletion(true);

			printSpecialize(childNode);

			return this.recommandedQueryList;
		}

		// log.info("WE WILL START THE SPECIALIZATION PROCESS...");
		// log.info("this.specializableQueryAndContextNodeList.size() " +
		// this.specializableQueryAndContextNodeList.size());
		while (this.specializableQueryAndContextNodeList.size() > 0) {

			// 1. Get and Remove the QueryAndContextNode with qRScore max
			QueryAndContextNode parentQueryAndContextNode = getMaxQueryAndContextNode();
			if (parentQueryAndContextNode != null) {
				// 2. Store the QueryAndContextNode with qRScore max into the
				// recommandedQueryList as it could be one of the recommendated query
				this.recommandedQueryList.add(parentQueryAndContextNode);
				// this.notifyQueryRecommendation(parentQueryAndContextNode.getqR(),
				// parentQueryAndContextNode.getqRScore());

				// 4. check if we can apply a instanciation operation;
				if (isIProcessable(parentQueryAndContextNode)) {
					Query queryChild = QueryFactory.create(parentQueryAndContextNode.getqR());
					List<QuerySolution> qSolList = parentQueryAndContextNode.getQueryTempVarSolutionSpace();
					log.info("queryChild Instanciation step: " + queryChild.toString());
					log.info("qSolList size: " + qSolList.size());
					int solProgressNumber = 0;
					for (QuerySolution sol : qSolList) {
						solProgressNumber = solProgressNumber + 1;

						// for (int i = 0; i < 10; i++) {
						// // calling repeatedly to increase chances of a clean-up
						// System.gc();
						// }
						Query childQueryCopy = QueryFactory.create(queryChild.toString());

						// [ REPLACED ] Query childQueryCopyInstanciated=
						// applyInstanciationOP(childQueryCopy, sol);
						Set<Var> qTempVarSet = getQueryTemplateVariableSet(childQueryCopy);
						Query childQueryCopyInstanciated = null;

						// this contains all the tuples <varName, entityQo, entityQr> for each varName
						// that is going to be instantiated
						ArrayList<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList = new ArrayList();
						for (Var tv : qTempVarSet) {
							// log.info("Var tv: " +tv.getVarName());
							// log.info("Var tv: " +tv.getName());
							RDFNode node = sol.get(tv.getName());
							// log.info("RDFNode node: " +node.toString());

							SPARQLQueryInstantiation instOP = new SPARQLQueryInstantiation();
							childQueryCopyInstanciated = instOP.instantiateVarTemplate(childQueryCopy, tv,
									node.asNode());

							String entityQo = getEntityQo(tv);
							String entityQr = node.asNode().getURI(); // as it is the name of a concrete node and not of
																		// a variable;
							VarTemplateAndEntityQoQr item = new VarTemplateAndEntityQoQr(tv, entityQo, entityQr);
							templVarEntityQoQrInstanciatedList.add(item);
						}
						if (childQueryCopyInstanciated != null) {
							// 4.1.2. Check if it is alredy indexed and therefore generated
							if (!(isQueryIndexed(childQueryCopyInstanciated))) {
								// add qWithoutTriple to the index
								addQueryToIndexIFAbsent(childQueryCopyInstanciated);

								// ...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...
								SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
								if (qs.isSatisfiableWrtResults(childQueryCopyInstanciated, rdfd2)) {
									QueryAndContextNode childNode = createNewQueryAndContextNodeForInstanciateOp(
											childQueryCopyInstanciated, parentQueryAndContextNode,
											templVarEntityQoQrInstanciatedList);

									// ======
									// Ho commentato questa riga perche non ha un senso logico. Non c'e' motivo
									// di aggiungere la query instantiata nella lista di query da specializzare.
									// did it 13/05/2017
									// addSpecializableQueryList(childNode);
									// invece, ho aggiunto questa in quanto la query e' pronta per essere
									// raccomandata e non avra piu :
									this.recommandedQueryList.add(childNode);

									// =====
									long qRArrivalTime = System.currentTimeMillis();
									long queryElapsedTime = qRArrivalTime - startTime;
									log.info("qR score ======" + childNode.getqRScore());
									log.info("qR " + childNode.getqR());

									notifyQueryRecommendation(childNode.getqR(), childNode.getqRScore());

									// add qWithoutTriple to the index
									addQueryToIndexIFAbsent(childQueryCopyInstanciated);
									// printQuerySolutionSpaceMap(parentQueryAndContextNode);

									String newTextFile = fullPrefix
											+ Integer.toString(QuerySpecializer4.testResultIndex) + ".txt";
									// String newTextFile = TEST_RESULT_DIR_PREFIX +
									// Integer.toString(QuerySpecializer4.testResultIndex) + ".txt";
									// log.info("newTextFile 2 =======" + newTextFile);

									try {
										FileWriter fw = new FileWriter(newTextFile, true);
										fw.write("\n");
										fw.write("qSolSpace size ==" + qSolList.size());
										fw.write("\n");
										fw.write("solProgressNumber ==" + solProgressNumber);
										fw.write("\n");
										fw.write("qR Arrival Time == " + Long.toString(qRArrivalTime));
										fw.write("\n");
										fw.write("qR Query Elapsed Time == " + Long.toString(queryElapsedTime));
										fw.write("\n");
										fw.write("qR score == " + Float.toString(childNode.getqRScore()));
										fw.write("\n");
										fw.write(childNode.getqR().toString());
										fw.close();
									} catch (IOException e) {
										// exception handling left as an exercise for the reader
									}

									//////////// START FOR TESTING PURPOSE
								} else {
									addQueryToIndexIFAbsent(childQueryCopyInstanciated);
								}
							}
						}
					} // for
				}
			}

		} // end while
		this.notifyQueryRecommendationCompletion(true);

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		// String fullPrefix = "";
		// switch (QuerySpecializer4.class_instanciation_number) {
		// case 1:
		// fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS + TEST_ONE +
		// TEST_RESULT_NAME_PREFIX;
		// break;
		// case 2:
		// fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS + TEST_TWO +
		// TEST_RESULT_NAME_PREFIX;
		// break;
		// case 3:
		// fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS + TEST_THREE +
		// TEST_RESULT_NAME_PREFIX;
		// break;
		// case 4:
		// fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS + TEST_FOUR +
		// TEST_RESULT_NAME_PREFIX;
		// break;
		// case 5:
		// fullPrefix = TEST_RESULT_DIR_PREFIX + TEST_RESULT_METRICS + TEST_FIVE +
		// TEST_RESULT_NAME_PREFIX;
		// break;
		// default:
		// break;
		// }
		String newTextFile = fullPrefix + Integer.toString(QuerySpecializer4.testResultIndex) + ".txt";
		// log.info("newTextFile ======= 3" +newTextFile);
		// String newTextFile = TEST_RESULT_DIR_PREFIX +
		// Integer.toString(QuerySpecializer4.testResultIndex) + ".txt";
		try {
			FileWriter fw = new FileWriter(newTextFile, true);
			fw.write("\n");
			fw.write("\n");
			fw.write("Process Stop Time == " + Long.toString(stopTime));
			fw.write("\n");
			fw.write("Process Elapsed Time == " + Long.toString(elapsedTime));
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			// exception handling left as an exercise for the reader
		}

		return this.recommandedQueryList;
	}

	// private boolean isQueryIndexed(QueryAndContextNode qRScoreMaxNodeCloned) {
	// Query q = qRScoreMaxNodeCloned.getqR();
	// Set<TriplePath> triplePathCollection =
	// qRScoreMaxNodeCloned.getqRTriplePathSet();
	// ArrayList<String> s = new ArrayList<String>(); //and use Collections.sort()
	// for (TriplePath tp : triplePathCollection) {
	// s.add(tp.toString());
	// }
	// Collections.sort(s);
	// return queryAndContextNodeIndex.containsKey(s.toString());
	// }
	private void addQueryToIndexIFAbsent(Query qWithoutTriple) {

		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);

		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}
		queryIndex.putIfAbsent(s.toString(), qWithoutTriple);
	}

	private void addSpecializableQueryList(QueryAndContextNode d1) {
		this.specializableQueryAndContextNodeList.add(d1);
		Collections.sort(this.specializableQueryAndContextNodeList, new QueryAndContextNode.QRScoreComparator());
	}

	private Query applyInstanciationOP(Query queryChild, QuerySolution sol) {

		Set<Var> qTempVarSet = getQueryTemplateVariableSet(queryChild);
		for (Var tv : qTempVarSet) {
			RDFNode node = sol.get(tv.getName());
			SPARQLQueryInstantiation instOP = new SPARQLQueryInstantiation();
			queryChild = instOP.instantiateVarTemplate(queryChild, tv, node.asNode());

			// String entityqO =
			// this.classVarTable.getClassFromVar(templateVar.getVarName());
			// String entityqR = clas;
			//
			// ArrayList<String> childOperationList = new ArrayList();
			// childOperationList.addAll(pNode.getOperationList());
			// childOperationList.add(INSTANCE_OP);
			// String op = INSTANCE_OP;
		}
		return queryChild;
	}

	private void applyRemovalOp(QueryAndContextNode qRScoreMaxNode) {

		Query qRCopy = qRScoreMaxNode.getqR();
		Set<TriplePath> triplePathSet = getQueryTriplePathSet(qRCopy);

		for (TriplePath tp : triplePathSet) {

			// 1. Remove the TriplePath tp from the qRCopy
			RemoveTriple instance = new RemoveTriple();
			Query qWithoutTriple = instance.removeTP(qRCopy, tp.asTriple());

			// 2. Check if it is alredy indexed and therefore generated
			if (!(isQueryIndexed(qWithoutTriple))) {

				// // BUILD A NEW QueryAndContextNode
				// // 2.1. Clone the QueryAndContextNode with qRScore max so it can be processed
				// for applying operations
				// QueryAndContextNode qRScoreMaxNodeCloned =
				// qRScoreMaxNode.cloneMe(qRScoreMaxNode);
				//
				// // 2.2 Update the triplePathSet of the clonedNode
				// qRScoreMaxNodeCloned.getqRTriplePathSet().remove(tp);
				//
				// // 2.3. Update the recommended query of the clonedNode
				// qRScoreMaxNodeCloned.setqR(qWithoutTriple);
				//
				// // 2.4. devo fare tutti gli aggiornameti: operation list, le quattro misure,
				// etc...
				// ArrayList<String> clonedOperationList = new ArrayList();
				// clonedOperationList.addAll(qRScoreMaxNode.getOperationList());
				// clonedOperationList.add(REMOVE_TP_OP);
				// qRScoreMaxNodeCloned.setOperationList(clonedOperationList);
				//
				// qRScoreMaxNodeCloned.setOp(REMOVE_TP_OP);
				//
				// //In generale guarda dall'altra procedura per capire cosa manca
				//
				// // 7. devo aggiungere to specializableQueryAndContextNodeList in order to be
				// further specialized
				// addSpecializableQueryList(qRScoreMaxNodeCloned);
				//
				// this.recommandedQueryList.add(qRScoreMaxNodeCloned);
				//
				// // 8. Add node to the index
				// addQueryToIndexIFAbsent(qWithoutTriple);
			}

		}

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

	private float computeInstanciationOperationCost(List<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList) {
		int size = templVarEntityQoQrInstanciatedList.size();
		float nodeCost = 0;
		if (size > 0) {
			for (VarTemplateAndEntityQoQr item : templVarEntityQoQrInstanciatedList) {
				String entityqO_TMP = getLocalName(item.getEntityQo());
				String entityqR_TMP = getLocalName(item.getEntityQr());
				nodeCost = nodeCost + computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);
			}
			return (float) 1 - (nodeCost / size); // we divide by size as we want the value to be between 0 and 1.
		}
		return 0;
	}

	private float computeRemoveOperationCost(Query originalQuery, Query childQuery) {
		QueryGPESim queryGPEsim = new QueryGPESim();
		float sim = queryGPEsim.computeQueryPatternsSim(originalQuery, childQuery);
		return (float) 1.0 - sim;
		// return sim;
		// return 0;
	}

	private QueryAndContextNode createEmptyQueryAndContextNode(Query qo) throws Exception {
		QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();

		Query clonedqO = QueryFactory.create(qo);
		childQueryAndContextNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(qo);
		childQueryAndContextNode.setqR(clonedqR);

		childQueryAndContextNode.setqRScore(1);

		return childQueryAndContextNode;

	}

	private QueryAndContextNode createNewQueryAndContextNodeForInstanciateOp(Query childQueryCopyInstanciated,
			QueryAndContextNode parentQueryAndContextNode,
			ArrayList<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList) {
		QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();
		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentQueryAndContextNode.getqO());
		childQueryAndContextNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(childQueryCopyInstanciated.toString());
		childQueryAndContextNode.setqR(clonedqR);

		// XXX cloning the dataset object for the child nodes, why?
		// ..set the RDF dataset 1
		IRDFDataset rdfD1 = parentQueryAndContextNode.getRdfD1();
		if (rdfD1 instanceof SparqlIndexedDataset) {
			childQueryAndContextNode
					.setRdfD1(new SparqlIndexedDataset((String) parentQueryAndContextNode.getRdfD1().getEndPointURL(),
							(String) parentQueryAndContextNode.getRdfD1().getGraph()));
		} else { // TO ADD the case of FILEBASED dataset
		}
		// ..set the RDF dataset 2
		IRDFDataset rdfD2 = parentQueryAndContextNode.getRdfD2();
		if (rdfD2 instanceof SparqlIndexedDataset) {
			childQueryAndContextNode
					.setRdfD2(new SparqlIndexedDataset(((String) parentQueryAndContextNode.getRdfD2().getEndPointURL()),
							(String) parentQueryAndContextNode.getRdfD2().getGraph()));
			// //C. Compute the QueryTempVarSolutionSpace
			// QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			// // [REPLACED] List<QuerySolution> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2);
			// //Map<Var, Set<RDFNode>> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2, null);
			// childQueryAndContextNode.setQueryTempVarValueMap(qTsolMap);
			childQueryAndContextNode
					.setSolutionSpace(new ArrayList<>(parentQueryAndContextNode.getQueryTempVarSolutionSpace()));
		} else { // TO ADD the case of FILEBASED dataset
		}

		// //...SET THE CLASS, OBJECT AND DATATYPE PROPERTIES SETs...;
		// ArrayList<String> clonedcSetD2 = new ArrayList();
		// clonedcSetD2.addAll(parentQueryAndContextNode.getRdfD2().getClassSet());
		// childQueryAndContextNode.setcSetD2(clonedcSetD2);
		//
		// ArrayList<String> clonedDpSetD2 = new ArrayList();
		// clonedDpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getDatatypePropertySet());
		// childQueryAndContextNode.setDpSetD2(clonedDpSetD2);
		//
		// ArrayList<String> clonedOpSetD2 = new ArrayList();
		// clonedOpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getObjectPropertySet());
		// childQueryAndContextNode.setOpSetD2(clonedOpSetD2);
		//
		// ArrayList<String> clonedlSetD2 = new ArrayList();
		// clonedlSetD2.addAll(parentQueryAndContextNode.getRdfD2().getLiteralSet());
		// childQueryAndContextNode.setlSetD2(clonedlSetD2);
		//
		// ArrayList<String> clonedIndSetD2 = new ArrayList();
		// clonedIndSetD2.addAll(parentQueryAndContextNode.getRdfD2().getIndividualSet());
		// childQueryAndContextNode.setIndSetD2(clonedIndSetD2);
		//
		// ArrayList<String> clonedpSetD2 = new ArrayList();
		// clonedpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getPropertySet());
		// childQueryAndContextNode.setpSetD2(clonedpSetD2);
		//
		// ArrayList<String> clonedRdfVSetD2 = new ArrayList();
		// clonedRdfVSetD2.addAll(parentQueryAndContextNode.getRdfD2().getRDFVocabulary());
		// childQueryAndContextNode.setRdfVD2(clonedRdfVSetD2);
		// ...set the openration list
		ArrayList<String> clonedOperationList = new ArrayList();
		clonedOperationList.addAll(parentQueryAndContextNode.getOperationList());
		clonedOperationList.add(INSTANCE_OP);
		childQueryAndContextNode.setOperationList(clonedOperationList);

		childQueryAndContextNode.setOp(INSTANCE_OP);
		// ...set the score measurements

		// A. Compute the query recommentedQueryScore:
		// 1)...QueryRootDistance
		float newQueryRootDist = parentQueryAndContextNode.getQueryRootDistance()
				+ computeInstanciationOperationCost(templVarEntityQoQrInstanciatedList);
		childQueryAndContextNode.setQueryRootDistance(newQueryRootDist);

		float queryRootDistSim = 1 - newQueryRootDist;
		childQueryAndContextNode.setQueryRootDistanceSimilarity(queryRootDistSim);

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, this.qR);
		float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(this.qO, this.qR);
		float qSpecificitySim = 1 - (qSpecDistSimVar + qSpecDistSimTriplePattern);
		childQueryAndContextNode.setQuerySpecificityDistanceSimilarity(qSpecificitySim);

		// log.info("newQueryRootDistI " +newQueryRootDist);
		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float newResulttypeSim = qRTS.computeQueryResultTypeDistance(childQueryAndContextNode.getqO(), this.rdfd1,
				childQueryAndContextNode.getqR(), this.rdfd2);
		// log.info("newQueryRootDistI " +newQueryRootDist);
		log.info("newResulttypeSimI " + newResulttypeSim);
		childQueryAndContextNode.setQueryResultTypeSimilarity(newResulttypeSim);

		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;
		// float recommentedQueryScore = ((queryRootDistanceDegree * newQueryRootDist) +
		// (resultTypeSimilarityDegree * newResulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar+qSpecDistTP)));
		// float recommentedQueryScore = newQueryRootDist + newResulttypeSim +
		// (qSpecDistVar/qSpecDistSimTriplePattern);

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree *
		// newResulttypeSim);

		float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * newResulttypeSim)
				+ (querySpecificityDistanceDegree * (qSpecificitySim)));

		// float recommentedQueryScore = (newResulttypeSim +
		// (qSpecDistSimVar+qSpecDistSimTriplePattern));
		//
		// log.info("qSpecDistSimTriplePatternI " +qSpecDistSimTriplePattern);
		// log.info("qSpecDistSimVar/qSpecDistSimTriplePatternI "
		// +(qSpecDistSimVar+qSpecDistSimTriplePattern));
		// log.info("recommentedQueryScoreI " +recommentedQueryScore);
		//// float justSUM= 1* newQueryRootDist +newResulttypeSim +
		// (qSpecDistVar/qSpecDistSimTriplePattern);
		//// log.info("recommentedQueryScoreI2 "+justSUM );
		log.info("clonedqR I " + clonedqR);

		childQueryAndContextNode.setqRScore(recommentedQueryScore);

		return childQueryAndContextNode;

	}

	private QueryAndContextNode createNewQueryAndContextNodeForRemovalOp(Query qWithoutTriple,
			QueryAndContextNode parentQueryAndContextNode) throws Exception {
		QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();
		// ...set the original query and the recommendated query;
		Query clonedqO = QueryFactory.create(parentQueryAndContextNode.getqO());
		childQueryAndContextNode.setqO(clonedqO);

		Query clonedqR = QueryFactory.create(qWithoutTriple.toString());
		childQueryAndContextNode.setqR(clonedqR);

		// ...set the entities: EntityqO and EntityqR
		childQueryAndContextNode.setEntityqO("");
		childQueryAndContextNode.setEntityqR("");
		// ..set the RDF dataset 1
		IRDFDataset rdfD1 = parentQueryAndContextNode.getRdfD1();
		if (rdfD1 instanceof SparqlIndexedDataset) {
			IRDFDataset newRdfD1 = new SparqlIndexedDataset(
					((String) parentQueryAndContextNode.getRdfD1().getEndPointURL()),
					(String) parentQueryAndContextNode.getRdfD1().getGraph());
			childQueryAndContextNode.setRdfD1(newRdfD1);
		} else { // TO ADD the case of FILEBASED dataset
		}
		// ..set the RDF dataset 2
		IRDFDataset rdfD2 = parentQueryAndContextNode.getRdfD2();
		if (rdfD2 instanceof SparqlIndexedDataset) {
			IRDFDataset newRdfD2 = new SparqlIndexedDataset(
					((String) parentQueryAndContextNode.getRdfD2().getEndPointURL()),
					(String) parentQueryAndContextNode.getRdfD2().getGraph());
			childQueryAndContextNode.setRdfD2(newRdfD2);
			// //C. Compute the QueryTempVarSolutionSpace
			// QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			// // [REPLACED] List<QuerySolution> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2);
			// //Map<Var, Set<RDFNode>> qTsolMap =
			// temVarValueSpace.computeTempVarSolutionSpace(clonedqR, this.rdfd2, null);
			// childQueryAndContextNode.setQueryTempVarValueMap(qTsolMap);
			// List<QuerySolution> qTsol =
			// parentQueryAndContextNode.getQueryTempVarSolutionSpace();
			List<QuerySolution> qTsolChild = new ArrayList();

			// Compute the QueryTempVarSolutionSpace
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			List<QuerySolution> qTsolTMP = temVarValueSpace.computeTempVarSolutionSpace(qWithoutTriple, this.rdfd2);

			// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
			// <http://purl.org/dc/terms/title> ),
			List<QuerySolution> qTsolCleaned = eliminateSolutionBindedToTheSameValue(qTsolTMP);

			qTsolChild.addAll(qTsolCleaned);
			childQueryAndContextNode.setSolutionSpace(qTsolCleaned);
		} else { // TO ADD the case of FILEBASED dataset
		}

		// ...set the set of classes, object property, datatype property,...;
		// ArrayList<String> clonedcSetD2 = new ArrayList();
		// clonedcSetD2.addAll(parentQueryAndContextNode.getRdfD2().getClassSet());
		// childQueryAndContextNode.setcSetD2(clonedcSetD2);
		//
		// ArrayList<String> clonedDpSetD2 = new ArrayList();
		// clonedDpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getDatatypePropertySet());
		// childQueryAndContextNode.setDpSetD2(clonedDpSetD2);
		//
		// ArrayList<String> clonedOpSetD2 = new ArrayList();
		// clonedOpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getObjectPropertySet());
		// childQueryAndContextNode.setOpSetD2(clonedOpSetD2);
		//
		// ArrayList<String> clonedlSetD2 = new ArrayList();
		// clonedlSetD2.addAll(parentQueryAndContextNode.getRdfD2().getLiteralSet());
		// childQueryAndContextNode.setlSetD2(clonedlSetD2);
		//
		// ArrayList<String> clonedIndSetD2 = new ArrayList();
		// clonedIndSetD2.addAll(parentQueryAndContextNode.getRdfD2().getIndividualSet());
		// childQueryAndContextNode.setIndSetD2(clonedIndSetD2);
		//
		// ArrayList<String> clonedpSetD2 = new ArrayList();
		// clonedpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getPropertySet());
		// childQueryAndContextNode.setpSetD2(clonedpSetD2);
		//
		// ArrayList<String> clonedRdfVSetD2 = new ArrayList();
		// clonedRdfVSetD2.addAll(parentQueryAndContextNode.getRdfD2().getRDFVocabulary());
		// childQueryAndContextNode.setRdfVD2(clonedRdfVSetD2);
		// ...set the openration list
		ArrayList<String> clonedOperationList = new ArrayList();
		clonedOperationList.addAll(parentQueryAndContextNode.getOperationList());
		clonedOperationList.add(REMOVE_TP_OP);
		childQueryAndContextNode.setOperationList(clonedOperationList);

		childQueryAndContextNode.setOp(REMOVE_TP_OP);
		// ...set the score measurements

		/*
		 * Compute the query recommentedQueryScore: 1)QueryRootDistance
		 */
		float newQueryRootDist = parentQueryAndContextNode.getQueryRootDistance()
				+ computeRemoveOperationCost(childQueryAndContextNode.getqO(), childQueryAndContextNode.getqR());
		childQueryAndContextNode.setQueryRootDistance(newQueryRootDist);
		float queryRootDistSim = 1 - newQueryRootDist;
		childQueryAndContextNode.setQueryRootDistanceSimilarity(queryRootDistSim);

		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistSimVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, this.qR);
		float qSpecDistSimTriplePattern = qSpecDist.computeQSDwrtQueryTP(this.qO, this.qR);
		float qSpecificitySim = 1 - (qSpecDistSimVar + qSpecDistSimTriplePattern);
		childQueryAndContextNode.setQuerySpecificityDistanceSimilarity(qSpecificitySim);

		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.rdfd1, this.qR, this.rdfd2);
		float resultTypeSim = 1 - resulTtypeDist;
		childQueryAndContextNode.setQueryResultTypeSimilarity(resultTypeSim);

		log.debug("newResulttypeSimR = {}", resultTypeSim);

		// log.info("qSpecDistSimVarR " + Float.toString(qSpecDistVar));
		// log.info("qSpecDistSimTriplePatternR " + Float.toString(qSpecDistTP));
		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;
		// float recommentedQueryScore = ( (queryRootDistanceDegree * newQueryRootDist)
		// +
		// (resultTypeSimilarityDegree * newResulttypeSim) +
		// (querySpecificityDistanceDegree * (qSpecDistVar+qSpecDistTP)));

		// float recommentedQueryScore = ( ( newQueryRootDist) +
		// ( newResulttypeSim) +
		// ( (qSpecDistVar/qSpecDistSimTriplePattern)));
		// log.info("qSpecDistSimVar/qSpecDistSimTriplePattern " +
		// (qSpecDistSimVar+qSpecDistSimTriplePattern));
		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim));
		// float recommentedQueryScore = (querySpecificityDistanceDegree *
		// qSpecificitySim);
		// float recommentedQueryScore = (resultTypeSimilarityDegree * resultTypeSim);

		float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
				+ (resultTypeSimilarityDegree * resultTypeSim) + (querySpecificityDistanceDegree * (qSpecificitySim)));

		// float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDistSim)
		// + (resultTypeSimilarityDegree * resultTypeSim)
		// + (querySpecificityDistanceDegree * (qSpecificitySim)));
		// float recommentedQueryScore = (
		// ( newResulttypeSim) +
		// ( (qSpecDistSimVar+qSpecDistSimTriplePattern)));
		log.info("recommentedQueryScoreR " + recommentedQueryScore);
		childQueryAndContextNode.setqRScore(recommentedQueryScore);

		log.info("qR R " + clonedqR);

		return childQueryAndContextNode;
	}

	// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	// <http://purl.org/dc/terms/title> ),
	private List<QuerySolution> eliminateSolutionBindedToTheSameValue(List<QuerySolution> qSolList) {
		List<QuerySolution> output = new ArrayList<>();

		if (!qSolList.isEmpty()) {
			for (QuerySolution qs : qSolList) {
				ArrayList<String> valuesList = new ArrayList<>();
				Iterator<String> varIter = qs.varNames();
				while (varIter.hasNext()) {
					String varName = varIter.next();
					valuesList.add(qs.get(varName).toString());
				}

				if (valuesList.size() == 1) {
					output.add(qs);
				} else {
					String firstValue = valuesList.get(0);
					// log.info("firstValue " +firstValue);
					valuesList.remove(0);
					boolean isAllDifferent = true;
					Iterator<String> varValueIter = valuesList.iterator();
					while ((varValueIter.hasNext()) && (isAllDifferent)) {
						String varValue = varValueIter.next();
						// log.info("varValue" +varValue);
						if (varValue.equals(firstValue)) {
							isAllDifferent = false;
						}
					}
					if (isAllDifferent) {
						output.add(qs);
					}
				}
			}
			return output;
		} else {
			return qSolList;
		}

	}

	private String getEntityQo(Var tv) {
		String entityQo = "";
		String varName = tv.getVarName();

		if (varName.startsWith(CLASS_TEMPLATE_VAR)) {
			entityQo = this.classVarTable.getClassFromVar(varName);
		} else if (varName.startsWith(OBJ_PROP_TEMPLATE_VAR)) {
			entityQo = this.objectProperyVarTable.getObjectProperyFromVar(varName);
		} else if (varName.startsWith(DT_PROP_TEMPLATE_VAR)) {
			entityQo = this.datatypePropertyVarTable.getDatatypeProperyFromVar(varName);
		}
		return entityQo;
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

	private QueryAndContextNode getMaxQueryAndContextNode() {

		// Collections.sort(this.specializableQueryAndContextNodeList, new
		// QueryAndContextNode.QRScoreComparator());
		if (this.specializableQueryAndContextNodeList.size() > 0) {
			QueryAndContextNode maxNode = this.specializableQueryAndContextNodeList.get(0);
			this.specializableQueryAndContextNodeList.remove(maxNode);
			return maxNode;
		}
		return null;
	}

	private Set<Var> getQueryTemplateVariableSet(Query qR) {
		SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
		// ... This will walk through all parts of the query
		ElementWalker.walk(qR.getQueryPattern(), v);
		return v.getQueryTemplateVariableSet();

	}

	private Set<TriplePath> getQueryTriplePathSet(Query q) {
		if (q == null) {
			throw new IllegalStateException(
					"[QueryRecommendation::getTriplePathSet(Query originalQuery)]The query is null!!");
		}
		// Remember distinct objects in this
		final Set<TriplePath> tpSet = new HashSet<TriplePath>();
		// This will walk through all parts of the query
		ElementWalker.walk(q.getQueryPattern(),
				// For each element
				new ElementVisitorBase() {
					// ...when it's a block of triples...
					public void visit(ElementPathBlock el) {
						// ...go through all the triples...
						Iterator<TriplePath> triples = el.patternElts();
						while (triples.hasNext()) {
							tpSet.add(triples.next());
						}
					}
				});
		return tpSet;
	}

	private boolean isIProcessable(QueryAndContextNode qRScoreMaxNodeCloned) {
		Set<Var> tempVarSet = getQueryTemplateVariableSet(qRScoreMaxNodeCloned.getqR());
		return tempVarSet.size() > 0;
	}

	private boolean isQueryIndexed(Query qWithoutTriple) {
		Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
		ArrayList<String> s = new ArrayList<String>(); // and use Collections.sort()
		for (TriplePath tp : triplePathCollection) {
			s.add(tp.toString());
		}
		Collections.sort(s);
		return queryIndex.containsKey(s.toString());
	}

	private void printMap(Map<Var, Set<RDFNode>> tmpMap) {

		System.out.println("[QueryTempVarSolutionSpace::printMap]");

		if (tmpMap != null) {
			Iterator<Map.Entry<Var, Set<RDFNode>>> iter = tmpMap.entrySet().iterator();

			while (iter.hasNext()) {
				Map.Entry<Var, Set<RDFNode>> entry = iter.next();
				System.out.println("Var= " + entry.getKey().asNode().getName());
				Set<RDFNode> valuList = entry.getValue();

				System.out.println("CardinalitySet= " + valuList.size());

				// for (RDFNode value : valuList) {
				// System.out.println("Value= " + value.toString());
				// }
			}

		}

	}

	private void printQueryChildNodeSolutionSpace(QueryAndContextNode parentQueryAndContextNode) {
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("Query === ");
		System.out.println(parentQueryAndContextNode.getqR().toString());

		System.out.println("Solution === ");

		List<QuerySolution> solList = parentQueryAndContextNode.getQueryTempVarSolutionSpace();
		for (QuerySolution qSol : solList) {
			System.out.println(qSol.toString());
		}
	}

	private void printQuerySolutionSpace(QueryAndContextNode qRScoreMaxNode) {

		List<QuerySolution> qTsolList = qRScoreMaxNode.getQueryTempVarSolutionSpace();

		// System.out.println("[QuerySpecializer::printQuerySolutionSpace] There are " +
		// qTsolList.size() + " solutions");
		if (qTsolList != null) {
			System.out.println(
					"[QuerySpecializer::printQuerySolutionSpace] There are " + qTsolList.size() + " solutions");

			// Set<Var> tempVarSet = qRScoreMaxNode.getqRTemplateVariableSet();
			Set<Var> tempVarSet = getQueryTemplateVariableSet(qRScoreMaxNode.getqR());// .getqRTemplateVariableSet();

			for (QuerySolution sol : qTsolList) {
				for (Var vt : tempVarSet) {
					System.out.println("[QuerySpecializer::printQuerySolutionSpace] " + vt.getName() + "="
							+ sol.get(vt.getName()).toString());
				}
				System.out.println("");
			}

		}

		//
		// for (QuerySolution sol : qTsolList) {
		// Iterator<String> varNameItr = sol.varNames();
		// while (varNameItr.hasNext()) {
		// System.out.println("[QuerySpecializer::specialize] varNameItr.next() == " +
		// varNameItr.next());
		// }
		//
		//// if (sol.get("class").asResource().getURI() != null) {
		//// this.classSet.add(sol.get("class").asResource().getURI());
		//// }
		// }
	}

	private void printQuerySolutionSpaceMap(QueryAndContextNode parentQueryAndContextNode) {

		System.out.println("Query Child === ");
		System.out.println(parentQueryAndContextNode.getqR().toString());

		System.out.println("Query Child Template Var === ");

		Map<Var, Set<RDFNode>> tmpMap = parentQueryAndContextNode.getQueryTempVarValueMap();
		printMap(tmpMap);

		// if (tmpMap!=null) {
		// Iterator<Entry<Var, Set<RDFNode>>> iter = tmpMap.entrySet().iterator();
		//
		// while (iter.hasNext()) {
		// Entry<Var, Set<RDFNode>> entry = iter.next();
		// System.out.println("Var= " + entry.getKey().asNode().getName());
		// Set<RDFNode> valuList = entry.getValue();
		// for (RDFNode value : valuList) {
		// System.out.println("Value= " + value.asNode().getName());
		// }
		// }
		//
		// }
	}

	/*
	 * Part of a desperate attempt to reduce the amount of code in specialize()
	 */
	private void printSpecialize(QueryAndContextNode node) throws IOException {
		log.info(new File(fullPrefix).getAbsolutePath());
		String newTextFile = fullPrefix + Integer.toString(QuerySpecializer4.testResultIndex) + ".txt";
		log.info("newTextFile 2 =======" + newTextFile);
		long qRArrivalTime = System.currentTimeMillis();
		FileWriter fw = new FileWriter(newTextFile, true);
		fw.write("\n");
		fw.write("\n");
		fw.write("qR Arrival Time == " + qRArrivalTime);
		fw.write("\n");
		fw.write("qR Query Elapsed Time == " + (qRArrivalTime - startTime));
		fw.write("\n");
		fw.write("qR score == " + node.getqRScore());
		fw.write("\n");
		fw.write(node.getqR().toString());
		stopTime = System.currentTimeMillis(); // Do we really need to measure it again here?
		fw.write("\n");
		fw.write("\n");
		fw.write("Process Stop Time == " + stopTime);
		fw.write("\n");
		fw.write("Process Elapsed Time == " + (stopTime - startTime));
		fw.write("\n");
		fw.close();

	}

}
