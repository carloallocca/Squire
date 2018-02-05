/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.rdfdataset;

import java.util.Set;

import uk.ac.open.kmi.squire.core4.AbstractQueryRecommendationObservable;

/**
 *
 * @author carloallocca it is based on the JaroWinklerSimilarity
 */
public class RDFDatasetSimilarity extends AbstractQueryRecommendationObservable {

	public RDFDatasetSimilarity(String token) {
		this.token = token;
	}

	public RDFDatasetSimilarity() {
		super();
	}

	public float computeSim(IRDFDataset d1, IRDFDataset d2) {

		Set<String> classSetD1 = d1.getClassSet();
		Set<String> objectPropertySetD1 = d1.getObjectPropertySet();
		Set<String> datatypePropertySetD1 = d1.getDatatypePropertySet();

		Set<String> classSetD2 = d2.getClassSet();
		Set<String> objectPropertySetD2 = d2.getObjectPropertySet();
		Set<String> datatypePropertySetD2 = d2.getDatatypePropertySet();

		float classSim = computeSetSim(classSetD1, classSetD2);
		float objectPropertySetSim = computeSetSim(objectPropertySetD1, objectPropertySetD2);
		float datatypePropertySetSim = computeSetSim(datatypePropertySetD1, datatypePropertySetD2);

		// return ((classSim + objectPropertySetSim + datatypePropertySetSim)/3);
		this.notifyDatatsetSimilarity((classSim + objectPropertySetSim + datatypePropertySetSim));
		return ((classSim + objectPropertySetSim + datatypePropertySetSim));
	}

	private float computeSetSim(Set<String> setD1, Set<String> setD2) {
		if (setD1 != null && setD2 != null) {
			if (setD1.size() > 0 && setD2.size() > 0) {
				int d1Size = setD1.size();
				int d2Size = setD2.size();
				int intersection = 0;
				for (String s : setD1) {
					if (setD2.contains(s)) {
						intersection = intersection + 1;
					}
				}
				if (intersection != 0) {
					if (d1Size > d2Size) {
						return (float) ((1.0 * intersection) / (1.0 * d1Size));
					} else {
						return (float) ((1.0 * intersection) / (1.0 * d2Size));
					}
				}
				return (float) 0;
			}
			return (float) 0;
		}
		return (float) 0;
	}

}
