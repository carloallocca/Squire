/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.index;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.util.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import uk.ac.open.kmi.squire.rdfdataset.SPARQLEndPointBasedRDFDataset;

/**
 *
 * @author carloallocca
 *
 */
public class RDFDatasetIndexer {

    private static Log log = LogFactory.getLog(RDFDatasetIndexer.class);
    private final Version version = Version.LUCENE_5_4_0;

    private String datasetIndexDir;

    public RDFDatasetIndexer(String indexDir) {
        this.datasetIndexDir = indexDir;
        createIndex();
    }

    public RDFDatasetIndexer() {
        File file = new File("RDFDatasetIndex");
        this.datasetIndexDir = file.getAbsoluteFile().getAbsolutePath();
        createIndex();
    }

    private void createIndex() {
//        Analyzer analyzer = new StopAnalyzer();
          Analyzer analyzer = new StandardAnalyzer();
        Directory index = null;
        try {
            Path path = Paths.get(this.datasetIndexDir);
            index = FSDirectory.open(path); //getDirectory(this.datasetIndexDir);
            //IndexWriterConfig config = new IndexWriterConfig(analyzer);//.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriterConfig config = new IndexWriterConfig(null);//.setOpenMode(OpenMode.CREATE_OR_APPEND);
            /*
            IndexWriterConfig.OpenMode.CREATE_OR_APPEND if used IndexWriter will create a new index if there is not
            already an index at the provided path and otherwise open the existing index.
             */
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter indexWriter = new IndexWriter(index, config);
            indexWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Add a new SPARQL EndPoint to the index 
    public void addSPARQLEndPointSignature(String urlAddress, String graphName,
            ArrayList<String> classSet,
            ArrayList<String> objectPropertySet,
            ArrayList<String> datatypePropertySet,
            ArrayList<String> individualSet,
            ArrayList<String> literalSet,
            ArrayList<String> rdfVocabulary,
            ArrayList<String> propertySet) {

        //Analyzer analyzer = new WhitespaceAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();
        Directory index = null;

        try {
            if (!(alreadyExists(urlAddress, graphName))) {

                Path path = Paths.get(this.datasetIndexDir);
                index = FSDirectory.open(path); //getDirectory(this.datasetIndexDir);
                IndexWriterConfig config = new IndexWriterConfig(analyzer);//.setOpenMode(OpenMode.CREATE_OR_APPEND);
                /*
                    IndexWriterConfig.OpenMode.CREATE_OR_APPEND if used IndexWriter will create a new index if there is not
                    already an index at the provided path and otherwise open the existing index.
                 */
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                IndexWriter indexWriter = new IndexWriter(index, config);

                Document doc = new Document();
                //index the SPARQL endpoint
                

                
 
                StringField url = new StringField("URL", urlAddress, Field.Store.YES);
                StringField gName = new StringField("GraphName", graphName, Field.Store.YES);
                StringField cSet = new StringField("ClassSet", classSet.toString(), Field.Store.YES);
                StringField oPropSet = new StringField("ObjectPropertySet", objectPropertySet.toString(), Field.Store.YES);
                StringField dPropertySet = new StringField("DatatypePropertySet", datatypePropertySet.toString(), Field.Store.YES);
                StringField litSet = new StringField("LiteralSet", literalSet.toString(), Field.Store.YES);
                StringField indSet = new StringField("IndividualSet", individualSet.toString(), Field.Store.YES);
                StringField rdfVoc = new StringField("RDFVocabulary", rdfVocabulary.toString(), Field.Store.YES);
                StringField propSet = new StringField("PropertySet", propertySet.toString(), Field.Store.YES);

                
//                Field url = new Field("URL", urlAddress, Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field gName = new Field("GraphName", graphName, Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field cSet = new Field("ClassSet", classSet.toString(), Field.Store.YES, Field.Index.NO);
//                Field oPropSet = new Field("ObjectPropertySet", objectPropertySet.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field dPropertySet = new Field("DatatypePropertySet", datatypePropertySet.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field litSet = new Field("LiteralSet", literalSet.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field indSet = new Field("IndividualSet", individualSet.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field rdfVoc = new Field("RDFVocabulary", rdfVocabulary.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
//                Field propSet = new Field("PropertySet", propertySet.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);

                doc.add(url);
                doc.add(gName);
                doc.add(cSet);
                doc.add(oPropSet);
                doc.add(dPropertySet);
                doc.add(litSet);
                doc.add(indSet);
                doc.add(rdfVoc);
                doc.add(propSet);

                indexWriter.addDocument(doc);
                //indexWriter.optimize();
                indexWriter.close();
                
//                            Logger.getLogger(SPARQEndPoint.class.getName()).log(Level.SEVERE, null, e.getMessage());
//            Logger.getLogger(SPARQEndPoint.class.getName()).log(Level.SEVERE, null, Arrays.toString(e.getStackTrace()));
//            Logger.getLogger(SPARQEndPoint.class.getName()).log(Level.SEVERE, null, e.getCause());
//
                
                
            }
        } catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockObtainFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean alreadyExists(String urlAddress, String graphName) {
        try {
            if (null != graphName || !graphName.isEmpty()) {
                Path path = Paths.get(this.datasetIndexDir);
                Directory index1 = FSDirectory.open(path);
                IndexReader reader = DirectoryReader.open(index1);
                IndexSearcher searcher = new IndexSearcher(reader);
                BooleanQuery query = new BooleanQuery();
                query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
                query.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
                TopDocs results = searcher.search(query, 1);
                return results.totalHits > 0;
            } else {
                Path path = Paths.get(this.datasetIndexDir);
                Directory index1 = FSDirectory.open(path);
                IndexReader reader = DirectoryReader.open(index1);
                IndexSearcher searcher = new IndexSearcher(reader);
                BooleanQuery query = new BooleanQuery();
                query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
                TopDocs results = searcher.search(query, 1);
                return results.totalHits > 0;
            }
        } catch (IOException ex) {
            Logger.getLogger(RDFDatasetIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    //public SPARQLEndPointBasedRDFDataset getSPARQLEndPointSignature(String urlAddress, String graphName){    
    public Document getSPARQLEndPointSignature(String urlAddress, String graphName) {
        // SPARQLEndPointBasedRDFDataset output= new SPARQLEndPointBasedRDFDataset(urlAddress, graphName);
        try {
            if (null != graphName || !graphName.isEmpty()) {
                Path path = Paths.get(this.datasetIndexDir);
                Directory index1 = FSDirectory.open(path);
                IndexReader reader = DirectoryReader.open(index1);
                IndexSearcher searcher = new IndexSearcher(reader);
                //..prepare the query
                BooleanQuery query = new BooleanQuery();
                query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
                query.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
                //..find the docs by executing the query
                int hitsPerPage = 1;
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
                searcher.search(query, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                if (hits.length > 0) {
                    //..retrieve the related doc
                    int docId = hits[0].doc;
                    Document d = searcher.doc(docId);
                    return d;
                }
                else{
                    return null;
                }
            } else {
                Path path = Paths.get(this.datasetIndexDir);
                Directory index1 = FSDirectory.open(path);
                IndexReader reader = DirectoryReader.open(index1);
                IndexSearcher searcher = new IndexSearcher(reader);
                //..prepare the query
                BooleanQuery query = new BooleanQuery();
                query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
                //..find the docs by executing the query
                int hitsPerPage = 1;
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
                searcher.search(query, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                if (hits.length > 0) {
                    //..retrieve the related doc
                    int docId = hits[0].doc;
                    Document d = searcher.doc(docId);
                    return d;
                }
                else{
                    return null;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(RDFDatasetIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
 
    
}
