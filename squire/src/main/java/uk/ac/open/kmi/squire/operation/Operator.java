package uk.ac.open.kmi.squire.operation;

import uk.ac.open.kmi.squire.core4.AbstractMappedQueryTransform;

/**
 * An operator that, once applied, returns an object that can be assimilated
 * with a query (e.g. a Jena Query or set of queries).
 * 
 * In general, operators do not implement policies, i.e. they do not "decide"
 * when to be applied and when not to. Operators blindly apply what is decided
 * by a {@link AbstractMappedQueryTransform}.
 * 
 * Nothing is assumed as to whether the operator returns an altered object
 * supplied to it, or a cloned one.
 *
 * @author carloallocca, Alessandro Adamou<alexdma@apache.org>
 */
public interface Operator<Q> {

	/**
	 * Performs the operation. The operands are taken from the object that
	 * implements the operation (e.g. passed to the its constructor).
	 * 
	 * @return the result of applying the operation (can be e.g. a query or set
	 *         thereof).
	 */
	public Q apply();

	/**
	 * Returns the list of operands in the order supplied to the constructor of this
	 * Operation.
	 * 
	 * @return the list of parameters.
	 */
	public Object[] getOperands();

}
