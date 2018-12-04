package uk.ac.open.kmi.squire.core4;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import uk.ac.open.kmi.squire.entityvariablemapping.VarMapping;

public interface MappedQueryTransform {

	public VarMapping<Var,Node> getClassVarTable();

	public VarMapping<Var,Node> getDatatypePropertyVarTable();

	public VarMapping<Var,Node> getIndividualVarTable();

	public VarMapping<Var,Node> getLiteralVarTable();

	public VarMapping<Var,Node> getObjectProperyVarTable();

	public VarMapping<Var,Node> getPlainProperyVarTable();

	public VarMapping<Var,Node> getRdfVocVarTable();

}
