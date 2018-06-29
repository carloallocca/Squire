package uk.ac.open.kmi.squire.evaluation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.rdfdataset.FileBasedRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.rdfdataset.SparqlIndexedDataset;
import uk.ac.open.kmi.squire.utils.SparqlUtils;
import uk.ac.open.kmi.squire.utils.SparqlUtils.SparqlException;

/**
 *
 * @author carloallocca
 */
public class QueryResultTypeDistance {

	private class VarTypeMap {

		private String varName;
		private String varType;

		public VarTypeMap(String varN, String varT) {
			this.varName = varN;
			this.varType = varT;
		}

		public String getVarName() {
			return varName;
		}

		public String getVarType() {
			return varType;
		}

	}

	private static final String TYPE_OF_URI = "uri";
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * The more query solutions change their types (e.g. from URIs to literals, or
	 * from strings to integers), the longer this distance.
	 * 
	 * @param qOvars
	 * @param qRvars
	 * @return
	 */
	public float computeQueryResultTypeDistance(Query qOri, IRDFDataset d1, Query qRec, IRDFDataset d2) {
		float dist;
		List<VarTypeMap> qOri_d1_signature, qRec_d2_signature;
		if (d1 instanceof FileBasedRDFDataset)
			qOri_d1_signature = computeQueryVariableSignature(qOri, (FileBasedRDFDataset) d1);
		else qOri_d1_signature = computeQueryVariableSignature(qOri, (SparqlIndexedDataset) d1);
		if (d2 instanceof FileBasedRDFDataset)
			qRec_d2_signature = computeQueryVariableSignature(qRec, (FileBasedRDFDataset) d2);
		else qRec_d2_signature = computeQueryVariableSignature(qRec, (SparqlIndexedDataset) d2);
		dist = 1 - rtqOverlapRate(qOri_d1_signature, qRec_d2_signature);
		return dist;
	}

	private List<VarTypeMap> computeQueryVariableSignature(Query qOri, FileBasedRDFDataset d1) {
		List<VarTypeMap> signature = new ArrayList<>();
		try {
			OntModel inf = ModelFactory.createOntologyModel();
			inf.read(new FileInputStream((String) d1.getEndPointURL()), "");
			// ...querying ...
			// Query q = QueryFactory.create(qString);
			QueryExecution qexec = QueryExecutionFactory.create(qOri, inf);
			if (qOri.isSelectType()) {
				ResultSet results = qexec.execSelect();
				List<QuerySolution> resList = ResultSetFormatter.toList(results);// .out(, results, q);
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
			log.error("{}", ex);
		}
		return signature;
	}

	private List<VarTypeMap> computeQueryVariableSignature(Query qOri, SparqlIndexedDataset d1) {
		if (!qOri.isSelectType())
			throw new UnsupportedOperationException("Only SELECT queries are supported at the moment.");
		List<VarTypeMap> signature = new ArrayList<>();
		// Clone as a query with LIMIT 1 (XXX transform to ASK?)
		Query qTMP = QueryFactory.create(qOri.toString(), Syntax.syntaxSPARQL_11);
		qTMP.setLimit(1);
		List<QuerySolution> resList;
		try {
			String raw = SparqlUtils.doRawQuery(qTMP.toString(), d1.getEndPointURL().toString());
			resList = SparqlUtils.extractProjectedValues(raw, qTMP.getProjectVars());
			log.debug(" ... solution space result size = {}", resList.size());
		} catch (SparqlException e) {
			log.error("SPARQL query for solution space failed. Reason follows.", e);
			log.error("Assuming empty solution space.");
			return signature;
		}

		if (resList.size() > 0) {
			QuerySolution firstSol = resList.get(0);
			for (Iterator<String> it = firstSol.varNames(); it.hasNext();) {
				final String varName = it.next();
				final String varType;
				RDFNode varValue = firstSol.get(varName);
				if (varValue.isURIResource()) varType = TYPE_OF_URI;
				else if (varValue.isLiteral()) {
					RDFDatatype literalValue = varValue.asLiteral().getDatatype();
					varType = literalValue.getURI();
				} else varType = "";
				VarTypeMap vtm = new VarTypeMap(varName, varType);
				signature.add(vtm);
			}
		}

		return signature;
	}

	/**
	 * True iff every variable name and type (URI if object property variable, or
	 * the specific datatype of data property variables) in the given VarTypeMap has
	 * a correspondent in the dataset signature.
	 * 
	 * @param qRec_d2_signature
	 * @param map
	 * @return
	 */
	private boolean contains(List<VarTypeMap> qRec_d2_signature, VarTypeMap map) {
		// boolean found=false;
		String varName = map.getVarName();
		String varType = map.getVarType();
		for (VarTypeMap map1 : qRec_d2_signature) {
			String varName1 = map1.getVarName();
			String varType1 = map1.getVarType();
			if (varName.equals(varName1) && varType.equals(varType1)) return true;
		}
		return false;
	}

	/**
	 * The ratio between the number of matching (variableName, variableType) pairs
	 * in the query solution signatures (extracted from the sample query solution)
	 * and that of all the unique (variableName, variableType) pairs across both
	 * query solutions. The more variables preserve their bindings to the URI type
	 * or to datatypes, the higher the value.
	 * 
	 * @param qOvars
	 * @param qRvars
	 * @return
	 */
	private float rtqOverlapRate(List<VarTypeMap> qOri_d1_signature, List<VarTypeMap> qRec_d2_signature) {
		if (qOri_d1_signature.isEmpty() || qOri_d1_signature.isEmpty()) {
			log.warn("Cannot compute overlap rate with an empty VarTypeMap list.");
			return 0f;
		}
		int intersection = 0;
		for (VarTypeMap map : qOri_d1_signature)
			if (contains(qRec_d2_signature, map)) intersection++;
		int cardUnionSignature = qOri_d1_signature.size() + qRec_d2_signature.size() - intersection;
		return (float) ((1.0 * intersection) / (1.0 * cardUnionSignature));
	}

}
