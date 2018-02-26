package uk.ac.open.kmi.squire.core;

/**
 * An abstraction over a BGP of set of BGPs. For example, the same query
 * template can be generated from:
 * 
 * SELECT ?x ?y WHERE { ?x a ?t ; ?p ?y }
 * 
 * and from
 * 
 * SELECT ?x ?y WHERE { ?x a ?class ; ?p ?y }
 * 
 * @author alexdma
 *
 */
public class QueryTemplate {

}
