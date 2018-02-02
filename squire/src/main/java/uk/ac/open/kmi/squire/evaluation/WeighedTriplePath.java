/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.evaluation;

import org.apache.jena.sparql.core.TriplePath;

/**
 *
 * @author carloallocca
 */
public class WeighedTriplePath {

    private TriplePath tp;
    private float weigh;

    public WeighedTriplePath() {
        super();
    }

    public WeighedTriplePath(TriplePath newTP, float newWeigh) {
        this.tp = newTP;
        this.weigh = newWeigh;
    }

    public void setTp(TriplePath tp) {
        this.tp = tp;
    }

    public void setWeigh(float weigh) {
        this.weigh = weigh;
    }

    public TriplePath getTp() {
        return tp;
    }

    public float getWeigh() {
        return weigh;
    }

}
