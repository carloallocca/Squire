package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractVarMapping implements VarMapping {

	protected int succ = 0;
	protected Map<String, String> valueToVar;
	protected Map<String, String> varToValue;

	public AbstractVarMapping() {
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
	public Map<String, String> getValueToVarTable() {
		return this.valueToVar;
	}

	@Override
	public Map<String, String> getVarToValueTable() {
		return this.varToValue;
	}

}
