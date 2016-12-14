/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.syntax.ElementWalker;
import uk.ac.open.kmi.squire.rdfdataset.FileBasedRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPointBasedRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQClassVisitor;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQDatatypePropertyVisitor;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.SQObjectPropertyVisitor;

/**
 *
 * @author carloallocca
 */
public class QueryResultSizeDistance {

    private ArrayList<String> classSet_QO = new ArrayList();
    private ArrayList<String> objectPropertySet_QO = new ArrayList();
    private ArrayList<String> datatypePropertySet_QO = new ArrayList();
    private ArrayList<String> literalSet_QO = new ArrayList();

    private Map<String, ArrayList<String>> classEXTMap_QO = new HashMap<String, ArrayList<String>>();
    private Map<String, ArrayList<String>> objpropEXTMap_QO = new HashMap<String, ArrayList<String>>();
    private Map<String, ArrayList<String>> dtpropEXTMap_QO = new HashMap<String, ArrayList<String>>();

    private ArrayList<String> classSet_Q1 = new ArrayList();
    private ArrayList<String> objectPropertySet_Q1 = new ArrayList();
    private ArrayList<String> datatypePropertySet_Q1 = new ArrayList();
    private ArrayList<String> literalSet_Q1 = new ArrayList();

    private Map<String, ArrayList<String>> classEXTMap_Q1 = new HashMap<String, ArrayList<String>>();
    private Map<String, ArrayList<String>> objpropEXTMap_Q1 = new HashMap<String, ArrayList<String>>();
    private Map<String, ArrayList<String>> dtpropEXTMap_Q1 = new HashMap<String, ArrayList<String>>();

//    private ArrayList<String> individualSet = new ArrayList();
//    private ArrayList<String> rdfVocabulary = new ArrayList();
//    private ArrayList<String> propertySet = new ArrayList();
    public QueryResultSizeDistance() {
        super();
    }

    public float computeQRSSim(Query qOri, IRDFDataset d1, Query qRec, IRDFDataset d2) {

        float qOriRSrate = computeQRSrate(qOri, d1);
        System.out.println(" ");
        System.out.println("[QueryResultSizeSimilarity::computeQRSSim] qOriRSrate "+qOriRSrate);
        System.out.println(" ");
        float qRecRSrate = computeQRSrate(qRec, d2);
                System.out.println(" ");
        System.out.println("[QueryResultSizeSimilarity::computeQRSSim] qRecRSrate "+qRecRSrate);

        return computeQRSDistance(qOriRSrate, qRecRSrate);
    }

    private float computeQRSDistance(float qOriRSrate, float qRecRSrate) {
        return Math.abs((qOriRSrate - qRecRSrate));
    }

    private float computeQRSrate(Query qOri, IRDFDataset d1) {

        // compute Query ResultSet cardinality
        int qOriQueryResultSetCard = computeQueryResultSetCardinality(qOri, d1);

        // compute QueryClassDensity
        float qcd = computeQueryClassDensity(qOri, d1);

        System.out.println("[QueryResultSizeSimilarity::computeQRSrate] qcd " + qcd);

        // compute QueryObjectPropertDensity
        float qopd = computeQueryObjectPropertyDensity(qOri, d1);

        System.out.println("[QueryResultSizeSimilarity::computeQRSrate] qopd " + qopd);

        // compute QueryDatatypePropertDensity
        float qdpd = computeQueryDatatypePropertyDensity(qOri, d1);

        System.out.println("[QueryResultSizeSimilarity::computeQRSrate] qdpd " + qdpd);

        float res = (float) ((1.0 * qOriQueryResultSetCard) / (1.0 * (qcd + qopd + qdpd)));
        return res;
    }

    private int computeQueryResultSetCardinality(Query qOri, IRDFDataset d1) {
        if (d1 instanceof FileBasedRDFDataset) {
            return computeFileBasedQueryResultSetCardinality(qOri, d1);
        } else if (d1 instanceof SPARQLEndPointBasedRDFDataset) {
            return computeSPARQLEndPointQueryResultSetCardinality(qOri, d1);
        }
        return 0;
    }

    private int computeFileBasedQueryResultSetCardinality(Query qOri, IRDFDataset d1) {
        int cardinality = 0;
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
                cardinality = resList.size();
                qexec.close();

                return cardinality;

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryResultTypeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cardinality;
    }

    private int computeSPARQLEndPointQueryResultSetCardinality(Query qOri, IRDFDataset d1) {
        int cardinality = 0;

        String endpoint = (String) d1.getPath();
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri, (String)d1.getGraph());

//        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, qOri);
        if (qOri.isSelectType()) {
            ResultSet results = qexec.execSelect();
            List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
            cardinality = resList.size();
            qexec.close();
            return cardinality;
        }
        return cardinality;
    }

    // QueryClassDensity
    private float computeQueryClassDensity(Query qOri, IRDFDataset d1) {
        float queryClassDensity = 0;
        //...extract the set of classes from a query
        Set<String> queryClasssSet = extractQueryClassSet(qOri, d1);
        System.out.println("[QueryResultSizeSimilarity::computeQueryClassDensity] queryClasssSet " + queryClasssSet.toString());

        //... for each class, compute its extension
        //... and compute the sum of EXT(Ci)
        int sumClassesEXT = 0;
        for (String classe : queryClasssSet) {
            int classEXTcardinality = computeClassEXTcardinality(classe, d1);
            //this.classEXTMap_QO.put(classe, classEXT);
            sumClassesEXT = sumClassesEXT + classEXTcardinality;
        }
        if (!queryClasssSet.isEmpty()) {
            // compute the query class density
            queryClassDensity = (float) ((1.0 * sumClassesEXT) / (1.0 * queryClasssSet.size()));
            System.out.println("[QueryResultSizeSimilarity::computeQueryClassDensity] queryClassDensity  " + queryClassDensity);
        }
        return queryClassDensity;
    }

    private Set extractQueryClassSet(Query qOri, IRDFDataset d1) {
        SQClassVisitor v = new SQClassVisitor(d1);
        // This will walk through all parts of the query
        ElementWalker.walk(qOri.getQueryPattern(), v);
        System.out.println("[QueryResultSizeSimilarity::extractQueryClassSet] v.getQueryClassSet()  " + v.getQueryClassSet().toString());

        return v.getQueryClassSet();
    }

    private int computeClassEXTcardinality(String classe, IRDFDataset d1) {
        int class_ext = 0;
        if (d1 instanceof FileBasedRDFDataset) {
            class_ext = computeFileBasedClassExtCardinality(classe, d1);
        } else if (d1 instanceof SPARQLEndPointBasedRDFDataset) {
            class_ext = computeSPARQLEndPointBasedClassExtCardinality(classe, d1);
        }
        return class_ext;
    }

    private int computeFileBasedClassExtCardinality(String classe, IRDFDataset d1) {
        int card = 0;
        try {
            OntModel inf = ModelFactory.createOntologyModel();
            InputStream in = new FileInputStream((String) d1.getPath());
            if (in == null) {
                throw new IllegalArgumentException("File: " + (String) d1.getPath() + " not found");
            }   //...import the content of the owl file in the Jena model. 
            inf.read(in, "");
            //...querying ...

            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + " SELECT DISTINCT ?indiv where "
                    + "{ "
                    + " ?indiv rdf:type <" + classe + "> . "
                    + "}";
            // Query q = QueryFactory.create(qString);
            QueryExecution qexec = QueryExecutionFactory.create(qString, inf);

            ResultSet results = qexec.execSelect();
            List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);

            card = resList.size();

            System.out.println("[QueryResultSizeSimilarity::computeFileBasedClassExtCardinality] " + classe + " has EXT cardinality = " + card);

            qexec.close();
            return card;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryResultTypeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return card;
    }

    private int computeSPARQLEndPointBasedClassExtCardinality(String classe, IRDFDataset d1) {

        int card = 0;
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + " SELECT DISTINCT ?indiv where "
                + "{ "
                + " ?indiv rdf:type <" + classe + "> . "
                + "}";
        QueryExecution qexec = QueryExecutionFactory.sparqlService((String) d1.getPath(), qString, (String) d1.getGraph());
        ResultSet results = qexec.execSelect();
        List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
        card = resList.size();
        qexec.close();
        return card;
    }

    // QueryObjectPropertyDensity    
    private float computeQueryObjectPropertyDensity(Query qOri, IRDFDataset d1) {
        float queryObjectPropertyDensity = 0;
        //...extract the set of classes from a query
        Set<String> queryObjectPropertySet = extractQueryObjectPropertySet(qOri, d1);
        System.out.println("[QueryResultSizeSimilarity::computeQueryObjectPropertyDensity] queryObjectPropertySet " + queryObjectPropertySet.toString());
        //... for each class, compute its extension
        //... and compute the sum of EXT(Ci)
        int sumObjectPropertiesEXT = 0;
        for (String prop : queryObjectPropertySet) {
            int objectPropertiesEXTcardinality = computeObjectPropertiesEXTcardinality(prop, d1);
            //this.classEXTMap_QO.put(classe, classEXT);
            sumObjectPropertiesEXT = sumObjectPropertiesEXT + objectPropertiesEXTcardinality;
        }

        if (!queryObjectPropertySet.isEmpty()) {
            // compute the query class density
            queryObjectPropertyDensity = (float) ((1.0 * sumObjectPropertiesEXT) / (1.0 * queryObjectPropertySet.size()));
            System.out.println("[QueryResultSizeSimilarity::computeQueryObjectPropertyDensity] queryObjectPropertyDensity  " + queryObjectPropertyDensity);
        }

        return queryObjectPropertyDensity;
    }

    private Set<String> extractQueryObjectPropertySet(Query qOri, IRDFDataset d1) {
        SQObjectPropertyVisitor v = new SQObjectPropertyVisitor(d1);
        // This will walk through all parts of the query
        ElementWalker.walk(qOri.getQueryPattern(), v);
        System.out.println("[QueryResultSizeSimilarity::extractQueryObjectPropertySet] v.ObjectProperty()  " + v.getQueryObjectPropertySet().toString());

        return v.getQueryObjectPropertySet();
    }

    private int computeObjectPropertiesEXTcardinality(String prop, IRDFDataset d1) {
        int objprop_ext = 0;
        if (d1 instanceof FileBasedRDFDataset) {
            objprop_ext = computeFileBasedObjectPropertyExtCardinality(prop, d1);
        } else if (d1 instanceof SPARQLEndPointBasedRDFDataset) {
            objprop_ext = computeSPARQLEndPointBasedObjectPropertExtCardinality(prop, d1);
        }
        return objprop_ext;
    }

    private int computeFileBasedObjectPropertyExtCardinality(String prop, IRDFDataset d1) {
        int card = 0;
        try {
            OntModel inf = ModelFactory.createOntologyModel();
            InputStream in = new FileInputStream((String) d1.getPath());
            if (in == null) {
                throw new IllegalArgumentException("File: " + (String) d1.getPath() + " not found");
            }   //...import the content of the owl file in the Jena model. 
            inf.read(in, "");
            //...querying ...

            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                    + " SELECT DISTINCT ?indiv1 ?indiv2 where "
                    + "{ "
                    + " <" + prop + "> rdf:type owl:ObjectProperty . "
                    + " ?indiv1  <" + prop + "> ?indiv2 . "
                    + "}";

//            
//            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
//                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//                    + " SELECT DISTINCT ?indiv1 ?indiv2 where "
//                    + "{ "
//                    + " ?indiv1 rdf:type ?class1 . "
//                    + " {?class1 rdf:type owl:Class .} "
//                    + " UNION "
//                    + " {?class1 rdf:type rdfs:Class .} "
//                    + " ?indiv2 rdf:type ?class2 . "
//                    + " {?class2 rdf:type owl:Class .} "
//                    + " UNION "
//                    + " {?class2 rdf:type rdfs:Class .} "
//                    + " ?indiv1  <" + prop + "> ?indiv2 . "
//                    + "}";
//            // Query q = QueryFactory.create(qString);
            QueryExecution qexec = QueryExecutionFactory.create(qString, inf);
            ResultSet results = qexec.execSelect();
            List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
            card = resList.size();
            System.out.println("[QueryResultSizeSimilarity::computeFileBasedObjectPropertyExtCardinality] the Obj" + prop + " has EXT cardinality = " + card);
            qexec.close();
            return card;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryResultSizeDistance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return card;
    }

    private int computeSPARQLEndPointBasedObjectPropertExtCardinality(String prop, IRDFDataset d1) {
        int card = 0;
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                + " SELECT DISTINCT ?indiv1 ?indiv2 where "
                + "{ "
                + " ?indiv1 rdf:type ?class1 . "
                + " {?class1 rdf:type owl:Class .} "
                + " UNION "
                + " {?class1 rdf:type rdfs:Class .} "
                + " ?indiv2 rdf:type ?class2 . "
                + " {?class2 rdf:type owl:Class .} "
                + " UNION "
                + " {?class2 rdf:type rdfs:Class .} "
                + " ?indiv1  <" + prop + "> ?indiv2 . "
                + "}";
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService((String) d1.getPath(), qString, (String) d1.getGraph())) {
            ResultSet results = qexec.execSelect();
            List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
            card = resList.size();
        }
        return card;
    }

    // QueryDatatypePropertyDensity    
    private float computeQueryDatatypePropertyDensity(Query qOri, IRDFDataset d1) {

        float queryDatatypePropertyDensity = 0;
        //...extract the set of classes from a query
        Set<String> queryDatatypePropertySet = extractQueryDatatypePropertySet(qOri, d1);
        System.out.println("[QueryResultSizeSimilarity::computeQueryDatatypePropertyDensity] queryDatatypePropertySet " + queryDatatypePropertySet.toString());
        //... for each class, compute its extension
        //... and compute the sum of EXT(Ci)
        int sumDatatypePropertiesEXT = 0;
        for (String prop : queryDatatypePropertySet) {
            int datatypePropertiesEXTcardinality = computeDatatypePropertiesEXTcardinality(prop, d1);
            //this.classEXTMap_QO.put(classe, classEXT);
            sumDatatypePropertiesEXT = sumDatatypePropertiesEXT + datatypePropertiesEXTcardinality;
        }
        // compute the query class density

        if (!queryDatatypePropertySet.isEmpty()) {
            queryDatatypePropertyDensity = (float) ((1.0 * sumDatatypePropertiesEXT) / (1.0 * queryDatatypePropertySet.size()));
            System.out.println("[QueryResultSizeSimilarity::computeQueryClassDensity] queryDatatypePropertyDensity  " + queryDatatypePropertyDensity);
        }

        return queryDatatypePropertyDensity;
    }

    private Set<String> extractQueryDatatypePropertySet(Query qOri, IRDFDataset d1) {
        SQDatatypePropertyVisitor v = new SQDatatypePropertyVisitor(d1);
        // This will walk through all parts of the query
        ElementWalker.walk(qOri.getQueryPattern(), v);
        System.out.println("[QueryResultSizeSimilarity::extractQueryDatatypePropertySet] v.ObjectProperty()  " + v.getQueryDatatypePropertySet().toString());

        return v.getQueryDatatypePropertySet();
    }

    private int computeDatatypePropertiesEXTcardinality(String prop, IRDFDataset d1) {
        int dtprop_ext = 0;
        if (d1 instanceof FileBasedRDFDataset) {
            dtprop_ext = computeFileBasedDatatypePropertyExtCardinality(prop, d1);
        } else if (d1 instanceof SPARQLEndPointBasedRDFDataset) {
            dtprop_ext = computeSPARQLEndPointBasedDatatypePropertExtCardinality(prop, d1);
        }
        return dtprop_ext;
    }

    private int computeFileBasedDatatypePropertyExtCardinality(String prop, IRDFDataset d1) {
        int card = 0;
        try {
            OntModel inf = ModelFactory.createOntologyModel();
            InputStream in = new FileInputStream((String) d1.getPath());
            if (in == null) {
                throw new IllegalArgumentException("File: " + (String) d1.getPath() + " not found");
            }   //...import the content of the owl file in the Jena model. 
            inf.read(in, "");
            //...querying ...

            String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                    + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                    + " SELECT DISTINCT ?indiv1 ?indiv2 where "
                    + "{ "
                    + " <" + prop + "> rdf:type owl:DatatypeProperty . "
                    + " ?indiv1  <" + prop + "> ?indiv2 . "
                    + "}";
            // Query q = QueryFactory.create(qString);
            QueryExecution qexec = QueryExecutionFactory.create(qString, inf);

            ResultSet results = qexec.execSelect();
            List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);

            card = resList.size();

            System.out.println("[QueryResultSizeSimilarity::computeFileBasedDatatypePropertyExtCardinality] the Datatype" + prop + " has EXT cardinality = " + card);

            qexec.close();
            return card;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryResultSizeDistance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return card;
    }

    private int computeSPARQLEndPointBasedDatatypePropertExtCardinality(String prop, IRDFDataset d1) {
        int card = 0;
        String qString = "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "prefix owl:<http://www.w3.org/2002/07/owl#> "
                + " SELECT DISTINCT ?indiv1 ?indiv2 where "
                + "{ "
                + " <" + prop + "> rdf:type owl:DatatypeProperty . "
                + " ?indiv1  <" + prop + "> ?indiv2 . "
                + "}";
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService((String) d1.getPath(), qString, (String) d1.getGraph())) {
            ResultSet results = qexec.execSelect();
            List<QuerySolution> resList = ResultSetFormatter.toList(results);//.out(, results, q);
            card = resList.size();
        }
        return card;
    }

}
