package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.HashMap;
import java.util.Map;

public class GeneralVarMapping implements VarMapping<String, String> {

	/**
	 * This is appended to the next template variable when it is generated.
	 */
	protected int index = 0;
	protected Map<String, String> valueToVar;
	protected Map<String, String> varToValue;

	public GeneralVarMapping() {
		init();
	}

	@Override
	public void clear() {
		valueToVar = null;
		varToValue = null;
		index = 0;
	}

	@Override
	public String getOrCreateVar(String uri, String varPrefix) {
		if (valueToVar == null || index == 0)
			throw new IllegalStateException("The mapping table needs to be initialized before use. Call init() first.");
		if (!valueToVar.containsKey(uri)) {
			// this.classVar = "ct"+Integer.toString(++index);
			String tmp = varPrefix + Integer.toString(index++);
			valueToVar.put(uri, tmp);
			varToValue.put(tmp, uri);
			return tmp;
		} else return valueToVar.get(uri);
	}

	@Override
	public String getValueFromVar(String varString) {
		if (varToValue == null)
			throw new IllegalStateException("The mapping table needs to be initialized before use. Call init() first.");
		if (!varToValue.containsKey(varString)) return null;
		else return varToValue.get(varString);
	}

	@Override
	public Map<String, String> getValueToVarTable() {
		return this.valueToVar;
	}

	@Override
	public Map<String, String> getVarToValueTable() {
		return this.varToValue;
	}

	@Override
	public void init() {
		valueToVar = new HashMap<>();
		varToValue = new HashMap<>();
		index = 1;
	}

}
