/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.index.II;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.util.Version;
import uk.ac.open.kmi.squire.index.RDFDatasetIndexer;

/**
 *
 * @author carloallocca
 */
public class RDFDatasetIndexerII {

    private final Version version = Version.LUCENE_5_4_0;
    private final String indexDirectoryPath;
    private static IndexWriter indexWriter = null;
    private static Directory indexDirectory = null;
    private static Path path;

    public RDFDatasetIndexerII(String indexDirPath) throws IOException {
        this.indexDirectoryPath = indexDirPath;
        getIndexWriter();
    }

    public RDFDatasetIndexerII() throws IOException {
        File file = new File("RDFDatasetIndex");
        this.indexDirectoryPath = file.getAbsoluteFile().getAbsolutePath();
        getIndexWriter();
    }

    // Add a new SPARQL EndPoint to the index 
    public void addSignature(String urlAddress, String graphName,
            ArrayList<String> classSet,
            ArrayList<String> objectPropertySet,
            ArrayList<String> datatypePropertySet,
            ArrayList<String> individualSet,
            ArrayList<String> literalSet,
            ArrayList<String> rdfVocabulary,
            ArrayList<String> propertySet) throws IOException, CorruptIndexException, LockObtainFailedException, Exception {
        if (!(_alreadyExists(urlAddress, graphName))) {
            Document doc = new Document();
            StringField url = new StringField("URL", urlAddress, Field.Store.YES);
            StringField gName = new StringField("GraphName", graphName, Field.Store.YES);
            StringField cSet = new StringField("ClassSet", classSet.toString(), Field.Store.YES);
            StringField oPropSet = new StringField("ObjectPropertySet", objectPropertySet.toString(), Field.Store.YES);
            StringField dPropertySet = new StringField("DatatypePropertySet", datatypePropertySet.toString(), Field.Store.YES);
            StringField litSet = new StringField("LiteralSet", literalSet.toString(), Field.Store.YES);
            StringField indSet = new StringField("IndividualSet", individualSet.toString(), Field.Store.YES);
            StringField rdfVoc = new StringField("RDFVocabulary", rdfVocabulary.toString(), Field.Store.YES);
            StringField propSet = new StringField("PropertySet", propertySet.toString(), Field.Store.YES);
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
            indexWriter.commit();
        }
    }

    public Document getSignature(String urlAddress, String graphName) throws IOException {

        IndexWriter writer = getIndexWriter();
        IndexReader reader = DirectoryReader.open(writer, true);
        IndexSearcher searcher = new IndexSearcher(reader);
        //..prepare the query
        BooleanQuery query = new BooleanQuery();
        if (null != graphName || !graphName.isEmpty()) {
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
            } else {
                return null;
            }
        } else {
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
            } else {
                return null;
            }
        }
    }

    public boolean isIndexed(String urlAddress, String graphName) throws IOException, NullPointerException {
        IndexWriter writer = getIndexWriter();
        String nullPointerExceptionReason = "[RDFDatasetIndexerII::alreadyExists] NullPointerException: The DirectoryReader cannot open the IndexWriter.";
        IndexReader reader;
        reader = DirectoryReader.open(writer, true);//.open(index1);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();

        if (null != graphName || !graphName.isEmpty()) {
            query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
            query.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
            TopDocs results = searcher.search(query, 1);
            return results.totalHits > 0;
        } else {
            query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
            TopDocs results = searcher.search(query, 1);
            return results.totalHits > 0;
        }
    }

    
    
    
    
    
    
    
    
    private boolean _alreadyExists(String urlAddress, String graphName) throws IOException, NullPointerException {
        IndexWriter writer = getIndexWriter();
        String nullPointerExceptionReason = "[RDFDatasetIndexerII::alreadyExists] NullPointerException: The DirectoryReader cannot open the IndexWriter.";
        IndexReader reader;
        reader = DirectoryReader.open(writer, true);//.open(index1);
//        try {
//            reader = DirectoryReader.open(writer, true);
//        } catch (NullPointerException e) {
//            throw new NullPointerException(nullPointerExceptionReason);
//        }
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();

        if (null != graphName || !graphName.isEmpty()) {
            query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
            query.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
            TopDocs results = searcher.search(query, 1);
            return results.totalHits > 0;
        } else {
            query.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
            TopDocs results = searcher.search(query, 1);
            return results.totalHits > 0;
        }
    }

    private IndexWriter getIndexWriter() throws LockObtainFailedException, IOException {
        if (indexWriter == null) {
            String securityExceptionReason = "[RDFDatasetIndexerII::getIndexWriter] permission issue to access the file system.";
            String fileSystemNotFoundExceptionReason = "[RDFDatasetIndexerII::getIndexWriter] The file system, identified by the " + this.indexDirectoryPath + "does not exist and cannot be created automatically.";
            String illegalArgumentExceptionReason = "[RDFDatasetIndexerII::getIndexWriter] Wrong format of the" + this.indexDirectoryPath + "parameter.";
            Analyzer analyzer = new StandardAnalyzer();
            try {
                path = Paths.get(this.indexDirectoryPath);
                indexDirectory = FSDirectory.open(path, NativeFSLockFactory.INSTANCE);//The primary benefit of NativeFSLockFactory is that locks (not the lock file itsself) will be properly removed (by the OS) if the JVM has an abnormal exit.
                try {
                    IndexWriterConfig config = new IndexWriterConfig(analyzer);//.setOpenMode(OpenMode.CREATE_OR_APPEND);
                    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);/*IndexWriterConfig.OpenMode.CREATE_OR_APPEND if used IndexWriter will create a new index if there is not already an index at the provided path and otherwise open the existing index.*/
                    indexWriter = new IndexWriter(indexDirectory, config);
                } catch (LockObtainFailedException lofe) {
                    indexDirectory.deleteFile(this.indexDirectoryPath + "/write.lock");
                }
            } catch (IllegalArgumentException iae) {
                Logger.getLogger(RDFDatasetIndexerII.class.getName()).log(Level.SEVERE, illegalArgumentExceptionReason, iae);
                throw new IllegalArgumentException(illegalArgumentExceptionReason);
            } catch (FileSystemNotFoundException fse) {
                throw new FileSystemNotFoundException(fileSystemNotFoundExceptionReason);
            } catch (SecurityException se) {
                throw new SecurityException(securityExceptionReason);
            }
        }
        return indexWriter;
    }

}
