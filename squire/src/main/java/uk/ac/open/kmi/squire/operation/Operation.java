package uk.ac.open.kmi.squire.operation;

/**
 * An operation that, once applied, returns an object that can be assimilated
 * with a query (e.g. a Query or set of queries).
 *
 * @author carloallocca, alessandro.adamou<alexdma@apache.org>
 */
public interface Operation<Q> {

	/**
	 * Performs the operation. The operands are taken from the object that
	 * implements the operation.
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
