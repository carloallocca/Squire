package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Two-way map implementation. TODO make it one-way.
 * 
 * @author alessandro
 *
 * @param <V>
 *            the variables
 * @param <N>
 *            the nodes
 */
public abstract class AbstractVarMapping<V, N> implements VarMapping<V, N> {

	/**
	 * This is appended to the next template variable when it is generated.
	 */
	protected int index = 0;
	protected Map<N, V> valueToVar;
	protected Map<V, N> varToValue;

	public AbstractVarMapping() {
		init();
	}

	public AbstractVarMapping(VarMapping<V, N> toClone) {
		this();
		if (toClone != null) {
			for (Entry<V, N> e : toClone.getVarToValueEntries())
				varToValue.put(e.getKey(), e.getValue());
			for (Entry<N, V> e : toClone.getValueToVarEntries())
				valueToVar.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		valueToVar = null;
		varToValue = null;
		index = 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof VarMapping))
			return false;
		@SuppressWarnings("unchecked")
		VarMapping<V, N> vm = (VarMapping<V, N>) obj;
		return getVarToValueTable().equals(vm.getVarToValueTable());
	}

	@Override
	public N getValueFromVar(V var) {
		if (varToValue == null)
			throw new IllegalStateException("The mapping table needs to be initialized before use. Call init() first.");
		if (!varToValue.containsKey(var))
			return null;
		else
			return varToValue.get(var);
	}

	@Override
	public Set<Entry<N, V>> getValueToVarEntries() {
		return valueToVar.entrySet();
	}

	@Override
	public Map<N, V> getValueToVarTable() {
		return this.valueToVar;
	}

	@Override
	public Set<Entry<V, N>> getVarToValueEntries() {
		return varToValue.entrySet();
	}

	@Override
	public Map<V, N> getVarToValueTable() {
		return this.varToValue;
	}

	@Override
	public int hashCode() {
		return getVarToValueTable().hashCode();
	}

	@Override
	public void init() {
		valueToVar = new HashMap<>();
		varToValue = new HashMap<>();
		index = 1;
	}

	@Override
	public N put(V var, N value) {
		valueToVar.put(value, var);
		varToValue.put(var, value);
		return value;
	}

}
