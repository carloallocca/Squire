package uk.ac.open.kmi.squire.entityvariablemapping;

public class GeneralVarMapping extends AbstractVarMapping<String, String> {

	@Override
	public String getOrCreateVar(String uri, String varPrefix) {
		if (valueToVar == null || index == 0)
			throw new IllegalStateException("The mapping table needs to be initialized before use. Call init() first.");
		if (!valueToVar.containsKey(uri)) {
			// this.classVar = "ct"+Integer.toString(++index);
			String tmp = varPrefix + Integer.toString(index++);
			put(tmp, uri);
			return tmp;
		} else
			return valueToVar.get(uri);
	}

}
