/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQTemplateVariableVisitor;

/**
 *
 * @author carloallocca
 *
 * This class transfor a given query q with template variable into another query
 * q' whose project variables are the template variables Ex: q = SELECT DISTINCT
 * ?mod ?title ?code WHERE { ?mod rdf:type ?ct1 ;
 * <http://purl.org/dc/terms/title> ?title ; ?dpt1 ?code } into
 *
 * q' = SELECT DISTINCT ?ct1 ?dpt1 WHERE { ?mod rdf:type ?ct1 ;
 * <http://purl.org/dc/terms/title> ?title ; ?dpt1 ?code }
 *
 */
public class QueryTempVarSolutionSpace {

    public QueryTempVarSolutionSpace() {
        super();
    }

    public Map<Var, Set<RDFNode>> compute(Query qChild, IRDFDataset rdfd2, QueryAndContextNode parentNode) {
        if (parentNode == null) {//compute 
            // 0. Check if the input query has aany template variable, otherwise qTsol is empty
            Set<Var> templateVarSet = getQueryTemplateVariableSet(qChild);
            if (templateVarSet.size() > 0) {
                try {
                    // 1. Transform the the query qChild into a query containg the template variable only.
                    Query qT = rewriteQueryWithTemplateVar(qChild);
                    // 2. Compute the QuerySolution for qT;
                    qT.setLimit(1000);
                    List<QuerySolution> qTsol = computeSolutionSpace(qT, rdfd2);
                    Map<Var, Set<RDFNode>> qTsolMap = tranformToMap(qTsol, qChild);
                    return qTsolMap;
                } catch (Exception ex) {
                    Logger.getLogger(QueryTempVarSolutionSpace.class.getName()).log(Level.SEVERE, null, ex);
                    return new HashMap();
                }
            }
        } else { //take it from parent node
            Set<Var> templateVarSet = getQueryTemplateVariableSet(qChild);
            if (templateVarSet.size() > 0) {
                Map<Var, Set<RDFNode>> qTsolChildMap = new HashMap();
                Map<Var, Set<RDFNode>> qTsolParentMap = parentNode.getQueryTempVarValueMap();
                for (Var vt : templateVarSet) {
                    Set<RDFNode> value = new HashSet();
                    value.addAll(qTsolParentMap.get(vt));
                    qTsolChildMap.put(vt, value);
                }
                printQuerySolutionSpaceMap(qTsolChildMap);
                return qTsolChildMap;
            }
            return new HashMap();
        }
        return new HashMap();
    }
       
        private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
        
        
    public List<QuerySolution> computeTempVarSolutionSpace(Query qChild, IRDFDataset rdfd2) {
        // 0. Check if the input query has aany template variable, otherwise qTsol is empty
        Set<Var> templateVarSet = getQueryTemplateVariableSet(qChild);
        if (templateVarSet.size() > 0) {
            try {
                // 1. Transform the the query qChild into a query containg the template variable only.
                Query qT = rewriteQueryWithTemplateVar(qChild);
                // 2. Compute the QuerySolution for qT;
                
//                qT.setLimit(1000);
                
                log.info("[QueryTempVarSolutionSpace::compute]qT.setLimit(1000); " +qT);
                
                List<QuerySolution> qTsol = computeSolutionSpace(qT, rdfd2);
                log.info("[QueryTempVarSolutionSpace::compute]qTsol size; " +qTsol.size());

                return qTsol;
            } catch (Exception ex) {
                Logger.getLogger(QueryTempVarSolutionSpace.class.getName()).log(Level.SEVERE, null, ex);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    private static Set<Var> getQueryTemplateVariableSet(Query qR) {
        SQTemplateVariableVisitor v = new SQTemplateVariableVisitor();
        //... This will walk through all parts of the query
        ElementWalker.walk(qR.getQueryPattern(), v);
        return v.getQueryTemplateVariableSet();

    }

    private static Query rewriteQueryWithTemplateVar(Query qR) {
        Set<Var> templateVarSet = getQueryTemplateVariableSet(qR);
        Element elem = qR.getQueryPattern();
        Query qT = QueryFactory.make();
        qT.setDistinct(true);
        qT.setQueryPattern(elem);
        qT.setQuerySelectType();
        for (Var tv : templateVarSet) {
            qT.addResultVar(tv.getName());
        }
        return qT;
    }

    private List<QuerySolution> computeSolutionSpace(Query q, IRDFDataset rdfd2) throws java.net.ConnectException {
        try{
            QueryExecution qexec = new QueryEngineHTTP((String) rdfd2.getEndPointURL(), q);
            ResultSet results = qexec.execSelect();
            
            List<QuerySolution> solList = ResultSetFormatter.toList(results);//.out(, results, q);
            log.info("[computeSolutionSpace]solList " +solList.size());

            return solList;
        }catch(Exception ex){
            throw ex;
        }
//        return new ArrayList<>();
    }

    private Map<Var, Set<RDFNode>> tranformToMap(List<QuerySolution> qTsol, Query qChild) {

        if (qTsol != null && qTsol.size() > 0) {
            
//            System.out.println("[QuerySpecializer::printQuerySolutionSpace] There are " + qTsolList.size() + " solutions");

            Map<Var, Set<RDFNode>> map = new HashMap();
            
//        Set<Var> tempVarSet = qRScoreMaxNode.getqRTemplateVariableSet();
            Set<Var> tempVarSet = getQueryTemplateVariableSet(qChild);

            for (Var vt : tempVarSet) {
                Set<RDFNode> valueList = new HashSet();
                //    System.out.println("[QuerySpecializer::printQuerySolutionSpace] " + vt.getName() + "=" + sol.get(vt.getName()).toString());
                for (QuerySolution sol : qTsol) {
                    
                    valueList.add(sol.get(vt.getName()));
                }
                map.put(vt, valueList);
            }
            return map;
        }
        return new HashMap();
    }

    private void printQuerySolutionSpaceMap(Map<Var, Set<RDFNode>> tmpMap) {
        
        System.out.println("[QueryTempVarSolutionSpace::printQuerySolutionSpaceMap]");
        
        if (tmpMap != null) {
            Iterator<Map.Entry<Var, Set<RDFNode>>> iter = tmpMap.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<Var, Set<RDFNode>> entry = iter.next();
                System.out.println("Var= " + entry.getKey().asNode().getName());
                Set<RDFNode> valuList = entry.getValue();
                for (RDFNode value : valuList) {
                    System.out.println("Value= " + value.toString());
                }
            }

        }

    }

}
