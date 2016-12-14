/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.sparqlqueryvisitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprNode;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;

/**
 *
 * @author carloallocca
 */
public class TransformRemoveOp implements Transform {

    private Query query;
    private Triple triple;

    private boolean isParent = false;
    private boolean isStarted = false;
    private boolean isFinished = false;

    private int opBGPCounter = 1;

    public TransformRemoveOp(Query q, Triple tp) {
        this.query = q;
        this.triple = tp;
    }

    @Override
    public Op transform(OpTable opUnit) {

        System.out.println("[TransformRemoveOp::transform(OpTable opUnit)] " + opUnit.toString());
        System.out.println("");
        return opUnit;

    }

    @Override
    public Op transform(OpGroup opGroup, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpGroup opGroup, Op subOp)] " + opGroup.toString());
        System.out.println("");

        return opGroup;
    }

    @Override
    public Op transform(OpBGP opBGP) {

        System.out.println("[TransformRemoveOp::transform(OpBGP opBGP)] opBGPCounter " + opBGPCounter++);
        System.out.println("[TransformRemoveOp::transform(OpBGP opBGP)] " + opBGP.toString());
        System.out.println("");
        Op newOpBGP = opBGP.copy();
        BasicPattern newBP = ((OpBGP) newOpBGP).getPattern();
        List<Triple> tripleList = newBP.getList();

        Iterator<Triple> itr = tripleList.iterator();
        while (itr.hasNext()) {
            Triple tp = itr.next();
            if (tp.matches(this.triple)) {
                itr.remove();
                isParent = true;
                isStarted = true;
            }
        }
        //...it can be empty
        if (((OpBGP) newOpBGP).getPattern().getList().isEmpty()) {
            System.out.println("[TransformRemoveOp::transform(OpBGP opBGP)] opBGP is empty " + opBGP.toString());
            //return subOp;
        }
        return newOpBGP;
    }

//    @Override
//    public Op transform(OpFilter opFilter, Op subOp) {
//        Op op = null;
//        if(subOp instanceof OpBGP ){
//            System.out.println("YOU ARE HERE!!!!!");
//            if (((OpBGP) subOp).getPattern().getList().isEmpty()){
//                System.out.println("YOU ARE HERE 22 2222222!!!!!");
//                return subOp;
//            }
//        }
//        return op; 
//    }    
//    @Override
//    public Op transform(OpFilter opFilter, Op subOp) {
//        System.out.println("[TransformRemoveOp::transform(OpFilter opFilter, Op subOp)] opFilter " + opFilter.toString());
//        System.out.println("[TransformRemoveOp::transform(OpFilter opFilter, Op subOp)] subOp Name " + subOp.getName());
//        System.out.println("");
//
//        if (isParent == false) {
//            return opFilter;
//        }
//
//        //...get the variables of the triple pattern that we want to delete
//        Set<Var> tpVars = new HashSet();
//        Node subj = this.triple.getSubject();
//        if (subj.isVariable()) {
//            tpVars.add((Var) subj);
//        }
//        Node pred = this.triple.getPredicate();
//        if (pred.isVariable()) {
//            tpVars.add((Var) pred);
//        }
//        Node obj = this.triple.getObject();
//        if (obj.isVariable()) {
//            tpVars.add((Var) obj);
//        }
//        //...get the variables of the FILTER expression
//        Op opNew = opFilter.copy(subOp);
//        Set<Var> expVars = ((OpFilter) opNew).getExprs().getVarsMentioned();
//
//        //...check whether the FILTER expression contains any of the triple pattern variable
//        boolean isContained=false;
//        for (Var var : expVars) {
//            //..if it does then we have to delete the entire FILTER expression
//            if (tpVars.contains(var)) {
//                isContained=true;
//            }
//                //System.out.println("[TransformRemoveOp::transform(OpFilter opFilter, Op subOp)] FILTER ? ");
//                //System.out.println("111111 "+opFilter.getSubOp().getName());
//
//                
//                /////////////
//                /////////////
//                
////                ExprList exprList=exprList = opFilter.getExprs();
////
////                ExprList newExprList = new ExprList();	// contains the translated expressions
////                
////                OpFilter copiedOpFilter = (OpFilter) OpFilter.filter(exprList, opFilter.getSubOp());
////                exprList = copiedOpFilter.getExprs();
////
////                // for every expression of the filter, apply first DeMorgan-law and then Distributive-law
////                for (Expr expr : exprList) {
////                    Expr e = new NodeValueBoolean(true);
////                    newExprList.add(e);
////                }
////                return copiedOpFilter.filter(newExprList, opFilter);//filter(newExprList, opFilter);
//                
//                
//                /////////////
//                /////////////
//            if (subOp instanceof OpBGP) {
//                    if (((OpBGP) subOp).getPattern().getList().isEmpty()) {
//                        System.out.println("YOU ARE HERE 22 2222222!!!!!");
//                        return subOp;
//                    }
//                    Expr e = new NodeValueBoolean(true);
//                    Op newOP = OpFilter.filter(e, opFilter);//filter(e, );
//
//                    //
//                    //opFilter.apply(this, subOp);
//                    //newOP = new OpProject(newOP, Arrays.asList(Var.alloc("s")));
//                    //Op newNewOp= OpFilter.filter(e, newOP);  
//                    isFinished = true;
//                    return newOP;
//            }
//        }
//
//        // opFilter.apply(new TransformFilterCNF(), subOp);
//        System.out.println("[TransformRemoveOp::transform(OpFilter opFilter, Op subOp)] BEFORE RETURN");
//        return opFilter;
//    }
//
    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpFilter opFilter, Op subOp)] opFilter " + opFilter.toString());
        System.out.println("[TransformRemoveOp::transform(OpFilter opFilter, Op subOp)] subOp Name " + subOp.getName());
        System.out.println("");

        if (isParent == false) {
            return opFilter;
        }

        //...get the variables of the triple pattern that we want to delete
        Set<Var> tpVars = new HashSet();
        Node subj = this.triple.getSubject();
        if (subj.isVariable()) {
            tpVars.add((Var) subj);
        }
        Node pred = this.triple.getPredicate();
        if (pred.isVariable()) {
            tpVars.add((Var) pred);
        }
        Node obj = this.triple.getObject();
        if (obj.isVariable()) {
            tpVars.add((Var) obj);
        }
        //...get the variables of the FILTER expression
        Op opNew = opFilter.copy(subOp);
        Set<Var> expVars = ((OpFilter) opNew).getExprs().getVarsMentioned();

        //...check whether the FILTER expression contains any of the triple pattern variable
        boolean isContained = false;
        for (Var var : expVars) {
            //..if it does then we have to delete the entire FILTER expression
            if (tpVars.contains(var)) {
                isContained = true;
                break;
            }
        }
        //... if the filter contains any variable of the triple that has been removed, then....
        if (isContained) {
            Op newOP;
            Expr e;
            if (subOp instanceof OpBGP) {
                if (((OpBGP) subOp).getPattern().getList().isEmpty()) {
                    e = new NodeValueBoolean(true);
                    newOP = OpFilter.filter(e, opFilter);//filter(e, );
                    return newOP;
                } else {
                    e = new NodeValueBoolean(false);
                    newOP = OpFilter.filter(e, opFilter);//filter(e, );
                    return newOP;
                }
            }
        }
        return opFilter;
    }

    @Override
    public Op transform(OpTriple opTriple) {
        System.out.println("[TransformRemoveOp::transform(OpTriple opTriple)] " + opTriple.toString());
        System.out.println("");

        return opTriple;
    }

    @Override
    public Op transform(OpQuad opQuad) {
        System.out.println("[TransformRemoveOp::transform(OpQuad opQuad)] " + opQuad.toString());
        System.out.println("");

        return opQuad;
    }

    @Override
    public Op transform(OpPath opPath) {
        System.out.println("[TransformRemoveOp::transform(OpPath opPath)] " + opPath.toString());
        System.out.println("");

        return opPath;
    }

    @Override
    public Op transform(OpDatasetNames dsNames) {
        System.out.println("[TransformRemoveOp::transform(OpDatasetNames dsNames)] " + dsNames.toString());
        System.out.println("");

        return dsNames;
    }

    @Override
    public Op transform(OpQuadPattern quadPattern) {
        System.out.println("[TransformRemoveOp::transform(OpQuadPattern quadPattern)] " + quadPattern.toString());
        System.out.println("");

        return quadPattern;
    }

    @Override
    public Op transform(OpQuadBlock quadBlock) {
        System.out.println("[TransformRemoveOp::transform(OpQuadBlock quadBlock)] " + quadBlock.toString());
        System.out.println("");

        return quadBlock;
    }

    @Override
    public Op transform(OpNull opNull) {
        System.out.println("[TransformRemoveOp::transform(OpNull opNull)] " + opNull.toString());
        System.out.println("");

        return opNull;
    }

    @Override
    public Op transform(OpGraph opGraph, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpService opService, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpProcedure opProcedure, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpPropFunc opPropFunc, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpLabel opLabel, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        System.out.println("[TransformRemoveOp::transform(OpJoin opJoin, Op left, Op right)] " + opJoin.toString());
        System.out.println("");

        return opJoin;
    }

    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        System.out.println("[TransformRemoveOp::transform(OpLeftJoin opLeftJoin, Op left, Op right)] " + opLeftJoin.toString());
        System.out.println("");

        return opLeftJoin;
    }

    @Override
    public Op transform(OpDiff opDiff, Op left, Op right) {
        System.out.println("[TransformRemoveOp::transform(OpDiff opDiff, Op left, Op right)] " + opDiff.toString());
        System.out.println("");

        return opDiff;
    }

    @Override
    public Op transform(OpMinus opMinus, Op left, Op right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        System.out.println("[TransformRemoveOp::transform(OpUnion opUnion, Op left, Op right)] left: " + left.toString());
        System.out.println("[TransformRemoveOp::transform(OpUnion opUnion, Op left, Op right)] right: " + right.toString());
        System.out.println("");
        
        

        return opUnion;
    }

    @Override
    public Op transform(OpConditional opCondition, Op left, Op right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpSequence opSequence, List<Op> elts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) {
        System.out.println("[TransformRemoveOp::transform(OOpDisjunction opDisjunction, List<Op> elts)] " + opDisjunction.toString());
        System.out.println("");

        return opDisjunction;
    }

    @Override
    public Op transform(OpExt opExt) {
        System.out.println("[TransformRemoveOp::transform(OpExt opExt)] " + opExt.toString());
        System.out.println("");

        return opExt;
    }

    @Override
    public Op transform(OpList opList, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpList opList, Op subOp)] " + opList.toString());
        System.out.println("");

        return opList;
    }

    @Override
    public Op transform(OpOrder opOrder, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpOrder opOrder, Op subOp)] " + opOrder.toString());
        System.out.println("");

        return opOrder;
    }

    @Override
    public Op transform(OpTopN opTop, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpTopN opTop, Op subOp)] " + opTop.toString());
        System.out.println("");

        return opTop;
    }

    @Override
    public Op transform(OpProject opProject, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpProject opProject, Op subOp)] " + opProject.toString());
        System.out.println("");

        return opProject;
    }

    @Override
    public Op transform(OpDistinct opDistinct, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpDistinct opDistinct, Op subOp)] " + opDistinct.toString());
        System.out.println("");

        return opDistinct;
    }

    @Override
    public Op transform(OpReduced opReduced, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpReduced opReduced, Op subOp)] " + opReduced.toString());
        System.out.println("");

        return opReduced;
    }

    @Override
    public Op transform(OpSlice opSlice, Op subOp) {
        System.out.println("[TransformRemoveOp::transform(OpSlice opSlice, Op subOp)] " + opSlice.toString());
        System.out.println("");

        return opSlice;
    }

}
