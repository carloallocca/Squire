package uk.ac.open.kmi.squire.core4;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.entityvariablemapping.RdfVarMapping;
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

	protected VarMapping<Var, Node> classVarTable, datatypePropertyVarTable, individualVarTable, literalVarTable,
			objectProperyVarTable, rdfVocVarTable, plainPropertyVarTable;

	protected AbstractMappedQueryTransform() {
		classVarTable = new RdfVarMapping();
		individualVarTable = new RdfVarMapping();
		literalVarTable = new RdfVarMapping();
		objectProperyVarTable = new RdfVarMapping();
		datatypePropertyVarTable = new RdfVarMapping();
		rdfVocVarTable = new RdfVarMapping();
		plainPropertyVarTable = new RdfVarMapping();
	}

	@Override
	public VarMapping<Var, Node> getClassVarTable() {
		return classVarTable;
	}

	@Override
	public VarMapping<Var, Node> getDatatypePropertyVarTable() {
		return datatypePropertyVarTable;
	}

	@Override
	public VarMapping<Var, Node> getIndividualVarTable() {
		return individualVarTable;
	}

	@Override
	public VarMapping<Var, Node> getLiteralVarTable() {
		return literalVarTable;
	}

	@Override
	public VarMapping<Var, Node> getObjectProperyVarTable() {
		return objectProperyVarTable;
	}

	@Override
	public VarMapping<Var, Node> getPlainProperyVarTable() {
		return plainPropertyVarTable;
	}

	@Override
	public VarMapping<Var, Node> getRdfVocVarTable() {
		return rdfVocVarTable;
	}

}
