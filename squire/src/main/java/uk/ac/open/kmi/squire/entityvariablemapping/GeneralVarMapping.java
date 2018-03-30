package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.HashMap;
import java.util.Map;

public class GeneralVarMapping implements VarMapping {

	protected int succ = 0;
	protected Map<String, String> valueToVar;
	protected Map<String, String> varToValue;

	public GeneralVarMapping() {
		valueToVar = new HashMap<>();
		varToValue = new HashMap<>();
		succ = 1;
	}

	@Override
	public void clear() {
		valueToVar = null;
		varToValue = null;
		succ = 0;
	}

	@Override
	public String getValueFromVar(String varString) {
		if (varToValue == null)
			throw new IllegalStateException("The mapping table needs to be initialized before use.");
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
	public String generateVarIfAbsent(String uri, String varPrefix) {
		if (valueToVar == null || succ == 0)
			throw new IllegalStateException("The mapping table needs to be initialized before use.");
		if (!valueToVar.containsKey(uri)) {
			// this.classVar = "ct"+Integer.toString(++succ);
			String tmp = varPrefix + Integer.toString(succ++);
			valueToVar.put(uri, tmp);
			varToValue.put(tmp, uri);
			return tmp;
		} else return valueToVar.get(uri);
	}

}
