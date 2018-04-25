package uk.ac.open.kmi.squire.core4;

import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;

public interface MappedQueryTransform {

	public VarMapping getClassVarTable();

	public VarMapping getDatatypePropertyVarTable();

	public VarMapping getIndividualVarTable();

	public VarMapping getLiteralVarTable();

	public VarMapping getObjectProperyVarTable();

	public VarMapping getPlainProperyVarTable();

	public VarMapping getRdfVocVarTable();

}
