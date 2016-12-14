/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.querytemplate;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import org.apache.jena.query.Query;

/**
 *
 * @author callocca
 * we declared this interface as we consider that it may be the case that each type of queries
 * (e.g. ASK, SELECT, DECLARE) may have its logic for building the query template.
 * 
 */
public interface IQueryTemplate {
    
    public Query generateQueryTemplate();
    public Query generateQueryTemplate(String querySPARQL);
    public Query generateQueryTemplate(String querySPARQL, IRDFDataset d1, IRDFDataset d2);
    
}
