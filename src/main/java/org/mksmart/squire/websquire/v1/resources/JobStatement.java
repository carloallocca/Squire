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
public class JobStatement {

    private String token;
    private String message;

    public JobStatement() {}

    public JobStatement(String token) {
        this.token = token;
        this.message = "Job Created. Use the URL /job/" + token;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
