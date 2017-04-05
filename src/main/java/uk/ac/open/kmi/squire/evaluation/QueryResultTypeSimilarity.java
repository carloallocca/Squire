/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import uk.ac.open.kmi.squire.rdfdataset.FileBasedRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPoint;

/**
 *
 * @author carloallocca
 */
public class QueryResultTypeSimilarity {

    private static final String TYPE_OF_URI = "uri";

    public QueryResultTypeSimilarity() {
        super();

    }

    public float computeQueryResultTypeSim(Query qOri, IRDFDataset d1, Query qRec, IRDFDataset d2) {

//        System.out.println("[ResultTypeQuerySimilarity::computeRTQS] INPUT QUERY  " + qOri.toString());
//        System.out.println("[ResultTypeQuerySimilarity::computeRTQS] INPUT QUERY  " + qRec.toString());

        float sim;
        List<VarTypeMap> qOri_d1_signature = computeQueryVariableSignature(qOri, d1);
//        System.out.println("[ResultTypeQuerySimilarity::computeRTQS] qOri_d1_signature.size  " + qOri_d1_signature.size());
//        print(qOri_d1_signature);
        List<VarTypeMap> qRec_d2_signature = computeQueryVariableSignature(qRec, d2);
//        System.out.println("[ResultTypeQuerySimilarity::computeRTQS] qRec_d2_signature.size " + qRec_d2_signature.size());
//        print(qRec_d2_signature);
        sim = computeRTQSim(qOri_d1_signature, qRec_d2_signature);
        return sim;
    }

    private float computeRTQSim(List<VarTypeMap> qOri_d1_signature, List<VarTypeMap> qRec_d2_signature) {

        float sim = 0;
        int cardSignatureQo = qOri_d1_signature.size();
        int cardSignatureQr = qRec_d2_signature.size();
        if (!(cardSignatureQo == 0) && !(cardSignatureQr == 0)) {
            int intersection = 0;
            for (VarTypeMap map : qOri_d1_signature) {
                if (contains(qRec_d2_signature, map)) {
                    intersection++;
                }
            }

            //            sim =(float) (1.0*(((1.0*qOvarList.size())/(1.0*qRvarList.size()))));
            int cardUnionSignature = computeUnionCardinality(qOri_d1_signature, qRec_d2_signature);
            sim = (float) ((1.0 * intersection) / (1.0 * cardUnionSignature));
            //System.out.println("[ResultTypeQuerySimilarity::computeRTQS] computeRTQSim sim:: " + sim);
            return sim;
        }
        return sim;
    }

    private List<VarTypeMap> computeQueryVariableSignature(Query qOri, IRDFDataset d1) {
        List<VarTypeMap> signature = new ArrayList<>();
        if (d1 instanceof FileBasedRDFDataset) {
            signature = computeFileBasedQVS(qOri, d1);
        } else if (d1 instanceof SPARQLEndPoint) {
            signature = computeSPARQLEndPointBasedQVS(qOri, d1);
        }
        return signature;
    }

    private List<VarTypeMap> computeFileBasedQVS(Query qOri, IRDFDataset d1) {
        List<VarTypeMap> signature = new ArrayList<>();
        try {
            OntModel inf = ModelFactory.createOntologyModel();
            InputStream in = new FileInputStream((String) d1.getPath());
            if (in == null) {
                throw new IllegalArgumentException("File: " + (String) d1.getPath() + " not found");
            }   //...import the content of the owl file in the Jena model. 
            inf.read(in, "");
            //...querying ...
            // Query q = QueryFactory.create(qString);
            QueryExecution qexec = QueryExecutionFactory.create(qOri, inf);
            if (qOri.isSelectType()) {
                ResultSet results = qexec.execSelect();
                List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
                if (resList.size() > 0) {
                    QuerySolution firstSol = resList.get(0);
                    Iterator<String> varIter = firstSol.varNames();
                    while (varIter.hasNext()) {
                        final String varName = varIter.next();
                        final String varType;
                        RDFNode varValue = firstSol.get(varName);
                        if (varValue.isURIResource()) {
                            varType = TYPE_OF_URI;
                        } else if (varValue.isLiteral()) {
                            RDFDatatype literalValue = varValue.asLiteral().getDatatype();
                            varType = literalValue.getURI();
                        } else {
                            varType = "";
                        }
                        VarTypeMap vtm = new VarTypeMap(varName, varType);
                        signature.add(vtm);
                    }
                }
                qexec.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryResultTypeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return signature;
    }

    private List<VarTypeMap> computeSPARQLEndPointBasedQVS(Query qOri, IRDFDataset d1) {
        List<VarTypeMap> signature = new ArrayList<>();

        String endpoint = (String) d1.getPath();

//        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri);
//          QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri, (String)d1.getGraph());
        if (qOri.isSelectType()) {
            try (QueryExecution qexec = new QueryEngineHTTP(endpoint, qOri)) {
//                try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri, (String)d1.getGraph());) {
                ResultSet results = qexec.execSelect();
                List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
                if (resList.size() > 0) {
                    QuerySolution firstSol = resList.get(0);
                    Iterator<String> varIter = firstSol.varNames();
                    while (varIter.hasNext()) {
                        final String varName = varIter.next();
                        final String varType;
                        RDFNode varValue = firstSol.get(varName);
                        if (varValue.isURIResource()) {
                            varType = TYPE_OF_URI;
                        } else if (varValue.isLiteral()) {
                            RDFDatatype literalValue = varValue.asLiteral().getDatatype();
                            varType = literalValue.getURI();
                        } else {
                            varType = "";
                        }
                        VarTypeMap vtm = new VarTypeMap(varName, varType);
                        signature.add(vtm);
                    }
                }
            } catch (RuntimeException ex) {
                return signature;
//                System.out.println(ex.getMessage());
                
            }
        }
        return signature;
    }

    private void print(List<VarTypeMap> qOri_d1_signature) {
        for (VarTypeMap map : qOri_d1_signature) {
            System.out.println("[ResultTypeQuerySimilarity::print] varName " + map.getVarName());
            System.out.println("[ResultTypeQuerySimilarity::print] varType " + map.getVarType());
        }
    }

    private boolean contains(List<VarTypeMap> qRec_d2_signature, VarTypeMap map) {
        //  boolean found=false; 
        String varName = map.getVarName();
        String varType = map.getVarType();
        for (VarTypeMap map1 : qRec_d2_signature) {
            String varName1 = map1.getVarName();
            String varType1 = map1.getVarType();
//            if (!varType.equals("") && !varType1.equals("")) {
            if (varName.equals(varName1) && varType.equals(varType1)) {
                return true;
            }
//            }
        }
        return false;
    }

    private int computeUnionCardinality(List<VarTypeMap> qOri_d1_signature, List<VarTypeMap> qRec_d2_signature) {

        List<VarTypeMap> union = new ArrayList<VarTypeMap>();
        union.addAll(qOri_d1_signature);
        for (VarTypeMap map : qRec_d2_signature) {
            if (!contains(union, map)) {
                union.add(map);
            }
        }
        return union.size();
    }

}
