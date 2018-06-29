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

	protected VarMapping<String,String> classVarTable, datatypePropertyVarTable, individualVarTable, literalVarTable,
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
	public VarMapping<String,String> getClassVarTable() {
		return classVarTable;
	}

	@Override
	public VarMapping<String,String> getDatatypePropertyVarTable() {
		return datatypePropertyVarTable;
	}

	@Override
	public VarMapping<String,String> getIndividualVarTable() {
		return individualVarTable;
	}

	@Override
	public VarMapping<String,String> getLiteralVarTable() {
		return literalVarTable;
	}

	@Override
	public VarMapping<String,String> getObjectProperyVarTable() {
		return objectProperyVarTable;
	}

	@Override
	public VarMapping<String,String> getPlainProperyVarTable() {
		return plainPropertyVarTable;
	}

	@Override
	public VarMapping<String,String> getRdfVocVarTable() {
		return rdfVocVarTable;
	}

}
