/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.jobs;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;

/**
 *
 * @author carloallocca
 */
@XmlRootElement
public class RecommendationJobStatus {

	private boolean isFinished;
	private String message;
	private float datasetsSim;
	private SortedSet<QueryStringScorePair> recommandedQueryList;
	private boolean satisfiable;

	public RecommendationJobStatus() {
		this.recommandedQueryList = new TreeSet();
	}

	public SortedSet<QueryStringScorePair> getRecommandedQueryList() {
		return recommandedQueryList;
	}

	public void setRecommandedQueryList(SortedSet<QueryStringScorePair> recommandedQueryList) {
		this.recommandedQueryList = recommandedQueryList;
	}

	public boolean isSatisfiable() {
		return satisfiable;
	}

	public void setSatisfiable(boolean satisfiable) {
		this.satisfiable = satisfiable;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setDatasetsSim(float datasetsSim) {
		this.datasetsSim = datasetsSim;
	}

	public String getMessage() {
		return message;
	}

	public float getDatasetsSim() {
		return datasetsSim;
	}

	public void setIsFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public boolean isIsFinished() {
		return isFinished;
	}

}
