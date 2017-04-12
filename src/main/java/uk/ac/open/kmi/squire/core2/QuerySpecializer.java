/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
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
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQVariableVisitor;
import uk.ac.open.kmi.squire.treequerypatterns.DataNode;
import uk.ac.open.kmi.squire.treequerypatterns.TreeNode;

/**
 *
 * @author carloallocca
 */
public class QuerySpecializer {

    private static String INSTANCE_OP = "I";
    private static String REMOVE_TP_OP = "R";

    private final List<QueryAndContextNode> specializableQueryList = new ArrayList();
    private final List<QueryAndContextNode> recommandedQueryList = new ArrayList();

    private HashMap<String, Query> queryIndex = new HashMap<>();

    private IRDFDataset rdfd1;
    private IRDFDataset rdfd2;

    private Query qO;
    private Query qR;

    private float resultTypeSimilarityDegree;
    private float queryRootDistanceDegree;
    private float resultSizeSimilarityDegree;
    private float querySpecificityDistanceDegree;

    private LiteralVarMapping literalVarTable;
    private ClassVarMapping classVarTable;
    private DatatypePropertyVarMapping datatypePropertyVarTable;
    private IndividualVarMapping individualVarTable;
    private ObjectPropertyVarMapping objectProperyVarTable;
    private RDFVocVarMapping rdfVocVarTable;

    private static final String CLASS_TEMPLATE_VAR = "ct";
    private static final String OBJ_PROP_TEMPLATE_VAR = "opt";
    private static final String DT_PROP_TEMPLATE_VAR = "dpt";
    private static final String INDIVIDUAL_TEMPLATE_VAR = "it";
    private static final String LITERAL_TEMPLATE_VAR = "lt";

    public QuerySpecializer() {
        super();
    }

    public QuerySpecializer(Query qo, Query qr, IRDFDataset d1, IRDFDataset d2,
            ClassVarMapping cVM,
            ObjectPropertyVarMapping opVM,
            DatatypePropertyVarMapping dpVM,
            IndividualVarMapping indVM,
            LiteralVarMapping lVM,
            RDFVocVarMapping rdfVM,
            float resultTypeSimilarityDegree,
            float queryRootDistanceDegree,
            float resultSizeSimilarityDegree,
            float querySpecificityDistanceDegree) {

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

        this.resultSizeSimilarityDegree = resultTypeSimilarityDegree;
        this.queryRootDistanceDegree = queryRootDistanceDegree;
        this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
        this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;

        //A. Compute the query recommentedQueryScore:
        // 1)...QueryRootDistance
        float queryRootDist = 0;

        // 2)...QueryResultTypeSimilarity
        QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
        float resulttypeSim = qRTS.computeQueryResultTypeSim(this.qO, this.rdfd1, this.qR, this.rdfd2);

        // 3)...QuerySpecificityDistance        
        QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
        float qSpecDistSimVar = qSpecDist.computeQuerySpecificityDistanceWRTQueryVariable(this.qO, this.qR);
        float qSpecDistSimTriplePattern = qSpecDist.computeQuerySpecificityDistanceWRTQueryTriplePatter(this.qO, this.qR);

        // 4)...QueryResultSizeSimilarity        
        float queryResultSizeSimilarity = 0;

        float recommentedQueryScore = ((queryRootDistanceDegree * queryRootDist) + (resultTypeSimilarityDegree * resulttypeSim) + (querySpecificityDistanceDegree * (qSpecDistSimVar + qSpecDistSimTriplePattern)));
        //float recommentedQueryScore = resulttypeSim + qSpecDistSimVar+qSpecDistSimTriplePattern;//This is working as it should but it does not consider the similarity distance between the replased entities

        String op = ""; //It can be either R (for Removal) or I (Instanciation).
        ArrayList<String> operationList = new ArrayList();

        //B. Compute the qRTemplateVariableSet and qRTriplePatternSet            
        Set<Var> qRTemplateVariableSet = getQueryTemplateVariableSet(this.qR);
        Set<TriplePath> qRTriplePatternSet = getQueryTriplePathSet(this.qR);

        //C. Compute the QueryTempVarSolutionSpace
        QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
        List<QuerySolution> qTsol = temVarValueSpace.compute(qr, this.rdfd2);

        //Map<Var, Set<RDFNode>> qTsolMap=temVarValueSpace.compute(qr, this.rdfd2, null);
        //D. Build the QueryAndContextNode from the query
        QueryAndContextNode qAndcNode = new QueryAndContextNode();

        //...set the original query and the recommendated query;
        qAndcNode.setqO(qo);
        qAndcNode.setqR(qr);

        qAndcNode.setRdfD1(d1);
        qAndcNode.setRdfD2(d2);

        qAndcNode.setEntityqO("");
        qAndcNode.setEntityqR("");
        
        
//...SET THE CLASS, OBJECT AND DATATYPE PROPERTIES SETs...;        
//        qAndcNode.setcSetD2(d2.getClassSet());
//        qAndcNode.setDpSetD2(d2.getDatatypePropertySet());
//        qAndcNode.setOpSetD2(d2.getObjectPropertySet());
//        qAndcNode.setlSetD2(d2.getLiteralSet());
//        qAndcNode.setIndSetD2(d2.getIndividualSet());
//        // As we have the issue of indexing long String when merging dpPropertySet and opPropertySet, I do not index and I do their merging here
//        ArrayList<String> propertySet = new ArrayList();
//        propertySet.addAll(d2.getDatatypePropertySet());
//        propertySet.addAll(d2.getObjectPropertySet());
//        d2.setPropertySet(propertySet);
//        qAndcNode.setpSetD2(propertySet);
//        qAndcNode.setRdfVD2(d2.getRDFVocabulary());

        //...set the score measurements
        qAndcNode.setQueryRootDistance(queryRootDist); // do also for the other measurements, compute them...
        qAndcNode.setQueryResultTypeSimilarity(resulttypeSim);
        qAndcNode.setQuerySpecificityDistance(qSpecDistSimVar + qSpecDistSimTriplePattern);
        qAndcNode.setQueryResultSizeSimilarity(queryResultSizeSimilarity);
        qAndcNode.setqRScore(recommentedQueryScore);

        qAndcNode.setOp(op);
        qAndcNode.setOperationList(operationList);

//        qAndcNode.setqRTemplateVariableSet(qRTemplateVariableSet);
//        qAndcNode.setqRTriplePathSet(qRTriplePatternSet);
        //...set the QueryTempVarSolutionSpace
        qAndcNode.setSolutionSpace(qTsol);
        //qAndcNode.setQueryTempVarValueMap(qTsolMap);

        //E. Sorted Insert of the QueryAndContextNode into the specializableQueryList
        //specializableQueryList.add(qAndcNode);
        specializableQueryListInsertSorted(qAndcNode);

//        this.recommandedQueryList.add(qAndcNode);
    }

    public List<QueryAndContextNode> specialize() throws Exception {
        System.out.println("");
        System.out.println("[QuerySpecializer::specialize] SPECIALIZATION PROCESS RUNNING ...");
        System.out.println("");

        while (this.specializableQueryList.size() > 0) {

            // 1. Get and Remove the QueryAndContextNode with qRScore max
            QueryAndContextNode parentQueryAndContextNode = getMaxQueryAndContextNode();

            // 2. Store the QueryAndContextNode with qRScore max into the recommandedQueryList as it could be one of the recommendated query
            this.recommandedQueryList.add(parentQueryAndContextNode);

//            // 3. check if we can apply a removal operation;
//            if (isRProcessable(parentQueryAndContextNode)) {
//                isRprocessable = true;
//                Query parentqRCopy = QueryFactory.create(parentQueryAndContextNode.getqR());
//                Set<TriplePath> triplePathSet = getQueryTriplePathSet(parentqRCopy);
//
//                // 4.1 Apply removal operations
//                for (TriplePath tp : triplePathSet) {
//                    // 4.1.1. Remove the TriplePath tp from the  parentqRCopy                    
//                    Query qToProcess = QueryFactory.create(parentqRCopy);
//
//                    RemoveTriple instance = new RemoveTriple();
//                    Query qWithoutTriple = instance.removeTP(qToProcess, tp.asTriple());
//
//                    // 4.1.2. Check if it is alredy indexed and therefore generated
//                    if (!(isQueryIndexed(qWithoutTriple))) {
//                        //...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...
//                        SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
//                        if (qs.isSatisfiableWRTResults(qWithoutTriple, rdfd2)) {
//                            QueryAndContextNode childNode = createNewQueryAndContextNodeForRemovalOp(qWithoutTriple, parentQueryAndContextNode);
//                            specializableQueryListInsertSorted(childNode);
//                            //add qWithoutTriple to the index
//                            addQueryToIndexIFAbsent(qWithoutTriple);
//                            //printQuerySolutionSpaceMap(parentQueryAndContextNode);
//                        } else {
//                            addQueryToIndexIFAbsent(qWithoutTriple);
//                        }
//                    }
//                }
//            }

            // 4. check if we can apply a instanciation operation;
            if (isIProcessable(parentQueryAndContextNode)) {
                
                Query queryChild = QueryFactory.create(parentQueryAndContextNode.getqR());
                List<QuerySolution> qSolList = parentQueryAndContextNode.getQueryTempVarSolutionSpace();

                for (QuerySolution sol : qSolList) {
                    Query childQueryCopy = QueryFactory.create(queryChild.toString());

                    //[ REPLACED ] Query childQueryCopyInstanciated= applyInstanciationOP(childQueryCopy, sol);
                    Set<Var> qTempVarSet = getQueryTemplateVariableSet(childQueryCopy);
                    Query childQueryCopyInstanciated = null;

                    ArrayList<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList = new ArrayList(); // this contains all the tuples <varName, entityQo, entityQr> for each varName that is going to be instantiated
                    for (Var tv : qTempVarSet) {
                        RDFNode node = sol.get(tv.getName());
                        SPARQLQueryInstantiation instOP = new SPARQLQueryInstantiation();
                        childQueryCopyInstanciated = instOP.instantiateFromVarTemplateToNode(childQueryCopy, tv, node.asNode());

                        String entityQo = getEntityQo(tv);
                        String entityQr = node.asNode().getURI(); //as it is the name of a concrete node and not of a variable;
                        VarTemplateAndEntityQoQr item = new VarTemplateAndEntityQoQr(tv, entityQo, entityQr);
                        templVarEntityQoQrInstanciatedList.add(item);
                    }
                    if (childQueryCopyInstanciated != null) {
                        // 4.1.2. Check if it is alredy indexed and therefore generated
                        if (!(isQueryIndexed(childQueryCopyInstanciated))) {
                            //add qWithoutTriple to the index
                            addQueryToIndexIFAbsent(childQueryCopyInstanciated);

                            //...checking if the qWithoutTriple is satisfiable w.r.t. D2 ...
                            SPARQLQuerySatisfiable qs = new SPARQLQuerySatisfiable();
                            if (qs.isSatisfiableWRTResults(childQueryCopyInstanciated, rdfd2)) {
                                QueryAndContextNode childNode = 
                                        createNewQueryAndContextNodeForInstanciateOp(childQueryCopyInstanciated, parentQueryAndContextNode, templVarEntityQoQrInstanciatedList);
                                specializableQueryListInsertSorted(childNode);
                                //add qWithoutTriple to the index
                                addQueryToIndexIFAbsent(childQueryCopyInstanciated);
                                //printQuerySolutionSpaceMap(parentQueryAndContextNode);
                            } else {
                                addQueryToIndexIFAbsent(childQueryCopyInstanciated);
                            }
                        }
                    }
                }
            }

//            if (isRprocessable == false && isIprocessable == false) {
//                this.recommandedQueryList.add(qRScoreMaxNode);
//            }
        }
        return this.recommandedQueryList;
    }
    
    
    private QueryAndContextNode createNewQueryAndContextNodeForInstanciateOp(
                                                                            Query childQueryCopyInstanciated, 
                                                                            QueryAndContextNode parentQueryAndContextNode, 
                                                                            ArrayList<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList) throws Exception {
        QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();
        //...set the original query and the recommendated query;
        Query clonedqO = QueryFactory.create(parentQueryAndContextNode.getqO());
        childQueryAndContextNode.setqO(clonedqO);

        Query clonedqR = QueryFactory.create(childQueryCopyInstanciated.toString());
        childQueryAndContextNode.setqR(clonedqR);

        
        
        //..set the RDF dataset 1
        IRDFDataset rdfD1 = parentQueryAndContextNode.getRdfD1();
        if (rdfD1 instanceof SPARQLEndPoint) {
            IRDFDataset newRdfD1 = new SPARQLEndPoint(((String) parentQueryAndContextNode.getRdfD1().getEndPointURL()),
                    (String) parentQueryAndContextNode.getRdfD1().getGraph());
            childQueryAndContextNode.setRdfD1(newRdfD1);
        } else { // TO ADD the case of FILEBASED dataset
        }
        //..set the RDF dataset 2
        IRDFDataset rdfD2 = parentQueryAndContextNode.getRdfD2();
        if (rdfD2 instanceof SPARQLEndPoint) {
            IRDFDataset newRdfD2 = new SPARQLEndPoint(((String) parentQueryAndContextNode.getRdfD2().getEndPointURL()),
                    (String) parentQueryAndContextNode.getRdfD2().getGraph());
            childQueryAndContextNode.setRdfD2(newRdfD2);
//            //C. Compute the QueryTempVarSolutionSpace
//            QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
//            // [REPLACED]    List<QuerySolution> qTsolMap = temVarValueSpace.compute(clonedqR, this.rdfd2);
//            //Map<Var, Set<RDFNode>> qTsolMap = temVarValueSpace.compute(clonedqR, this.rdfd2, null);
//            childQueryAndContextNode.setQueryTempVarValueMap(qTsolMap);
            List<QuerySolution> qTsol = parentQueryAndContextNode.getQueryTempVarSolutionSpace();
            List<QuerySolution> qTsolChild = new ArrayList();
            qTsolChild.addAll(qTsol);
            childQueryAndContextNode.setSolutionSpace(qTsolChild);
        } else { // TO ADD the case of FILEBASED dataset
        }

//        //...SET THE CLASS, OBJECT AND DATATYPE PROPERTIES SETs...;        
//        ArrayList<String> clonedcSetD2 = new ArrayList();
//        clonedcSetD2.addAll(parentQueryAndContextNode.getRdfD2().getClassSet());
//        childQueryAndContextNode.setcSetD2(clonedcSetD2);
//
//        ArrayList<String> clonedDpSetD2 = new ArrayList();
//        clonedDpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getDatatypePropertySet());
//        childQueryAndContextNode.setDpSetD2(clonedDpSetD2);
//
//        ArrayList<String> clonedOpSetD2 = new ArrayList();
//        clonedOpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getObjectPropertySet());
//        childQueryAndContextNode.setOpSetD2(clonedOpSetD2);
//
//        ArrayList<String> clonedlSetD2 = new ArrayList();
//        clonedlSetD2.addAll(parentQueryAndContextNode.getRdfD2().getLiteralSet());
//        childQueryAndContextNode.setlSetD2(clonedlSetD2);
//
//        ArrayList<String> clonedIndSetD2 = new ArrayList();
//        clonedIndSetD2.addAll(parentQueryAndContextNode.getRdfD2().getIndividualSet());
//        childQueryAndContextNode.setIndSetD2(clonedIndSetD2);
//
//        ArrayList<String> clonedpSetD2 = new ArrayList();
//        clonedpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getPropertySet());
//        childQueryAndContextNode.setpSetD2(clonedpSetD2);
//
//        ArrayList<String> clonedRdfVSetD2 = new ArrayList();
//        clonedRdfVSetD2.addAll(parentQueryAndContextNode.getRdfD2().getRDFVocabulary());
//        childQueryAndContextNode.setRdfVD2(clonedRdfVSetD2);

        //...set the openration list
        ArrayList<String> clonedOperationList = new ArrayList();
        clonedOperationList.addAll(parentQueryAndContextNode.getOperationList());
        clonedOperationList.add(INSTANCE_OP);
        childQueryAndContextNode.setOperationList(clonedOperationList);

        childQueryAndContextNode.setOp(INSTANCE_OP);
        //...set the score measurements

        //A. Compute the query recommentedQueryScore:
        // 1)...QueryRootDistance
        float newQueryRootDist
                = parentQueryAndContextNode.getQueryRootDistance()
                + computeInstanciationOperationCost(templVarEntityQoQrInstanciatedList);                
        childQueryAndContextNode.setQueryRootDistance(newQueryRootDist);
        
        // 2)...QueryResultTypeSimilarity
        QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
        float newResulttypeSim = qRTS.computeQueryResultTypeSim(childQueryAndContextNode.getqO(), this.rdfd1, childQueryAndContextNode.getqR(), this.rdfd2);
        childQueryAndContextNode.setQueryResultTypeSimilarity(newResulttypeSim);
        // 3)...QuerySpecificityDistance        
        QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
        float qSpecDistSimVar = qSpecDist.computeQuerySpecificityDistanceWRTQueryVariable(childQueryAndContextNode.getqO(), childQueryAndContextNode.getqR());
        float qSpecDistSimTriplePattern = qSpecDist.computeQuerySpecificityDistanceWRTQueryTriplePatter(childQueryAndContextNode.getqO(), childQueryAndContextNode.getqR());
        childQueryAndContextNode.setQuerySpecificityDistance(qSpecDistSimVar + qSpecDistSimTriplePattern);
        // 4)...QueryResultSizeSimilarity        
        float queryResultSizeSimilarity = 0;
        float recommentedQueryScore = ((queryRootDistanceDegree * newQueryRootDist) + (resultTypeSimilarityDegree * newResulttypeSim) + (querySpecificityDistanceDegree * (qSpecDistSimVar + qSpecDistSimTriplePattern)));
        childQueryAndContextNode.setqRScore(recommentedQueryScore);

        return childQueryAndContextNode;
        
    }

    private float computeInstanciationOperationCost(ArrayList<VarTemplateAndEntityQoQr> templVarEntityQoQrInstanciatedList) {
        int size=templVarEntityQoQrInstanciatedList.size();
        float nodeCost=0;
        if(size>0){
            for(VarTemplateAndEntityQoQr item:templVarEntityQoQrInstanciatedList){
                String entityqO_TMP = getLocalName(item.getEntityQo());
                String entityqR_TMP = getLocalName(item.getEntityQr());
                nodeCost = nodeCost+ computeInstanciateOperationCost(entityqO_TMP, entityqR_TMP);                
            }
        }
        return nodeCost/size;
    }

    private float computeInstanciateOperationCost(String entityqO, String entityqR) {
        if (entityqO == null || entityqR == null) {
            return (float) 0.0;
        }
        JaroWinklerSimilarity jwSim = new JaroWinklerSimilarity();
        float sim = jwSim.computeMatchingScore(entityqO, entityqR);
        //return (float) (1.0 - sim);
        return sim;
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

    private Query applyInstanciationOP(Query queryChild, QuerySolution sol) {

        Set<Var> qTempVarSet = getQueryTemplateVariableSet(queryChild);
        for (Var tv : qTempVarSet) {
            RDFNode node = sol.get(tv.getName());
            SPARQLQueryInstantiation instOP = new SPARQLQueryInstantiation();
            queryChild = instOP.instantiateFromVarTemplateToNode(queryChild, tv, node.asNode());

//            String entityqO = this.classVarTable.getClassFromVar(templateVar.getVarName());
//            String entityqR = clas;
//            
//            ArrayList<String> childOperationList = new ArrayList();
//            childOperationList.addAll(pNode.getOperationList());
//            childOperationList.add(INSTANCE_OP);
//            String op = INSTANCE_OP;
        }
        return queryChild;
    }

    private QueryAndContextNode createNewQueryAndContextNodeForRemovalOp(Query qWithoutTriple, QueryAndContextNode parentQueryAndContextNode) throws Exception {
        QueryAndContextNode childQueryAndContextNode = new QueryAndContextNode();
        //...set the original query and the recommendated query;
        Query clonedqO = QueryFactory.create(parentQueryAndContextNode.getqO());
        childQueryAndContextNode.setqO(clonedqO);

        Query clonedqR = QueryFactory.create(qWithoutTriple.toString());
        childQueryAndContextNode.setqR(clonedqR);

        //...set the entities: EntityqO and EntityqR
        childQueryAndContextNode.setEntityqO("");
        childQueryAndContextNode.setEntityqR("");
        //..set the RDF dataset 1
        IRDFDataset rdfD1 = parentQueryAndContextNode.getRdfD1();
        if (rdfD1 instanceof SPARQLEndPoint) {
            IRDFDataset newRdfD1 = new SPARQLEndPoint(((String) parentQueryAndContextNode.getRdfD1().getEndPointURL()),
                    (String) parentQueryAndContextNode.getRdfD1().getGraph());
            childQueryAndContextNode.setRdfD1(newRdfD1);
        } else { // TO ADD the case of FILEBASED dataset
        }
        //..set the RDF dataset 2
        IRDFDataset rdfD2 = parentQueryAndContextNode.getRdfD2();
        if (rdfD2 instanceof SPARQLEndPoint) {
            IRDFDataset newRdfD2 = new SPARQLEndPoint(((String) parentQueryAndContextNode.getRdfD2().getEndPointURL()),
                    (String) parentQueryAndContextNode.getRdfD2().getGraph());
            childQueryAndContextNode.setRdfD2(newRdfD2);
//            //C. Compute the QueryTempVarSolutionSpace
//            QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
//            // [REPLACED]    List<QuerySolution> qTsolMap = temVarValueSpace.compute(clonedqR, this.rdfd2);
//            //Map<Var, Set<RDFNode>> qTsolMap = temVarValueSpace.compute(clonedqR, this.rdfd2, null);
//            childQueryAndContextNode.setQueryTempVarValueMap(qTsolMap);
            List<QuerySolution> qTsol = parentQueryAndContextNode.getQueryTempVarSolutionSpace();
            List<QuerySolution> qTsolChild = new ArrayList();
            qTsolChild.addAll(qTsol);
            childQueryAndContextNode.setSolutionSpace(qTsolChild);
        } else { // TO ADD the case of FILEBASED dataset
        }

        //...set the set of classes, object property, datatype property,...;        
        ArrayList<String> clonedcSetD2 = new ArrayList();
        clonedcSetD2.addAll(parentQueryAndContextNode.getRdfD2().getClassSet());
        childQueryAndContextNode.setcSetD2(clonedcSetD2);

        ArrayList<String> clonedDpSetD2 = new ArrayList();
        clonedDpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getDatatypePropertySet());
        childQueryAndContextNode.setDpSetD2(clonedDpSetD2);

        ArrayList<String> clonedOpSetD2 = new ArrayList();
        clonedOpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getObjectPropertySet());
        childQueryAndContextNode.setOpSetD2(clonedOpSetD2);

        ArrayList<String> clonedlSetD2 = new ArrayList();
        clonedlSetD2.addAll(parentQueryAndContextNode.getRdfD2().getLiteralSet());
        childQueryAndContextNode.setlSetD2(clonedlSetD2);

        ArrayList<String> clonedIndSetD2 = new ArrayList();
        clonedIndSetD2.addAll(parentQueryAndContextNode.getRdfD2().getIndividualSet());
        childQueryAndContextNode.setIndSetD2(clonedIndSetD2);

        ArrayList<String> clonedpSetD2 = new ArrayList();
        clonedpSetD2.addAll(parentQueryAndContextNode.getRdfD2().getPropertySet());
        childQueryAndContextNode.setpSetD2(clonedpSetD2);

        ArrayList<String> clonedRdfVSetD2 = new ArrayList();
        clonedRdfVSetD2.addAll(parentQueryAndContextNode.getRdfD2().getRDFVocabulary());
        childQueryAndContextNode.setRdfVD2(clonedRdfVSetD2);

        //...set the openration list
        ArrayList<String> clonedOperationList = new ArrayList();
        clonedOperationList.addAll(parentQueryAndContextNode.getOperationList());
        clonedOperationList.add(REMOVE_TP_OP);
        childQueryAndContextNode.setOperationList(clonedOperationList);

        childQueryAndContextNode.setOp(REMOVE_TP_OP);
        //...set the score measurements

        //A. Compute the query recommentedQueryScore:
        // 1)...QueryRootDistance
        float newQueryRootDist
                = parentQueryAndContextNode.getQueryRootDistance()
                + computeRemoveOperationCost(childQueryAndContextNode.getqO(), childQueryAndContextNode.getqR());
        childQueryAndContextNode.setQueryRootDistance(newQueryRootDist);
        // 2)...QueryResultTypeSimilarity
        QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
        float newResulttypeSim = qRTS.computeQueryResultTypeSim(childQueryAndContextNode.getqO(), this.rdfd1, childQueryAndContextNode.getqR(), this.rdfd2);
        childQueryAndContextNode.setQueryResultTypeSimilarity(newResulttypeSim);
        // 3)...QuerySpecificityDistance        
        QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
        float qSpecDistSimVar = qSpecDist.computeQuerySpecificityDistanceWRTQueryVariable(childQueryAndContextNode.getqO(), childQueryAndContextNode.getqR());
        float qSpecDistSimTriplePattern = qSpecDist.computeQuerySpecificityDistanceWRTQueryTriplePatter(childQueryAndContextNode.getqO(), childQueryAndContextNode.getqR());
        childQueryAndContextNode.setQuerySpecificityDistance(qSpecDistSimVar + qSpecDistSimTriplePattern);
        // 4)...QueryResultSizeSimilarity        
        float queryResultSizeSimilarity = 0;
        float recommentedQueryScore = ((queryRootDistanceDegree * newQueryRootDist) + (resultTypeSimilarityDegree * newResulttypeSim) + (querySpecificityDistanceDegree * (qSpecDistSimVar + qSpecDistSimTriplePattern)));
        childQueryAndContextNode.setqRScore(recommentedQueryScore);

        return childQueryAndContextNode;
    }

    public List<QueryAndContextNode> getRecommandedQueryList() {
        return recommandedQueryList;
    }

    private void specializableQueryListInsertSorted(QueryAndContextNode d1) {
        this.specializableQueryList.add(d1);
        Collections.sort(this.specializableQueryList, new QueryAndContextNode.QRScoreComparator());
    }

    private Set<TriplePath> getQueryTriplePathSet(Query q) {
        if (q == null) {
            throw new IllegalStateException("[QueryRecommendation::getTriplePathSet(Query originalQuery)]The query is null!!");
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
        }
        );
        return tpSet;
    }

    private Set<Var> getQueryTemplateVariableSet(Query qR) {
        SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
        //... This will walk through all parts of the query
        ElementWalker.walk(qR.getQueryPattern(), v);
        return v.getQueryTemplateVariableSet();

    }

    private void printQuerySolutionSpace(QueryAndContextNode qRScoreMaxNode) {

        List<QuerySolution> qTsolList = qRScoreMaxNode.getQueryTempVarSolutionSpace();

//            System.out.println("[QuerySpecializer::printQuerySolutionSpace] There are " + qTsolList.size() + " solutions");
        if (qTsolList != null) {
            System.out.println("[QuerySpecializer::printQuerySolutionSpace] There are " + qTsolList.size() + " solutions");

//        Set<Var> tempVarSet = qRScoreMaxNode.getqRTemplateVariableSet();
            Set<Var> tempVarSet = getQueryTemplateVariableSet(qRScoreMaxNode.getqR());//.getqRTemplateVariableSet();

            for (QuerySolution sol : qTsolList) {
                for (Var vt : tempVarSet) {
                    System.out.println("[QuerySpecializer::printQuerySolutionSpace] " + vt.getName() + "=" + sol.get(vt.getName()).toString());
                }
                System.out.println("");
            }

        }

//        
//        for (QuerySolution sol : qTsolList) {
//            Iterator<String> varNameItr = sol.varNames();
//            while (varNameItr.hasNext()) {
//                System.out.println("[QuerySpecializer::specialize] varNameItr.next() == " + varNameItr.next());
//            }
//
////                if (sol.get("class").asResource().getURI() != null) {
////                    this.classSet.add(sol.get("class").asResource().getURI());
////                }
//        }
    }

    private QueryAndContextNode getMaxQueryAndContextNode() {
        QueryAndContextNode maxNode = this.specializableQueryList.get(0);
        this.specializableQueryList.remove(maxNode);
        return maxNode;
    }

    private boolean isRProcessable(QueryAndContextNode qRScoreMaxNodeCloned) {
        Query q = qRScoreMaxNodeCloned.getqR();
        return getQueryTriplePathSet(q).size() > 1;
    }

    private boolean isIProcessable(QueryAndContextNode qRScoreMaxNodeCloned) {
        Set<Var> tempVarSet = getQueryTemplateVariableSet(qRScoreMaxNodeCloned.getqR());
        return tempVarSet.size() > 0;
    }

    private void applyRemovalOp(QueryAndContextNode qRScoreMaxNode) {

        Query qRCopy = qRScoreMaxNode.getqR();
        Set<TriplePath> triplePathSet = getQueryTriplePathSet(qRCopy);

        for (TriplePath tp : triplePathSet) {

            // 1. Remove the TriplePath tp from the  qRCopy
            RemoveTriple instance = new RemoveTriple();
            Query qWithoutTriple = instance.removeTP(qRCopy, tp.asTriple());

            // 2. Check if it is alredy indexed and therefore generated
            if (!(isQueryIndexed(qWithoutTriple))) {

//                // BUILD A NEW QueryAndContextNode 
//                // 2.1. Clone the QueryAndContextNode with qRScore max so it can be processed for applying operations
//                QueryAndContextNode qRScoreMaxNodeCloned = qRScoreMaxNode.cloneMe(qRScoreMaxNode);
//
//                // 2.2 Update the triplePathSet of the clonedNode
//                qRScoreMaxNodeCloned.getqRTriplePathSet().remove(tp);
//                
//                // 2.3. Update the recommended query of the clonedNode
//                qRScoreMaxNodeCloned.setqR(qWithoutTriple);
//                
//                // 2.4. devo fare tutti gli aggiornameti:  operation list, le quattro misure, etc...
//                ArrayList<String> clonedOperationList = new ArrayList();
//                clonedOperationList.addAll(qRScoreMaxNode.getOperationList());
//                clonedOperationList.add(REMOVE_TP_OP);
//                qRScoreMaxNodeCloned.setOperationList(clonedOperationList);
//
//                qRScoreMaxNodeCloned.setOp(REMOVE_TP_OP);
//
//                //In generale guarda dall'altra procedura per capire cosa manca
//                
//                // 7. devo aggiungere to specializableQueryList in order to be further specialized
//                specializableQueryListInsertSorted(qRScoreMaxNodeCloned);
//            
//                this.recommandedQueryList.add(qRScoreMaxNodeCloned);
//                
//                // 8. Add node to the index
//                addQueryToIndexIFAbsent(qWithoutTriple);
            }

        }

    }

//    private boolean isQueryIndexed(QueryAndContextNode qRScoreMaxNodeCloned) {    
//        Query q = qRScoreMaxNodeCloned.getqR();
//        Set<TriplePath> triplePathCollection = qRScoreMaxNodeCloned.getqRTriplePathSet();
//        ArrayList<String> s = new ArrayList<String>(); //and use Collections.sort()
//        for (TriplePath tp : triplePathCollection) {
//            s.add(tp.toString());
//        }
//        Collections.sort(s);
//        return queryAndContextNodeIndex.containsKey(s.toString());
//    }
    private void addQueryToIndexIFAbsent(Query qWithoutTriple) {

        Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);

        ArrayList<String> s = new ArrayList<String>(); //and use Collections.sort()
        for (TriplePath tp : triplePathCollection) {
            s.add(tp.toString());
        }
        queryIndex.putIfAbsent(s.toString(), qWithoutTriple);
    }

    private boolean isQueryIndexed(Query qWithoutTriple) {
        Set<TriplePath> triplePathCollection = getQueryTriplePathSet(qWithoutTriple);
        ArrayList<String> s = new ArrayList<String>(); //and use Collections.sort()
        for (TriplePath tp : triplePathCollection) {
            s.add(tp.toString());
        }
        Collections.sort(s);
        return queryIndex.containsKey(s.toString());
    }

    private float computeRemoveOperationCost(Query originalQuery, Query childQuery) {
        QueryGPESim queryGPEsim = new QueryGPESim();
        float sim = queryGPEsim.computeQueryPatternsSim(originalQuery, childQuery);
//        return (float) 1.0 - sim;
        return sim;
//          return 0;
    }

    private void printQuerySolutionSpaceMap(QueryAndContextNode parentQueryAndContextNode) {

        System.out.println("Query Child === ");
        System.out.println(parentQueryAndContextNode.getqR().toString());

        System.out.println("Query Child Template Var === ");

        Map<Var, Set<RDFNode>> tmpMap = parentQueryAndContextNode.getQueryTempVarValueMap();
        printMap(tmpMap);

//        if (tmpMap!=null) {
//            Iterator<Entry<Var, Set<RDFNode>>> iter = tmpMap.entrySet().iterator();
//
//            while (iter.hasNext()) {
//                Entry<Var, Set<RDFNode>> entry = iter.next();
//                System.out.println("Var= " + entry.getKey().asNode().getName());
//                Set<RDFNode> valuList = entry.getValue();
//                for (RDFNode value : valuList) {
//                    System.out.println("Value= " + value.asNode().getName());
//                }
//            }
//
//        }
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

//                for (RDFNode value : valuList) {
//                    System.out.println("Value= " + value.toString());
//                }
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



}
