/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire.v1.resources;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author carloallocca
 */
@XmlRootElement
public class JobStatement {

    private List<String> dependencyTokens;
    private String message;
    private String token;

    public JobStatement() {
        this.dependencyTokens = new LinkedList<String>();
    }

    public JobStatement(String token) {
        this();
        this.token = token;
        this.message = "Job Created. Use the URL /job/" + token;
    }

    public List<String> getDependencyTokens() {
        return dependencyTokens;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public void setDependencyTokens(List<String> dependenceTokenList) {
        this.dependencyTokens = dependenceTokenList;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
