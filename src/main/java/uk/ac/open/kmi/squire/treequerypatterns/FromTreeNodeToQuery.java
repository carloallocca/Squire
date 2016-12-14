/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.TripleCollectorBGP;
import uk.ac.open.kmi.squire.queryvariablepartition.Partition;

/**
 *
 * @author callocca This class has the scope of turning a TreeNode into a set of
 * SPARQL queries, depending on the variable and the template variable.
 */
public class FromTreeNodeToQuery<T> {

    private final TreeNode<T> node;  //input
    private final ArrayList<Query> queryList; //output

    public FromTreeNodeToQuery(TreeNode<T> n) {
        this.node = n;
        this.queryList = new ArrayList<>();
    }

    public void querySelectBuilding() {
        
        //ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
        
        
        System.out.println("[FromTreeNodeToQuery::querySelectBuilding] This is a new selectQueryBuilding() call");
        
        ArrayList<TriplePath> tripleSet = (ArrayList<TriplePath>) this.node.getData();
        
        System.out.println("[FromTreeNodeToQuery::querySelectBuilding] This is the current triple patterns set "+tripleSet.toString());
        

        //Building the triplePatternBlock as a conjtion of Triple patterns.
        ArrayList<String> resultVar = new ArrayList();

        ElementTriplesBlock triplePatternBlock = new ElementTriplesBlock();
//        TripleCollectorBGP triplePatternBlock = new TripleCollectorBGP();
        for (TriplePath tp : tripleSet) {
            //If the Subject is a variable Then it is potentially a Result variable of the query 
            if (tp.getSubject().isVariable()) {
                String varSubj = tp.getSubject().toString();
                if (!(resultVar.contains(varSubj))) {
                    resultVar.add(varSubj);
                }
            }
            //If the Predicate is a variable Then it is potentially a Result variable of the query 
            if (tp.getPredicate().isVariable()) {
                String varPred = tp.getPredicate().toString();
                if (!(resultVar.contains(varPred))) {
                    resultVar.add(varPred);
                }
            }
            //If the Object is a variable Then it is potentially a Result variable of the query 
            if (tp.getObject().isVariable()) {
                String varObj = tp.getObject().toString();
                if (!(resultVar.contains(varObj))) {
                    resultVar.add(varObj);
                }
            }
            Triple pattern =Triple.create(tp.getSubject(), tp.getPredicate(), tp.getObject());
           // System.out.println("[FromTreeNodeToQuery::querySelectBuilding] YES");

            triplePatternBlock.addTriple(pattern);//.addTriplePath(tp);
          //  System.out.println("[FromTreeNodeToQuery::querySelectBuilding] NO");

        }
        //For each partition of the varResutlSet, generate a new query.  
        Partition compParts = new Partition();
        for (List<List<String>> partitions : compParts.partitions(resultVar, 0)) {
          //  System.out.println("[FromTreeNodeToQuery::selectQueryBuilding] This is a new partition");
            System.out.println(partitions);
            for (List<String> part : partitions) {
                Query query = QueryFactory.create();
                //specify the type of Sparql query SELECT.
                query.setQuerySelectType();
                // adding the "triplePatternBlock" to the query
                query.setQueryPattern(triplePatternBlock);
                //variable bindings that we want to return
                for (String var : part) {
                    //System.out.print(var);
                    query.addResultVar(var);
                }
                this.queryList.add(query);
            //    System.out.println("[FromTreeNodeToQuery::selectQueryBuilding] This is a new query");
                System.out.println(query);
            }
        }
    }

}
