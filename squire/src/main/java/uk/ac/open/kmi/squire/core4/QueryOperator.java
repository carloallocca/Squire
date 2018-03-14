package uk.ac.open.kmi.squire.core4;

import uk.ac.open.kmi.squire.entityvariablemapping.ClassVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.DatatypePropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.IndividualVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.LiteralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.ObjectPropertyVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.RDFVocVarMapping;

public abstract class QueryOperator extends AbstractQueryRecommendationObservable {

	protected enum NodeRole {
		OBJECT, PREDICATE, SUBJECT
	}

	protected static final String CLASS_TEMPLATE_VAR = "ct";
	protected static final String DT_PROP_TEMPLATE_VAR = "dpt";
	protected static final String INDIVIDUAL_TEMPLATE_VAR = "it";
	protected static final String LITERAL_TEMPLATE_VAR = "lt";
	protected static final String OBJ_PROP_TEMPLATE_VAR = "opt";

	protected ClassVarMapping classVarTable;
	protected DatatypePropertyVarMapping datatypePropertyVarTable;
	protected IndividualVarMapping individualVarTable;
	protected LiteralVarMapping literalVarTable;
	protected ObjectPropertyVarMapping objectProperyVarTable;
	protected RDFVocVarMapping rdfVocVarTable;

	protected QueryOperator() {
		classVarTable = new ClassVarMapping();
		individualVarTable = new IndividualVarMapping();
		literalVarTable = new LiteralVarMapping();
		objectProperyVarTable = new ObjectPropertyVarMapping();
		datatypePropertyVarTable = new DatatypePropertyVarMapping();
		rdfVocVarTable = new RDFVocVarMapping();
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

	public LiteralVarMapping getLiteralVarTable() {
		return literalVarTable;
	}

	public ObjectPropertyVarMapping getObjectProperyVarTable() {
		return objectProperyVarTable;
	}

	public RDFVocVarMapping getRdfVocVarTable() {
		return rdfVocVarTable;
	}

}
