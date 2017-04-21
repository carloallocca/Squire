/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQGraphPatternExpressionVisitor;

/**
 *
 * @author carloallocca
 *
 * This class compute the similarity in the context of Remove operation between
 * two SPARQL queries.
 *
 */
public class QueryGPESim {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    public QueryGPESim() {
        super();

    }

    public float computeQueryPatternsSim(Query qO, Query qR) {
        //...get the GPE of  qOri        
        SQGraphPatternExpressionVisitor gpeVisitorO = new SQGraphPatternExpressionVisitor();
        ElementWalker.walk(qO.getQueryPattern(), gpeVisitorO);
        Set<TriplePath> qOGPE = gpeVisitorO.getQueryGPE();
//        log.info("qOGPE : " +qOGPE.toString());

        //...get the GPE of  qRec                
        SQGraphPatternExpressionVisitor gpeVisitorR = new SQGraphPatternExpressionVisitor();
        ElementWalker.walk(qR.getQueryPattern(), gpeVisitorR);
        Set<TriplePath> qRGPE = gpeVisitorR.getQueryGPE();
//        log.info("qRGPE : " +qRGPE.toString());

        //this is as it was before 13-04-2017        
//        if (qRGPE.size() > 0 && qOGPE.size() > 0) {
//            return (float) (((1.0) * qRGPE.size()) / ((1.0) * qOGPE.size()));
//        }
//        return (float) 0.0;
// this is the new one
        float r = computeSim(qOGPE, qRGPE);
        //log.info("computeQueryPatternsSim : " + r);

        return r;
    }

    private float computeSim(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {
        if (!(qOGPE.isEmpty() && !(qRGPE.isEmpty()))) {
            float commonTriplePattern = computeCommonTriplePattern(qOGPE, qRGPE);
            float weighedNoNCommonTriplePattern = computeWeighedNonCommonTriplePattern(qOGPE, qRGPE);
            return commonTriplePattern + weighedNoNCommonTriplePattern;
        }
        return 0;
    }

    private float computeCommonTriplePattern(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {

        float sim = (float) 0;
        int cardSignatureQo = qOGPE.size();
        int cardSignatureQr = qRGPE.size();

        if (!(cardSignatureQo == 0) && !(cardSignatureQr == 0)) {
            int intersection = 0;
            for (TriplePath tp : qOGPE) {
                if (contains(qRGPE, tp)) {
                    intersection = intersection + 1;
                }
            }
//            log.info("computeCommonTriplePattern::intersection : " + intersection);
            //            sim =(float) (1.0*(((1.0*qOvarList.size())/(1.0*qRvarList.size()))));

            sim = (float) ((1.0 * intersection) / (1.0 * cardSignatureQo));
            //log.info("computeCommonTriplePattern::sim : " + sim);
            return sim;
        }
        return sim;
    }

    private boolean contains(Set<TriplePath> qRGPE, TriplePath tp) {
        return qRGPE.contains(tp);
    }

    private float computeWeighedNonCommonTriplePattern(Set<TriplePath> qOGPE, Set<TriplePath> qRGPE) {
        float sim = (float) 0;
        int cardSignatureQo = qOGPE.size();
        int cardSignatureQr = qRGPE.size();
        
        if (!(cardSignatureQo == 0) && !(cardSignatureQr == 0)) {            
            // compute qOGPEComplementaryqRGPE
            Set<WeighedTriplePath> weighedTriplePathSetqoqr = new HashSet<>();
            for (TriplePath tp : qOGPE) {
                if (!(contains(qRGPE, tp))) {
            //        qOGPEComplqRGPECardinality = qOGPEComplqRGPECardinality + 1;
                    float tpWeigh=computeTriplePatternWeigh(tp);      
                    WeighedTriplePath wtp= new WeighedTriplePath(tp,tpWeigh);
                    weighedTriplePathSetqoqr.add(wtp);
                }
            }
            // compute qRGPEComplementaryqOGPE
            Set<WeighedTriplePath> weighedTriplePathSetqrqo = new HashSet<>();
            for (TriplePath tp : qRGPE) {
                if (!(contains(qOGPE, tp))) {
                    float tpWeigh=computeTriplePatternWeigh(tp);      
                    WeighedTriplePath wtp= new WeighedTriplePath(tp,tpWeigh);
                    weighedTriplePathSetqrqo.add(wtp);
                }
            }
            float qoqr = sumWeighedTriplePathSet(weighedTriplePathSetqoqr);
            float qrqo = sumWeighedTriplePathSet(weighedTriplePathSetqrqo);
            sim = (float) ((1.0 * qrqo) / (1.0 * qoqr));
            //log.info("computeWeighedNonCommonTriplePattern::sim : " + sim);
            return sim;
        }
        return sim;   
    }

    private float sumWeighedTriplePathSet(Set<WeighedTriplePath> weighedTriplePathSetqoqr) {
        float sum = (float) 0;
        for(WeighedTriplePath wtp:weighedTriplePathSetqoqr){
            sum=sum+wtp.getWeigh();
        }
        return sum;
    }

    private float computeTriplePatternWeigh(TriplePath tp) {
        float weigh = (float) 0;
        
        Node sub=tp.getSubject();
        Node pred=tp.getPredicate();
        Node obj=tp.getObject();
        
        if(sub.isURI()){
            weigh=(float) (weigh+0.5);
        }
        else{
            if(sub.isVariable()){
                if(sub.getName().startsWith("ct") || sub.getName().startsWith("opt") || sub.getName().startsWith("dtp") ){
                    weigh=(float) (weigh+0.1);
                }
                else{
                    weigh=(float) (weigh+0.4);
                }
            }
        }
        
        if(pred.isURI()){
            weigh=(float) (weigh+0.5);
        }
        else{
            if(pred.isVariable()){
                if(pred.getName().startsWith("ct") || pred.getName().startsWith("opt") || pred.getName().startsWith("dtp") ){
                    weigh=(float) (weigh+0.1);
                }
                else{
                    weigh=(float) (weigh+0.4);
                }
            }
        }
        
        if(obj.isURI()){
            weigh=(float) (weigh+0.5);
        }
        else{
            if(obj.isVariable()){
                if(obj.getName().startsWith("ct") || obj.getName().startsWith("opt") || obj.getName().startsWith("dtp") ){
                    weigh=(float) (weigh+0.1);
                }
                else{
                    weigh=(float) (weigh+0.4);
                }
            }
        }
        
        return weigh;   
        
    }
    
    
    

    
    
}
