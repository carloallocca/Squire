package uk.ac.open.kmi.squire.core4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.squire.core2.QueryAndContextNode;
import uk.ac.open.kmi.squire.core2.QueryTempVarSolutionSpace;
import uk.ac.open.kmi.squire.evaluation.Metrics;
import uk.ac.open.kmi.squire.evaluation.QueryResultTypeSimilarity;
import uk.ac.open.kmi.squire.evaluation.QuerySpecificityDistance;
import uk.ac.open.kmi.squire.operation.InstantiateTemplateVar;
import uk.ac.open.kmi.squire.operation.TooGeneralException;
import uk.ac.open.kmi.squire.rdfdataset.IRDFDataset;
import uk.ac.open.kmi.squire.sparqlqueryvisitor.TemplateVariableScanner;

/**
 * 
 * @author alessandro
 *
 */
public class BestFirstSpecializer extends AbstractMappedQueryTransform {

	private final IRDFDataset dFrom, dTo;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Query qG, qO;

	private List<QueryAndContextNode> recommendations;

	public BestFirstSpecializer(Query originalQuery, Query generalQuery, IRDFDataset dFrom, IRDFDataset dTo) {
		super();
		recommendations = new ArrayList<>();
		this.qO = originalQuery;
		this.qG = generalQuery;
		this.dFrom = dFrom;
		this.dTo = dTo;
	}

	public List<QueryAndContextNode> getRecommendations() {
		return recommendations;
	}

	public float score(Metrics weights, Query qTransformed) {
		// 1)...QueryRootDistance ... we start with both value set to 0
		float queryRootDist = 0;
		float queryRootDistSim = 0;
		// 2)...QuerySpecificityDistance
		QuerySpecificityDistance qSpecDist = new QuerySpecificityDistance();
		float qSpecDistVar = qSpecDist.computeQSDwrtQueryVariable(this.qO, qTransformed);
		float qSpecDistTP = qSpecDist.computeQSDwrtQueryTP(this.qO, qTransformed);
		float qSpecificitySim = 1 - (qSpecDistVar + qSpecDistTP);
		// 3)...QueryResultTypeSimilarity
		QueryResultTypeSimilarity qRTS = new QueryResultTypeSimilarity();
		// float resulTtypeDist = qRTS.computeQueryResultTypeDistance(this.qO, this.dFrom, qTransformed, this.dTo);
		// float resultTypeSim = 1 - resulTtypeDist;
		// 4)...QueryResultSizeSimilarity
		float queryResultSizeSimilarity = 0;

		float score = (weights.queryRootDistanceCoefficient * queryRootDistSim)
			//	+ (weights.resultTypeSimilarityCoefficient * resultTypeSim)
				+ (weights.querySpecificityDistanceCoefficient * qSpecificitySim)
		// Formula does not consider result size similarity right now
		;
		return score;
	}

	public void specialize() {

		/**
		 * What I want to do here:
		 *
		 * <li>Select one (templated) triple pattern from the generalized query.
		 * <li>Get the solutions for that (i.e. project on the templates for that TP
		 * only).
		 * <li>Score each (partially templated?) resulting query.
		 * <li>If a new high score is attained, branch to that node
		 */
		Metrics base = new Metrics();
		List<QuerySolution> qTsol;
		try {
			QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
			qTsol = temVarValueSpace.computeTempVarSolutionSpace(this.qG, this.dTo);
			// Not doing this optimization now (didn't seem to work anyway)
			// // e.g. ( ?opt2 = dc:title ) ( ?opt1 = dc:title )
			// qTsol = eliminateSolutionBindedToTheSameValue(qTsol);
			log.debug("Solution space size BEFORE = {}",qTsol.size());
			qTsol = eliminateSolutionsBoundToSameValue(qTsol);
			log.debug("Solution space size AFTER = {}",qTsol.size());
		} catch (TooGeneralException gex) {
			log.warn("Query is too general to execute safely. Assuming solution exists.");
			log.warn(" * Query : '{}'", gex.getQuery());
			qTsol = new ArrayList<>();
			qTsol.add(new QuerySolutionMap());
		}
		log.debug("{}", qG.getQueryPattern());

		ElementVisitor vis = new ElementVisitorBase() {
			@Override
			public void visit(ElementGroup el) {
				for (Element e : el.getElements())
					e.visit(this);
			}

			@Override
			public void visit(ElementPathBlock el) {
				TemplateVariableScanner scan = new TemplateVariableScanner();
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					TriplePath tp = triples.next();

					final SortedSet<QueryStringScorePair> rank = new TreeSet<>();

					List<QuerySolution> qTsolTemp;
					log.debug("Triple pattern : {}", tp);
					Set<Var> tempVar4Tp = scan.extractTemplateVariables(tp);
					for (Var v : tempVar4Tp) {
						log.debug(" ... has template variable {}", v);
					}
					QueryTempVarSolutionSpace temVarValueSpace = new QueryTempVarSolutionSpace();
					try {
						qTsolTemp = temVarValueSpace.computeTempVarSolutionSpace(qG, dTo,
								tempVar4Tp.toArray(new Var[0]));
					} catch (TooGeneralException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						qTsolTemp = Collections.emptyList();
					}
					int i = 0;
					for (QuerySolution sol : qTsolTemp) {
						log.debug("Solution {} : {}", i++, sol);
						InstantiateTemplateVar instOP = new InstantiateTemplateVar();
						Query qClone = QueryFactory.create(qG);
						for (Iterator<String> it = sol.varNames(); it.hasNext();) {
							String v = it.next();
							log.debug("Instantiating on {}", v);
							qClone = instOP.instantiateVarTemplate(qClone, Var.alloc(v), sol.get(v).asNode());
							log.debug("{}", qClone);
						}
						float score = score(base, qClone);
						log.debug(" - score = {}", score);
						rank.add(new QueryStringScorePair(qClone.toString(), score));
					}
					log.debug("Top-ranked intermediate queries:");
					for (QueryStringScorePair pair : rank)
						log.debug("{} : {}", pair.getScore(), pair.getQuery());
				}
			}
		};

		qG.getQueryPattern().visit(vis);

	}
	
	// e.g. ( ?opt2 = <http://purl.org/dc/terms/title> ) ( ?opt1 =
	// <http://purl.org/dc/terms/title> )
	private List<QuerySolution> eliminateSolutionsBoundToSameValue(List<QuerySolution> qSolList) {
		List<QuerySolution> output = new ArrayList<>();
		if (qSolList.isEmpty()) return qSolList;
		for (QuerySolution qs : qSolList) {
			List<String> valuesList = new ArrayList<>();
			Iterator<String> varIter = qs.varNames();
			while (varIter.hasNext()) {
				String varName = varIter.next();
				valuesList.add(qs.get(varName).toString());
			}
			if (valuesList.size() == 1) output.add(qs);
			else {
				String firstValue = valuesList.get(0);
				valuesList.remove(0);
				boolean isAllDifferent = true;
				Iterator<String> varValueIter = valuesList.iterator();
				while (varValueIter.hasNext() && isAllDifferent) {
					String varValue = varValueIter.next();
					if (varValue.equals(firstValue)) isAllDifferent = false;
				}
				if (isAllDifferent) output.add(qs);
			}
		}
		return output;
	}

}
