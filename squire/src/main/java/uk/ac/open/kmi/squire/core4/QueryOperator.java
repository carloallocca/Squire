package uk.ac.open.kmi.squire.core4;

import uk.ac.open.kmi.squire.entityvariablemapping.GeneralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;

public abstract class QueryOperator extends AbstractQueryRecommendationObservable {

	protected enum NodeRole {
		OBJECT, PREDICATE, SUBJECT
	}

	public static final String TEMPLATE_VAR_CLASS = "ct";
	public static final String TEMPLATE_VAR_INDIVIDUAL = "it";
	public static final String TEMPLATE_VAR_LITERAL = "lt";
	public static final String TEMPLATE_VAR_PROP_DT = "dpt";
	public static final String TEMPLATE_VAR_PROP_OBJ = "opt";
	public static final String TEMPLATE_VAR_PROP_PLAIN = "ppt";

	protected VarMapping classVarTable, datatypePropertyVarTable, individualVarTable, literalVarTable,
			objectProperyVarTable, rdfVocVarTable, plainPropertyVarTable;

	protected QueryOperator() {
		classVarTable = new GeneralVarMapping();
		individualVarTable = new GeneralVarMapping();
		literalVarTable = new GeneralVarMapping();
		objectProperyVarTable = new GeneralVarMapping();
		datatypePropertyVarTable = new GeneralVarMapping();
		rdfVocVarTable = new GeneralVarMapping();
		plainPropertyVarTable = new GeneralVarMapping();
	}

	public VarMapping getClassVarTable() {
		return classVarTable;
	}

	public VarMapping getDatatypePropertyVarTable() {
		return datatypePropertyVarTable;
	}

	public VarMapping getIndividualVarTable() {
		return individualVarTable;
	}

	public VarMapping getLiteralVarTable() {
		return literalVarTable;
	}

	public VarMapping getObjectProperyVarTable() {
		return objectProperyVarTable;
	}

	public VarMapping getPlainProperyVarTable() {
		return plainPropertyVarTable;
	}

	public VarMapping getRdfVocVarTable() {
		return rdfVocVarTable;
	}

}
