/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core4;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
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
import uk.ac.open.kmi.squire.operation.SPARQLQueryGeneralization;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 */
public class QueryGeneralizer4 {

    private final IRDFDataset rdfd1;
    private final IRDFDataset rdfd2;

    private final Query originalQuery;
    private Query originalQueryCopy;
    private Query queryTemplate;

    private final LiteralVarMapping literalVarTable;
    private final ClassVarMapping classVarTable;
    private final DatatypePropertyVarMapping datatypePropertyVarTable;
    private final IndividualVarMapping individualVarTable;
    private final ObjectPropertyVarMapping objectProperyVarTable;
    private final RDFVocVarMapping rdfVocVarTable;

    private static final String CLASS_TEMPLATE_VAR = "ct";
    private static final String OBJ_PROP_TEMPLATE_VAR = "opt";
    private static final String DT_PROP_TEMPLATE_VAR = "dpt";
    private static final String INDIVIDUAL_TEMPLATE_VAR = "it";
    private static final String LITERAL_TEMPLATE_VAR = "lt";

    public QueryGeneralizer4(Query qo, IRDFDataset d1, IRDFDataset d2) {
        this.originalQuery = qo;
        this.originalQueryCopy= QueryFactory.create(qo.toString());

        this.rdfd1 = d1;
        this.rdfd2 = d2;

        classVarTable = new ClassVarMapping();
        individualVarTable = new IndividualVarMapping();
        literalVarTable = new LiteralVarMapping();
        objectProperyVarTable = new ObjectPropertyVarMapping();
        datatypePropertyVarTable = new DatatypePropertyVarMapping();
        rdfVocVarTable = new RDFVocVarMapping();

    }

    public Query generalize() {
        if (this.originalQueryCopy == null) {
            throw new IllegalStateException("[QueryGeneralizer4::generalize()]The query is null!!");
        }
        Set<Node> subjects = getSubjectsSet(this.originalQueryCopy);
        Set<Node> predicates = getPredicatesSet(this.originalQueryCopy);
        Set<Node> objects = getObjectsSet(this.originalQueryCopy);
        SPARQLQueryGeneralization qg = new SPARQLQueryGeneralization();
        //SUBJECT
        for (Node subj : subjects) {
            if (!(subj.isVariable()) && !(subj.isBlank())) {
                Var templateVarSub = ifSubjectIsNotD2ThenGenerateVariableNew(subj);
                if (templateVarSub != null) {
                    Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQueryCopy, subj, templateVarSub);
                    this.originalQueryCopy = genQuery;
                }
            }
        }
        //PREDICATE
        for (Node pred : predicates) {
            if (!(pred.isVariable()) && !(pred.isBlank())) {
                Var templateVarPred = ifPredicateIsNotD2ThenGenerateVariableNew(pred);
                if (templateVarPred != null) {
                    Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQueryCopy, pred, templateVarPred);
                    this.originalQueryCopy = genQuery;
                }
            }
        }
        //OBEJCT
        for (Node obj : objects) {
            if (!(obj.isVariable()) && !(obj.isBlank())) {
                Var templateVarObj = ifObjectIsNotD2ThenGenerateVariableNew(obj);
                if (templateVarObj != null) {
                    Query genQuery = qg.generalizeFromNodeToVarTemplate(this.originalQueryCopy, obj, templateVarObj);
                    this.originalQueryCopy = genQuery;
                }
            }
        }
        return this.originalQueryCopy;
    }

    private Var ifSubjectIsNotD2ThenGenerateVariableNew(Node subj) {
        if (subj == null || rdfd2 == null) {
            throw new IllegalStateException("[QueryGeneralizer4::ifSubjectIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
        }
        final Var result;
        //SUBJECT
        if (subj.isURI()) {
            // s= classURI
            String sub = subj.getURI();
            if ((rdfd1.getClassSet().contains(sub)) && !(rdfd2.getClassSet().contains(sub))) {
                result = Var.alloc(classVarTable.generateIFAbsentClassVar(sub));
                return result;
            } else if (rdfd1.isInObjectPropertySet(sub) && !(rdfd2.isInObjectPropertySet(sub))) {
                //if (!(rdfd2.isInObjectPropertySet(o))) {
                result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(sub));
                //          System.out.println("[QTTree::generalize] The Sub is an Object Property URI");
                return result;
            } else if (rdfd1.isInDatatypePropertySet(sub) && !(rdfd2.isInDatatypePropertySet(sub))) {
                //if (!(rdfd2.isInDatatypePropertySet(o))) {
                result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(sub));
                //    System.out.println("[QTTree::generalize] The Sub is an datatype Property URI");
                return result;
            } else if (rdfd1.isInRDFVocabulary(sub) && !(rdfd2.isInRDFVocabulary(sub))) {
                //if (!(rdfd2.isInRDFVocabulary(o))) {
                result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(sub));
                //System.out.println("[QTTree::generalize] The Sub is an RDF voc term URI");
                return result;
            } else {
                // this means that it is an individual
                result = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(sub));
                return result;
            }
        } else if (subj.isLiteral()) {
            String subjAsString = subj.getLiteralValue().toString();
            result = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(subjAsString));
            return result;
        } else {
            //subject = tp.getSubject();
            result = (Var) subj;
            return result;
        }
    }

    private Var ifPredicateIsNotD2ThenGenerateVariableNew(Node pred) {

        if (pred == null || rdfd2 == null) {
            throw new IllegalStateException("[QueryGeneralizer4::ifPredicateIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
        }
        final Var result;
        if (pred.isURI()) {
            String pre = pred.getURI();
            if (rdfd1.isInObjectPropertySet(pre) && !(rdfd2.isInObjectPropertySet(pre))) {
                result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(pre));
                return result;
            } else if (rdfd1.isInDatatypePropertySet(pre) && !(rdfd2.isInDatatypePropertySet(pre))) {
                result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(pre));
                return result;
            } //            else if (rdfd1.isInRDFVocabulary(pre) && !(rdfd2.isInRDFVocabulary(pre))) {
            //                result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(pre));
            //                return result;
            //            } 
            else {
                result = null;
                return result;
            }
        } else {
            result = (Var) pred;
            return result;
        }

    }

    private Var ifObjectIsNotD2ThenGenerateVariableNew(Node obj) {

        if (obj == null || rdfd2 == null) {
            throw new IllegalStateException("[QueryGeneralizer::ifSubjectIsNotD2ThenGenerateVariable(Node subj)]The subj or rdfd2 is null!!");
        }
        final Var result;
        //SUBJECT
        if (obj.isURI()) {
            // s= classURI
            String o = obj.getURI();
            //System.out.println("[QTTree::generalize] The Sub is an URI " + subj);
            if ((rdfd1.getClassSet().contains(o)) && !(rdfd2.getClassSet().contains(o))) {
                result = Var.alloc(classVarTable.generateIFAbsentClassVar(o));
                return result;
            } else if (rdfd1.isInObjectPropertySet(o) && !(rdfd2.isInObjectPropertySet(o))) {
                //if (!(rdfd2.isInObjectPropertySet(o))) {                
                result = Var.alloc(objectProperyVarTable.generateIFAbsentObjectPropertyVar(o));
                //          System.out.println("[QTTree::generalize] The Sub is an Object Property URI");
                return result;
            } else if (rdfd1.isInDatatypePropertySet(o) && !(rdfd2.isInDatatypePropertySet(o))) {
                //if (!(rdfd2.isInDatatypePropertySet(o))) {

                result = Var.alloc(datatypePropertyVarTable.generateIFAbsentDatatypePropertyVar(o));
                //    System.out.println("[QTTree::generalize] The Sub is an datatype Property URI");
                return result;
            } else if (rdfd1.isInRDFVocabulary(o) && !(rdfd2.isInRDFVocabulary(o))) {
                //if (!(rdfd2.isInRDFVocabulary(o))) {

                result = Var.alloc(rdfVocVarTable.generateIFAbsentRDFVocVar(o));
                //System.out.println("[QTTree::generalize] The Sub is an RDF voc term URI");
                return result;
            } else {
                // this means that it is an individual
                result = Var.alloc(individualVarTable.generateIFAbsentIndividualVar(o));
                return result;
            }
        } else if (obj.isLiteral()) {
            String subjAsString = obj.getLiteralValue().toString();
            result = Var.alloc(literalVarTable.generateIFAbsentLiteralVar(subjAsString));
            return result;
        } else {
            //subject = tp.getSubject();
            result = (Var) obj;
            return result;
        }
//        return result;

    }

    private Set<Node> getSubjectsSet(Query originalQuery) {

        if (this.originalQuery == null) {
            throw new IllegalStateException("[QueryGeneralizer::getSubjectsSet(Query originalQuery)]The query is null!!");
        }
        final Set<Node> subjects = new HashSet<Node>();
        // This will walk through all parts of the query
        ElementWalker.walk(this.originalQuery.getQueryPattern(),
                // For each element
                new ElementVisitorBase() {
            // ...when it's a block of triples...
            public void visit(ElementPathBlock el) {
                // ...go through all the triples...
                Iterator<TriplePath> triples = el.patternElts();
                while (triples.hasNext()) {
                    // ...and grab the subject
                    subjects.add(triples.next().getSubject());
                }
            }
        }
        );
        return subjects;
    }

    private Set<Node> getPredicatesSet(Query originalQuery) {
        if (this.originalQuery == null) {
            throw new IllegalStateException("[QueryGeneralizer::getSubjectsSet(Query originalQuery)]The query is null!!");
        }

        // Remember distinct predicates in this
        final Set<Node> predicates = new HashSet<Node>();
        // This will walk through all parts of the query
        ElementWalker.walk(this.originalQuery.getQueryPattern(),
                // For each element
                new ElementVisitorBase() {
            // ...when it's a block of triples...
            public void visit(ElementPathBlock el) {
                // ...go through all the triples...
                Iterator<TriplePath> triples = el.patternElts();
                while (triples.hasNext()) {
                    // ...and grab the subject
                    predicates.add(triples.next().getPredicate());
                }
            }
        }
        );
        return predicates;
    }

    private Set<Node> getObjectsSet(Query originalQuery) {
        if (this.originalQuery == null) {
            throw new IllegalStateException("[QueryGeneralizer::getSubjectsSet(Query originalQuery)]The query is null!!");
        }
        // Remember distinct objects in this
        final Set<Node> objects = new HashSet<Node>();
        // This will walk through all parts of the query
        ElementWalker.walk(this.originalQuery.getQueryPattern(),
                // For each element
                new ElementVisitorBase() {
            // ...when it's a block of triples...
            public void visit(ElementPathBlock el) {
                // ...go through all the triples...
                Iterator<TriplePath> triples = el.patternElts();
                while (triples.hasNext()) {
                    // ...and grab the objects
                    objects.add(triples.next().getObject());
                }
            }
        }
        );
        return objects;

    }

    
    
    
    
    public Query getOriginalQuery() {
        return originalQuery;
    }

    public Query getQueryTemplate() {
        return queryTemplate;
    }

    public LiteralVarMapping getLiteralVarTable() {
        return literalVarTable;
    }

    public ClassVarMapping getClassVarTable() {
        return classVarTable;
    }

    public DatatypePropertyVarMapping getDatatypePropertyVarTable() {
        return datatypePropertyVarTable;
    }

    public IndividualVarMapping getIndividualVarTable() {
        return individualVarTable;
    }

    public ObjectPropertyVarMapping getObjectProperyVarTable() {
        return objectProperyVarTable;
    }

    public RDFVocVarMapping getRdfVocVarTable() {
        return rdfVocVarTable;
    }

}
