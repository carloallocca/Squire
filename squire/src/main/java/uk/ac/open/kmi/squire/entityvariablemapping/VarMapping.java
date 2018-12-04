package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	public V getOrCreateVar(N value, String varPrefix);

	public N getValueFromVar(V var);

	public Set<Entry<N, V>> getValueToVarEntries();

	public Map<N, V> getValueToVarTable();

	public Set<Entry<V, N>> getVarToValueEntries();

	public Map<V, N> getVarToValueTable();

	@Deprecated
	public void init();

	public N put(V var, N value);
}
