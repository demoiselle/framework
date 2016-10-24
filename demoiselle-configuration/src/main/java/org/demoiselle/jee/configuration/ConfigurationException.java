package org.demoiselle.jee.configuration;

import org.demoiselle.jee.core.exception.DemoiselleException;

/**
 * 
 * Principal exceção do Demoiselle Configuration
 *
 */
public class ConfigurationException extends DemoiselleException{

	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message) {
		super(message);
	}
	
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
