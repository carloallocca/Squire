/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.entityvariablemapping.ClassVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.DatatypePropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.IndividualVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.LiteralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.ObjectPropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.RDFVocVarMapping;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.treequerypatterns.TreeNode;

/**
 *
 * @author carloallocca
 * @param <T>
 */
public class QueryRecommendator3<T> {

//    private final HashMap<String, TreeNode<T>> treeNodeIndex = new HashMap<>();
    
    private static final ExecutorService threadpool = Executors.newCachedThreadPool();// .newFixedThreadPool(3);
    private static final ExecutorService threadpool1 = Executors.newCachedThreadPool();// .newFixedThreadPool(3);

    private final IRDFDataset rdfD1;
    private final Query q0;
    private final Query q0Copy;
    private Query qTemplate;

    private final float resultTypeSimilarityDegree;
    private final float queryRootDistanceDegree;
    private final float resultSizeSimilarityDegree;
    private final float querySpecificityDistanceDegree;

    private final IRDFDataset rdfD2;

    private List<QueryAndContextNode> qRList = new ArrayList(); //this is for storing the utput of the specilizer
    
    private List<QueryScorePair> sortedRecomQueryList=new ArrayList(); //this is for storing the utput of the QueryRecommendator

    private  LiteralVarMapping literalVarTable;
    private  ClassVarMapping classVarTable;
    private  DatatypePropertyVarMapping datatypePropertyVarTable;
    private  IndividualVarMapping individualVarTable;
    private  ObjectPropertyVarMapping objectProperyVarTable;
    private  RDFVocVarMapping rdfVocVarTable;

    private static final String CLASS_TEMPLATE_VAR = "ct";
    private static final String OBJ_PROP_TEMPLATE_VAR = "opt";
    private static final String DT_PROP_TEMPLATE_VAR = "dpt";
    private static final String INDIVIDUAL_TEMPLATE_VAR = "it";
    private static final String LITERAL_TEMPLATE_VAR = "lt";

    private static final String INSTANCE_OP = "I";
    private static final String REMOVE_TP_OP = "R";

    public QueryRecommendator3(Query query, IRDFDataset d1, IRDFDataset d2,
                                                            float resultTypeSimilarityDegree,
                                                            float queryRootDistanceDegree,
                                                            float resultSizeSimilarityDegree,
                                                            float querySpecificityDistanceDegree) {
        q0 = QueryFactory.create(query.toString());
        q0Copy = QueryFactory.create(query.toString());
        rdfD1 = d1;
        classVarTable = new ClassVarMapping();
        individualVarTable = new IndividualVarMapping();
        literalVarTable = new LiteralVarMapping();
        objectProperyVarTable = new ObjectPropertyVarMapping();
        datatypePropertyVarTable = new DatatypePropertyVarMapping();
        rdfVocVarTable = new RDFVocVarMapping();
        rdfD2 = d2;
        this.queryRootDistanceDegree = queryRootDistanceDegree;
        this.querySpecificityDistanceDegree = querySpecificityDistanceDegree;
        this.resultSizeSimilarityDegree = resultSizeSimilarityDegree;
        this.resultTypeSimilarityDegree = resultTypeSimilarityDegree;
    }

    public void buildRecommendation() {
        
        // GENERALIZE...
        QueryGeneralizer3 qG = new QueryGeneralizer3(this.q0Copy, this.rdfD1, this.rdfD2);
        try {
            System.out.println("[QueryRecommendator3::buildRecommendation] Submitting the Query Generalization Task ..."); 
            Future future = threadpool.submit(qG);
            System.out.println("[QueryRecommendator3::buildRecommendation] Query Generalization Task is submitted");
            while (!future.isDone()) { 
                System.out.println("[QueryRecommendator3::buildRecommendation] Query Generalization Task is not completed yet....");
                Thread.sleep(1); //sleep for 1 millisecond before checking again 
            }
            System.out.println("[QueryRecommendator3::buildRecommendation] Query Generalization Task is completed...");
            this.qTemplate = (Query) future.get();
            System.out.println("[QueryRecommendator3::buildRecommendation] Template Query is : ");
            System.out.println(this.qTemplate.toString());
            threadpool.shutdown();
        } catch (Exception ex) {
            Logger.getLogger(QueryRecommendator3.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println(" ");
//        System.out.println("[QueryRecommendation, generalizeToQueryTemplate()] THE GENERALIZED QUERY: ");
//        System.out.println(this.qTemplate.toString());

        this.classVarTable=qG.getClassVarTable();
        this.individualVarTable = qG.getIndividualVarTable();
        this.literalVarTable = qG.getLiteralVarTable();
        this.objectProperyVarTable = qG.getObjectProperyVarTable();
        this.datatypePropertyVarTable = qG.getDatatypePropertyVarTable();
        this.rdfVocVarTable = qG.getRdfVocVarTable();

        //SPECIALIZE...
        QuerySpecializer3 qS= new QuerySpecializer3(this.q0Copy, this.qTemplate,this.rdfD1, this.rdfD2,
                
                                                                this.classVarTable,
                                                                this.objectProperyVarTable,
                                                                this.datatypePropertyVarTable,
                                                                this.individualVarTable,
                                                                this.literalVarTable,
                                                                this.rdfVocVarTable,
                
                                                                this.resultTypeSimilarityDegree,
                                                                this.queryRootDistanceDegree,
                                                                this.resultSizeSimilarityDegree,
                                                                this.querySpecificityDistanceDegree);
        
        try {
            System.out.println("[QueryRecommendator3::buildRecommendation] Submitting the Query Specialization Task ..."); 
            Future future1 = threadpool1.submit(qS);
            System.out.println("[QueryRecommendator3::buildRecommendation] Query Specialization Task is submitted");
            while (!future1.isDone()) { 
                System.out.println("[QueryRecommendator3::buildRecommendation] Query Specialization Task is not completed yet....");
                Thread.sleep(10000); //sleep for 1 millisecond before checking again 
            }
            System.out.println("[QueryRecommendator3::buildRecommendation] Query Specialization Task is completed...");
//            
//            this.qTemplate = (Query) future1.get();
//            System.out.println("[QueryRecommendator3::buildRecommendation] Template Query is : ");
//            System.out.println(this.qTemplate.toString());
            threadpool1.shutdown();
        } catch (Exception ex) {
            Logger.getLogger(QueryRecommendator3.class.getName()).log(Level.SEVERE, null, ex);
        }

        //RANKING...
        this.qRList=qS.getRecommandedQueryList();
        applyRankingToRecommandedQueryList(qRList);
        
    }

    
    public List<QueryScorePair> getSortedRecomQueryList() {
        return sortedRecomQueryList;
    }

    
    
    private void applyRankingToRecommandedQueryList(List<QueryAndContextNode> qRList) {
        for(QueryAndContextNode qrRecom: qRList){
            QueryScorePair pair= new QueryScorePair(qrRecom.getqR(), qrRecom.getqRScore());
            this.sortedRecomQueryList.add(pair);
        }
        Collections.sort(this.sortedRecomQueryList, QueryScorePair.queryScoreComp);  
    }

    

}
