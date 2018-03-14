/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.entityvariablemapping;

/**
 *
 * @author callocca
 */
public class RDFVocVarMapping extends AbstractVarMapping {

	@Override
	public String generateVarIfAbsent(String uri) {
		if (valueToVar == null || succ == 0 || varToValue == null) throw new IllegalStateException(
				"The ClassURIVarTable needs to be initialized. Pls, Call the Class constructor.");
		if (!(valueToVar.containsKey(uri))) {
			// this.classVar = "ct"+Integer.toString(++succ);
			String tmp = "rdf" + Integer.toString(succ++);
			valueToVar.put(uri, tmp);
			varToValue.put(tmp, uri);
			return tmp;
		} else return valueToVar.get(uri);
	}

	// this gets the rdf term from its variable
	public String getRDFVocFromVar(String varString) {
		if (varToValue == null) throw new IllegalStateException(
				"The ClassURIVarTable needs to be initialized. You cannot use it otherwise.");
		if (!(varToValue.containsKey(varString))) return null;
		else return varToValue.get(varString);
	}

}
