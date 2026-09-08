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
	
	private boolean showErrorDetails = false;

	private String errorFormat = "legacy";

	/**
	 * Whether security response headers should be applied. Enabled by default.
	 */
	private boolean securityHeadersEnabled = true;

	/**
	 * Whether the {@code Demoiselle-Version} header should be exposed on responses.
	 * Disabled by default to avoid leaking framework version information.
	 */
	private boolean exposeFrameworkVersion = false;

	/**
	 * Configurable map of security headers applied to responses. Populated with
	 * conservative defaults; no HSTS or Content-Security-Policy is set by default.
	 */
	private Map<String, String> securityHeaders = defaultSecurityHeaders();

	private static Map<String, String> defaultSecurityHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Content-Type-Options", "nosniff");
		headers.put("X-Frame-Options", "DENY");
		headers.put("Referrer-Policy", "no-referrer");
		headers.put("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
		return headers;
	}

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

	/**
	 * Return the error format configuration.
	 * 
	 * @return "legacy" or "rfc9457"
	 */
	public String getErrorFormat() {
		return errorFormat;
	}

	/**
	 * Set the error format. Any value other than "rfc9457" is normalized to "legacy".
	 * 
	 * @param errorFormat the desired error format
	 */
	public void setErrorFormat(String errorFormat) {
		if ("rfc9457".equals(errorFormat)) {
			this.errorFormat = errorFormat;
		} else {
			this.errorFormat = "legacy";
		}
	}

	/**
	 * Return true if the error format is RFC 9457.
	 * 
	 * @return true if errorFormat is "rfc9457"
	 */
	public boolean isRfc9457() {
		return "rfc9457".equals(errorFormat);
	}

	/**
	 * Return whether security response headers should be applied.
	 *
	 * @return true if security headers are enabled
	 */
	public boolean isSecurityHeadersEnabled() {
		return securityHeadersEnabled;
	}

	/**
	 * Set whether security response headers should be applied.
	 *
	 * @param securityHeadersEnabled the flag value
	 */
	public void setSecurityHeadersEnabled(boolean securityHeadersEnabled) {
		this.securityHeadersEnabled = securityHeadersEnabled;
	}

	/**
	 * Return whether the {@code Demoiselle-Version} header should be exposed.
	 *
	 * @return true if the framework version should be exposed
	 */
	public boolean isExposeFrameworkVersion() {
		return exposeFrameworkVersion;
	}

	/**
	 * Set whether the {@code Demoiselle-Version} header should be exposed.
	 *
	 * @param exposeFrameworkVersion the flag value
	 */
	public void setExposeFrameworkVersion(boolean exposeFrameworkVersion) {
		this.exposeFrameworkVersion = exposeFrameworkVersion;
	}

	/**
	 * Return the configurable map of security headers. Never {@code null}.
	 *
	 * @return the security headers map
	 */
	public Map<String, String> getSecurityHeaders() {
		return securityHeaders;
	}

	/**
	 * Set the map of security headers. A {@code null} value resets the map to
	 * the conservative defaults so callers always observe a usable configuration.
	 *
	 * @param securityHeaders the security headers map
	 */
	public void setSecurityHeaders(Map<String, String> securityHeaders) {
		this.securityHeaders = securityHeaders == null ? defaultSecurityHeaders() : securityHeaders;
	}

}