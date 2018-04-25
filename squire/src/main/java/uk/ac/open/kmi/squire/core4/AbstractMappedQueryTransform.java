package uk.ac.open.kmi.squire.core4;

import uk.ac.open.kmi.squire.entityvariablemapping.GeneralVarMapping;
import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;

public abstract class AbstractMappedQueryTransform extends AbstractQueryRecommendationObservable
		implements MappedQueryTransform {

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

	protected AbstractMappedQueryTransform() {
		classVarTable = new GeneralVarMapping();
		individualVarTable = new GeneralVarMapping();
		literalVarTable = new GeneralVarMapping();
		objectProperyVarTable = new GeneralVarMapping();
		datatypePropertyVarTable = new GeneralVarMapping();
		rdfVocVarTable = new GeneralVarMapping();
		plainPropertyVarTable = new GeneralVarMapping();
	}

	@Override
	public VarMapping getClassVarTable() {
		return classVarTable;
	}

	@Override
	public VarMapping getDatatypePropertyVarTable() {
		return datatypePropertyVarTable;
	}

	@Override
	public VarMapping getIndividualVarTable() {
		return individualVarTable;
	}

	@Override
	public VarMapping getLiteralVarTable() {
		return literalVarTable;
	}

	@Override
	public VarMapping getObjectProperyVarTable() {
		return objectProperyVarTable;
	}

	@Override
	public VarMapping getPlainProperyVarTable() {
		return plainPropertyVarTable;
	}

	@Override
	public VarMapping getRdfVocVarTable() {
		return rdfVocVarTable;
	}

}
