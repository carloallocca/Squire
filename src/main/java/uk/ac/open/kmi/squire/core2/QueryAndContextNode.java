/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import uk.ac.open.kmi.squire.core.QueryScorePair;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryAndContextNode {


    private  IRDFDataset rdfD1;
    private  IRDFDataset rdfD2;
    private  IRDFDataset rdfD2View;
    
    private Query qO;
    private Query qR;

//    private Set<Var> qRTemplateVariableSet;
//    private Set<TriplePath> qRTriplePathSet;
    
    private String entityqO;
    private String entityqR; 
    
    private ArrayList<String> operationList;
    private String op; //It can be either R (for Removal) or I (Instanciation). 

    private float queryResultTypeSimilarity;
    private float queryRootDistance;
    private float queryResultSizeSimilarity;
    private float querySpecificityDistance;
    
    private float qRScore; 
    
//    private ArrayList<VarTemplateAndEntityQoQr> tvEntityQoQrInstanciatedList;
    
    private ArrayList<String> indSetD2;
    private ArrayList<String> dpSetD2;
    private ArrayList<String> cSetD2;
    private ArrayList<String> opSetD2;
    private ArrayList<String> lSetD2;
    private ArrayList<String> rdfVD2;
    private ArrayList<String> pSetD2; 
    
    private List<QuerySolution> queryTempVarSolutionSpace;
    private Map<Var, Set<RDFNode>> queryTempVarValueMap;
    
    
    
    public QueryAndContextNode() {
        super();
    }

    
//    public ArrayList<VarTemplateAndEntityQoQr> getTvEntityQoQrInstanciatedList() {
//        return tvEntityQoQrInstanciatedList;
//    }
//
//    public void setTvEntityQoQrInstanciatedList(ArrayList<VarTemplateAndEntityQoQr> tvEntityQoQrInstanciatedList) {
//        this.tvEntityQoQrInstanciatedList = tvEntityQoQrInstanciatedList;
//    }

    
    public Map<Var, Set<RDFNode>> getQueryTempVarValueMap() {
        return queryTempVarValueMap;
    }

    public void setQueryTempVarValueMap(Map<Var, Set<RDFNode>> queryTempVarValueMap) {
        this.queryTempVarValueMap = queryTempVarValueMap;
    }
    
    public List<QuerySolution> getQueryTempVarSolutionSpace() {
        return queryTempVarSolutionSpace;
    }

    public void setQueryTempVarSolutionSpace(List<QuerySolution> queryTempVarSolutionSpace) {
        this.queryTempVarSolutionSpace = queryTempVarSolutionSpace;
    }

public QueryAndContextNode cloneMe(QueryAndContextNode qRScoreMaxNode) {

        QueryAndContextNode clonedNode= new QueryAndContextNode();
        
        //...set the original query and the recommendated query;
        Query clonedqO= QueryFactory.create(qRScoreMaxNode.getqO().toString());
        clonedNode.setqO(clonedqO);
        
        Query clonedqR= QueryFactory.create(qRScoreMaxNode.getqR().toString());
        clonedNode.setqR(clonedqR);
        
        
        clonedNode.setRdfD1(qRScoreMaxNode.getRdfD1());
        clonedNode.setRdfD2(qRScoreMaxNode.getRdfD2());

        
        
        String clonedEntityqO=qRScoreMaxNode.getEntityqO();        
        clonedNode.setEntityqO(clonedEntityqO);

        String clonedEntityqR=qRScoreMaxNode.getEntityqR();        
        clonedNode.setEntityqR(clonedEntityqR);

        
        //...set the set of classes, object property, datatype property,...;        
        ArrayList<String> clonedcSetD2 = new ArrayList();
        clonedcSetD2.addAll(qRScoreMaxNode.getRdfD2().getClassSet());
        clonedNode.setcSetD2(clonedcSetD2);
        
        ArrayList<String> clonedDpSetD2 = new ArrayList();
        clonedDpSetD2.addAll(qRScoreMaxNode.getRdfD2().getDatatypePropertySet());
        clonedNode.setDpSetD2(clonedDpSetD2);
            
        
        ArrayList<String> clonedOpSetD2 = new ArrayList();
        clonedOpSetD2.addAll(qRScoreMaxNode.getRdfD2().getObjectPropertySet());
        clonedNode.setOpSetD2(clonedOpSetD2);

        ArrayList<String> clonedlSetD2 = new ArrayList();
        clonedlSetD2.addAll(qRScoreMaxNode.getRdfD2().getLiteralSet());
        clonedNode.setlSetD2(clonedlSetD2);
        

        ArrayList<String> clonedIndSetD2 = new ArrayList();
        clonedIndSetD2.addAll(qRScoreMaxNode.getRdfD2().getIndividualSet());
        clonedNode.setIndSetD2(clonedIndSetD2);

        ArrayList<String> clonedpSetD2 = new ArrayList();
        clonedpSetD2.addAll(qRScoreMaxNode.getRdfD2().getPropertySet());
        clonedNode.setpSetD2(clonedpSetD2);
        
        ArrayList<String> clonedRdfVSetD2 = new ArrayList();
        clonedRdfVSetD2.addAll(qRScoreMaxNode.getRdfD2().getRDFVocabulary());
        clonedNode.setRdfVD2(clonedRdfVSetD2);
        
 
        //...set the score measurements
        
        float clonedQueryRootDistance=qRScoreMaxNode.getQueryRootDistance();
        clonedNode.setQueryRootDistance(clonedQueryRootDistance);
        
        float clonedQueryResultTypeSimilarity=qRScoreMaxNode.getQueryResultTypeSimilarity();
        clonedNode.setQueryResultTypeSimilarity(clonedQueryResultTypeSimilarity);
        
        float clonedQuerySpecificityDistance=qRScoreMaxNode.getQuerySpecificityDistance();
        clonedNode.setQuerySpecificityDistance(clonedQuerySpecificityDistance);
        
        float clonedQueryResultSizeSimilarity=qRScoreMaxNode.getQueryResultSizeSimilarity();
        clonedNode.setQueryResultSizeSimilarity(clonedQueryResultSizeSimilarity);
        
        float clonedqRScore=qRScoreMaxNode.getqRScore();        
        clonedNode.setqRScore(clonedqRScore);

        String clonedOp=qRScoreMaxNode.getOp();
        clonedNode.setOp(clonedOp);
        
        
        ArrayList<String> clonedOperationList = new ArrayList();
        clonedOperationList.addAll(qRScoreMaxNode.getOperationList());
        clonedNode.setOperationList(clonedOperationList);

//        Set<Var> clonedqRTemplateVariableSet = new HashSet();        
//        clonedqRTemplateVariableSet.addAll(qRScoreMaxNode.getqRTemplateVariableSet());
//        clonedNode.setqRTemplateVariableSet(clonedqRTemplateVariableSet);
//
//        Set<TriplePath> clonedqRTriplePathSet = new HashSet();        
//        clonedqRTriplePathSet.addAll(qRScoreMaxNode.getqRTriplePathSet());
//        clonedNode.setqRTriplePathSet(clonedqRTriplePathSet);
               
 
        //...set the QueryTempVarSolutionSpace
        List<QuerySolution> clonedQueryTempVarSolutionSpace = new ArrayList();        
        clonedQueryTempVarSolutionSpace.addAll(qRScoreMaxNode.getQueryTempVarSolutionSpace());
        clonedNode.setQueryTempVarSolutionSpace(clonedQueryTempVarSolutionSpace);
            
        return clonedNode;
        
        
    }
   
    public static class QRScoreComparator implements Comparator<QueryAndContextNode>
    {
    	public int compare(QueryAndContextNode p1, QueryAndContextNode p2)
    	{
            float score1 = p1.getqRScore();
            float score2 = p2.getqRScore();
            //...For ascending order
            return Float.compare(score2, score1);
//            return (-1)*Float.compare(score1, score2); // For discending order
    	}
    }
    
    
    
    
    public static Comparator<QueryAndContextNode> queryScoreComp = new Comparator<QueryAndContextNode>() {

        @Override
        public int compare(QueryAndContextNode o1, QueryAndContextNode o2) {
            float score1 = o1.getqRScore();
            float score2 = o2.getqRScore();
            //...For ascending order
            return Float.compare(score2, score1);
//            return (-1)*Float.compare(score1, score2); // For discending order
        }
    };

    
    

//    public Set<TriplePath> getqRTriplePathSet() {
//        return qRTriplePathSet;
//    }
//
//    public void setqRTriplePathSet(Set<TriplePath> qRTriplePathSet) {
//        this.qRTriplePathSet = qRTriplePathSet;
//    }

    public void setRdfD1(IRDFDataset rdfD1) {
        this.rdfD1 = rdfD1;
    }

    public void setRdfD2(IRDFDataset rdfD2) {
        this.rdfD2 = rdfD2;
    }

    public void setRdfD2View(IRDFDataset rdfD2View) {
        this.rdfD2View = rdfD2View;
    }

    public void setqO(Query qO) {
        this.qO = qO;
    }

    public void setqR(Query qR) {
        this.qR = qR;
    }

//    public void setqRTemplateVariableSet(Set<Var> qRTemplateVariableSet) {
//        this.qRTemplateVariableSet = qRTemplateVariableSet;
//    }


    public void setEntityqO(String entityqO) {
        this.entityqO = entityqO;
    }

    public void setEntityqR(String entityqR) {
        this.entityqR = entityqR;
    }

    public void setOperationList(ArrayList<String> operationList) {
        this.operationList = operationList;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public void setQueryResultTypeSimilarity(float queryResultTypeSimilarity) {
        this.queryResultTypeSimilarity = queryResultTypeSimilarity;
    }

    public void setQueryRootDistance(float queryRootDistance) {
        this.queryRootDistance = queryRootDistance;
    }

    public void setQueryResultSizeSimilarity(float queryResultSizeSimilarity) {
        this.queryResultSizeSimilarity = queryResultSizeSimilarity;
    }

    public void setQuerySpecificityDistance(float querySpecificityDistance) {
        this.querySpecificityDistance = querySpecificityDistance;
    }

    public void setqRScore(float qRScore) {
        this.qRScore = qRScore;
    }

    public void setIndSetD2(ArrayList<String> indSetD2) {
        this.indSetD2 = indSetD2;
    }

    public void setDpSetD2(ArrayList<String> dpSetD2) {
        this.dpSetD2 = dpSetD2;
    }

    public void setcSetD2(ArrayList<String> cSetD2) {
        this.cSetD2 = cSetD2;
    }

    public void setOpSetD2(ArrayList<String> opSetD2) {
        this.opSetD2 = opSetD2;
    }

    public void setlSetD2(ArrayList<String> lSetD2) {
        this.lSetD2 = lSetD2;
    }

    public void setRdfVD2(ArrayList<String> rdfVD2) {
        this.rdfVD2 = rdfVD2;
    }

    public void setpSetD2(ArrayList<String> pSetD2) {
        this.pSetD2 = pSetD2;
    }
    
    
    
    

    public IRDFDataset getRdfD1() {
        return rdfD1;
    }

    public IRDFDataset getRdfD2() {
        return rdfD2;
    }
        
    public IRDFDataset getRdfD2View() {
        return rdfD2View;
    }

    public Query getqO() {
        return qO;
    }

    public Query getqR() {
        return qR;
    }

//    public Set<Var> getqRTemplateVariableSet() {
//        return qRTemplateVariableSet;
//    }


    public String getEntityqO() {
        return entityqO;
    }

    public String getEntityqR() {
        return entityqR;
    }

    public ArrayList<String> getOperationList() {
        return operationList;
    }

    public String getOp() {
        return op;
    }

    public float getQueryResultTypeSimilarity() {
        return queryResultTypeSimilarity;
    }

    public float getQueryRootDistance() {
        return queryRootDistance;
    }

    public float getQueryResultSizeSimilarity() {
        return queryResultSizeSimilarity;
    }

    public float getQuerySpecificityDistance() {
        return querySpecificityDistance;
    }

    public float getqRScore() {
        return qRScore;
    }

    public ArrayList<String> getIndSetD2() {
        return indSetD2;
    }

    public ArrayList<String> getDpSetD2() {
        return dpSetD2;
    }

    public ArrayList<String> getcSetD2() {
        return cSetD2;
    }

    public ArrayList<String> getOpSetD2() {
        return opSetD2;
    }

    public ArrayList<String> getlSetD2() {
        return lSetD2;
    }

    public ArrayList<String> getRdfVD2() {
        return rdfVD2;
    }

    public ArrayList<String> getpSetD2() {
        return pSetD2;
    }
    
    
    
    
    
    

    
    

}
