/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.ontologymatching;

/**
 *
 * @author callocca
 */
public interface IMatchingStrategy {

	// public Object computeMatching();
	// public Object computeClassMatching();
	// public Object computeObjectPropertyMatching();
	// public Object computeDatatypePropertyMatching();
	public float computeMatchingScore(String s1, String s2);

}
