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

import java.util.HashMap;
import java.util.Map;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Configurations of REST module.
 * 
 * @author SERPRO
 */
@Configuration(prefix = "demoiselle.rest")
public class DemoiselleRestConfig {
	
	private Map<String, String> sqlError = new HashMap<String,String>();	
	
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
	 * Set if the detailed errors should return to user.
	 * 
	 * @param showErrorDetails
	 */
	public void setShowErrorDetails(boolean showErrorDetails) {
		this.showErrorDetails = showErrorDetails;
	}
	
	/**
	 * Set the map of custom database error messages
	 * 
	 * @param sqlError 
	 */	
	public void setSqlError(Map<String, String> sqlError) {
		this.sqlError = sqlError;
	}	
	
	/**
	 * Return the map of custom Sql Error messages from demoiselle.properties loaded by configuration module.
	 * 
	 * @return mapped sql Errors
	 */	
	public Map<String, String> getSqlError() {
		return  this.sqlError;
	}

}