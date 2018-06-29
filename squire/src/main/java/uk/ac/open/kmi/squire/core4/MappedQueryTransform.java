package uk.ac.open.kmi.squire.core4;

import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;

public interface MappedQueryTransform {

	public VarMapping<String,String> getClassVarTable();

	public VarMapping<String,String> getDatatypePropertyVarTable();

	public VarMapping<String,String> getIndividualVarTable();

	public VarMapping<String,String> getLiteralVarTable();

	public VarMapping<String,String> getObjectProperyVarTable();

	public VarMapping<String,String> getPlainProperyVarTable();

	public VarMapping<String,String> getRdfVocVarTable();

}
