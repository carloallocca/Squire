/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core;

import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import java.util.List;
import org.apache.jena.query.Query;

/**
 *
 * @author callocca
 */
public interface IQueryRecommendation {
    
    public List<Query> queryRecommendation();
    public List<Query> queryRecommendation(String querySPARQL);
    public List<Query> queryRecommendation(String querySPARQL, IRDFDataset d1, IRDFDataset d2);
    
}
