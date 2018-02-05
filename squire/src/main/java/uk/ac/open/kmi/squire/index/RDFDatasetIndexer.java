/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author carloallocca
 *
 */
public class RDFDatasetIndexer {

	private static RDFDatasetIndexer me;

	public static RDFDatasetIndexer getInstance() {
		if (me == null) me = new RDFDatasetIndexer();
		return me;
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Version version = Version.LUCENE_5_5_5;

	private String datasetIndexDir;

	private RDFDatasetIndexer() {
		log.debug("Setting up new Lucene {} indexer", this.version);
		File file = new File("RDFDatasetIndex");
		this.datasetIndexDir = file.getAbsoluteFile().getAbsolutePath();
		log.debug("Using index at directory {}", datasetIndexDir);
		createIndex();
	}

	public Document getSignature(String urlAddress, String graphName) {
		Builder queryBuilder = new Builder();
		try {
			IndexReader reader = DirectoryReader.open(getIndex());
			IndexSearcher searcher = new IndexSearcher(reader);
			queryBuilder.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
			if (null != graphName && !graphName.isEmpty())
				queryBuilder.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
			TopScoreDocCollector collector = TopScoreDocCollector.create(1);
			searcher.search(queryBuilder.build(), collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			if (hits.length > 0) return searcher.doc(hits[0].doc);
		} catch (IOException ex) {
			log.error("Exception occurred while trying to access index.", ex);
		}
		return null;
	}

	public Document indexSignature(String urlAddress, String graphName, Collection<String> classSet,
			Collection<String> objectPropertySet, Collection<String> datatypePropertySet,
			Collection<String> individualSet, Collection<String> literalSet, Collection<String> rdfVocabulary) {

		// Analyzer analyzer = new WhitespaceAnalyzer();
		Analyzer analyzer = new StandardAnalyzer();

		try {
			if (!(alreadyExists(urlAddress, graphName))) {
				IndexWriterConfig config = new IndexWriterConfig(analyzer);// .setOpenMode(OpenMode.CREATE_OR_APPEND);
				/*
				 * IndexWriterConfig.OpenMode.CREATE_OR_APPEND if used IndexWriter will create a
				 * new index if there is not already an index at the provided path and otherwise
				 * open the existing index.
				 */
				config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
				IndexWriter indexWriter = new IndexWriter(getIndex(), config);

				Document doc = new Document();
				// index the SPARQL endpoint

				// StringField url = new StringField("URL", urlAddress, Field.Store.YES);
				// StringField gName = new StringField("GraphName", graphName, Field.Store.YES);
				// StringField cSet = new StringField("ClassSet", classSet.toString(),
				// Field.Store.NO);
				// StringField oPropSet = new StringField("ObjectPropertySet",
				// objectPropertySet.toString(),
				// Field.Store.NO);
				// StringField dPropertySet = new StringField("DatatypePropertySet",
				// datatypePropertySet.toString(), Field.Store.NO);
				// StringField litSet = new StringField("LiteralSet", literalSet.toString(),
				// Field.Store.NO);
				// StringField indSet = new StringField("IndividualSet",
				// individualSet.toString(),
				// Field.Store.NO);
				// StringField rdfVoc = new StringField("RDFVocabulary",
				// rdfVocabulary.toString(),
				// Field.Store.NO);
				// StringField propSet = new StringField("PropertySet", propertySet.toString(),
				// Field.Store.NO);

				doc.add(new Field("URL", urlAddress, Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("GraphName", graphName, Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("ClassSet", classSet.toString(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field("ObjectPropertySet", objectPropertySet.toString(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field("DatatypePropertySet", datatypePropertySet.toString(), Field.Store.YES,
						Field.Index.NO));
				doc.add(new Field("LiteralSet", literalSet.toString(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field("IndividualSet", individualSet.toString(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field("RDFVocabulary", rdfVocabulary.toString(), Field.Store.YES, Field.Index.NO));

				indexWriter.addDocument(doc);
				// indexWriter.optimize();
				indexWriter.close();

				// Logger.getLogger(SPARQEndPoint.class.getName()).log(Level.SEVERE, null,
				// e.getMessage());
				// Logger.getLogger(SPARQEndPoint.class.getName()).log(Level.SEVERE, null,
				// Arrays.toString(e.getStackTrace()));
				// Logger.getLogger(SPARQEndPoint.class.getName()).log(Level.SEVERE, null,
				// e.getCause());
				//

				return doc;
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
		return null;
	}

	// Add a new SPARQL EndPoint to the index
	public Document indexSignature(String urlAddress, String graphName, Collection<String> classSet,
			Collection<String> objectPropertySet, Collection<String> datatypePropertySet,
			Collection<String> individualSet, Collection<String> literalSet, Collection<String> rdfVocabulary,
			Collection<String> propertySet) {
		Document res = indexSignature(urlAddress, graphName, classSet, objectPropertySet, datatypePropertySet,
				individualSet, literalSet, rdfVocabulary);
		if (res != null) res.add(new Field("PropertySet", propertySet.toString(), Field.Store.YES, Field.Index.NO));
		return res;
	}

	private boolean alreadyExists(String urlAddress, String graphName) {
		Builder queryBuilder = new Builder();
		TopDocs results;
		try {
			IndexReader reader = DirectoryReader.open(getIndex());
			IndexSearcher searcher = new IndexSearcher(reader);
			// Prepare the query
			queryBuilder.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
			if (null != graphName && !graphName.isEmpty())
				queryBuilder.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
			results = searcher.search(queryBuilder.build(), 1);
		} catch (IOException ex) {
			log.error("Exception occurred while trying to access index.", ex);
			return false;
		}
		return results.totalHits > 0;
	}

	private void createIndex() {
		// Analyzer analyzer = new StopAnalyzer();
		Analyzer analyzer = new StandardAnalyzer();
		Directory index = null;
		try {
			Path path = Paths.get(this.datasetIndexDir);
			index = FSDirectory.open(path); // getDirectory(this.datasetIndexDir);
			// IndexWriterConfig config = new
			// IndexWriterConfig(analyzer);//.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriterConfig config = new IndexWriterConfig(null);// .setOpenMode(OpenMode.CREATE_OR_APPEND);
			/*
			 * IndexWriterConfig.OpenMode.CREATE_OR_APPEND if used IndexWriter will create a
			 * new index if there is not already an index at the provided path and otherwise
			 * open the existing index.
			 */
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			IndexWriter indexWriter = new IndexWriter(index, config);
			indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Directory getIndex() throws IOException {
		Path path = Paths.get(this.datasetIndexDir);
		return FSDirectory.open(path);
	}

}
