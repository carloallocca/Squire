/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mksmart.squire.websquire;

import javax.ws.rs.ApplicationPath;

/**
 *
 * @author carloallocca
 */

import com.sun.jersey.api.core.PackagesResourceConfig;

@ApplicationPath("/services/")
public class SquireWebApplication extends PackagesResourceConfig {

	public SquireWebApplication() {
		super("org.mksmart.squire.websquire.v1.resources.impl");
	}

}
