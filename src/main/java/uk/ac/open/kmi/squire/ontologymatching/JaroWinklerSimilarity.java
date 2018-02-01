/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.ontologymatching;

import org.apache.lucene.search.spell.JaroWinklerDistance;

/**
 *
 * @author carloallocca This is has been used to compute the instantiation
 *         matching in our entire process.
 */
public class JaroWinklerSimilarity implements IMatchingStrategy {

	@Override
	public float computeMatchingScore(String s1, String s2) {
		JaroWinklerDistance d = new JaroWinklerDistance();
		return d.getDistance(s1, s2);
	}

}
