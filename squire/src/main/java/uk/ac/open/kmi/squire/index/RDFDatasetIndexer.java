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
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
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
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.ClassSignature;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;

/**
 *
 * @author carloallocca
 *
 */
public class RDFDatasetIndexer {

	public static enum Fieldd {
		CLASS_SIGNATURES;
	}

	private static RDFDatasetIndexer me;

	public static RDFDatasetIndexer getInstance() {
		if (me == null) me = new RDFDatasetIndexer();
		return me;
	}

	private String datasetIndexDir;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Version version = Version.LUCENE_5_5_5;

	private RDFDatasetIndexer() {
		log.debug("Setting up new Lucene {} indexer", this.version);
		File file = new File("RDFDatasetIndex");
		this.datasetIndexDir = file.getAbsoluteFile().getAbsolutePath();
		log.debug("Using index at directory {}", datasetIndexDir);
		initIndex();
	}

	public Document getSignature(String urlAddress, String graphName) {
		Builder queryBuilder = new Builder();
		try {
			IndexReader reader = DirectoryReader.open(getIndex());
			IndexSearcher searcher = new IndexSearcher(reader);
			queryBuilder.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
			log.debug("Searching in index: URL={}", urlAddress);
			if (null != graphName && !graphName.isEmpty()) {
				log.debug(" ... and GraphName={}", graphName);
				queryBuilder.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
			}
			TopScoreDocCollector collector = TopScoreDocCollector.create(1);
			searcher.search(queryBuilder.build(), collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			log.trace(" ... Got {} hit{}", hits.length, hits.length > 1 ? "s" : "");
			if (hits.length > 0) {
				log.debug("Signature index present.");
				return searcher.doc(hits[0].doc);
			} else log.debug("Signature index not present, should create now.");
		} catch (IOException ex) {
			log.error("Exception occurred while trying to access index.", ex);
		}
		return null;
	}

	// Add a new SPARQL EndPoint to the index
	public Document indexSignature(String urlAddress, String graphName, IRDFDataset indexand, boolean overwrite) {
		return indexSignature(urlAddress, graphName, indexand, null, overwrite);
	}

	public Document indexSignature(String urlAddress, String graphName, IRDFDataset indexand,
			Collection<String> propertySet, boolean overwrite) {

		if (alreadyIndexed(urlAddress, graphName) && !overwrite) {
			log.warn("Already indexed: {}{}", urlAddress, graphName == null ? "" : "::" + graphName);
			log.warn(" ... overwrite not set, so not indexing.");
			return null;
		}
		Analyzer analyzer = new StandardAnalyzer(); // = new WhitespaceAnalyzer();
		IndexWriter indexWriter;
		/*
		 * IndexWriterConfig.OpenMode.CREATE_OR_APPEND if used IndexWriter will create a
		 * new index if there is not already an index at the provided path and otherwise
		 * open the existing index.
		 */
		IndexWriterConfig config = new IndexWriterConfig(analyzer);// .setOpenMode(OpenMode.CREATE_OR_APPEND);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		try {
			indexWriter = new IndexWriter(getIndex(), config);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// For every dataset a document
		Document doc = new Document();

		// XXX AA I think the values are so because it is assumed that Set#toString()
		// prints [ one, two, ... ] but can it be trusted?
		doc.add(new Field("URL", urlAddress, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("GraphName", graphName, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("ClassSet", indexand.getClassSet().toString(), Field.Store.YES, Field.Index.NO));
		doc.add(new Field("ObjectPropertySet", indexand.getObjectPropertySet().toString(), Field.Store.YES,
				Field.Index.NO));
		doc.add(new Field("DatatypePropertySet", indexand.getDatatypePropertySet().toString(), Field.Store.YES,
				Field.Index.NO));
		doc.add(new Field("LiteralSet", indexand.getLiteralSet().toString(), Field.Store.YES, Field.Index.NO));
		doc.add(new Field("IndividualSet", indexand.getIndividualSet().toString(), Field.Store.YES, Field.Index.NO));
		doc.add(new Field("RDFVocabulary", indexand.getRDFVocabulary().toString(), Field.Store.YES, Field.Index.NO));
		if (propertySet != null && !propertySet.isEmpty())
			doc.add(new Field("PropertySet", propertySet.toString(), Field.Store.YES, Field.Index.NO));

		// TODO handle serialization elsewhere
		JsonObject jSign = new JsonObject();
		for (Entry<String, ClassSignature> entry : indexand.getClassSignatures().entrySet())
			jSign.put(entry.getKey(), entry.getValue().jsonifyPaths());

		doc.add(new StoredField(Fieldd.CLASS_SIGNATURES.toString(), jSign.toString()));

		// Remove the old one(s) if any
		Builder queryBuilder = new Builder();
		queryBuilder.add(new TermQuery(new Term("URL", urlAddress)), BooleanClause.Occur.MUST);
		if (graphName != null && !graphName.isEmpty())
			queryBuilder.add(new TermQuery(new Term("GraphName", graphName)), BooleanClause.Occur.MUST);
		try {
			indexWriter.deleteDocuments(queryBuilder.build());
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				indexWriter.close();
			} catch (IOException e) {
				log.warn("Failed to close index writer."
						+ " This is often recoverable, but you may want to check what happened.", e);
			}
		}
		return doc;
	}

	private boolean alreadyIndexed(String urlAddress, String graphName) {
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

	private Directory getIndex() throws IOException {
		return FSDirectory.open(Paths.get(this.datasetIndexDir));
	}

	private void initIndex() {
		// Analyzer analyzer = new StopAnalyzer();
		Analyzer analyzer = new StandardAnalyzer();
		Directory index = null;
		try {
			Path path = Paths.get(this.datasetIndexDir);
			index = FSDirectory.open(path); // getDirectory(this.datasetIndexDir);
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			;
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

}
