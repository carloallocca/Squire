/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author carloallocca
 */
@XmlRootElement
public class QueryStringScorePair implements Comparable<QueryStringScorePair> {

    private String query;
    private float score;

    public QueryStringScorePair() {}

    public QueryStringScorePair(String q, float s) {
        this.query = q;
        this.score = s;
    }

    @Override
    public int compareTo(QueryStringScorePair another) {
        // ...For ascending order
        return Float.compare(another.getScore(), this.getScore());

    }

    public String getQuery() {
        return query;
    }

    public float getScore() {
        return score;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setScore(float score) {
        this.score = score;
    }

}
