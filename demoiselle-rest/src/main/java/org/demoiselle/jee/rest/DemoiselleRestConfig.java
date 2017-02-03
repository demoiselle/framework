/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.rest;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Configurations of REST module.
 * 
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.rest")
public class DemoiselleRestConfig {

	private boolean showErrorDetails = true;

	/**
	 * Return true or false if the detailed errors should return to user.
	 * 
	 * @return true or false
	 */
	public boolean isShowErrorDetails() {
		return showErrorDetails;
	}

	/**
	 * Set if the detailed erros should return to user.
	 * 
	 * @param showErrorDetails
	 */
	public void setShowErrorDetails(boolean showErrorDetails) {
		this.showErrorDetails = showErrorDetails;
	}

}
