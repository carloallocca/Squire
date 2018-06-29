package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.Map;

/**
 * 
 * @author alessandro
 *
 * @param <V>
 *            the class of variables
 * @param <N>
 *            the class of nodes
 */
public interface VarMapping<V, N> {

	public void clear();

	@Deprecated
	public void init();

	public V getOrCreateVar(N value, String varPrefix);

	public N getValueFromVar(V var);

	public Map<N, V> getValueToVarTable();

	public Map<V, N> getVarToValueTable();
}
