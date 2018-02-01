/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.core;

import java.util.Comparator;

import org.apache.jena.query.Query;

/**
 *
 * @author carloallocca
 */
public class QueryScorePair {

	Query query;
	float score;

	public QueryScorePair(Query q, float s) {
		this.query = q;
		this.score = s;
	}

	public Query getQuery() {
		return query;
	}

	public float getScore() {
		return score;
	}

	public static Comparator<QueryScorePair> queryScoreComp = new Comparator<QueryScorePair>() {

		@Override
		public int compare(QueryScorePair o1, QueryScorePair o2) {
			float score1 = o1.getScore();
			float score2 = o2.getScore();
			// ...For ascending order
			return Float.compare(score2, score1);
			// return Float.compare(score1, score2);
		}
	};

}
