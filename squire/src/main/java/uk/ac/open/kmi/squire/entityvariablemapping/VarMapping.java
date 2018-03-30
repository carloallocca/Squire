package uk.ac.open.kmi.squire.entityvariablemapping;

import java.util.Map;

public interface VarMapping {

	public void clear();

	public String generateVarIfAbsent(String uri, String varPrefix);

	public String getValueFromVar(String varString);

	public Map<String, String> getValueToVarTable();

	public Map<String, String> getVarToValueTable();
}
